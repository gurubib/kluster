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

@Resource("/indexes")
class Indexes {
    @Resource("{name}")
    class Name(val parent: Indexes = Indexes(), val name: String)
}

@Resource("/fetch-queries")
class FetchQueries {
    @Resource("{id}")
    class Id(val parent: FetchQueries = FetchQueries(), val id: String)
}
