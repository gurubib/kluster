package hu.gurubib.domain.cluster.metrics

import hu.gurubib.domain.cluster.clusterings.CentroidProducer
import hu.gurubib.domain.cluster.distances.DistanceMeasure
import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.withinEpsilon
import kotlin.math.max

fun calculateSilhouette(
    clusters: List<List<TimeSeries>>,
    centroidOf: CentroidProducer,
    dist: DistanceMeasure,
): List<List<Pair<String, Double>>> {
    val clustersByCentroids = clusters.associateBy { centroidOf(it) }
    return silhouette(clustersByCentroids, dist)
}

fun silhouette(
    clusters: Map<TimeSeries, List<TimeSeries>>,
    dist: DistanceMeasure,
): List<List<Pair<String, Double>>> {
    val centroids = clusters.keys
    return clusters.entries.map { (centroid, objects) ->
        objects.map { o ->
            val within = dist(o, centroid)
            val otherCentroids = centroids.filterNot { it.withinEpsilon(centroid) }
            val between = otherCentroids.minOf { c -> dist(o, c) }

            val silhouette = (between - within) / (max(within, between))
            Pair(o.symbol, silhouette)
        }
    }
}
