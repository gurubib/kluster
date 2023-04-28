package hu.gurubib.domain.stock.models

import java.time.LocalDateTime

enum class FetchStatus {
    NEW, STARTED, FINISHED, STOPPED
}

data class FetchQuery(
    val uuid: String,
    val symbol: String,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val status: FetchStatus,
    val finishedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val headCursor: PriceCursor,
    val tailCursor: PriceCursor,
)