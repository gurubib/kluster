package hu.gurubib.api.stock.dtos

import hu.gurubib.domain.stock.models.Price
import hu.gurubib.util.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class RPrice(
    val uuid: String,
    val symbol: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
    val sequenceId: Long,
)

fun Price.dto(): RPrice = RPrice(
    uuid = uuid,
    symbol = symbol,
    open - open,
    high = high,
    low = low,
    close = close,
    volume = volume,
    date = date,
    sequenceId = sequenceId,
)
