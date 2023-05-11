package hu.gurubib.domain.metric

import hu.gurubib.domain.cluster.metrics.generatePairs
import kotlin.test.Test
import kotlin.test.assertEquals

class MetricTest {

    @Test
    fun testGeneratePairs() {
        val pairs = generatePairs(6)

        println(pairs)
        assertEquals((6 * 5) / 2, pairs.size)
    }
}