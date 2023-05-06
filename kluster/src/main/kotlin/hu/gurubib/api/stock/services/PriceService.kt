package hu.gurubib.api.stock.services

import java.time.LocalDate

interface PriceService {
    suspend fun getPrice(symbol: String, date: LocalDate)
}