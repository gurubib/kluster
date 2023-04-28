package hu.gurubib.domain.stock.services

import hu.gurubib.dao.models.PFetchQuery
import hu.gurubib.dao.models.toPPriceInitializer
import hu.gurubib.dao.repositories.FetchQueryRepository
import hu.gurubib.dao.repositories.PriceRepository
import hu.gurubib.domain.stock.models.FetchStatus
import hu.gurubib.domain.stock.models.Price
import hu.gurubib.domain.stock.models.PriceCursor
import hu.gurubib.domain.stock.services.YahooChartDataColumn.*
import hu.gurubib.util.uuid
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

interface StockFetcherService {
    suspend fun fetch(fetchQueryUuid: String)
}

class StockFetcherServiceImpl(
    private val fetchQueryRepository: FetchQueryRepository,
    private val priceRepository: PriceRepository,
) : StockFetcherService {
    override suspend fun fetch(fetchQueryUuid: String) {
        val fetchQuery = fetchQueryRepository.findByUuid(fetchQueryUuid) ?:
            throw IllegalArgumentException("Invalid fetch query uuid: (${fetchQueryUuid})!")

        try {
            fetchAndSaveStock(fetchQuery, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            fetchQueryRepository.updateTransactional(fetchQuery) { status = FetchStatus.STOPPED.name }
        }

    }

    private suspend fun fetchAndSaveStock(fetchQuery: PFetchQuery, sequenceIdPrefix: Int) {
        fetchQueryRepository.updateTransactional(fetchQuery) { status = FetchStatus.STARTED.name }

        val queryUri = buildQuery(fetchQuery)
        HttpClient(CIO).use { client ->
            val response: String = client.get(queryUri.toUriString()).bodyAsText()
            val prices: List<Price> = response.splitToSequence("\n").asFlow()
                .drop(1)
                .map { createYahooChartData(it) }
                .map {
                    it.toPrice(fetchQuery.symbol) { timestamp ->
                        PriceCursor.createPriceCursor(sequenceIdPrefix, timestamp)
                    }
                }.toList()

            newSuspendedTransaction {
                prices.asFlow()
                    .collect { priceRecord ->
                        priceRepository.create(priceRecord.toPPriceInitializer())
                    }

                fetchQuery.headCursor = prices.last().sequenceId
                fetchQuery.tailCursor = prices.first().sequenceId
            }
        }

        fetchQueryRepository.updateTransactional(fetchQuery) {
            val now = LocalDateTime.now()
            status = FetchStatus.FINISHED.name
            finishedAt = now
            updatedAt = now
        }
    }

    private fun buildQuery(fetchQuery: PFetchQuery): YahooQueryUri = YahooQueryUri(
        symbol = fetchQuery.symbol,
        YahooQueryParams(
            fromPeriod = fetchQuery.fromDate.toLocalDate(),
            toPeriod = fetchQuery.toDate.toLocalDate(),
        )
    )
}

private class YahooQueryUri(
    private val symbol: String,
    private val queryParams: YahooQueryParams,
) {
    fun toUriString(): String = url {
        protocol = URLProtocol.HTTPS
        host = "query1.finance.yahoo.com"
        path("v7", "finance", "download", symbol)
        queryParams.fill(parameters)
    }
}

private class YahooQueryParams(
    private val fromPeriod: LocalDate,
    private val toPeriod: LocalDate,
    private val events: String = "history",
    private val includeAdjustedClose: Boolean = true,
) {
    fun fill(parameters: ParametersBuilder) = with(parameters) {
        append("period1", fromPeriod.atStartOfDay().toEpochSecond(ZoneOffset.UTC).toString())
        append("period2", toPeriod.atStartOfDay(ZoneOffset.UTC).toEpochSecond().toString())
        append("interval", "1d")
        append("events", events)
        append("includeAdjustedClose", includeAdjustedClose.toString())
    }
}

private enum class YahooChartDataColumn(
    val index: Int
) {
    DATE(0), OPEN(1), HIGH(2), LOW(3), CLOSE(4), VOLUME(6)
}

private class YahooPriceData(
    val data: List<String>,
) {
    val date get() = getColumn(DATE)
    val open get() = getColumn(OPEN)
    val high get() = getColumn(HIGH)
    val low get() = getColumn(LOW)
    val close get() = getColumn(CLOSE)
    val volume get() = getColumn(VOLUME)

    private fun getColumn(column: YahooChartDataColumn) = data[column.index]
}

private fun createYahooChartData(data: String) = YahooPriceData(data.split(","))

private fun YahooPriceData.toPrice(
    symbol: String,
    newCursor: (timestamp: LocalDateTime) -> PriceCursor,
): Price {
    val date = LocalDate.parse(date)
    return Price(
        uuid = uuid(),
        symbol = symbol,
        open = open.toDouble(),
        high =  high.toDouble(),
        low = low.toDouble(),
        close = close.toDouble(),
        volume = volume.toDouble(),
        date = date,
        sequenceId = newCursor(date.atStartOfDay()).value
    )
}
