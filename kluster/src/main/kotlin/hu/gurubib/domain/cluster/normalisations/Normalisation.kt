package hu.gurubib.domain.cluster.normalisations

import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.fromValuesWithSymbol
import hu.gurubib.domain.cluster.series.normalised
import kotlin.math.*

typealias NormalisationMethod = (a: TimeSeries) -> TimeSeries

enum class Normalisations(
    val method: NormalisationMethod
) {
    NORMALISATION(::normalise),
    SCALE_TO_MINUS_1_AND_1(::scaleToMinusOneAndOne),
    DECIMAL_SCALING(::decimalScaling),
    LOG_SCALING(::logScaling),
    Z_SCORE(::zScore),
    PERFORMANCE(::performance),
}

fun normalise(series: TimeSeries): TimeSeries = series.normalised()

fun scaleToMinusOneAndOne(series: TimeSeries): TimeSeries = scaleBetween(series, -1.0, 1.0)

private fun scaleBetween(series: TimeSeries, min: Double, max: Double): TimeSeries {
    require(min < max) { "Minimum must be less than maximum" }

    return fromValuesWithSymbol(
        series.normalised().values.map { v -> v * (max - min) - ((max - min) / 2) },
        series.symbol,
    )
}

fun decimalScaling(series: TimeSeries): TimeSeries {
    val max = series.values.max()
    val d = ceil(log10(max))
    val den = (10.0).pow(d)

    return fromValuesWithSymbol(
        series.values.map { it / den },
        series.symbol,
    )
}

fun logScaling(series: TimeSeries, base: Int = 2): TimeSeries {
    val b = base.toDouble()
    return fromValuesWithSymbol(
        series.values.map { log(it, b) },
        series.symbol,
    )
}

fun zScore(series: TimeSeries): TimeSeries {
    val mean = series.values.average()
    val std = sqrt(series.values.fold(0.0) { acc, next -> acc + (next - mean).pow(2.0) } / series.length)

    return fromValuesWithSymbol(
        series.values.map { (it - mean) / std },
        series.symbol,
    )
}

fun performance(series: TimeSeries): TimeSeries {
    val start = series.values.first()
    return fromValuesWithSymbol(
        series.values.map { (it - start) / start },
        series.symbol,
    )
}
