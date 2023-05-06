package hu.gurubib.domain.stock.models

data class Metric(
    val clusteringUuid: String,
    val name: String,
    val value: Double,
)
