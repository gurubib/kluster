package hu.gurubib.domain.cluster.normalisations

import hu.gurubib.domain.cluster.series.TimeSeries
import hu.gurubib.domain.cluster.series.fromValuesWithSymbol
import hu.gurubib.domain.cluster.series.normalised

typealias NormalisationMethod = (a: TimeSeries) -> TimeSeries

enum class Normalisations(
    val method: NormalisationMethod
) {
    NORMALISATION(::normalise),
    SCALE_TO_MINUS_1_AND_1(::scaleToMinusOneAndOne),
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
