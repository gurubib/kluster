package hu.gurubib.plugins

import hu.gurubib.dao.models.FetchQueries
import hu.gurubib.dao.models.Prices
import hu.gurubib.dao.models.Stocks
import hu.gurubib.modules.klusterAppModule
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()

        val config = environment.config
        val db = createAndConfigureDB(config)
        modules(klusterAppModule(config, db))
    }
}

private fun createAndConfigureDB(config: ApplicationConfig): Database {
    val db = Database.connect(
            url = "jdbc:postgresql://localhost:5432/${config.property("jdbc.db").getString()}",
            driver = "org.postgresql.Driver",
            user = config.property("jdbc.user").getString(),
            password = config.property("jdbc.password").getString()
        )

    transaction(db) {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(Stocks)
        SchemaUtils.create(FetchQueries)
        SchemaUtils.create(Prices)
    }

    return db
}