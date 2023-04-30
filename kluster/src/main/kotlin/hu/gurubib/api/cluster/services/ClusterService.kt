package hu.gurubib.api.cluster.services

import hu.gurubib.dao.models.PStock
import hu.gurubib.dao.models.Prices
import hu.gurubib.dao.repositories.PriceRepository
import hu.gurubib.dao.repositories.StockRepository
import hu.gurubib.domain.cluster.clusterings.Clusterings
import hu.gurubib.domain.cluster.distances.Distances
import hu.gurubib.domain.cluster.normalisations.Normalisations
import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.fromValuesWithSymbol
import hu.gurubib.domain.stock.models.Clustering
import hu.gurubib.domain.stock.models.PriceCursor
import hu.gurubib.domain.stock.models.cursorIdOf
import hu.gurubib.util.enumValueOrDefault
import org.jetbrains.exposed.sql.and
import java.time.LocalDate

interface ClusterService {
    suspend fun executeClustering(clustering: Clustering): List<List<String>>
}

class ClusterServiceImpl(
    private val stockRepository: StockRepository,
    private val priceRepository: PriceRepository,
) : ClusterService {
    override suspend fun executeClustering(clustering: Clustering): List<List<String>> {
        val objects = getTimeSeries(clustering)

        checkObjects(objects)

        val (normalisation, distance, algorithm) = parseParameters(clustering)
        val normalizedObjects = objects.map { normalisation.method(it) }
        val clusters = algorithm.clust(normalizedObjects, distance.dist, clustering.numOfClusters)

        return clusters.values.map { series -> series.map { it.symbol } }
    }

    private fun checkObjects(objects: List<TimeSeries>) {
        val length = objects.firstOrNull()?.length
        objects.forEach {
            require(it.length == length) {
                "Objects must have the same length! Required: ${length}, got: ${it.length} with ${it.symbol}!"
            }
        }
    }

    private suspend fun getTimeSeries(clustering: Clustering): List<TimeSeries> {
        return clustering.symbols
            .map { symbol ->
                stockRepository.findBySymbol(symbol)
                    ?: throw IllegalArgumentException("No stock found with symbol (${symbol})")
            }
            .map { stock ->
                val (head, tail) = createCursors(stock, clustering)
                val prices = priceRepository.find {
                    (Prices.sequenceId greater head.value) and (Prices.sequenceId less tail.value)
                }

                fromValuesWithSymbol(prices.map { it.close }, stock.symbol) //TODO not always close
            }
    }

    private fun createCursors(stock: PStock, clustering: Clustering): Pair<PriceCursor, PriceCursor> = Pair(
        createHeadCursor(stock, clustering.from),
        createTailCursor(stock, clustering.to),
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