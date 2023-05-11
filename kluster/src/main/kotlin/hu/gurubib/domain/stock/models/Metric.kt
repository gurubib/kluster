package hu.gurubib.domain.stock.models

data class Metric(
    val clusteringUuid: String,
    val name: String,
    val value: Double,
)

data class SimilarityMetric(
    val oneClusteringUuid: String,
    val otherClusteringUuid: String,
    val name: String,
    val value: Double,
)
