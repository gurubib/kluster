package hu.gurubib.plugins

import hu.gurubib.modules.klusterAppModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(klusterAppModule(environment.config))
    }
}