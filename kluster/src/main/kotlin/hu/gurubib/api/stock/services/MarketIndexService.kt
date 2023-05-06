package hu.gurubib.api.stock.services

import hu.gurubib.dao.models.domain
import hu.gurubib.dao.models.toInitializer
import hu.gurubib.dao.repositories.MarketIndexRepository
import hu.gurubib.domain.stock.models.MarketIndex

interface MarketIndexService {
    suspend fun createMarketIndex(marketIndex: MarketIndex): MarketIndex
    suspend fun getMarketIndex(name: String): MarketIndex?
}

class MarketIndexServiceImpl(
    private val marketIndexRepository: MarketIndexRepository
) : MarketIndexService {
    override suspend fun createMarketIndex(marketIndex: MarketIndex): MarketIndex {
        val constituents = marketIndexRepository.createConstituentsFor(marketIndex.name, marketIndex.constituents)
            .map { it.stockSymbol }
        return marketIndexRepository.createTransactional(marketIndex.toInitializer()).domain(constituents)
    }

    override suspend fun getMarketIndex(name: String): MarketIndex? {
        val constituents = findConstituentsFor(name)
        return marketIndexRepository.findByName(name)?.domain(constituents)
    }

    private suspend fun findConstituentsFor(index: String): List<String> =
        marketIndexRepository.findConstituentsFor(index).map { it.stockSymbol }
}
