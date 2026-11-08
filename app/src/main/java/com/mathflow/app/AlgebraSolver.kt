package com.mathflow.app

import java.math.BigInteger
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.exp
import kotlin.math.asin
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.PI
import kotlin.math.pow

data class EquationSolution(
    val kind: String,
    val answer: String,
    val steps: List<String>
)

/**
 * Solves one-variable polynomial and rational equations whose reduced
 * numerator has degree at most four. Denominator factors are retained so
 * roots outside the original domain are never reported as valid solutions.
 */
class AlgebraSolver {
    private data class Poly private constructor(val coefficients: List<Rational>) {
        val degree: Int get() = coefficients.indexOfLast { it != Rational.ZERO }.coerceAtLeast(0)
        val isZero: Boolean get() = coefficients.all { it == Rational.ZERO }
        operator fun get(index: Int): Rational = coefficients.getOrElse(index) { Rational.ZERO }

        operator fun plus(other: Poly) =
            of((0..maxOf(degree, other.degree)).map { this[it] + other[it] })

        operator fun minus(other: Poly) =
            of((0..maxOf(degree, other.degree)).map { this[it] - other[it] })

        operator fun unaryMinus() = of(coefficients.map { -it })

        operator fun times(other: Poly): Poly {
            if (isZero || other.isZero) return ZERO
            require(degree + other.degree <= 4) {
                "После преобразования степень уравнения выше четвёртой"
            }
            val result = MutableList(degree + other.degree + 1) { Rational.ZERO }
            for (i in 0..degree) for (j in 0..other.degree) {
                if (this[i] != Rational.ZERO && other[j] != Rational.ZERO) {
                    result[i + j] = result[i + j] + this[i] * other[j]
                }
            }
            return of(result)
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
                while (trimmed.size > 1 && trimmed.last() == Rational.ZERO) {
                    trimmed.removeAt(trimmed.lastIndex)
                }
                return Poly(trimmed.ifEmpty { listOf(Rational.ZERO) })
            }
        }
    }

    private data class RationalExpression(
        val numerator: Poly,
        val denominatorFactors: List<Poly> = emptyList(),
        val domainFactors: List<Poly> = denominatorFactors.filter { it.degree > 0 }
    ) {
        fun denominatorProduct(): Poly =
            denominatorFactors.fold(Poly.ONE) { product, factor -> product * factor }

        operator fun plus(other: RationalExpression) = RationalExpression(
            numerator * other.denominatorProduct() + other.numerator * denominatorProduct(),
            denominatorFactors + other.denominatorFactors,
            domainFactors + other.domainFactors
        )

        operator fun minus(other: RationalExpression) = RationalExpression(
            numerator * other.denominatorProduct() - other.numerator * denominatorProduct(),
            denominatorFactors + other.denominatorFactors,
            domainFactors + other.domainFactors
        )

        operator fun times(other: RationalExpression) = RationalExpression(
            numerator * other.numerator,
            denominatorFactors + other.denominatorFactors,
            domainFactors + other.domainFactors
        )

        operator fun div(other: RationalExpression): RationalExpression {
            require(!other.numerator.isZero) { "Деление на ноль" }
            val newDenominator = denominatorFactors + listOf(other.numerator)
            val newDomain = domainFactors + other.domainFactors +
                if (other.numerator.degree > 0) listOf(other.numerator) else emptyList()
            return RationalExpression(
                numerator * other.denominatorProduct(),
                newDenominator,
                newDomain
            )
        }

        fun pow(exponent: Int): RationalExpression {
            require(exponent in -4..4) { "Поддерживаются степени от −4 до 4" }
            if (exponent == 0) return RationalExpression(Poly.ONE, domainFactors = domainFactors)
            if (exponent > 0) {
                val poweredFactors = if (exponent == 1) denominatorFactors
                    else denominatorFactors.flatMap { factor -> List(exponent) { factor } }
                return RationalExpression(numerator.pow(exponent), poweredFactors, domainFactors)
            }
            require(!numerator.isZero) { "Нельзя возвести ноль в отрицательную степень" }
            val power = -exponent
            val newDomain = domainFactors +
                if (numerator.degree > 0) listOf(numerator) else emptyList()
            return RationalExpression(
                denominatorProduct().pow(power),
                List(power) { numerator },
                newDomain
            )
        }

        companion object {
            fun polynomial(value: Poly) = RationalExpression(value)
            val ONE = RationalExpression(Poly.ONE)
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

        val parsed = try {
            val parsedLeft = Parser(leftText).parse()
            val parsedRight = Parser(rightText).parse()
            val polynomialDifference = parsedLeft.numerator * parsedRight.denominatorProduct() -
                parsedRight.numerator * parsedLeft.denominatorProduct()
            Triple(parsedLeft, parsedRight, polynomialDifference)
        } catch (_: IllegalArgumentException) {
            return solveGeneral(equation)
        }
        val left = parsed.first
        val right = parsed.second
        val reduced = parsed.third
        val excludedFactors = left.domainFactors + right.domainFactors
        val excludedRoots = excludedFactors.flatMap { factor ->
            if (factor.degree > 0) {
                findRealRoots((0..factor.degree).map {
                    factor[it].toBigDecimal().toDouble()
                }.toDoubleArray())
            } else emptyList()
        }.distinctBy { fmt(it) }.sorted()

        fun isAllowed(value: Double): Boolean =
            value.isFinite() && excludedRoots.none {
                abs(it - value) <= 1.0e-7 * max(1.0, abs(value))
            }

        fun excludedDescription(): String =
            if (excludedRoots.isEmpty()) "" else excludedRoots.joinToString(", ") { fmt(it) }

        val steps = mutableListOf<String>()
        steps += "Переносим всё в левую часть: ${format(reduced)} = 0"
        if (excludedRoots.isNotEmpty()) {
            steps += "ОДЗ: знаменатели не равны нулю; исключаем x = ${excludedDescription()}"
        }

        if (reduced.isZero) {
            steps += "Получили верное равенство 0 = 0"
            val answer = if (excludedRoots.isEmpty()) {
                "x — любое число"
            } else {
                "x — любое число, кроме ${excludedDescription()}"
            }
            return EquationSolution("identity", answer, steps)
        }

        val degree = reduced.degree
        if (degree == 0) {
            steps += "Получили противоречие ${reduced[0].toDisplayString()} = 0"
            return EquationSolution("no_solution", "Решений нет", steps)
        }

        val coefficients = (0..degree).map {
            reduced[it].toBigDecimal().toDouble()
        }.toDoubleArray()
        require(coefficients.all { it.isFinite() }) {
            "Коэффициенты слишком велики для численного решения"
        }

        if (degree == 1) {
            val x = -reduced[0] / reduced[1]
            val numeric = x.toBigDecimal().toDouble()
            steps += "Оставляем член с x: ${reduced[1].toDisplayString()}x = ${(-reduced[0]).toDisplayString()}"
            steps += "Делим обе части на ${reduced[1].toDisplayString()}"
            if (!isAllowed(numeric)) {
                steps += "Полученное значение не входит в ОДЗ"
                return EquationSolution("no_solution", "Решений нет", steps)
            }
            steps += "x = ${x.toDisplayString()}"
            return EquationSolution("linear", "x = ${x.toDisplayString()} (${x.toDecimalString()})", steps)
        }

        if (degree == 2) {
            val a = reduced[2]
            val b = reduced[1]
            val c = reduced[0]
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
                val roots = listOf(x1, x2).distinct().filter { isAllowed(it.toBigDecimal().toDouble()) }
                if (roots.isEmpty()) {
                    steps += "Все найденные корни исключены условиями ОДЗ"
                    return EquationSolution("no_solution", "Решений нет", steps)
                }
                if (x1 == x2) {
                    steps += "x = ${x1.toDisplayString()}"
                    return EquationSolution("quadratic_double", "x = ${x1.toDisplayString()}", steps)
                }
                if (roots.size == 1) {
                    steps += "После проверки ОДЗ остаётся один корень: ${roots[0].toDisplayString()}"
                    return EquationSolution("quadratic", "x = ${roots[0].toDisplayString()}", steps)
                }
                steps += "x₁ = (−b + √D)/(2a) = ${x1.toDisplayString()}"
                steps += "x₂ = (−b − √D)/(2a) = ${x2.toDisplayString()}"
                return EquationSolution(
                    "quadratic",
                    "x₁ = ${roots[0].toDisplayString()}, x₂ = ${roots[1].toDisplayString()}",
                    steps
                )
            }

            val root = sqrt(d.toBigDecimal().toDouble())
            val aDouble = a.toBigDecimal().toDouble()
            val bDouble = b.toBigDecimal().toDouble()
            val rawRoots = listOf(
                (-bDouble + root) / (2.0 * aDouble),
                (-bDouble - root) / (2.0 * aDouble)
            )
            val roots = rawRoots.filter { isAllowed(it) }.distinctBy { fmt(it) }
            steps += "√D иррационален; корни вычислены приближённо"
            if (roots.isEmpty()) {
                steps += "Все найденные корни исключены условиями ОДЗ"
                return EquationSolution("no_solution", "Решений нет", steps)
            }
            steps += roots.mapIndexed { index, value -> "x${subscript(index + 1)} ≈ ${fmt(value)}" }
            return EquationSolution(
                "quadratic_approx",
                roots.mapIndexed { index, value -> "x${subscript(index + 1)} ≈ ${fmt(value)}" }.joinToString(", "),
                steps
            )
        }

        steps += "Это многочлен степени $degree. Ищем все действительные корни численно."
        steps += "Коэффициенты сохранены в точной рациональной форме до этапа численного поиска."
        val roots = findRealRoots(coefficients).filter { isAllowed(it) }
        if (roots.isEmpty()) {
            steps += "Действительных корней в допустимой области нет"
            return EquationSolution("no_real_roots", "Действительных корней нет", steps)
        }
        val labels = roots.mapIndexed { index, root -> "x${subscript(index + 1)} ≈ ${fmt(root)}" }
        steps += "Проверка корней выполнена подстановкой в исходный многочлен с численной точностью."
        return EquationSolution("polynomial_numeric", labels.joinToString(", "), steps)
    }

    /**
     * Fallback for equations containing functions or powers above degree four.
     * Numerical roots are searched on a documented finite interval; poles and
     * undefined function values are rejected by checking the residual at each root.
     */
    private fun solveGeneral(equation: String): EquationSolution {
        val equal = equation.indexOf('=')
        require(equal >= 0 && equation.indexOf('=', equal + 1) < 0) {
            "Введите уравнение с одним знаком ="
        }
        val leftText = equation.substring(0, equal).trim()
        val rightText = equation.substring(equal + 1).trim()
        require(leftText.isNotEmpty() && rightText.isNotEmpty()) {
            "Обе части уравнения должны быть заполнены"
        }
        val left = NumericParser(leftText).parse()
        val right = NumericParser(rightText).parse()
        fun valueAt(x: Double): Double = try {
            (left(x) - right(x)).takeIf { it.isFinite() } ?: Double.NaN
        } catch (_: Exception) {
            Double.NaN
        }

        val lower = -100.0
        val upper = 100.0
        val sampleCount = 20_000
        val step = (upper - lower) / sampleCount
        val values = DoubleArray(sampleCount + 1) { index ->
            valueAt(lower + index * step)
        }
        val validValues = values.filter { it.isFinite() }
        require(validValues.isNotEmpty()) {
            "На выбранном интервале нет допустимых действительных значений"
        }
        if (validValues.all { abs(it) <= 1.0e-11 }) {
            return EquationSolution(
                "general_identity",
                "Равенство верно на всей области определения",
                listOf(
                    "Использовано численное исследование выражения",
                    "На проверенном интервале [-100; 100] равенство выполняется для всех допустимых значений"
                )
            )
        }

        val roots = mutableListOf<Double>()
        fun addRoot(candidate: Double) {
            if (!candidate.isFinite() || abs(valueAt(candidate)) > 1.0e-7) return
            if (roots.none { abs(it - candidate) <= 1.0e-4 * max(1.0, abs(candidate)) }) {
                roots += candidate
            }
        }

        for (index in 0 until sampleCount) {
            val x1 = lower + index * step
            val x2 = x1 + step
            val y1 = values[index]
            val y2 = values[index + 1]
            if (!y1.isFinite() || !y2.isFinite()) continue
            if (abs(y1) <= 1.0e-10) addRoot(x1)
            if ((y1 < 0.0) != (y2 < 0.0)) {
                var low = x1
                var high = x2
                var lowValue = y1
                repeat(90) {
                    val middle = low + (high - low) / 2.0
                    val middleValue = valueAt(middle)
                    if (!middleValue.isFinite()) {
                        high = middle
                    } else if (abs(middleValue) <= 1.0e-14) {
                        low = middle
                        high = middle
                    } else if ((middleValue < 0.0) == (lowValue < 0.0)) {
                        low = middle
                        lowValue = middleValue
                    } else {
                        high = middle
                    }
                }
                addRoot(low + (high - low) / 2.0)
            }
        }
        if (values.last().isFinite() && abs(values.last()) <= 1.0e-10) addRoot(upper)

        // Find even-multiplicity roots, which do not create a sign change.
        for (index in 1 until sampleCount) {
            val before = values[index - 1]
            val current = values[index]
            val after = values[index + 1]
            if (!before.isFinite() || !current.isFinite() || !after.isFinite()) continue
            if (abs(current) > 0.05 || abs(current) > abs(before) || abs(current) > abs(after)) continue
            var low = lower + (index - 1) * step
            var high = lower + (index + 1) * step
            repeat(55) {
                val leftProbe = low + (high - low) / 3.0
                val rightProbe = high - (high - low) / 3.0
                val leftValue = valueAt(leftProbe)
                val rightValue = valueAt(rightProbe)
                if (!leftValue.isFinite() || (rightValue.isFinite() && abs(leftValue) > abs(rightValue))) {
                    low = leftProbe
                } else {
                    high = rightProbe
                }
            }
            addRoot(low + (high - low) / 2.0)
        }

        val ordered = roots.sorted()
        if (ordered.isEmpty()) {
            return EquationSolution(
                "general_no_roots_in_range",
                "На интервале [-100; 100] действительные корни не найдены",
                listOf(
                    "Уравнение содержит функции или степени, для которых применён численный метод",
                    "Проверена область [-100; 100]; это не доказывает отсутствие корней за её пределами"
                )
            )
        }
        val shown = ordered.take(20)
        val answer = shown.mapIndexed { index, root ->
            "x${subscript(index + 1)} ≈ ${fmt(root)}"
        }.joinToString(", ")
        val steps = mutableListOf(
            "Использован численный метод с проверкой допустимых значений",
            "Область поиска: [-100; 100]; тригонометрические функции вычисляются в радианах",
            "Каждый найденный корень проверен подстановкой в исходное уравнение"
        )
        if (ordered.size > shown.size) {
            steps += "Показаны первые 20 корней из ${ordered.size} найденных на этом интервале"
        }
        return EquationSolution("general_numeric", answer, steps)
    }

    private class NumericParser(input: String) {
        private val normalizedInput = input
            .replace("−", "-").replace("×", "*").replace("÷", "/")
            .replace("π", "pi").replace("√", "sqrt")
            .replace("⁴", "^4").replace("³", "^3").replace("²", "^2")
            .filterNot { it.isWhitespace() }
        private val tokens = Regex("""\d+(?:[.,]\d+)?|[A-Za-z]+|[()+\-*/^,|]""")
            .findAll(normalizedInput).map { it.value }.toList()
        private var index = 0
        private var absoluteDepth = 0

        init {
            require(tokens.isNotEmpty() && tokens.joinToString("") == normalizedInput) {
                "Недопустимый символ в уравнении"
            }
        }

        fun parse(): (Double) -> Double {
            val result = expression()
            require(index == tokens.size) { "Проверьте запись выражения" }
            return result
        }

        private fun expression(): (Double) -> Double {
            var value = term()
            while (peek() == "+" || peek() == "-") {
                val operator = take()
                val left = value
                val right = term()
                value = if (operator == "+") {
                    { x -> left(x) + right(x) }
                } else {
                    { x -> left(x) - right(x) }
                }
            }
            return value
        }

        private fun term(): (Double) -> Double {
            var value = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val operator = if (peek() == "*" || peek() == "/") take() else "*"
                val left = value
                val right = unary()
                value = if (operator == "*") {
                    { x -> left(x) * right(x) }
                } else {
                    { x -> left(x) / right(x) }
                }
            }
            return value
        }

        private fun startsPrimary(token: String?): Boolean =
            token == "(" || (token == "|" && absoluteDepth == 0) ||
                token?.firstOrNull()?.isDigit() == true ||
                token?.firstOrNull()?.isLetter() == true

        private fun unary(): (Double) -> Double = when (peek()) {
            "+" -> { take(); unary() }
            "-" -> {
                take()
                val value = unary()
                val negated: (Double) -> Double = { x -> -value(x) }
                negated
            }
            else -> power()
        }

        private fun power(): (Double) -> Double {
            val base = primary()
            if (peek() != "^") return base
            take()
            val exponent = unary()
            return { x -> base(x).pow(exponent(x)) }
        }

        private fun primary(): (Double) -> Double {
            val token = take()
            return when {
                token == "(" -> {
                    val value = expression()
                    require(take() == ")") { "Не закрыта скобка" }
                    value
                }
                token == "|" -> {
                    absoluteDepth++
                    val value = expression()
                    require(take() == "|") { "Не закрыт модуль" }
                    absoluteDepth--
                    val absolute: (Double) -> Double = { x -> abs(value(x)) }
                    absolute
                }
                token.equals("x", true) -> { x -> x }
                token.equals("pi", true) -> { _ -> PI }
                token.equals("e", true) -> { _ -> kotlin.math.E }
                token.firstOrNull()?.isDigit() == true -> {
                    val number = token.replace(',', '.').toDouble()
                    val constant: (Double) -> Double = { _ -> number }
                    constant
                }
                token.firstOrNull()?.isLetter() == true -> parseFunction(token)
                else -> throw IllegalArgumentException("Ожидалось число, x, функция или скобка")
            }
        }

        private fun parseFunction(name: String): (Double) -> Double {
            require(peek() == "(") { "После имени функции поставьте скобку" }
            take()
            val first = expression()
            val second = if (peek() == ",") {
                take()
                expression()
            } else null
            require(take() == ")") { "Не закрыта скобка функции" }
            return when (name.lowercase(Locale.ROOT)) {
                "abs" -> { x -> abs(first(x)) }
                "sqrt" -> { x -> first(x).takeIf { it >= 0.0 }?.let(::sqrt) ?: Double.NaN }
                "sin" -> { x -> sin(first(x)) }
                "cos" -> { x -> cos(first(x)) }
                "tan" -> { x -> tan(first(x)) }
                "asin" -> { x -> asin(first(x)) }
                "acos" -> { x -> acos(first(x)) }
                "atan" -> { x -> atan(first(x)) }
                "ln" -> { x -> first(x).takeIf { it > 0.0 }?.let(::ln) ?: Double.NaN }
                "log" -> { x -> first(x).takeIf { it > 0.0 }?.let(::log10) ?: Double.NaN }
                "exp" -> { x -> exp(first(x)) }
                "min" -> {
                    val right = requireNotNull(second) { "Функции min нужны два аргумента" }
                    val minimum: (Double) -> Double = { x -> minOf(first(x), right(x)) }
                    minimum
                }
                "max" -> {
                    val right = requireNotNull(second) { "Функции max нужны два аргумента" }
                    val maximum: (Double) -> Double = { x -> maxOf(first(x), right(x)) }
                    maximum
                }
                else -> throw IllegalArgumentException("Неизвестная функция: $name")
            }
        }

        private fun peek() = tokens.getOrNull(index)
        private fun take() = tokens.getOrNull(index++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }

    private fun findRealRoots(input: DoubleArray): List<Double> {
        if (input.isEmpty() || input.any { !it.isFinite() }) return emptyList()
        val coefficientScale = input.maxOf { abs(it) }
        if (coefficientScale == 0.0 || !coefficientScale.isFinite()) return emptyList()
        val normalized = DoubleArray(input.size) { input[it] / coefficientScale }
        var degree = normalized.lastIndex
        while (degree > 0 && normalized[degree] == 0.0) degree--
        if (degree == 0) return emptyList()
        if (degree == 1) {
            val root = -normalized[0] / normalized[1]
            return if (root.isFinite()) listOf(root) else emptyList()
        }

        val derivative = DoubleArray(degree) { index -> normalized[index + 1] * (index + 1) }
        val criticalPoints = findRealRoots(derivative).filter { it.isFinite() }.sorted()
        val leading = abs(normalized[degree])
        if (leading == 0.0 || !leading.isFinite()) return emptyList()
        var ratio = 0.0
        for (i in 0 until degree) ratio = max(ratio, abs(normalized[i]) / leading)
        val bound = (1.0 + ratio).coerceAtMost(1.0e12)
        val points = (listOf(-bound) + criticalPoints.filter { it > -bound && it < bound } + listOf(bound))
            .distinct().sorted()
        val roots = mutableListOf<Double>()

        fun valueAt(x: Double): Double {
            var value = normalized[degree]
            for (i in degree - 1 downTo 0) value = value * x + normalized[i]
            return value
        }

        fun toleranceAt(x: Double): Double {
            val magnitude = max(1.0, abs(x))
            var scale = 0.0
            var power = 1.0
            for (i in 0..degree) {
                scale += abs(normalized[i]) * power
                power *= magnitude
            }
            return 1.0e-10 * max(1.0, scale)
        }

        for (point in criticalPoints) {
            if (point in -bound..bound && abs(valueAt(point)) <= toleranceAt(point)) {
                roots += point
            }
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
            if (acc.isEmpty() || abs(acc.last() - root) > 1.0e-7 * max(1.0, abs(root))) {
                acc += root
            }
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
        if (value.isFinite()) String.format(Locale.US, "%.10f", value).trimEnd('0').trimEnd('.')
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
            .replace("⁴", "^4").replace("³", "^3").replace("²", "^2")
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

        fun parse(): RationalExpression {
            val result = expression()
            require(index == tokens.size) { "Проверьте запись выражения" }
            return result
        }

        private fun expression(): RationalExpression {
            var value = term()
            while (peek() == "+" || peek() == "-") {
                val operator = take()
                val right = term()
                value = if (operator == "+") value + right else value - right
            }
            return value
        }

        private fun term(): RationalExpression {
            var value = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val operator = if (peek() == "*" || peek() == "/") take() else "*"
                val right = unary()
                value = if (operator == "*") value * right else value / right
            }
            return value
        }

        private fun startsPrimary(token: String?): Boolean =
            token == "(" || token?.equals("x", true) == true || token?.firstOrNull()?.isDigit() == true

        private fun unary(): RationalExpression = when (peek()) {
            "+" -> { take(); unary() }
            "-" -> {
                take()
                val value = unary()
                RationalExpression(-value.numerator, value.denominatorFactors, value.domainFactors)
            }
            else -> power()
        }

        private fun power(): RationalExpression {
            val value = primary()
            if (peek() != "^") return value
            take()
            val sign = if (peek() == "-") { take(); -1 } else 1
            val exponent = take().toIntOrNull()
                ?: throw IllegalArgumentException("После ^ укажите целую степень")
            return value.pow(sign * exponent)
        }

        private fun primary(): RationalExpression {
            val token = take()
            val poly = when {
                token == "(" -> {
                    val value = expression()
                    require(take() == ")") { "Не закрыта скобка" }
                    return value
                }
                token.equals("x", true) -> Poly.X
                token.firstOrNull()?.isDigit() == true ->
                    Poly.constant(Rational.parseDecimal(token.replace(',', '.')))
                else -> throw IllegalArgumentException("Ожидалось число, x или скобка")
            }
            return RationalExpression.polynomial(poly)
        }

        private fun peek() = tokens.getOrNull(index)
        private fun take() = tokens.getOrNull(index++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }
}
