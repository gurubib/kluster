package hu.gurubib.api.stock.dtos

import hu.gurubib.domain.stock.models.FetchQuery
import hu.gurubib.util.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class RFetchQuery(
    val uuid: String,
    val symbol: String,
    @Serializable(with = LocalDateTimeSerializer::class) val from: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val to: LocalDateTime,
    val status: String,
    @Serializable(with = LocalDateTimeSerializer::class) val finishedAt: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime,
)

fun FetchQuery.dto() = RFetchQuery(
    uuid = uuid,
    symbol = symbol,
    from = from,
    to = to,
    createdAt = createdAt,
    updatedAt = updatedAt,
    finishedAt = finishedAt,
    status = status.name,
)
