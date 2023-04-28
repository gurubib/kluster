package hu.gurubib.api.cluster.services

import java.time.LocalDate

interface PriceService {
    suspend fun getPrice(symbol: String, date: LocalDate)
}