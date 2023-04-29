package hu.gurubib.api.stock.dtos

import hu.gurubib.domain.stock.models.MarketIndex
import kotlinx.serialization.Serializable

@Serializable
data class RMarketIndex(
    val name: String,
    val symbol: String,
    val constituents: List<String>,
)

fun RMarketIndex.domain(): MarketIndex = MarketIndex(
    name = name,
    symbol = symbol,
    constituents = constituents,
)

fun MarketIndex.dto(): RMarketIndex = RMarketIndex(
    name = name,
    symbol = symbol,
    constituents = constituents,
)
