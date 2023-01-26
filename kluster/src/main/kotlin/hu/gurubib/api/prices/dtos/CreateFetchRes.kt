package hu.gurubib.api.prices.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateFetchRes(
    val symbol: String,
    val fromDate: String,
    val toDate: String,
)