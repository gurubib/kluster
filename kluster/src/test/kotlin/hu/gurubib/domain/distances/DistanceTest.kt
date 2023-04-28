package hu.gurubib.domain.distances

import hu.gurubib.domain.cluster.distances.euclideanDistance
import hu.gurubib.domain.cluster.series.fromValues
import kotlin.test.Test
import kotlin.test.assertEquals

class DistanceTest {

    @Test
    fun shouldCalculateEuclideanDistanceProperly() {
        val s1 = fromValues(listOf(1, 2, 3, 4).map { it.toDouble() })
        val s2 = fromValues(listOf(2, 3, 4, 5).map { it.toDouble() })
        val s3 = fromValues(listOf(0, 1, 2, 3).map { it.toDouble() })

        assertEquals(2.0, euclideanDistance(s1, s2))
        assertEquals(2.0, euclideanDistance(s1, s3))
    }

    @Test
    fun shouldEuclideanDistanceBeSymmetric() {
        val s1 = fromValues(listOf(1, 2, 3, 4).map { it.toDouble() })
        val s2 = fromValues(listOf(2, 3, 4, 5).map { it.toDouble() })

        assertEquals(2.0, euclideanDistance(s1, s2))
        assertEquals(2.0, euclideanDistance(s2, s1))
    }
}