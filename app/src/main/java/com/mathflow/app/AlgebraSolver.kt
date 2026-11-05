package com.mathflow.app

import java.math.BigInteger
import kotlin.math.sqrt

data class EquationSolution(
    val kind: String,
    val answer: String,
    val steps: List<String>
)

/**
 * Solves one-variable equations whose sides can be reduced to polynomials
 * of degree at most two. Coefficients are exact rational numbers.
 *
 * Supported: x, constants, parentheses, +, -, *, division by a non-zero
 * constant, and integer powers 0..2. Unsupported nonlinear expressions
 * fail explicitly rather than returning a guessed answer.
 */
class AlgebraSolver {
    private data class Poly(val c0: Rational, val c1: Rational = Rational.ZERO,
                            val c2: Rational = Rational.ZERO) {
        operator fun plus(b: Poly) = Poly(c0 + b.c0, c1 + b.c1, c2 + b.c2)
        operator fun minus(b: Poly) = Poly(c0 - b.c0, c1 - b.c1, c2 - b.c2)
        operator fun unaryMinus() = Poly(-c0, -c1, -c2)

        operator fun times(b: Poly): Poly {
            val a0 = c0 * b.c0
            val a1 = c0 * b.c1 + c1 * b.c0
            val a2 = c0 * b.c2 + c1 * b.c1 + c2 * b.c0
            val a3 = c1 * b.c2 + c2 * b.c1
            val a4 = c2 * b.c2
            require(a3 == Rational.ZERO && a4 == Rational.ZERO) {
                "Уравнение выше второй степени пока не поддерживается"
            }
            return Poly(a0, a1, a2)
        }

        fun dividedBy(value: Rational): Poly {
            require(value != Rational.ZERO) { "Деление на ноль" }
            return Poly(c0 / value, c1 / value, c2 / value)
        }

        fun pow(n: Int): Poly {
            require(n in 0..2) { "Поддерживаются степени x только от 0 до 2" }
            return when (n) {
                0 -> constant(Rational.ONE)
                1 -> this
                else -> this * this
            }
        }

        companion object {
            fun constant(value: Rational) = Poly(value)
            val X = Poly(Rational.ZERO, Rational.ONE)
        }
    }

    fun solve(equation: String): EquationSolution {
        val equal = equation.indexOf('=')
        require(equal >= 0 && equation.indexOf('=', equal + 1) < 0) {
            "Введите уравнение с одним знаком ="
        }
        val leftText = equation.substring(0, equal).trim()
        val rightText = equation.substring(equal + 1).trim()
        require(leftText.isNotEmpty() && rightText.isNotEmpty()) { "Обе части уравнения должны быть заполнены" }

        val left = Parser(leftText).parse()
        val right = Parser(rightText).parse()
        val reduced = left - right
        val steps = mutableListOf<String>()
        steps += "Переносим всё в левую часть: ${format(reduced)} = 0"

        val a = reduced.c2
        val b = reduced.c1
        val c = reduced.c0

        if (a == Rational.ZERO) {
            if (b == Rational.ZERO) {
                return if (c == Rational.ZERO) {
                    steps += "Получили верное равенство 0 = 0"
                    EquationSolution("identity", "x — любое число", steps)
                } else {
                    steps += "Получили противоречие ${c.toDisplayString()} = 0"
                    EquationSolution("no_solution", "Решений нет", steps)
                }
            }
            val x = -c / b
            steps += "Оставляем член с x: ${b.toDisplayString()}x = ${(-c).toDisplayString()}"
            steps += "Делим обе части на ${b.toDisplayString()}"
            steps += "x = ${x.toDisplayString()}"
            return EquationSolution("linear", "x = ${x.toDisplayString()} (${x.toDecimalString()})", steps)
        }

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

        val dDouble = d.toBigDecimal().toDouble()
        val aDouble = a.toBigDecimal().toDouble()
        val bDouble = b.toBigDecimal().toDouble()
        val root = sqrt(dDouble)
        val x1 = (-bDouble + root) / (2.0 * aDouble)
        val x2 = (-bDouble - root) / (2.0 * aDouble)
        steps += "√D иррационален; корни вычислены приближённо"
        steps += "x₁ ≈ ${fmt(x1)}, x₂ ≈ ${fmt(x2)}"
        return EquationSolution("quadratic_approx", "x₁ ≈ ${fmt(x1)}, x₂ ≈ ${fmt(x2)}", steps)
    }

