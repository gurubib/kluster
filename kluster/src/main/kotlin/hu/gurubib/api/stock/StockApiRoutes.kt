package hu.gurubib.api.stock

import hu.gurubib.api.stock.services.FetchQueryService
import hu.gurubib.api.stock.services.MarketIndexService
import hu.gurubib.api.stock.services.StockService
import hu.gurubib.api.stock.dtos.*
import hu.gurubib.plugins.API_ROOT_PATH
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import org.koin.ktor.ext.inject

const val STOCK_API_VERSION = "v1"
const val STOCK_API_NAME = "stock"
const val STOCK_API_PATH = "${API_ROOT_PATH}/${STOCK_API_NAME}/${STOCK_API_VERSION}"

fun Route.configureStockRoutes() {
    val service: StockService by inject()

    post<Stocks> {
        val toCreate = call.receive<RStock>()
        val created = service.createStock(toCreate.domain())
        call.respond(created.dto())
    }

    get<Stocks.Symbol> {
        val found = service.getStock(it.symbol)
        if (found != null) {
            call.respond(found.dto())
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun Route.configureFetchQueryRoutes() {
    val service: FetchQueryService by inject()

    post<FetchQueries> {
        val req = call.receive<RCreateFetchQueryReq>()
        val createdEntities = service.createFetchQuery(req.domain()).map { it.dto() }
        call.respond(createdEntities)
    }

    get<FetchQueries> {
        call.respondText { "GET fetch-request" }
    }

    get<FetchQueries.Id> {
        val found = service.getFetchQuery(it.id)
        if (found != null) {
            call.respond(found.dto())
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun Route.configureMarketIndexRoutes() {
    val service: MarketIndexService by inject()

    post<MarketIndexes> {
        val toCreate = call.receive<RMarketIndex>()
        val created = service.createMarketIndex(toCreate.domain())
        call.respond(created.dto())
    }

    get<MarketIndexes.Name> {
        val found = service.getMarketIndex(it.name)
        if (found != null) {
            call.respond(found.dto())
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun Application.configureStockApiRoutes() {
    routing {
        route(STOCK_API_PATH) {
            post<Stocks> {
                call.respondText { "POST stocks" }
            }

            get<Stocks.Symbol> {
                call.respondText { "GET stocks/${it.symbol}" }
            }

            get<Stocks.Symbol.Prices> {
                val queryParams = if (it.dimension != null) {
                    "?dimension=${it.dimension}"
                } else {
                    ""
                }

                call.respondText {
                    "GET stocks/${it.parent.symbol}/prices${queryParams}"
                }
            }


            configureStockRoutes()
            configureFetchQueryRoutes()
            configureMarketIndexRoutes()
        }
    }
}