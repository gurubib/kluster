package hu.gurubib.api.stock.services

import hu.gurubib.dao.models.domain
import hu.gurubib.dao.models.toInitializer
import hu.gurubib.dao.repositories.StockRepository
import hu.gurubib.domain.stock.models.Stock

interface StockService {
    suspend fun getStock(symbol: String): Stock?

    suspend fun createStock(stock: Stock): Stock
}

class StockServiceImpl(private val stockRepository: StockRepository) : StockService {
    override suspend fun getStock(symbol: String): Stock? = stockRepository.findBySymbol(symbol)?.domain()

    override suspend fun createStock(stock: Stock): Stock {
        val toCreate = stock.toInitializer()
        return stockRepository.createTransactional(toCreate).domain()
    }
}
