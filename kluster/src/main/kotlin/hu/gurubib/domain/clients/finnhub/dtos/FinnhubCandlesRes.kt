package hu.gurubib.domain.clients.finnhub.dtos

import hu.gurubib.api.prices.dtos.CreateFetchRes
import hu.gurubib.api.prices.services.epochSecondToDate
import hu.gurubib.api.prices.services.formatDate
import kotlinx.serialization.Serializable

@Serializable
data class FinnhubCandlesRes(
    val o: List<Double>,
    val h: List<Double>,
    val l: List<Double>,
    val c: List<Double>,
    val v: List<Int>,
    val t: List<Long>,
    val s: String,
)

fun FinnhubCandlesRes.toCreateFetchRes(): CreateFetchRes {
    return CreateFetchRes(
        symbol = s.uppercase(),
        fromDate = formatDate(epochSecondToDate(t.first())),
        toDate = formatDate(epochSecondToDate(t.last())),
    )
}

