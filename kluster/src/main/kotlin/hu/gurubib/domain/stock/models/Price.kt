package hu.gurubib.domain.stock.models

import java.time.LocalDate

data class Price(
    val uuid: String,
    val symbol: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val date: LocalDate,
    val sequenceId: Long,
)