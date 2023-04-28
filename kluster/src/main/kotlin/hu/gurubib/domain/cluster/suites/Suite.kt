package hu.gurubib.domain.cluster.suites

import hu.gurubib.domain.cluster.clusterings.Clusterings
import hu.gurubib.domain.cluster.distances.Distances
import hu.gurubib.domain.cluster.normalisations.Normalisations
import hu.gurubib.domain.cluster.series.TimeSeries

data class Suite(
    val normalisation: Normalisations,
    val distance: Distances,
    val clustering: Clusterings,
    val numOfClusters: Int,
)

fun executeSuiteOn(suite: Suite, series: List<TimeSeries>): List<List<String>> {
    val norm = suite.normalisation.method
    val dist = suite.distance.dist
    val clust = suite.clustering.clust

    val normalisedSeries = series.map { norm(it) }
    val clusters = clust(normalisedSeries, dist, suite.numOfClusters)
    return clusters.values.map { cluster -> cluster.map { it.id } }
}
