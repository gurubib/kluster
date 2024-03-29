package hu.gurubib.api.cluster.dtos

import hu.gurubib.domain.stock.models.Clustering
import hu.gurubib.util.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class RClustering(
    val uuid: String,
    @Serializable(with = LocalDateSerializer::class) val from: LocalDate,
    @Serializable(with = LocalDateSerializer::class) val to: LocalDate,
    val normalise: String,
    val distance: String,
    val algorithm: String,
    val numOfClusters: Int,
)

fun RClustering.domain(): Clustering = Clustering(
    uuid = uuid,
    from = from,
    to = to,
    normalise = normalise,
    distance = distance,
    algorithm = algorithm,
    numOfClusters = numOfClusters,
)

fun Clustering.dto(): RClustering = RClustering(
    uuid = uuid,
    from = from,
    to = to,
    normalise = normalise,
    distance = distance,
    algorithm = algorithm,
    numOfClusters = numOfClusters,
)
