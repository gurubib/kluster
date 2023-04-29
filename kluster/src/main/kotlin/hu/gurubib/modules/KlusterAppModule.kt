package hu.gurubib.modules

import hu.gurubib.api.cluster.services.*
import hu.gurubib.dao.repositories.*
import hu.gurubib.domain.stock.services.StockFetcherService
import hu.gurubib.domain.stock.services.StockFetcherServiceImpl
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

fun klusterAppModule(config: ApplicationConfig, db: Database) = module {
    single { config }
    single { db }

    single<FetchQueryRepository> { FetchQueryRepositoryImpl(db) }
    single<PriceRepository> { PriceRepositoryImpl(db) }
    single<StockRepository> { StockRepositoryImpl(db) }
    single<MarketIndexRepository> { MarketIndexRepositoryImpl(db) }

    single<StockFetcherService> { StockFetcherServiceImpl(get(), get()) }

    single<FetchQueryService> { FetchQueryServiceImpl(get(), get()) }
    single<ClusterService> { ClusterServiceImpl(get()) }
    single<StockService> { StockServiceImpl(get()) }
    single<MarketIndexService> { MarketIndexServiceImpl(get()) }
}
