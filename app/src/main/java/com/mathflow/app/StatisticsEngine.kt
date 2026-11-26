package com.mathflow.app

import kotlin.math.sqrt

data class FrequencyEntry(val value: Double, val frequency: Int)
data class HistogramBin(val lower: Double, val upper: Double, val frequency: Int)
data class StatisticsSummary(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val range: Double,
    val variance: Double,
    val standardDeviation: Double,
    val frequencies: List<FrequencyEntry>,
    val frequencyPolygon: List<Pair<Double, Int>>,
    val histogram: List<HistogramBin>,
    val cumulativeFrequencies: List<Pair<Double, Int>>
)

class StatisticsEngine {
    fun parseValues(source: String): List<Double> {
        val trimmed = source.trim()
        require(trimmed.isNotEmpty()) { "Введите хотя бы одно число" }
        val tokens = when {
            trimmed.contains(';') -> trimmed.split(';').map { it.trim() }
            trimmed.any(Char::isWhitespace) -> trimmed.split(Regex("\\s+")).map { it.trim().removeSuffix(",") }
            trimmed.count { it == ',' } == 1 -> listOf(trimmed)
            else -> trimmed.split(',')
        }.filter { it.isNotBlank() }
        require(tokens.size <= 10000) { "Допускается не более 10 000 значений" }
        return tokens.map {
            val value = it.replace(',', '.').toDoubleOrNull()
                ?: throw IllegalArgumentException("Некорректное число: " + it)
            require(value.isFinite()) { "Все значения должны быть конечными числами" }
            value
        }.also { require(it.isNotEmpty()) { "Введите хотя бы одно число" } }
    }

    fun analyze(values: List<Double>, requestedBins: Int? = null): StatisticsSummary {
        require(values.isNotEmpty()) { "Нет данных для анализа" }
        require(values.size <= 10000 && values.all { it.isFinite() }) { "Недопустимый набор данных" }
        val sorted = values.sorted()
        val count = sorted.size
        val sum = values.sum()
        require(sum.isFinite()) { "Сумма выходит за допустимый числовой диапазон" }
        val mean = sum / count
        val median = if (count % 2 == 1) sorted[count / 2]
            else (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        val frequencies = sorted.groupingBy { it }.eachCount().entries
            .sortedBy { it.key }.map { FrequencyEntry(it.key, it.value) }
        val maxFrequency = frequencies.maxOf { it.frequency }
        val mode = if (maxFrequency == 1 && frequencies.size > 1) emptyList()
            else frequencies.filter { it.frequency == maxFrequency }.map { it.value }
        val variance = values.sumOf { (it - mean) * (it - mean) } / count
        val bins = requestedBins ?: kotlin.math.ceil(1.0 + 3.322 * kotlin.math.log10(count.toDouble())).toInt().coerceAtLeast(1)
        require(bins in 1..100) { "Число интервалов должно быть от 1 до 100" }
        val min = sorted.first()
        val max = sorted.last()
        val width = if (max == min) 1.0 else (max - min) / bins
        val histogram = List(bins) { index ->
            val lower = min + index * width
            val upper = if (index == bins - 1) max else lower + width
            val frequency = if (max == min) {
                if (index == 0) count else 0
            } else {
                values.count { value ->
                    if (index == bins - 1) value >= lower && value <= upper
                    else value >= lower && value < upper
                }
            }
            HistogramBin(lower, upper, frequency)
        }
        var cumulativeCount = 0
        val cumulative = frequencies.map { entry ->
            cumulativeCount += entry.frequency
            entry.value to cumulativeCount
        }
        return StatisticsSummary(
            count, sum, mean, median, mode, max - min, variance, sqrt(variance),
            frequencies,
            frequencies.map { it.value to it.frequency },
            histogram,
            cumulative
        )
    }
}
