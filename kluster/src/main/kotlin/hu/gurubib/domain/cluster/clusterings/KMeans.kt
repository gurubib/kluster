package hu.gurubib.domain.cluster.clusterings

import hu.gurubib.domain.cluster.distances.DistanceMeasure
import hu.gurubib.domain.cluster.series.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlin.system.measureTimeMillis

private const val MAX_ITERATION_COUNT = 20

typealias ClusteringAlgorithm = suspend (objects: List<TimeSeries>, dist: DistanceMeasure, k: Int) -> Map<TimeSeries, List<TimeSeries>>
typealias CentroidProducer = (objects: List<TimeSeries>) -> TimeSeries

enum class Clusterings(
    val clust: ClusteringAlgorithm,
    val centroidOf: CentroidProducer,
) {
    KMEANS(::kMeans, ::kMeansCentroidProducer),
    KMEDOID(::kMedoid, ::kMeansCentroidProducer),
}

fun kMeans(
    objects: List<TimeSeries>,
    dist: DistanceMeasure,
    k: Int,
): Map<TimeSeries, List<TimeSeries>> {
    val initialCentroids = generateConstrainedBy(objects, k)
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

private fun kMedoid(
    objects: List<TimeSeries>,
    dist: DistanceMeasure,
    k: Int,
): Map<TimeSeries, List<TimeSeries>> {
    val initialCentroids = objects.shuffled().take(k)
    var previousCentroids = initialCentroids
    var clusters = assignToClusters(objects, initialCentroids, dist)

    var clustersChanged = true
    var iterationCount = 0

    val elapsedTimeInMillis = measureTimeMillis {
        while (clustersChanged && iterationCount < MAX_ITERATION_COUNT) {
            val centroids = previousCentroids
            clusters = assignToClusters(objects, centroids, dist)

            val (newCentroids, relocatedSomeCentroids) = relocateCentroidsAsMedoid(clusters, dist)
            previousCentroids = newCentroids
            clustersChanged = relocatedSomeCentroids
            iterationCount++
        }
    }

    println()
    println("kMedoid ran for $elapsedTimeInMillis ms for $iterationCount iterations")
    println()

    return clusters
}

private fun nearestCentroid(o: TimeSeries, centroids: List<TimeSeries>, dist: DistanceMeasure): TimeSeries =
    centroids.indices.minBy { i -> dist(o, centroids[i]) }.let { centroids[it] }

private fun  assignToClusters(
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
    val allObjects = clusters.values.flatten()
    var relocated = false

    val relocatedCentroids = clusters.map { (centroid, objects) ->
        if (objects.isNotEmpty()) {
            val newCentroid = reduceTimeSeries(objects, ::reduceToAverage)
            relocated = relocated || !newCentroid.withinEpsilon(centroid)
            newCentroid
        } else {
            relocated = true
//            generateConstrainedBy(allObjects, 1).first()
            allObjects.asSequence().shuffled().first()
        }
    }

    return Pair(relocatedCentroids, relocated)
}

fun relocateCentroidsAsMedoid(clusters: Map<TimeSeries, List<TimeSeries>>, dist: DistanceMeasure): Pair<List<TimeSeries>, Boolean> {
    val allObjects = clusters.values.flatten()
    var relocated = false

    val relocatedCentroids = clusters.map { (centroid, objects) ->
        if (objects.isNotEmpty()) {
            val newCentroid = reduceTimeSeries(objects, ::reduceToAverage)
            val medoid = objects.minBy { dist(it, newCentroid) }
            relocated = relocated || !medoid.withinEpsilon(centroid)
            medoid
        } else {
            relocated = true
            generateConstrainedBy(allObjects, 1).first()
        }
    }

    return Pair(relocatedCentroids, relocated)
}

fun kMeansCentroidProducer(objects: List<TimeSeries>): TimeSeries = reduceTimeSeries(objects, ::reduceToAverage)