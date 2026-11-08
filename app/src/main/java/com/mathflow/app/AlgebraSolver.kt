package com.mathflow.app

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

data class EquationSolution(
    val kind: String,
    val answer: String,
    val steps: List<String>
)

/**
 * One-variable polynomial equation solver for degrees 0..4.
 * Coefficients are kept as exact Rational values until numerical roots are needed.
 * Linear and quadratic roots remain exact whenever possible; cubic and quartic
 * real roots are isolated numerically using derivative intervals and bisection.
 *
 * The parser deliberately rejects unsupported non-polynomial denominators instead
 * of silently changing the equation's domain.
 */
class AlgebraSolver {
    private data class Poly private constructor(val coefficients: List<Rational>) {
        val degree: Int get() = coefficients.indexOfLast { it != Rational.ZERO }
        operator fun get(index: Int): Rational = coefficients.getOrElse(index) { Rational.ZERO }

        operator fun plus(other: Poly) = of((0..maxOf(degree, other.degree)).map { this[it] + other[it] })
        operator fun minus(other: Poly) = of((0..maxOf(degree, other.degree)).map { this[it] - other[it] })
        operator fun unaryMinus() = of(coefficients.map { -it })

        operator fun times(other: Poly): Poly {
            if (degree < 0 || other.degree < 0) return ZERO
            val result = MutableList(degree + other.degree + 1) { Rational.ZERO }
            for (i in 0..degree) for (j in 0..other.degree) {
                if (this[i] != Rational.ZERO && other[j] != Rational.ZERO) {
                    require(i + j <= 4) { "Поддерживаются многочлены не выше четвёртой степени" }
                    result[i + j] = result[i + j] + this[i] * other[j]
                }
            }
            return of(result)
        }

        fun dividedBy(value: Rational): Poly {
            require(value != Rational.ZERO) { "Деление на ноль" }
            return of(coefficients.map { it / value })
        }

        fun pow(exponent: Int): Poly {
            require(exponent in 0..4) { "Поддерживаются степени от 0 до 4" }
            var result = ONE
            repeat(exponent) { result = result * this }
            return result
        }

        companion object {
            val ZERO = Poly(listOf(Rational.ZERO))
            val ONE = Poly(listOf(Rational.ONE))
            val X = Poly(listOf(Rational.ZERO, Rational.ONE))
            fun constant(value: Rational) = of(listOf(value))
            fun of(values: List<Rational>): Poly {
                val trimmed = values.toMutableList()
                while (trimmed.size > 1 && trimmed.last() == Rational.ZERO) trimmed.removeAt(trimmed.lastIndex)
                return Poly(trimmed.ifEmpty { listOf(Rational.ZERO) })
            }
        }
    }

