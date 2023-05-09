package hu.gurubib.domain.cluster.distances

import hu.gurubib.domain.cluster.series.TimeSeries
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

typealias DistanceMeasure = (a: TimeSeries, b: TimeSeries) -> Double

enum class Distances(
    val dist: DistanceMeasure,
) {
    EUCLIDEAN(::euclideanDistance),
    DTW(::dtw),
    LCSS(::lcss),
}

fun euclideanDistance(one: TimeSeries, other: TimeSeries): Double {
    require(one.length == other.length) { "Time series must have the same length!" }

    val sumSquared = one.values.foldIndexed(0.0) { i, acc, v ->
        acc + (v - other.values[i]) * (v - other.values[i])
    }

    return sqrt(sumSquared)
}

fun dtw(one: TimeSeries, other: TimeSeries, window: Int = 10): Double {
    val n = one.length
    val m = other.length
    val w = max(window, abs(n - m))
    val matrix = Array(n + 1) { Array(m + 1) { Double.POSITIVE_INFINITY } }

    matrix[0][0] = 0.0

    (1 until (n + 1)).forEach { i ->
        (max(1, i - w) until (min(m, i + w) + 1)).forEach { j ->
            matrix[i][j] = 0.0
        }
    }

    (1 until (n + 1)).forEach { i ->
        (max(1, i - w) until (min(m, i + w) + 1)).forEach { j ->
            val cost = abs(one.values[i - 1] - other.values[j - 1])
            val min = listOf(matrix[i - 1][j], matrix[i][j - 1], matrix[i - 1][j - 1]).min()
            matrix[i][j] = cost + min
        }
    }

    return matrix[n][m]
}

fun lcss(one: TimeSeries, other: TimeSeries, delta: Double = 10.0, epsilon: Double = 0.001): Double {
    val n = one.length
    val m = other.length

    val matrix = Array(n + 1) { Array(m + 1) { 0.0 } }

    (1 until (n + 1)).forEach { i ->
        (1 until (m + 1)).forEach { j ->
            val cost = abs(one.values[i - 1] - other.values[j - 1])
            if (cost < epsilon && abs(i - j) < delta) {
                matrix[i][j] = matrix[i - 1][j - 1] + 1
            } else {
                matrix[i][j] = max(matrix[i][j - 1], matrix[i - 1][j])
            }
        }
    }

    return 1 - (matrix[n][m] / min(n, m))
}
