package hu.gurubib.api.stock.dtos

import hu.gurubib.domain.stock.models.Stock
import kotlinx.serialization.Serializable

@Serializable
data class RStock(
    val symbol: String,
    val name: String,
    val sector: String,
)

fun RStock.domain(): Stock = Stock(
    symbol = symbol,
    name = name,
    sector = sector,
)
fun Stock.dto(): RStock = RStock(
    symbol = symbol,
    name = name,
    sector = sector,
)
