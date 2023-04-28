package hu.gurubib.dao.models

import hu.gurubib.domain.stock.models.Price
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Prices : IntIdTable("prices") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val symbol = varchar("symbol", 6)
    val open = double("open")
    val high = double("high")
    val low = double("low")
    val close = double("close")
    val volume = double("volume")
    val date = date("date").index()
    val sequenceId = long("sequence_id").uniqueIndex()
}

class PPrice(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<PPrice>(Prices)

    var uuid            by Prices.uuid
    var symbol          by Prices.symbol
    var open            by Prices.open
    var high            by Prices.high
    var low             by Prices.low
    var close           by Prices.close
    var volume          by Prices.volume
    var date            by Prices.date
    var sequenceId      by Prices.sequenceId
}

fun PPrice.domain(): Price = Price(
    uuid = uuid,
    symbol = symbol,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume,
    date = date,
    sequenceId = sequenceId,
)

fun Price.toPPriceInitializer(): PPrice.() -> Unit = {
    uuid = this@toPPriceInitializer.uuid
    symbol = this@toPPriceInitializer.symbol
    open = this@toPPriceInitializer.open
    high = this@toPPriceInitializer.high
    low = this@toPPriceInitializer.low
    close = this@toPPriceInitializer.close
    volume = this@toPPriceInitializer.volume
    date = this@toPPriceInitializer.date
    sequenceId = this@toPPriceInitializer.sequenceId
}