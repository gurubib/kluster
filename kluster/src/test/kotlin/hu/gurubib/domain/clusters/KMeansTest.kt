package hu.gurubib.domain.clusters

import hu.gurubib.domain.cluster.clusterings.generateCentroids
import hu.gurubib.domain.cluster.clusterings.kMeans
import hu.gurubib.domain.cluster.distances.euclideanDistance
import hu.gurubib.domain.cluster.series.fromValues
import kotlin.test.Test
import kotlin.test.assertTrue

class KMeansTest {

    @Test
    fun shouldGenerateCentroidsProperly() {
        val s1 = fromValues(listOf(1, 2, 3, 4).map { it.toDouble() })
        val s2 = fromValues(listOf(2, 3, 4, 5).map { it.toDouble() })
        val s3 = fromValues(listOf(3, 4, 5, 6).map { it.toDouble() })
        val s4 = fromValues(listOf(4, 5, 6, 7).map { it.toDouble() })

        val cs1 = generateCentroids(listOf(s1, s2, s3, s4), 2)
        val cs2 = generateCentroids(listOf(s3, s4, s1, s2), 3)

        cs1.forEach { c ->
            assertTrue { c.values[0] in 1.0..4.0 }
            assertTrue { c.values[1] in 2.0..5.0 }
            assertTrue { c.values[2] in 3.0..6.0 }
            assertTrue { c.values[3] in 4.0..7.0 }
        }

        cs2.forEach { c ->
            assertTrue { c.values[0] in 1.0..4.0 }
            assertTrue { c.values[1] in 2.0..5.0 }
            assertTrue { c.values[2] in 3.0..6.0 }
            assertTrue { c.values[3] in 4.0..7.0 }
        }
    }

    @Test
    fun kMeansTest() {
        val s1 = fromValues(listOf(1, 2, 3, 4).map { it.toDouble() })
        val s2 = fromValues(listOf(2, 3, 4, 5).map { it.toDouble() })
        val s3 = fromValues(listOf(8, 9, 10, 11).map { it.toDouble() })
        val s4 = fromValues(listOf(9, 10, 11, 12).map { it.toDouble() })

        val clusters = kMeans(listOf(s1, s2, s3, s4), ::euclideanDistance, 2)

        clusters.forEach { (c, ts) ->
            println("Cluster:")
            println("--- --- ---")
            println("Centroid:")
            println("\t$c")
            println()
            println("Objects:")
            ts.forEach { println("\t$it") }
            println()
            println()
        }
    }
}