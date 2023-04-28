package hu.gurubib.domain.cluster.distances

import hu.gurubib.domain.cluster.series.TimeSeries
import kotlin.math.sqrt

typealias DistanceMeasure = (a: TimeSeries, b: TimeSeries) -> Double

enum class Distances(
    val dist: DistanceMeasure,
) {
    EUCLIDEAN(::euclideanDistance)
}

fun euclideanDistance(one: TimeSeries, other: TimeSeries): Double {
    require(one.length == other.length) { "Time series must have the same length!" }

    val sumSquared = one.values.foldIndexed(0.0) { i, acc, v ->
        acc + (v - other.values[i]) * (v - other.values[i])
    }

    return sqrt(sumSquared)
}