    private fun exactSqrt(value: Rational): Rational? {
        if (value < Rational.ZERO) return null
        val n = integerSqrt(value.numerator)
        val d = integerSqrt(value.denominator)
        if (n * n != value.numerator || d * d != value.denominator) return null
        return Rational.of(n, d)
    }

    // Newton iteration avoids relying on BigInteger.sqrt(), which is unavailable
    // on some Android API levels supported by this project.
    private fun integerSqrt(value: BigInteger): BigInteger {
        if (value < BigInteger.valueOf(2)) return value
        var x = BigInteger.ONE.shiftLeft((value.bitLength() + 1) / 2)
        while (true) {
            val next = x.add(value.divide(x)).shiftRight(1)
            if (next >= x) return x
            x = next
        }
    }

    private fun fmt(v: Double) = if (v.isFinite()) "%.10f".format(java.util.Locale.US, v).trimEnd('0').trimEnd('.') else "не определено"

    private fun format(p: Poly): String {
        val terms = mutableListOf<String>()
        fun add(coef: Rational, variable: String) {
            if (coef == Rational.ZERO) return
            val negative = coef < Rational.ZERO
            val abs = if (negative) -coef else coef
            val magnitude = when {
                variable.isEmpty() -> abs.toDisplayString()
                abs == Rational.ONE -> variable
                else -> "${abs.toDisplayString()}$variable"
            }
            terms += (if (terms.isEmpty()) { if (negative) "-$magnitude" else magnitude }
                      else { if (negative) " − $magnitude" else " + $magnitude" })
        }
        add(p.c2, "x²")
        add(p.c1, "x")
        add(p.c0, "")
        return terms.joinToString("").ifEmpty { "0" }
    }

    private class Parser(private val input: String) {
        private val normalizedInput = input
            .replace("²", "^2")
            .replace("−", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
        private val tokens = Regex("""\d+(?:[.,]\d+)?|[xX]|[()+\-*/^]""")
            .findAll(normalizedInput)
            .map { it.value }.toList()
        private var i = 0

        init {
            require(tokens.joinToString("") == normalizedInput) {
                "Недопустимый символ в уравнении: $input"
            }
        }

        fun parse(): Poly {
            val result = expression()
            require(i == tokens.size) { "Проверьте запись выражения" }
            return result
        }

        private fun expression(): Poly {
            var v = term()
            while (peek() == "+" || peek() == "-") {
                val op = take()
                val r = term()
                v = if (op == "+") v + r else v - r
            }
            return v
        }

        private fun term(): Poly {
            var v = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val op = if (peek() == "*" || peek() == "/") take() else "*"
                val r = unary()
                v = if (op == "*") v * r else {
                    require(r.c1 == Rational.ZERO && r.c2 == Rational.ZERO) {
                        "Делить можно только на число, не содержащее x"
                    }
                    v.dividedBy(r.c0)
                }
            }
            return v
        }

        private fun startsPrimary(token: String?): Boolean =
            token == "(" || token?.equals("x", true) == true ||
                token?.firstOrNull()?.isDigit() == true

        private fun unary(): Poly = when (peek()) {
            "+" -> { take(); unary() }
            "-" -> { take(); -unary() }
            else -> power()
        }

        private fun power(): Poly {
            var v = primary()
            if (peek() == "^") {
                take()
                val exponent = take().toIntOrNull()
                    ?: throw IllegalArgumentException("После ^ укажите целую степень")
                v = v.pow(exponent)
            }
            return v
        }

        private fun primary(): Poly {
            val token = take()
            return when {
                token == "(" -> {
                    val v = expression()
                    require(take() == ")") { "Не закрыта скобка" }
                    v
                }
                token.equals("x", true) -> Poly.X
                token.firstOrNull()?.isDigit() == true ->
                    Poly.constant(Rational.parseDecimal(token.replace(',', '.')))
                else -> throw IllegalArgumentException("Ожидалось число, x или скобка")
            }
        }

        private fun peek() = tokens.getOrNull(i)
        private fun take() = tokens.getOrNull(i++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }
}