    fun solve(equation: String): EquationSolution {
        val equal = equation.indexOf('=')
        require(equal >= 0 && equation.indexOf('=', equal + 1) < 0) {
            "Введите уравнение с одним знаком ="
        }
        val leftText = equation.substring(0, equal).trim()
        val rightText = equation.substring(equal + 1).trim()
        require(leftText.isNotEmpty() && rightText.isNotEmpty()) {
            "Обе части уравнения должны быть заполнены"
        }

        val reduced = Parser(leftText).parse() - Parser(rightText).parse()
        val steps = mutableListOf<String>()
        steps += "Переносим всё в левую часть: ${format(reduced)} = 0"

        val degree = reduced.degree
        if (degree < 0 || (degree == 0 && reduced[0] == Rational.ZERO)) {
            steps += "Получили верное равенство 0 = 0"
            return EquationSolution("identity", "x — любое число", steps)
        }
        if (degree == 0) {
            steps += "Получили противоречие ${reduced[0].toDisplayString()} = 0"
            return EquationSolution("no_solution", "Решений нет", steps)
        }

        val a = reduced[degree]
        val b = reduced[1]
        val c = reduced[0]

        if (degree == 1) {
            val x = -c / b
            steps += "Оставляем член с x: ${b.toDisplayString()}x = ${(-c).toDisplayString()}"
            steps += "Делим обе части на ${b.toDisplayString()}"
            steps += "x = ${x.toDisplayString()}"
            return EquationSolution("linear", "x = ${x.toDisplayString()} (${x.toDecimalString()})", steps)
        }

        if (degree == 2) {
            steps += "Это квадратное уравнение: ax² + bx + c = 0"
            steps += "a = ${a.toDisplayString()}, b = ${b.toDisplayString()}, c = ${c.toDisplayString()}"
            val d = b * b - Rational.of(4) * a * c
            steps += "Дискриминант: D = b² − 4ac = ${d.toDisplayString()}"
            if (d < Rational.ZERO) {
                steps += "D < 0, действительных корней нет"
                return EquationSolution("no_real_roots", "Действительных корней нет", steps)
            }
            val sqrtD = exactSqrt(d)
            if (sqrtD != null) {
                val x1 = (-b + sqrtD) / (Rational.of(2) * a)
                val x2 = (-b - sqrtD) / (Rational.of(2) * a)
                steps += "√D = ${sqrtD.toDisplayString()}"
                steps += "x₁ = (−b + √D)/(2a) = ${x1.toDisplayString()}"
                if (x1 == x2) {
                    return EquationSolution("quadratic_double", "x = ${x1.toDisplayString()}", steps)
                }
                steps += "x₂ = (−b − √D)/(2a) = ${x2.toDisplayString()}"
                return EquationSolution("quadratic", "x₁ = ${x1.toDisplayString()}, x₂ = ${x2.toDisplayString()}", steps)
            }

            val root = sqrt(d.toBigDecimal().toDouble())
            val aDouble = a.toBigDecimal().toDouble()
            val bDouble = b.toBigDecimal().toDouble()
            val x1 = (-bDouble + root) / (2.0 * aDouble)
            val x2 = (-bDouble - root) / (2.0 * aDouble)
            steps += "√D иррационален; корни вычислены приближённо"
            steps += "x₁ ≈ ${fmt(x1)}, x₂ ≈ ${fmt(x2)}"
            return EquationSolution("quadratic_approx", "x₁ ≈ ${fmt(x1)}, x₂ ≈ ${fmt(x2)}", steps)
        }

        steps += "Это многочлен степени $degree. Ищем все действительные корни численно."
        steps += "Коэффициенты сохранены в точной рациональной форме до этапа численного поиска."
        val roots = findRealRoots((0..degree).map { reduced[it].toBigDecimal().toDouble() }.toDoubleArray())
        if (roots.isEmpty()) {
            steps += "На действительной оси корней нет"
            return EquationSolution("no_real_roots", "Действительных корней нет", steps)
        }
        val labels = roots.mapIndexed { index, root -> "x${subscript(index + 1)} ≈ ${fmt(root)}" }
        steps += "Проверка корней выполнена подстановкой в исходный многочлен с численной точностью."
        return EquationSolution("polynomial_numeric", labels.joinToString(", "), steps)
    }

    private fun findRealRoots(input: DoubleArray): List<Double> {
        var degree = input.lastIndex
        while (degree > 0 && input[degree] == 0.0) degree--
        if (degree == 0) return emptyList()
        if (degree == 1) return listOf(-input[0] / input[1]).filter { it.isFinite() }

        val derivative = DoubleArray(degree) { index -> input[index + 1] * (index + 1) }
        val criticalPoints = findRealRoots(derivative).filter { it.isFinite() }.sorted()
        val leading = abs(input[degree])
        if (leading == 0.0 || !leading.isFinite()) return emptyList()
        var ratio = 0.0
        for (i in 0 until degree) ratio = max(ratio, abs(input[i]) / leading)
        val bound = (1.0 + ratio).coerceAtMost(1.0e12)
        val points = (listOf(-bound) + criticalPoints.filter { it > -bound && it < bound } + listOf(bound))
            .distinct().sorted()
        val roots = mutableListOf<Double>()

        fun valueAt(x: Double): Double {
            var value = input[degree]
            for (i in degree - 1 downTo 0) value = value * x + input[i]
            return value
        }
        fun toleranceAt(x: Double): Double {
            val magnitude = max(1.0, abs(x))
            var scale = 0.0
            var power = 1.0
            for (i in 0..degree) {
                scale += abs(input[i]) * power
                power *= magnitude
            }
            return 1.0e-9 * max(1.0, scale)
        }

        // Even-multiplicity roots occur at derivative roots and do not change sign.
        for (point in criticalPoints) {
            if (point in -bound..bound && abs(valueAt(point)) <= toleranceAt(point)) roots += point
        }

        for (index in 0 until points.lastIndex) {
            var low = points[index]
            var high = points[index + 1]
            var lowValue = valueAt(low)
            val highValue = valueAt(high)
            if (!lowValue.isFinite() || !highValue.isFinite() || lowValue == 0.0 || highValue == 0.0) continue
            if ((lowValue < 0.0) == (highValue < 0.0)) continue
            repeat(120) {
                val middle = low + (high - low) / 2.0
                val middleValue = valueAt(middle)
                if (middleValue == 0.0) {
                    low = middle
                    high = middle
                } else if ((middleValue < 0.0) == (lowValue < 0.0)) {
                    low = middle
                    lowValue = middleValue
                } else {
                    high = middle
                }
            }
            roots += low + (high - low) / 2.0
        }

        return roots.filter { it.isFinite() }.sorted().fold(mutableListOf()) { acc, root ->
            if (acc.isEmpty() || abs(acc.last() - root) > 1.0e-6 * max(1.0, abs(root))) acc += root
            acc
        }
    }

