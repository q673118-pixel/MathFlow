package com.mathflow.app

data class SimplificationResult(val expression: String, val steps: List<String>)

/** Exact simplification of one-variable polynomial expressions up to degree four. */
class PolynomialSimplifier {
    private data class Poly private constructor(val c: List<Rational>) {
        val degree get() = c.indexOfLast { it != Rational.ZERO }.coerceAtLeast(0)
        operator fun get(i: Int) = c.getOrElse(i) { Rational.ZERO }
        operator fun plus(o: Poly) = of((0..maxOf(degree, o.degree)).map { this[it] + o[it] })
        operator fun minus(o: Poly) = of((0..maxOf(degree, o.degree)).map { this[it] - o[it] })
        operator fun unaryMinus() = of(c.map { -it })
        operator fun times(o: Poly): Poly {
            require(degree + o.degree <= 4) { "Поддерживается степень не выше четвёртой" }
            val out = MutableList(degree + o.degree + 1) { Rational.ZERO }
            for (i in 0..degree) for (j in 0..o.degree) out[i + j] = out[i + j] + this[i] * o[j]
            return of(out)
        }
        fun pow(n: Int): Poly {
            require(n in 0..4) { "Поддерживаются степени от 0 до 4" }
            var result = ONE
            repeat(n) { result = result * this }
            return result
        }
        companion object {
            val ONE = of(listOf(Rational.ONE))
            val X = of(listOf(Rational.ZERO, Rational.ONE))
            fun of(values: List<Rational>): Poly {
                val v = values.toMutableList()
                while (v.size > 1 && v.last() == Rational.ZERO) v.removeAt(v.lastIndex)
                return Poly(v.ifEmpty { listOf(Rational.ZERO) })
            }
            fun constant(v: Rational) = of(listOf(v))
        }
    }
    fun simplify(source: String): SimplificationResult {
        val parsed = Parser(source.replace("−", "-").replace("×", "*").replace("÷", "/")).parse()
        val result = format(parsed)
        return SimplificationResult(result, listOf("Раскрываем скобки и приводим подобные слагаемые", "Собираем коэффициенты при одинаковых степенях x", "Итог: $result"))
    }
    private fun format(poly: Poly): String {
        val terms = mutableListOf<String>()
        for (power in poly.degree downTo 0) {
            val value = poly[power]
            if (value == Rational.ZERO) continue
            val negative = value < Rational.ZERO
            val abs = if (negative) -value else value
            val variable = when (power) { 0 -> ""; 1 -> "x"; else -> "x^$power" }
            val magnitude = when { power == 0 -> abs.toDisplayString(); abs == Rational.ONE -> variable; else -> abs.toDisplayString() + variable }
            terms += if (terms.isEmpty()) (if (negative) "-" else "") + magnitude else (if (negative) " - " else " + ") + magnitude
        }
        return terms.joinToString("").ifEmpty { "0" }
    }
    private class Parser(source: String) {
        private val input = source.filterNot(Char::isWhitespace)
        private val tokens = Regex("""\d+(?:[.,]\d+)?|[xX]|[()+\-*/^]""").findAll(input).map { it.value }.toList()
        private var index = 0
        init { require(input.isNotEmpty() && tokens.joinToString("") == input) { "Недопустимый символ в выражении" } }
        fun parse(): Poly { val result = expression(); require(index == tokens.size) { "Проверьте запись выражения" }; return result }
        private fun expression(): Poly {
            var v = term()
            while (peek() == "+" || peek() == "-") { val op = take(); val r = term(); v = if (op == "+") v + r else v - r }
            return v
        }
        private fun term(): Poly {
            var v = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val op = if (peek() == "*" || peek() == "/") take() else "*"
                val r = unary()
                if (op == "/") { require(r.degree == 0 && r[0] != Rational.ZERO) { "Делить можно только на ненулевую константу" }; v = Poly.of((0..v.degree).map { v[it] / r[0] }) }
                else v = v * r
            }
            return v
        }
        private fun startsPrimary(t: String?) = t == "(" || t?.equals("x", true) == true || t?.firstOrNull()?.isDigit() == true
        private fun unary(): Poly = when (peek()) { "+" -> { take(); unary() }; "-" -> { take(); -unary() }; else -> power() }
        private fun power(): Poly {
            val base = primary()
            if (peek() != "^") return base
            take()
            val n = take().toIntOrNull() ?: throw IllegalArgumentException("После ^ укажите целую степень")
            return base.pow(n)
        }
        private fun primary(): Poly {
            val t = take()
            if (t == "(") { val v = expression(); require(take() == ")") { "Не закрыта скобка" }; return v }
            if (t.equals("x", true)) return Poly.X
            return Poly.constant(Rational.parseDecimal(t.replace(',', '.')))
        }
        private fun peek() = tokens.getOrNull(index)
        private fun take() = tokens.getOrNull(index++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }
}
