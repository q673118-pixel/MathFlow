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
    val explanation: List<String>,
    val domainExclusions: List<Double> = emptyList()
)

/**
 * Numerical sign-chart solver. Roots and detected domain breaks are separate:
 * poles must split intervals but must never be reported as roots.
 */
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
            val value = left.evaluate(vars) - right.evaluate(vars)
            value.takeIf { it.isFinite() }
        }.getOrNull()
        fun satisfies(value: Double): Boolean =
            when (relation) {
                "<", "<=" -> value < 0.0
                ">", ">=" -> value > 0.0
                else -> false
            }

        val step = (upperBound - lowerBound) / sampleCount
        val values = Array(sampleCount + 1) { i -> valueAt(lowerBound + i * step) }
        val roots = ArrayList<Double>()
        val domainBreaks = ArrayList<Double>()
        fun addUnique(target: MutableList<Double>, x: Double) {
            if (x.isFinite() && target.none { abs(it - x) < step * 0.2 }) target += x
        }
        fun addRoot(x: Double) {
            if (abs(valueAt(x) ?: Double.POSITIVE_INFINITY) < 1.0e-7) addUnique(roots, x)
            else addUnique(domainBreaks, x)
        }

        for (i in 0 until sampleCount) {
            val a = values[i]
            val b = values[i + 1]
            val x0 = lowerBound + i * step
            val x1 = x0 + step
            if (a == null || b == null) {
                if (a == null && b != null) {
                    var invalid = x0
                    var valid = x1
                    repeat(55) {
                        val middle = (invalid + valid) * 0.5
                        if (valueAt(middle) == null) invalid = middle else valid = middle
                    }
                    addUnique(domainBreaks, (invalid + valid) * 0.5)
                } else if (a != null && b == null) {
                    var valid = x0
                    var invalid = x1
                    repeat(55) {
                        val middle = (valid + invalid) * 0.5
                        if (valueAt(middle) == null) invalid = middle else valid = middle
                    }
                    addUnique(domainBreaks, (valid + invalid) * 0.5)
                }
                continue
            }
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

        // Detect even-multiplicity roots, which do not change the sign.
        for (i in 1 until sampleCount) {
            val before = values[i - 1] ?: continue
            val current = values[i] ?: continue
            val after = values[i + 1] ?: continue
            if (abs(current) > abs(before) || abs(current) > abs(after) || abs(current) > 0.02) continue
            var low = lowerBound + (i - 1) * step
            var high = lowerBound + (i + 1) * step
            repeat(45) {
                val leftProbe = low + (high - low) / 3.0
                val rightProbe = high - (high - low) / 3.0
                val leftValue = abs(valueAt(leftProbe) ?: Double.POSITIVE_INFINITY)
                val rightValue = abs(valueAt(rightProbe) ?: Double.POSITIVE_INFINITY)
                if (leftValue <= rightValue) high = rightProbe else low = leftProbe
            }
            addRoot((low + high) * 0.5)
        }
        if (abs(values.last() ?: Double.POSITIVE_INFINITY) < 1.0e-10) addRoot(upperBound)

        val rootsSorted = roots.sorted()
        val breaksSorted = domainBreaks.filter { point -> roots.none { abs(it - point) < step * 0.2 } }.sorted()
        val cuts = (listOf(lowerBound) + rootsSorted + breaksSorted + listOf(upperBound)).distinct().sorted()
        val intervals = ArrayList<SolutionInterval>()
        for (i in 0 until cuts.lastIndex) {
            val leftEdge = cuts[i]
            val rightEdge = cuts[i + 1]
            val probe = leftEdge + (rightEdge - leftEdge) * 0.5
            val probeValue = valueAt(probe) ?: continue
            if (!satisfies(probeValue)) continue
            fun boundaryIncluded(x: Double): Boolean {
                if (breaksSorted.any { abs(it - x) < step * 0.2 }) return false
                if (rootsSorted.any { abs(it - x) < step * 0.2 }) {
                    return relation == "<=" || relation == ">="
                }
                return valueAt(x)?.let(::satisfies) == true
            }
            intervals += SolutionInterval(
                leftEdge,
                rightEdge,
                boundaryIncluded(leftEdge),
                boundaryIncluded(rightEdge)
            )
        }

        if (relation == "<=" || relation == ">=") {
            rootsSorted.forEach { root ->
                val epsilon = (step * 0.5).coerceAtLeast(1.0e-7)
                val leftSatisfied = valueAt(root - epsilon)?.let(::satisfies) == true
                val rightSatisfied = valueAt(root + epsilon)?.let(::satisfies) == true
                val alreadyIncluded = intervals.any {
                    (abs(it.left - root) < step && it.leftIncluded) ||
                        (abs(it.right - root) < step && it.rightIncluded)
                }
                if (!leftSatisfied && !rightSatisfied && !alreadyIncluded) {
                    intervals += SolutionInterval(root, root, true, true)
                }
            }
        }

        // Merge touching intervals only when their shared point is included.
        val merged = ArrayList<SolutionInterval>()
        intervals.sortedBy { it.left }.forEach { current ->
            val previous = merged.lastOrNull()
            if (previous != null &&
                abs(previous.right - current.left) < step * 0.2 &&
                (previous.rightIncluded || current.leftIncluded)
            ) {
                merged[merged.lastIndex] = previous.copy(
                    right = current.right,
                    rightIncluded = current.rightIncluded
                )
            } else merged += current
        }

        val explanation = listOf(
            "Найдены нули и обнаруженные точки разрыва; знак проверен внутри каждого промежутка.",
            "Область поиска: [" + lowerBound + "; " + upperBound + "].",
            "Метод численный: он не доказывает отсутствие решений за пределами интервала и может пропускать очень близкие корни."
        )
        return InequalitySolution(merged, rootsSorted, lowerBound..upperBound, explanation, breaksSorted)
    }
}
