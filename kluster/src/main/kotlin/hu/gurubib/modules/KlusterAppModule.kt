package hu.gurubib.modules

import hu.gurubib.api.cluster.services.ClusterService
import hu.gurubib.api.cluster.services.ClusterServiceImpl
import hu.gurubib.api.cluster.services.FetchQueryService
import hu.gurubib.api.cluster.services.FetchQueryServiceImpl
import hu.gurubib.dao.repositories.PriceRepositoryImpl
import hu.gurubib.dao.repositories.FetchQueryRepository
import hu.gurubib.dao.repositories.FetchQueryRepositoryImpl
import hu.gurubib.dao.repositories.PriceRepository
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

    single<StockFetcherService> { StockFetcherServiceImpl(get(), get()) }

    single<FetchQueryService> { FetchQueryServiceImpl(get(), get()) }
    single<ClusterService> { ClusterServiceImpl(get()) }
}
