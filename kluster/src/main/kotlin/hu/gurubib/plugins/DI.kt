package hu.gurubib.plugins

import hu.gurubib.dao.models.*
import hu.gurubib.modules.klusterAppModule
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
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
        SchemaUtils.create(MarketIndexes)
        SchemaUtils.create(MarketIndexConstituents)
//        SchemaUtils.createIndex(
//            Index(
//                listOf(MarketIndexConstituents.indexName, MarketIndexConstituents.stockSymbol),
//                true
//            ),
//        )
        SchemaUtils.create(Clusterings)
        SchemaUtils.create(ClusteredObjects)
        SchemaUtils.create(Metrics)
    }

    return db
}