package hu.gurubib.domain.cluster.metrics

import hu.gurubib.domain.cluster.clusterings.CentroidProducer
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
    CH(::ch)
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
