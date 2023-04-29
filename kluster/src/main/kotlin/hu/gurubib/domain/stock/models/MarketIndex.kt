package hu.gurubib.domain.stock.models

data class MarketIndex(
    val name: String,
    val symbol: String,
    val constituents: List<String> = listOf()
)