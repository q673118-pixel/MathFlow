package com.mathflow.app

import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Immutable parsed expression tree. Parse once, then evaluate repeatedly with
 * different variable values; safe to reuse across graph samples.
 */
class MathExpression private constructor(private val root: Node) {
    fun evaluate(variables: Map<String, Double> = emptyMap()): Double {
        val normalized = if (variables.keys.all { it == it.lowercase() }) variables
            else variables.mapKeys { it.key.lowercase() }
        return root.evaluate(normalized)
    }

    private sealed interface Node {
        fun evaluate(variables: Map<String, Double>): Double
    }

    private data class Constant(val value: Double) : Node {
        override fun evaluate(variables: Map<String, Double>) = value
    }

    private data class Variable(val name: String) : Node {
        override fun evaluate(variables: Map<String, Double>): Double =
            variables[name] ?: throw IllegalArgumentException("Не задана переменная: " + name)
    }

    private data class Unary(val negative: Boolean, val value: Node) : Node {
        override fun evaluate(variables: Map<String, Double>): Double {
            val result = value.evaluate(variables)
            return if (negative) -result else result
        }
    }

    private data class Binary(val operator: String, val left: Node, val right: Node) : Node {
        override fun evaluate(variables: Map<String, Double>): Double {
            val a = left.evaluate(variables)
            val b = right.evaluate(variables)
            return when (operator) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> {
                    require(abs(b) > 0.0) { "Деление на ноль" }
                    a / b
                }
                "^" -> a.powChecked(b)
                else -> error("Неизвестная операция")
            }
        }
    }

    private data class Function(val name: String, val arguments: List<Node>) : Node {
        override fun evaluate(variables: Map<String, Double>): Double {
            val values = arguments.map { it.evaluate(variables) }
            val x = values.first()
            return when (name) {
                "abs" -> abs(x)
                "sqrt" -> sqrt(x.also { require(it >= 0.0) { "Корень из отрицательного числа" } })
                "sin" -> sin(x)
                "cos" -> cos(x)
                "tan" -> tan(x).also { require(abs(cos(x)) > 1.0e-12) { "Тангенс не определён для этого угла" } }
                "cot" -> (1.0 / tan(x)).also { require(abs(sin(x)) > 1.0e-12) { "Котангенс не определён для этого угла" } }
                "asin" -> asin(x.also { require(it in -1.0..1.0) { "Арксинус определён только при |x| ≤ 1" } })
                "acos" -> acos(x.also { require(it in -1.0..1.0) { "Арккосинус определён только при |x| ≤ 1" } })
                "atan" -> atan(x)
                "ln" -> ln(x.also { require(it > 0.0) { "Аргумент ln должен быть положительным" } })
                "log" -> log10(x.also { require(it > 0.0) { "Аргумент log должен быть положительным" } })
                "exp" -> exp(x)
                "min" -> minOf(x, values[1])
                "max" -> maxOf(x, values[1])
                else -> throw IllegalArgumentException("Неизвестная функция: " + name)
            }
        }
    }

    private class Parser(source: String) {
        private val normalized = source
            .replace("−", "-").replace("×", "*").replace("÷", "/")
            .replace("π", "pi").replace("√", "sqrt")
            .replace("²", "^2").replace("³", "^3").replace("⁴", "^4")
            .filterNot(Char::isWhitespace)
        private val tokens = tokenize(normalized)
        private var index = 0

        fun parse(): Node {
            require(tokens.isNotEmpty()) { "Введите выражение" }
            val result = expression()
            require(peek() == null) { "Лишний фрагмент выражения: " + peek() }
            return result
        }

        private fun expression(): Node {
            var result = term()
            while (peek() == "+" || peek() == "-") {
                val operator = take()
                result = Binary(operator, result, term())
            }
            return result
        }

        private fun term(): Node {
            var result = unary()
            while (peek() == "*" || peek() == "/" || startsFactor(peek())) {
                val operator = if (peek() == "*" || peek() == "/") take() else "*"
                result = Binary(operator, result, unary())
            }
            return result
        }

        private fun unary(): Node = when (peek()) {
            "+" -> { take(); unary() }
            "-" -> { take(); Unary(true, unary()) }
            else -> power()
        }

        private fun power(): Node {
            val base = primary()
            if (peek() != "^") return base
            take()
            return Binary("^", base, unary())
        }

        private fun primary(): Node {
            val token = take()
            if (token == "(") {
                val value = expression()
                require(take() == ")") { "Не закрыта скобка" }
                return value
            }
            if (token == "|") {
                val value = expression()
                require(take() == "|") { "Не закрыт модуль" }
                return Function("abs", listOf(value))
            }
            token.toDoubleOrNull()?.let { return Constant(it) }
            val name = token.lowercase()
            if (name == "pi") return Constant(PI)
            if (name == "e") return Constant(E)
            if (name.firstOrNull()?.isLetter() == true) {
                if (peek() != "(") return Variable(name)
                take()
                val args = mutableListOf<Node>()
                if (peek() != ")") {
                    args += expression()
                    while (peek() == ",") {
                        take()
                        args += expression()
                    }
                }
                require(take() == ")") { "Не закрыта скобка функции" }
                val expected = if (name == "min" || name == "max") 2 else 1
                require(args.size == expected) { "Функция " + name + " принимает " + expected + " арг." }
                return Function(name, args)
            }
            throw IllegalArgumentException("Неожиданный символ: " + token)
        }

        private fun startsFactor(token: String?): Boolean =
            token == "(" ||
                token?.firstOrNull()?.isDigit() == true ||
                token?.firstOrNull()?.isLetter() == true

        private fun peek(): String? = tokens.getOrNull(index)
        private fun take(): String = tokens.getOrNull(index++)
            ?: throw IllegalArgumentException("Неожиданный конец выражения")

        private fun tokenize(input: String): List<String> {
            val result = ArrayList<String>()
            var i = 0
            while (i < input.length) {
                val c = input[i]
                when {
                    c.isDigit() || c == '.' || c == ',' -> {
                        val start = i
                        var decimalCount = 0
                        while (i < input.length && (input[i].isDigit() || input[i] == '.' || input[i] == ',')) {
                            if (input[i] == '.' || input[i] == ',') decimalCount++
                            require(decimalCount <= 1) { "Некорректное число" }
                            i++
                        }
                        result += input.substring(start, i).replace(',', '.')
                    }
                    c.isLetter() -> {
                        val start = i
                        while (i < input.length && input[i].isLetter()) i++
                        result += input.substring(start, i)
                    }
                    c in "+-*/^(),|" -> { result += c.toString(); i++ }
                    else -> throw IllegalArgumentException("Неизвестный символ: " + c)
                }
            }
            return result
        }
    }

    companion object {
        fun parse(source: String): MathExpression {
            require(source.isNotBlank()) { "Введите выражение" }
            return MathExpression(Parser(source).parse())
        }

        private fun Double.powChecked(exponent: Double): Double {
            if (this < 0.0 && exponent % 1.0 != 0.0) return Double.NaN
            if (this == 0.0 && exponent < 0.0) return Double.POSITIVE_INFINITY
            return this.pow(exponent)
        }
    }
}
