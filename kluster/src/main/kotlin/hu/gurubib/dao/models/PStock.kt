package hu.gurubib.dao.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Stocks : IntIdTable("stocks") {
    val symbol = varchar("symbol", 10).uniqueIndex()
    val name = varchar("name", 500)
    val exchange = varchar("exchange", length = 50)
}

class PStock(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<PStock>(Stocks)

    var symbol by Stocks.symbol
    var name by Stocks.name
    val exchange by Stocks.exchange
}

//fun PStock.toStock(): Stock = NasdaqStock(
//    symbol = symbol,
//    name = name,
//)