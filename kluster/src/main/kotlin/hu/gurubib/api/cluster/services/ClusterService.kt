package hu.gurubib.api.cluster.services

import hu.gurubib.dao.models.PStock
import hu.gurubib.dao.models.Prices
import hu.gurubib.dao.models.domain
import hu.gurubib.dao.models.toInitializer
import hu.gurubib.dao.repositories.ClusteringRepository
import hu.gurubib.dao.repositories.PriceRepository
import hu.gurubib.dao.repositories.StockRepository
import hu.gurubib.domain.cluster.clusterings.Clusterings
import hu.gurubib.domain.cluster.distances.Distances
import hu.gurubib.domain.cluster.metrics.calculateSilhouette
import hu.gurubib.domain.cluster.normalisations.Normalisations
import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.fromValuesWithSymbol
import hu.gurubib.domain.stock.models.*
import hu.gurubib.util.enumValueOrDefault
import org.jetbrains.exposed.sql.and
import java.time.LocalDate

interface ClusterService {
    suspend fun createClustering(req: CreateClusteringReq): Clustering
    suspend fun createLabelling(labelling: Labelling): List<List<Stock>>
    suspend fun getMetric(clusteringUuid: String, metricName: String): Metric
}

class ClusterServiceImpl(
    private val clusteringRepository: ClusteringRepository,
    private val stockRepository: StockRepository,
    private val priceRepository: PriceRepository,
) : ClusterService {
    override suspend fun createClustering(req: CreateClusteringReq): Clustering {
        val clustering = clusteringRepository.createTransactional(req.toClustering().toInitializer()).domain()
        val objects = filterShorterSeries(getTimeSeries(req.symbols, req.from, req.to))

        checkObjects(objects)

        val (normalisation, distance, algorithm) = parseParameters(clustering)
        val normalizedObjects = objects.map { normalisation.method(it) }
        val clusters = algorithm.clust(normalizedObjects, distance.dist, clustering.numOfClusters)
        clusteringRepository.createClusteredObjectsFor(
            clustering.uuid,
            clusters.values.map { cluster -> cluster.map { it.symbol } }
        )

        return clustering
    }

    override suspend fun createLabelling(labelling: Labelling): List<List<Stock>> {
        return labelling.clusters.map { cluster ->
            cluster.map { symbol ->
                stockRepository.findBySymbol(symbol)?.domain()
                    ?: throw IllegalArgumentException("No stock found with symbol ($symbol)!")
            }
        }
    }

    override suspend fun getMetric(clusteringUuid: String, metricName: String): Metric {
        val clustering = clusteringRepository.findByUuid(clusteringUuid)?.domain() ?:
            throw IllegalArgumentException("No clustering with UUID (${clusteringUuid})!")

        val clusteredObjects = clusteringRepository.findClusteredObjectsFor(clustering.uuid)
        val (normalisation, distance, algorithm) = parseParameters(clustering)
        val clusters = clusteredObjects.groupBy({ it.clusterId }) { it.objectId }.values.map { cluster ->
            getTimeSeries(cluster, clustering.from, clustering.to).map { normalisation.method(it) }
        }

        val silhouetteValuesByClusters = calculateSilhouette(clusters, algorithm.centroidOf, distance.dist)

        return Metric(
            clusteringUuid = clustering.uuid,
            name = "SILHOUETTE",
            value = silhouetteValuesByClusters.map { cluster -> cluster.map { it.second }.average() }.average(),
        )
    }

    private fun checkObjects(objects: List<TimeSeries>) {
        val length = objects.firstOrNull()?.length
        objects.forEach {
            require(it.length == length) {
                "Objects must have the same length! Required: ${length}, got: ${it.length} with ${it.symbol}!"
            }
        }
    }

    private suspend fun getTimeSeries(
        symbols: List<String>,
        from: LocalDate,
        to: LocalDate
    ): List<TimeSeries> {
        return symbols
            .map { symbol ->
                stockRepository.findBySymbol(symbol)
                    ?: throw IllegalArgumentException("No stock found with symbol (${symbol})")
            }
            .map { stock ->
                val (head, tail) = createCursors(stock, from, to)
                val prices = priceRepository.find {
                    (Prices.sequenceId greater head.value) and (Prices.sequenceId less tail.value)
                }

                fromValuesWithSymbol(prices.map { it.close }, stock.symbol) //TODO not always close
            }
    }

    private fun filterShorterSeries(series: List<TimeSeries>): List<TimeSeries> {
        val maxLength = series.maxOf { it.length }
        return series.filter { it.length == maxLength }.also { println("Num of full series: ${it.size}") }
    }

    private fun createCursors(stock: PStock, from: LocalDate, to: LocalDate): Pair<PriceCursor, PriceCursor> = Pair(
        createHeadCursor(stock, from),
        createTailCursor(stock, to),
    )

    private fun createHeadCursor(stock: PStock, from: LocalDate): PriceCursor =
        PriceCursor.createPriceCursor(cursorIdOf(stock), from.minusDays(1).atTime(12, 0))

    private fun createTailCursor(stock: PStock, to: LocalDate): PriceCursor =
        PriceCursor.createPriceCursor(stock.id.value, to.atTime(12, 0))

    private fun parseParameters(clustering: Clustering): Triple<Normalisations, Distances, Clusterings> = Triple(
        parseNormalise(clustering.normalise),
        parseDistance(clustering.distance),
        parseAlgorithm(clustering.algorithm),
    )

    private fun parseNormalise(normalisation: String): Normalisations =
        enumValueOrDefault(normalisation, Normalisations.NORMALISATION)

    private fun parseDistance(distance: String): Distances = enumValueOrDefault(distance, Distances.EUCLIDEAN)

    private fun parseAlgorithm(clustering: String): Clusterings = enumValueOrDefault(clustering, Clusterings.KMEANS)

}