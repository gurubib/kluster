package hu.gurubib.api.stock.services

import hu.gurubib.dao.models.domain
import hu.gurubib.dao.models.toInitializer
import hu.gurubib.dao.repositories.FetchQueryRepository
import hu.gurubib.domain.stock.models.CreateFetchQueryReq
import hu.gurubib.domain.stock.models.FetchQuery
import hu.gurubib.domain.stock.models.toFetchQuery
import hu.gurubib.domain.stock.services.StockFetcherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface FetchQueryService {
    suspend fun createFetchQuery(req: CreateFetchQueryReq): List<FetchQuery>
    suspend fun getFetchQuery(uuid: String): FetchQuery?
}

class FetchQueryServiceImpl(
    private val repository: FetchQueryRepository,
    private val stockFetcherService: StockFetcherService,
) : FetchQueryService {
    override suspend fun createFetchQuery(req: CreateFetchQueryReq): List<FetchQuery> {
        val entitiesToCreate = req.toFetchQuery()

        val createdEntities = entitiesToCreate.map {
            repository.createTransactional(it.toInitializer()).domain()
        }

        CoroutineScope(Dispatchers.IO).launch {
            createdEntities.forEach {
                stockFetcherService.fetch(it.uuid)
            }
        }

        return createdEntities
    }

    override suspend fun getFetchQuery(uuid: String): FetchQuery? = repository.findByUuid(uuid)?.domain()

}