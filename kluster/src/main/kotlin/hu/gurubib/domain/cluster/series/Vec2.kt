package hu.gurubib.domain.cluster.series

import kotlin.math.sqrt

data class Vec2(
    val x: Double = 0.0,
    val y: Double = 0.0,
) {
    fun distTo(q: Vec2): Double {
        return dist(this, q);
    }

    operator fun plus(q: Vec2): Vec2 {
        return Vec2(x + q.x, y + q.y)
    }

    operator fun minus(q: Vec2): Vec2 {
        return Vec2(x - q.x, y - q.y)
    }

    operator fun times(c: Double): Vec2 {
        return Vec2(c * x, c * y)
    }
}

operator fun Double.times(p: Vec2): Vec2 {
    return p * this
}

fun dist(p: Vec2, q: Vec2): Double {
    return sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y))
}