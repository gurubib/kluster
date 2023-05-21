package hu.gurubib.api.cluster.services

import hu.gurubib.dao.models.*
import hu.gurubib.dao.repositories.ClusteringRepository
import hu.gurubib.dao.repositories.PriceRepository
import hu.gurubib.dao.repositories.StockRepository
import hu.gurubib.domain.cluster.clusterings.Clusterings
import hu.gurubib.domain.cluster.distances.Distances
import hu.gurubib.domain.cluster.metrics.ClusteringMetrics
import hu.gurubib.domain.cluster.metrics.ClusteringSimilarityMetrics
import hu.gurubib.domain.cluster.normalisations.Normalisations
import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.fromValuesWithSymbol
import hu.gurubib.domain.stock.models.*
import hu.gurubib.domain.stock.models.Metric
import hu.gurubib.util.enumValueOrDefault
import org.jetbrains.exposed.sql.and
import java.time.LocalDate

interface ClusterService {
    suspend fun createClustering(req: CreateClusteringReq): Clustering
    suspend fun getClusters(clusteringUuid: String): List<List<String>>
    suspend fun createLabelling(labelling: Labelling): List<List<Stock>>
    suspend fun getMetrics(clusteringUuid: String, metricName: String): Metric

    suspend fun getSimilarityMetric(
        oneClusteringUuid: String,
        otherClusteringUuid: String,
        metricName: String
    ): SimilarityMetric
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

    override suspend fun getClusters(clusteringUuid: String): List<List<String>> {
        val clustering = clusteringRepository.findByUuid(clusteringUuid)?.domain()
            ?: throw IllegalArgumentException("No clustering with UUID (${clusteringUuid})!")

        val clusteredObjects = clusteringRepository.findClusteredObjectsFor(clustering.uuid)

        return clusteredObjects.groupBy({ it.clusterId }) { it.objectId }.values.toList()
    }

    override suspend fun createLabelling(labelling: Labelling): List<List<Stock>> {
        return labelling.clusters.map { cluster ->
            cluster.map { symbol ->
                stockRepository.findBySymbol(symbol)?.domain()
                    ?: throw IllegalArgumentException("No stock found with symbol ($symbol)!")
            }
        }
    }

    override suspend fun getMetrics(clusteringUuid: String, metricName: String): Metric {
        val clustering = clusteringRepository.findByUuid(clusteringUuid)?.domain()
            ?: throw IllegalArgumentException("No clustering with UUID (${clusteringUuid})!")

        val metric = parseClusteringMetric(metricName)
        val metricValue = calculateMetricValue(clustering, metric)

        return Metric(
            clusteringUuid = clustering.uuid,
            name = metricName,
            value = metricValue
        )
    }

    private suspend fun calculateMetricValue(clustering: Clustering, metric: ClusteringMetrics): Double {
        val clusteredObjects = clusteringRepository.findClusteredObjectsFor(clustering.uuid)
        val (normalisation, distance, algorithm) = parseParameters(clustering)
        val clusters = clusteredObjects.groupBy({ it.clusterId }) { it.objectId }.values.map { cluster ->
            getTimeSeries(cluster, clustering.from, clustering.to).map { normalisation.method(it) }
        }.associateBy { algorithm.centroidOf(it) }

        return metric.calc(clusters, distance.dist)

//        val metrics = clusteringRepository.findMetricsFor(clustering.uuid)
//        if (metrics.isNotEmpty()) {
//            return metrics
//        }
//
//        val clusteredObjects = clusteringRepository.findClusteredObjectsFor(clustering.uuid)
//        val (normalisation, distance, algorithm) = parseParameters(clustering)
//        val clusters = clusteredObjects.groupBy({ it.clusterId }) { it.objectId }.values.map { cluster ->
//            getTimeSeries(cluster, clustering.from, clustering.to).map { normalisation.method(it) }
//        }
//
//        //TODO: choose calculate metric based on metricName
//        val metricValues = calculateSilhouette(clusters, algorithm.centroidOf, distance.dist).flatten()
//        return clusteringRepository.createMetricsFor(clustering.uuid, metricName, metricValues)
    }

    override suspend fun getSimilarityMetric(
        oneClusteringUuid: String,
        otherClusteringUuid: String,
        metricName: String
    ): SimilarityMetric {
        val oneClustering = clusteringRepository.findByUuid(oneClusteringUuid)?.domain()
            ?: throw IllegalArgumentException("No clustering with UUID (${oneClusteringUuid})!")
        val otherClustering = clusteringRepository.findByUuid(otherClusteringUuid)?.domain()
            ?: throw IllegalArgumentException("No clustering with UUID (${otherClusteringUuid})!")


        val metric = parseClusteringSimilarityMetric(metricName)
        val metricValue = calculateSimilarityMetricValue(oneClustering, otherClustering, metric)

        return SimilarityMetric(
            oneClusteringUuid = oneClusteringUuid,
            otherClusteringUuid = otherClusteringUuid,
            name = metricName,
            value = metricValue
        )
    }

    private suspend fun calculateSimilarityMetricValue(
        oneClustering: Clustering,
        otherClustering: Clustering,
        metric: ClusteringSimilarityMetrics,
    ): Double {
        val objectsForOne = clusteringRepository.findClusteredObjectsFor(oneClustering.uuid)
        val objectsForOther = clusteringRepository.findClusteredObjectsFor(otherClustering.uuid)

        require(objectsForOne.map { it.objectId }.toSet() == objectsForOther.map { it.objectId }.toSet()) { "Clusterings must have been run on the same number of objects!" }

        return metric.calc(objectsForOne, objectsForOther)
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
        enumValueOf(normalisation)

    private fun parseDistance(distance: String): Distances = enumValueOf(distance)

    private fun parseAlgorithm(clustering: String): Clusterings = enumValueOf(clustering)

    private fun parseClusteringMetric(name: String): ClusteringMetrics =
        enumValueOf(name.uppercase())

    private fun parseClusteringSimilarityMetric(name: String): ClusteringSimilarityMetrics =
        enumValueOf(name.uppercase())
}