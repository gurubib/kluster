package hu.gurubib.modules

import hu.gurubib.api.prices.services.PricesService
import hu.gurubib.api.prices.services.PricesServiceImpl
import hu.gurubib.domain.clients.finnhub.services.FinnhubStockService
import hu.gurubib.domain.clients.finnhub.services.FinnhubStockServiceImpl
import io.ktor.server.config.*
import org.koin.dsl.module

fun klusterAppModule(config: ApplicationConfig) = module {
    single { config }
    single<FinnhubStockService> { FinnhubStockServiceImpl(get()) }
    single<PricesService> {  PricesServiceImpl(get()) }
}