package com.mathflow.app

import kotlin.math.abs

data class SolutionInterval(
    val left: Double,
    val right: Double,
    val leftIncluded: Boolean,
    val rightIncluded: Boolean
)
data class InequalitySolution(
    val intervals: List<SolutionInterval>,
    val criticalPoints: List<Double>,
    val searchedRange: ClosedFloatingPointRange<Double>,
    val explanation: List<String>
)

/** Numerical interval method for inequalities; explicitly reports its finite search domain. */
class InequalitySolver {
    fun solve(
        leftExpression: String,
        relation: String,
        rightExpression: String = "0",
        lowerBound: Double = -100.0,
        upperBound: Double = 100.0,
        sampleCount: Int = 8000
    ): InequalitySolution {
        require(relation in setOf("<", "<=", ">", ">=")) { "Поддерживаются знаки <, ≤, >, ≥" }
        require(lowerBound.isFinite() && upperBound.isFinite() && lowerBound < upperBound)
        require(sampleCount in 100..50000)
        val left = MathExpression.parse(leftExpression)
        val right = MathExpression.parse(rightExpression)
        fun valueAt(x: Double): Double? = runCatching {
            val vars = mapOf("x" to x)
            val a = left.evaluate(vars)
            val b = right.evaluate(vars)
            (a - b).takeIf { it.isFinite() }
        }.getOrNull()

        val step = (upperBound - lowerBound) / sampleCount
        val values = Array(sampleCount + 1) { i -> valueAt(lowerBound + i * step) }
        val roots = ArrayList<Double>()
        fun addRoot(x: Double) {
            if (x.isFinite() && abs(valueAt(x) ?: Double.POSITIVE_INFINITY) < 1.0e-7 &&
                roots.none { abs(it - x) < step * 0.1 }) roots += x
        }
        for (i in 0 until sampleCount) {
            val a = values[i] ?: continue
            val b = values[i + 1] ?: continue
            val x0 = lowerBound + i * step
            val x1 = x0 + step
            if (abs(a) < 1.0e-10) addRoot(x0)
            if ((a < 0.0) != (b < 0.0)) {
                var low = x0
                var high = x1
                var lowValue = a
                repeat(70) {
                    val middle = (low + high) * 0.5
                    val middleValue = valueAt(middle) ?: Double.NaN
                    if (!middleValue.isFinite()) {
                        high = middle
                    } else if ((middleValue < 0.0) == (lowValue < 0.0)) {
                        low = middle
                        lowValue = middleValue
                    } else high = middle
                }
                addRoot((low + high) * 0.5)
            }
        }
        if (abs(values.last() ?: Double.POSITIVE_INFINITY) < 1.0e-10) addRoot(upperBound)

        val cuts = (listOf(lowerBound) + roots.sorted() + listOf(upperBound)).distinct()
        val intervals = ArrayList<SolutionInterval>()
        for (i in 0 until cuts.lastIndex) {
            val left = cuts[i]
            val right = cuts[i + 1]
            val probe = left + (right - left) * 0.5
            val value = valueAt(probe) ?: continue
            val included = when (relation) {
                "<", "<=" -> value < 0.0
                ">", ">=" -> value > 0.0
                else -> false
            }
            if (included) {
                val leftRoot = roots.any { abs(it - left) < step * 0.2 }
                val rightRoot = roots.any { abs(it - right) < step * 0.2 }
                val includeBoundary = relation == "<=" || relation == ">="
                intervals += SolutionInterval(left, right,
                    if (leftRoot) includeBoundary else valueAt(left) != null,
                    if (rightRoot) includeBoundary else valueAt(right) != null)
            }
        }
        val explanation = listOf(
            "Найдены критические точки численно, затем проверен знак выражения на каждом промежутке.",
            "Область поиска: [" + lowerBound + "; " + upperBound + "].",
            "Численный метод не доказывает отсутствие решений за пределами этого интервала и может требовать уточнения около разрывов."
        )
        return InequalitySolution(intervals, roots.sorted(), lowerBound..upperBound, explanation)
    }
}
