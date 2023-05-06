package hu.gurubib.dao.models

import hu.gurubib.domain.stock.models.MarketIndex
import hu.gurubib.plugins.UserService.Users.index
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object MarketIndexes : IntIdTable("market_indexes") {
    val name = varchar("name", 100).uniqueIndex()
    val symbol = varchar("symbol", 10).uniqueIndex()
}

class PMarketIndex(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PMarketIndex>(MarketIndexes)

    var name by MarketIndexes.name
    var symbol by MarketIndexes.symbol
}

fun PMarketIndex.domain(constituents: List<String>): MarketIndex = MarketIndex(
    name = name,
    symbol = symbol,
    constituents = constituents
)

fun MarketIndex.toInitializer(): PMarketIndex.() -> Unit = {
    name = this@toInitializer.name
    symbol = this@toInitializer.symbol
}

object MarketIndexConstituents : IntIdTable("market_index_constituents") {
    val indexName = varchar("index_name", length = 100).index()
    val stockSymbol = varchar("stock_symbol", length = 10)
}

class PMarketIndexConstituent(id: EntityID<Int>) : IntEntity(id) {
   companion object : IntEntityClass<PMarketIndexConstituent>(MarketIndexConstituents)

    var indexName by MarketIndexConstituents.indexName.index()
    var stockSymbol by MarketIndexConstituents.stockSymbol
}
