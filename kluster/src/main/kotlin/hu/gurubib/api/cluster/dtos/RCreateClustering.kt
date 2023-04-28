package hu.gurubib.api.cluster.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RCreateClusteringReq(
    val index: String,
    val fromDate: String,
    val toDate: String,
    val normalise: String,
    val distance: String,
    val clustering: String,
    val numOfClusters: Int,
)

@Serializable
data class RCreateClusteringRes(
    val clusters: List<List<String>>,
)