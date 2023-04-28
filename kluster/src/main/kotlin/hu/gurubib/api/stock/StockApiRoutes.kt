package hu.gurubib.api.stock

import hu.gurubib.api.cluster.services.FetchQueryService
import hu.gurubib.api.stock.dtos.RCreateFetchQueryReq
import hu.gurubib.api.stock.dtos.domain
import hu.gurubib.api.stock.dtos.dto
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

fun Route.configureFetchQueryRoutes() {
    val service: FetchQueryService by inject()

    post<FetchQueries> {
        val req = call.receive<RCreateFetchQueryReq>()
        val created = service.createFetchQuery(req.domain())
        call.respond(created.dto())
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


            post<Indexes> {
                call.respondText { "POST indexes" }
            }

            get<Indexes.Name> {
                call.respondText { "GET indexes/${it.name}" }
            }


            configureFetchQueryRoutes()
        }
    }
}