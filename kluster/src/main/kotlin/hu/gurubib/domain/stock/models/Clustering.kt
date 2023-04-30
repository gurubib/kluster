package hu.gurubib.domain.stock.models

import java.time.LocalDate

data class Clustering(
    val symbols: List<String>,
    val from: LocalDate,
    val to: LocalDate,
    val normalise: String,
    val distance: String,
    val algorithm: String,
    val numOfClusters: Int,
)