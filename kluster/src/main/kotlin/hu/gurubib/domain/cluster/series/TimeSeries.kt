package hu.gurubib.domain.cluster.series

import java.util.UUID
import kotlin.math.abs

const val EPSILON = 0.000001

data class TimeSeries(
    val points: List<Vec2>,
    val id: String = UUID.randomUUID().toString(),
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

        return TimeSeries(points.slice(range))
    }

    fun subSeries(indices: List<Int>): TimeSeries {
        require(indices.size <= length) { "Too many indices (num: ${indices.size}) for series (length: $length)" }
        require(indices.last() < length) {"Index out of bounds (${indices.last()}) for series (length: $length)" }
        return TimeSeries(points.filterIndexed { i, _ -> indices.contains(i) })
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

    return TimeSeries(normalizedPoints)
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

fun fromValues(values: List<Double>): TimeSeries =
    TimeSeries(values.mapIndexed { i, v -> Vec2(i.toDouble(), v) })

fun reduceTimeSeries(objects: List<TimeSeries>, reducer: (values: List<Double>) -> Double): TimeSeries {
    val length = objects.firstOrNull()?.length ?: 0
    return fromValues((0 until length).map { t -> reducer(objects.map { o -> o.values[t] }) })
}

fun reduceToAverage(values: List<Double>): Double = values.average()
