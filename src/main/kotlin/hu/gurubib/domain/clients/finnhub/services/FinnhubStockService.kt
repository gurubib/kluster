package hu.gurubib.domain.clients.finnhub.services

import hu.gurubib.domain.clients.finnhub.dtos.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

const val FINNHUB_TOKEN_HEADER_NAME = "X-Finnhub-Token"

interface FinnhubStockService {
    suspend fun getCandles(req: FinnhubCandlesReq): FinnhubCandlesRes
}

class FinnhubStockServiceImpl(private val config: ApplicationConfig) : FinnhubStockService {
    override suspend fun getCandles(req: FinnhubCandlesReq): FinnhubCandlesRes = createDefaultClient().use { client ->
        val url = config.property("finnhub.url")
        val apiKey = config.property("finnhub.apiKey")
        val path = config.property("finnhub.stockApiCandlesPath")
        client.get(url.getString() + path.getString()) {
            buildRequest(apiKey, req)
        }.body()
    }

    private fun createDefaultClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
    }

    private fun HttpRequestBuilder.buildRequest(
        apiKey: ApplicationConfigValue,
        req: FinnhubCandlesReq
    ) {
        appendHeaders(apiKey)
        appendQueryParams(req)
    }

    private fun HttpRequestBuilder.appendHeaders(apiKey: ApplicationConfigValue) {
        headers {
            append(FINNHUB_TOKEN_HEADER_NAME, apiKey.getString())
        }
    }

    private fun HttpRequestBuilder.appendQueryParams(req: FinnhubCandlesReq) {
        url {
            parameters.append(FINNHUB_CANDLES_REQ_SYMBOL_PARAM_NAME, req.symbol)
            parameters.append(FINNHUB_CANDLES_REQ_RESOLUTION_PARAM_NAME, req.resolution.code)
            parameters.append(FINNHUB_CANDLES_REQ_FROM_PARAM_NAME, req.from.toString())
            parameters.append(FINNHUB_CANDLES_REQ_TO_PARAM_NAME, req.to.toString())
        }
    }
}

fun localDateToEpochTime(date: LocalDate): Long = date.atTime(12, 0).atZone(ZoneId.systemDefault()).toEpochSecond()


