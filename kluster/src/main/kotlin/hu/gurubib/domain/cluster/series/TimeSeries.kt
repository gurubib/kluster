package hu.gurubib.domain.cluster.series

import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

const val EPSILON = 0.000001

data class TimeSeries(
    val points: List<Vec2>,
    val symbol: String,
) {
    val length get() = points.size
    val values get() = points.map { it.y }
    val timeDomain get() = points.map { it.x }

    operator fun get(i: Int): Vec2 {
        require(i < length) { "Index ($i) is out of bounds for series (length: $length)!" }
        return points[i]
    }

    operator fun get(range: IntRange): Sequence<Vec2> {
        require(range.last <= length) {
            "Range (${range.first}..${range.last}) is out of bounds for series (length: $length)!"
        }

        return sequence {
            for (i in range.first until range.last) {
                yield(points[i])
            }
        }
    }

    fun subSeries(range: IntRange): TimeSeries {
        require(range.last < length) {
            "Range (${range.first}..${range.last}) is out of bounds for series (length: $length)!"
        }

        return TimeSeries(points.slice(range), symbol)
    }

    fun subSeries(indices: List<Int>): TimeSeries {
        require(indices.size <= length) { "Too many indices (num: ${indices.size}) for series (length: $length)" }
        require(indices.last() < length) {"Index out of bounds (${indices.last()}) for series (length: $length)" }
        return TimeSeries(points.filterIndexed { i, _ -> indices.contains(i) }, symbol)
    }

    fun toList() = points
}

fun TimeSeries.normalised(): TimeSeries {
    val xMin = timeDomain.minOf { it }
    val xMax = timeDomain.maxOf { it }
    val yMin = values.minOf { it }
    val yMax = values.maxOf { it }

    val normalizedPoints = points.map { p ->
        val xN = (p.x - xMin) / (xMax - xMin)
        val yN = (p.y - yMin) / (yMax - yMin)
        Vec2(xN, yN)
    }

    return TimeSeries(normalizedPoints, symbol)
}

fun TimeSeries.indexOfFarthestFrom(
    s: Int,
    e: Int,
    dist: (point: Vec2, oneNeighbour: Vec2, otherNeighbour: Vec2) -> Double
): Int {
    val oneNeighbour = points[s]
    val otherNeighbour = points[e]

    return (s + 1 until e).maxByOrNull { dist(points[it], oneNeighbour, otherNeighbour) } ?: -1
}

fun TimeSeries.withinEpsilon(other: TimeSeries): Boolean {
    return (0 until length).all { t -> abs(values[t] - other.values[t]) < EPSILON }
}

fun TimeSeries.withSymbol(s: String): TimeSeries = TimeSeries(
    points = points,
    symbol = s,
)

private fun fromValues(values: List<Double>): TimeSeries = TimeSeries(values.mapIndexed { i, v -> Vec2(i.toDouble(), v) }, UUID.randomUUID().toString())

fun fromValuesWithSymbol(values: List<Double>, symbol: String): TimeSeries =
    TimeSeries(values.mapIndexed { i, v -> Vec2(i.toDouble(), v) }, symbol)

fun reduceTimeSeries(objects: List<TimeSeries>, reducer: (values: List<Double>) -> Double): TimeSeries {
    val length = objects.firstOrNull()?.length ?: 0
    return fromValues((0 until length).map { t -> reducer(objects.map { o -> o.values[t] }) })
}

fun reduceToAverage(values: List<Double>): Double = values.average()

fun generateConstrainedBy(objects: List<TimeSeries>, n: Int): List<TimeSeries> {
    require(allHaveSameLength(objects)) { "All time series must have the same length!" }

    val length = objects.firstOrNull()?.length ?: 0
    val maxes = (0 until length).map { t -> objects.maxOfOrNull { it.values[t] } ?: 0.0 }
    val mins = (0 until length).map { t -> objects.minOfOrNull { it.values[t] } ?: 0.0 }

    return (0 until n).map {
        fromValues((0 until length).map { t ->
            if (mins[t] != maxes[t]) {
                Random.nextDouble(mins[t], maxes[t])
            } else {
                mins[t]
            }
        })
    }
}

private fun allHaveSameLength(objects: List<TimeSeries>): Boolean {
    val length = objects.firstOrNull()?.length ?: 0
    return objects.all { it.length == length }
}
