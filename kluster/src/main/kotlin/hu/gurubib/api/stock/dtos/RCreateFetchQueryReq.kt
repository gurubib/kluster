package hu.gurubib.api.stock.dtos

import hu.gurubib.domain.stock.models.CreateFetchQueryReq
import hu.gurubib.util.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class RCreateFetchQueryReq(
    val symbols: List<String>,
    @Serializable(with = LocalDateTimeSerializer::class) val from: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val to: LocalDateTime,
)

fun RCreateFetchQueryReq.domain() = CreateFetchQueryReq(
    symbols = symbols,
    from = from,
    to = to,
)
