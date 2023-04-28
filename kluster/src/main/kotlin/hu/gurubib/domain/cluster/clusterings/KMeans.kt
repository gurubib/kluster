package hu.gurubib.domain.cluster.clusterings

import hu.gurubib.domain.cluster.distances.DistanceMeasure
import hu.gurubib.domain.cluster.series.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

private const val MAX_ITERATION_COUNT = 1000

typealias ClusteringAlgorithm = (objects: List<TimeSeries>, dist: DistanceMeasure, k: Int) -> Map<TimeSeries, List<TimeSeries>>

enum class Clusterings(
    val clust: ClusteringAlgorithm,
) {
    KMEANS(::kMeans)
}

fun kMeans(
    objects: List<TimeSeries>,
    dist: DistanceMeasure,
    k: Int,
): Map<TimeSeries, List<TimeSeries>> {
    val initialCentroids = generateCentroids(objects, k)
    var previousCentroids = initialCentroids
    var clusters = assignToClusters(objects, initialCentroids, dist)

    var clustersChanged = true
    var iterationCount = 0

    val elapsedTimeInMillis = measureTimeMillis {
        while (clustersChanged && iterationCount < MAX_ITERATION_COUNT) {
            val centroids = previousCentroids
            clusters = assignToClusters(objects, centroids, dist)

            val (newCentroids, relocatedSomeCentroids) = relocateCentroids(clusters)
            previousCentroids = newCentroids
            clustersChanged = relocatedSomeCentroids
            iterationCount++
        }
    }

    println()
    println("kMeans ran for $elapsedTimeInMillis ms for $iterationCount iterations")
    println()

    return clusters
}

fun generateCentroids(objects: List<TimeSeries>, k: Int): List<TimeSeries> {
    require(allHaveSameLength(objects)) { "All time series must have the same length!" }

    val length = objects.firstOrNull()?.length ?: 0
    val maxes = (0 until length).map { t -> objects.maxOfOrNull { it.values[t] } ?: 0.0 }
    val mins = (0 until length).map { t -> objects.minOfOrNull { it.values[t] } ?: 0.0 }

    return (0 until k).map {
        fromValues((0 until length).map { t -> Random.nextDouble(mins[t], maxes[t]) })
    }
}

private fun allHaveSameLength(objects: List<TimeSeries>): Boolean {
    val length = objects.firstOrNull()?.length ?: 0
    return objects.all { it.length == length }
}

private fun nearestCentroid(o: TimeSeries, centroids: List<TimeSeries>, dist: DistanceMeasure): TimeSeries =
    centroids.indices.minBy { i -> dist(o, centroids[i]) }.let { centroids[it] }

fun assignToClusters(
    objects: List<TimeSeries>,
    centroids: List<TimeSeries>,
    dist: DistanceMeasure,
): Map<TimeSeries, List<TimeSeries>> {
    val clustersWithObjects = objects.groupBy { o -> nearestCentroid(o, centroids, dist) }
    val centroidsWithObjects = clustersWithObjects.keys
    val clustersWithoutObjects = centroids.filterNot { c -> centroidsWithObjects.contains(c) }.associateWith {
        listOf<TimeSeries>()
    }

    return clustersWithObjects + clustersWithoutObjects
}


fun relocateCentroids(clusters: Map<TimeSeries, List<TimeSeries>>): Pair<List<TimeSeries>, Boolean> {
    var relocated = false

    val relocatedCentroids = clusters.map { (centroid, objects) ->
        if (objects.isNotEmpty()) {
            val newCentroid = reduceTimeSeries(objects, ::reduceToAverage)
            relocated = relocated || !newCentroid.withinEpsilon(centroid)
            newCentroid
        } else {
            centroid
        }
    }

    return Pair(relocatedCentroids, relocated)
}