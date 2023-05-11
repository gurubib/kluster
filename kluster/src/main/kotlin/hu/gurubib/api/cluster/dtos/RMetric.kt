package hu.gurubib.api.cluster.dtos

import hu.gurubib.domain.stock.models.Metric
import hu.gurubib.domain.stock.models.SimilarityMetric
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

@Serializable
data class RSimilarityMetric(
    val oneClusteringUuid: String,
    val otherClusteringUuid: String,
    val name: String,
    val value: Double,
)

fun SimilarityMetric.dto(): RSimilarityMetric = RSimilarityMetric(
    oneClusteringUuid = oneClusteringUuid,
    otherClusteringUuid = otherClusteringUuid,
    name = name,
    value = value,
)
