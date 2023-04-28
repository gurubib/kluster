package hu.gurubib.plugins

import hu.gurubib.api.cluster.configureClusterApiRoutes
import hu.gurubib.api.stock.configureStockApiRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*

const val API_ROOT_PATH = "/api"

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    install(Resources)

    configureClusterApiRoutes()
    configureStockApiRoutes()
}
