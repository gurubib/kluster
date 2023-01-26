package hu.gurubib.api.prices

import hu.gurubib.api.prices.services.PricesService
import hu.gurubib.plugins.API_ROOT_PATH
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.post
import org.koin.ktor.ext.inject

const val PRICES_API_VERSION = "v1"
const val PRICES_API_NAME = "prices"
const val PRICES_API_PATH = "${API_ROOT_PATH}/${PRICES_API_NAME}/${PRICES_API_VERSION}"

fun Application.configurePricesApiRoutes() {

    val service: PricesService by inject()

    routing {
        route(PRICES_API_PATH) {
            post<Fetches> {
                val res = service.createFetch(call.receive())
                call.respond(res)
            }
        }
    }
}