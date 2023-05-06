package hu.gurubib.domain.stock.models

import hu.gurubib.util.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

data class CreateClusteringReq(
    val symbols: List<String>,
    val from: LocalDate,
    val to: LocalDate,
    val normalise: String,
    val distance: String,
    val algorithm: String,
    val numOfClusters: Int,
)

fun CreateClusteringReq.toClustering(): Clustering = Clustering(
    uuid = UUID.randomUUID().toString(),
    from = from,
    to = to,
    normalise = normalise,
    distance = distance,
    algorithm = algorithm,
    numOfClusters = numOfClusters,
)
