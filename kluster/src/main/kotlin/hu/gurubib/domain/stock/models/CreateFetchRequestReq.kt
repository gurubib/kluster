package hu.gurubib.domain.stock.models

import hu.gurubib.util.now
import hu.gurubib.util.uuid
import java.time.LocalDateTime

data class CreateFetchQueryReq(
    val symbol: String,
    val from: LocalDateTime,
    val to: LocalDateTime,
)

fun CreateFetchQueryReq.toFetchQuery(): FetchQuery {
    val now = now()
    return FetchQuery(
        uuid = uuid(),
        symbol = symbol,
        from = from,
        to = to,
        status = FetchStatus.NEW,
        finishedAt = null,
        createdAt = now,
        updatedAt = now,
        headCursor = PriceCursor.blindCursor,
        tailCursor = PriceCursor.blindCursor,
    )
}
