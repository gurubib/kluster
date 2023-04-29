package hu.gurubib.dao.models

import hu.gurubib.domain.stock.models.Stock
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Stocks : IntIdTable("stocks") {
    val symbol = varchar("symbol", 10).uniqueIndex()
    val name = varchar("name", 250)
    val sector = varchar("sector", length = 250)
}

class PStock(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PStock>(Stocks)

    var symbol by Stocks.symbol
    var name by Stocks.name
    var sector by Stocks.sector
}

fun PStock.domain(): Stock = Stock(
    symbol = symbol,
    name = name,
    sector = sector,
)

fun Stock.toInitializer(): PStock.() -> Unit = {
    symbol = this@toInitializer.symbol
    name = this@toInitializer.name
    sector = this@toInitializer.sector
}
