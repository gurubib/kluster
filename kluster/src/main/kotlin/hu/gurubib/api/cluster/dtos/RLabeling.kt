package hu.gurubib.api.cluster.dtos

import hu.gurubib.domain.stock.models.Labelling
import kotlinx.serialization.Serializable

@Serializable
data class RLabelling(
    val clusters: List<List<String>>,
)

fun RLabelling.domain(): Labelling = Labelling(
    clusters = clusters,
)
