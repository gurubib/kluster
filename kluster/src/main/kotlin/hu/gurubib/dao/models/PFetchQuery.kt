package hu.gurubib.dao.models

import hu.gurubib.domain.stock.models.FetchQuery
import hu.gurubib.domain.stock.models.PriceCursor
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object FetchQueries : IntIdTable("fetch_queries") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val symbol = varchar("symbol", 6).index()
    val fromDate = datetime("from_date")
    val toDate = datetime("to_date")
    val status = varchar("status", 20)
    val finishedAt = datetime("finished_at").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val headCursor = long("head_cursor")
    val tailCursor = long("tail_cursor")
}

class PFetchQuery(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<PFetchQuery>(FetchQueries)

    var uuid            by FetchQueries.uuid
    var symbol          by FetchQueries.symbol
    var fromDate        by FetchQueries.fromDate
    var toDate          by FetchQueries.toDate
    var status          by FetchQueries.status
    var finishedAt      by FetchQueries.finishedAt
    var createdAt       by FetchQueries.createdAt
    var updatedAt       by FetchQueries.updatedAt
    var headCursor      by FetchQueries.headCursor
    var tailCursor      by FetchQueries.tailCursor
}

fun PFetchQuery.domain() = FetchQuery(
    uuid = uuid,
    symbol = symbol,
    from = fromDate,
    to = toDate,
    status = enumValueOf(status),
    finishedAt = finishedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    headCursor = PriceCursor.of(headCursor),
    tailCursor = PriceCursor.of(tailCursor),
)

fun FetchQuery.toInitializer(): PFetchQuery.() -> Unit = {
    uuid = this@toInitializer.uuid
    symbol = this@toInitializer.symbol
    fromDate = this@toInitializer.from
    toDate = this@toInitializer.to
    status = this@toInitializer.status.name
    finishedAt = this@toInitializer.finishedAt
    createdAt = this@toInitializer.createdAt
    updatedAt = this@toInitializer.updatedAt
    headCursor = this@toInitializer.headCursor.value
    tailCursor = this@toInitializer.tailCursor.value
}