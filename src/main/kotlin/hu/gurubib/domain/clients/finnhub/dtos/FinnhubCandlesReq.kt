package hu.gurubib.domain.clients.finnhub.dtos

import hu.gurubib.api.prices.dtos.CreateFetchReq
import hu.gurubib.api.prices.services.parseDate
import hu.gurubib.domain.clients.finnhub.services.localDateToEpochTime

data class FinnhubCandlesReq(
    val symbol: String,
    val resolution: FinnhubCandleResolution,
    val from: Long,
    val to: Long,
)

fun CreateFetchReq.toFinnhubCandlesReq(): FinnhubCandlesReq = FinnhubCandlesReq(
    symbol = symbol.uppercase(),
    resolution = FinnhubCandleResolution.DAY,
    from = localDateToEpochTime(parseDate(fromDate)),
    to = localDateToEpochTime(parseDate(toDate))
)

const val FINNHUB_CANDLES_REQ_SYMBOL_PARAM_NAME = "symbol"
const val FINNHUB_CANDLES_REQ_RESOLUTION_PARAM_NAME = "resolution"
const val FINNHUB_CANDLES_REQ_FROM_PARAM_NAME = "from"
const val FINNHUB_CANDLES_REQ_TO_PARAM_NAME = "to"

enum class FinnhubCandleResolution(val code: String) {
    MINUTE("1"),
    FIVE_MINUTE("5"),
    FIFTEEN_MINUTE("15"),
    HALF_HOUR("30"),
    HOUR("60"),
    DAY("D"),
    WEEK("W"),
    MONTH("M"),
}