package hu.gurubib.api.prices.services

import hu.gurubib.api.prices.dtos.CreateFetchReq
import hu.gurubib.api.prices.dtos.CreateFetchRes
import hu.gurubib.domain.clients.finnhub.dtos.toCreateFetchRes
import hu.gurubib.domain.clients.finnhub.dtos.toFinnhubCandlesReq
import hu.gurubib.domain.clients.finnhub.services.FinnhubStockService
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface PricesService {
    suspend fun createFetch(req: CreateFetchReq): CreateFetchRes
}

class PricesServiceImpl(
    private val finnhubStockService: FinnhubStockService
) : PricesService {
    override suspend fun createFetch(req: CreateFetchReq): CreateFetchRes {
        val res = finnhubStockService.getCandles(req.toFinnhubCandlesReq())
        println(res)
        return res.toCreateFetchRes()
    }
}

fun parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr)

fun epochSecondToDate(epochSecond: Long): LocalDate =
    Instant.ofEpochSecond(epochSecond).atZone(ZoneId.systemDefault()).toLocalDate()

fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
