package hu.gurubib.api.stock

import io.ktor.resources.*

@Resource("/stocks")
class Stocks {
    @Resource("{symbol}")
    class Symbol(val parent: Stocks = Stocks(), val symbol: String) {
        @Resource("prices")
        class Prices(val parent: Symbol, val dimension: String? = null)
    }
}

@Resource("/market-indexes")
class MarketIndexes {
    @Resource("{name}")
    class Name(val parent: MarketIndexes = MarketIndexes(), val name: String)
}

@Resource("/fetch-queries")
class FetchQueries {
    @Resource("{id}")
    class Id(val parent: FetchQueries = FetchQueries(), val id: String)
}
