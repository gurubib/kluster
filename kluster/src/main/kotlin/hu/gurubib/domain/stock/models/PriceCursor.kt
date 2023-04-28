package hu.gurubib.domain.stock.models

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val CURSOR_ID_OFFSET = 100_000_000_000
private const val CURSOR_ID_SPACE = 99_999
private val cursorDefaultZoneOffset = ZoneOffset.UTC

class PriceCursor private constructor(
    val value: Long,
) {
    companion object {
        val blindCursor = PriceCursor(0)

        fun of(cursorValue: Long): PriceCursor =
            if (cursorValue != 0L) {
                PriceCursor(cursorValue)
            } else {
                blindCursor
            }

        fun createPriceCursor(cursorId: Int, timestamp: LocalDateTime): PriceCursor {
            val epochSeconds = timestamp.toEpochSecond(cursorDefaultZoneOffset)

            require(cursorId > 0) { "The cursor id must be positive!" }
            require(cursorId < CURSOR_ID_SPACE) { "The given cursor id is out of range!" }
            require(epochSeconds > 0) { "The cursor timestamp must be positive!" }

            val shiftedCursorId = cursorId * CURSOR_ID_OFFSET
            return PriceCursor(shiftedCursorId + epochSeconds)
        }
    }

    val id: Int by lazy { (value / CURSOR_ID_OFFSET).toInt() }
    val timestamp: Long by lazy { value - (id * CURSOR_ID_OFFSET) }
}

fun LocalDateTime.toPriceCursor(cursorId: Int): PriceCursor = PriceCursor.createPriceCursor(cursorId, this)

fun nowCursor(cursorId: Int): PriceCursor = LocalDateTime.now().toPriceCursor(cursorId)

fun PriceCursor.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), cursorDefaultZoneOffset)

//fun cursorIdOf(chart: PDailyChart): Int = chart.id.value