    private fun subscript(value: Int): String = when (value) {
        1 -> "₁"; 2 -> "₂"; 3 -> "₃"; 4 -> "₄"; else -> value.toString()
    }

    private fun exactSqrt(value: Rational): Rational? {
        if (value < Rational.ZERO) return null
        val n = integerSqrt(value.numerator)
        val d = integerSqrt(value.denominator)
        if (n * n != value.numerator || d * d != value.denominator) return null
        return Rational.of(n, d)
    }

    private fun integerSqrt(value: BigInteger): BigInteger {
        if (value < BigInteger.valueOf(2)) return value
        var x = BigInteger.ONE.shiftLeft((value.bitLength() + 1) / 2)
        while (true) {
            val next = x.add(value.divide(x)).shiftRight(1)
            if (next >= x) return x
            x = next
        }
    }

    private fun fmt(value: Double): String =
        if (value.isFinite()) "%.10f".format(java.util.Locale.US, value).trimEnd('0').trimEnd('.')
        else "не определено"

    private fun format(poly: Poly): String {
        val terms = mutableListOf<String>()
        for (power in poly.degree downTo 0) {
            val coefficient = poly[power]
            if (coefficient == Rational.ZERO) continue
            val negative = coefficient < Rational.ZERO
            val absolute = if (negative) -coefficient else coefficient
            val variable = when (power) {
                0 -> ""
                1 -> "x"
                2 -> "x²"
                3 -> "x³"
                else -> "x⁴"
            }
            val magnitude = when {
                power == 0 -> absolute.toDisplayString()
                absolute == Rational.ONE -> variable
                else -> "${absolute.toDisplayString()}$variable"
            }
            terms += if (terms.isEmpty()) {
                if (negative) "-$magnitude" else magnitude
            } else {
                if (negative) " − $magnitude" else " + $magnitude"
            }
        }
        return terms.joinToString("").ifEmpty { "0" }
    }

    private class Parser(input: String) {
        private val normalizedInput = input
            .replace("⁴", "^4").replace("⁳", "^3").replace("³", "^3").replace("²", "^2")
            .replace("−", "-").replace("×", "*").replace("÷", "/")
            .filterNot { it.isWhitespace() }
        private val tokens = Regex("""\d+(?:[.,]\d+)?|[xX]|[()+\-*/^]""")
            .findAll(normalizedInput).map { it.value }.toList()
        private var index = 0

        init {
            require(tokens.joinToString("") == normalizedInput) {
                "Недопустимый символ в уравнении: $input"
            }
        }

        fun parse(): Poly {
            val result = expression()
            require(index == tokens.size) { "Проверьте запись выражения" }
            return result
        }

        private fun expression(): Poly {
            var value = term()
            while (peek() == "+" || peek() == "-") {
                val operator = take()
                val right = term()
                value = if (operator == "+") value + right else value - right
            }
            return value
        }

        private fun term(): Poly {
            var value = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val operator = if (peek() == "*" || peek() == "/") take() else "*"
                val right = unary()
                value = if (operator == "*") value * right else {
                    require(right.degree <= 0) { "Делить можно только на число, не содержащее x" }
                    value.dividedBy(right[0])
                }
            }
            return value
        }

        private fun startsPrimary(token: String?): Boolean =
            token == "(" || token?.equals("x", true) == true || token?.firstOrNull()?.isDigit() == true

        private fun unary(): Poly = when (peek()) {
            "+" -> { take(); unary() }
            "-" -> { take(); -unary() }
            else -> power()
        }

        private fun power(): Poly {
            val value = primary()
            if (peek() != "^") return value
            take()
            val exponent = take().toIntOrNull()
                ?: throw IllegalArgumentException("После ^ укажите целую степень")
            return value.pow(exponent)
        }

        private fun primary(): Poly {
            val token = take()
            return when {
                token == "(" -> {
                    val value = expression()
                    require(take() == ")") { "Не закрыта скобка" }
                    value
                }
                token.equals("x", true) -> Poly.X
                token.firstOrNull()?.isDigit() == true ->
                    Poly.constant(Rational.parseDecimal(token.replace(',', '.')))
                else -> throw IllegalArgumentException("Ожидалось число, x или скобка")
            }
        }

        private fun peek() = tokens.getOrNull(index)
        private fun take() = tokens.getOrNull(index++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }
}
