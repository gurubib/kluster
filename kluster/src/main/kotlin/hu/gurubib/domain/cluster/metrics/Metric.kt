package hu.gurubib.domain.cluster.metrics

import hu.gurubib.dao.models.PClusteredObject
import hu.gurubib.domain.cluster.distances.DistanceMeasure
import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.reduceTimeSeries
import hu.gurubib.domain.cluster.series.reduceToAverage
import hu.gurubib.domain.cluster.series.withinEpsilon
import kotlin.math.max

typealias MetricCalculation = (
    clusters: Map<TimeSeries, List<TimeSeries>>,
    dist: DistanceMeasure,
) -> Double

enum class ClusteringMetrics(
    val calc: MetricCalculation,
) {
    SILHOUETTE(::silhouette),
    CH(::ch),
}

fun silhouette(
    clusters: Map<TimeSeries, List<TimeSeries>>,
    dist: DistanceMeasure,
): Double {
    val centroids = clusters.keys
    return clusters.entries.map { (centroid, objects) ->
        objects.map { o ->
            val within = dist(o, centroid)
            val otherCentroids = centroids.filterNot { it.withinEpsilon(centroid) }
            val between = otherCentroids.minOf { c -> dist(o, c) }

            val silhouette = (between - within) / (max(within, between))
            Pair(o.symbol, silhouette)
        }
    }.flatten().map { it.second }.average()
}

fun ch(
    clusters: Map<TimeSeries, List<TimeSeries>>,
    dist: DistanceMeasure,
): Double {
    val globalCentroid = reduceTimeSeries(clusters.values.flatten(), ::reduceToAverage)
    val numOfClusters = clusters.keys.size
    val between = (clusters.entries.sumOf { (centroid, objects) ->
        val d = dist(centroid, globalCentroid)
        objects.size * d * d
    }) / (numOfClusters - 1)

    val numOfObjects = clusters.values.flatten().size
    val within = (clusters.entries.sumOf { (centroid, objects) ->
        objects.sumOf { o -> dist(o, centroid) }
    }) / (numOfObjects - numOfClusters)

    return between / within
}


typealias SimilarityMetricCalculation = (
    objectsForOne: List<PClusteredObject>,
    objectsForOther: List<PClusteredObject>,
) -> Double

enum class ClusteringSimilarityMetrics(
    val calc: SimilarityMetricCalculation,
) {
    RI(::ri),
    ARI(::ari),
}

fun ri(
    objectsForOne: List<PClusteredObject>,
    objectsForOther: List<PClusteredObject>,
): Double {
    val numOfObjects = objectsForOne.size
    val objectInOneSorted = objectsForOne.sortedBy { it.objectId }
    val objectInOtherSorted = objectsForOther.sortedBy { it.objectId }

    var a = 0
    var b = 0
    generatePairs(numOfObjects).also { println(it.size) }.forEach { p ->
        val sameInOne = objectInOneSorted[p.first].clusterId == objectInOneSorted[p.second].clusterId
        val sameInOther = objectInOtherSorted[p.first].clusterId == objectInOtherSorted[p.second].clusterId

        if (sameInOne && sameInOther) {
            a++
        } else if (!sameInOne && !sameInOther) {
            b++
        }
    }

    val nChoose2 = (numOfObjects * (numOfObjects - 1)) / 2

    return (a + b).toDouble() / nChoose2
}

fun generatePairs(n: Int): List<Pair<Int, Int>> {
    return (0 until n - 1).flatMapIndexed { i: Int, first: Int ->
        ((i + 1) until n).map { j ->
            Pair(i, j)
        }
    }
}

fun ari(
    objectsForOne: List<PClusteredObject>,
    objectsForOther: List<PClusteredObject>,
): Double {
    val numOfObjects = objectsForOne.size
    val numOfClustersInOne = objectsForOne.map { it.clusterId }.toSet().size
    val numOfClustersInOther = objectsForOther.map { it.clusterId }.toSet().size

    val table = Array(numOfClustersInOne) { Array(numOfClustersInOther) { 0 } }

    (0 until numOfClustersInOne).forEach { i ->
        (0 until numOfClustersInOther).forEach { j ->
            val oneClusterId = ('A' + i).toString()
            val otherClusterId = ('A' + j).toString()

            val objectIdsWithOneCluster = objectsForOne.filter { it.clusterId == oneClusterId }.map { it.objectId }.toSet()
            val objectIdsWithOtherCluster = objectsForOther.filter { it.clusterId == otherClusterId }.map { it.objectId }.toSet()

            table[i][j] = objectIdsWithOneCluster.intersect(objectIdsWithOtherCluster).size
        }
    }

    val rowSums = table.map { row -> row.sum() }
    val columnSums = (0 until numOfClustersInOther).map { column -> table.sumOf { row -> row[column] } }

    val allSum = (0 until numOfClustersInOne).flatMap { i ->
        (0 until numOfClustersInOther).map { j ->
            nC2(table[i][j])
        }
    }.sum()

    val rowSum = rowSums.sumOf { nC2(it) }
    val columnSum = columnSums.sumOf { nC2(it) }

    val num = allSum - ((rowSum * columnSum).toDouble() / nC2(numOfObjects))
    val den = 0.5 * (rowSum + columnSum) - ((rowSum * columnSum).toDouble() / nC2(numOfObjects))

    return num / den
}

private fun nC2(n: Int) = (n * (n - 1)) / 2



