package hu.gurubib.api.cluster.dtos

import hu.gurubib.domain.stock.models.Metric
import kotlinx.serialization.Serializable

@Serializable
data class RMetric(
    val clusteringUuid: String,
    val name: String,
    val value: Double,
)

fun Metric.dto(): RMetric = RMetric(
    clusteringUuid = clusteringUuid,
    name = name,
    value = value,
)
