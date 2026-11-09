package com.mathflow.app

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class AngleMode { DEGREES, RADIANS }

data class CalculationResult(
    val exactValue: Rational?,
    val displayValue: String,
    val decimalValue: String,
    val steps: List<String>
)

/**
 * Recursive-descent expression engine.
 *
 * Exact rational arithmetic is retained for ordinary arithmetic and integer powers.
 * Transcendental functions and irrational roots use a documented decimal approximation.
 */
class ExpressionEngine(
    private val angleMode: AngleMode = AngleMode.DEGREES
) {
    private lateinit var tokens: List<Token>
    private var position = 0
    private val steps = mutableListOf<String>()
    private var variables: Map<String, BigDecimal> = emptyMap()
    private var recordSteps: Boolean = true

    fun calculate(
        source: String,
        variables: Map<String, BigDecimal> = emptyMap(),
        includeSteps: Boolean = true
    ): CalculationResult {
        require(source.isNotBlank()) { "Введите выражение" }
        this.variables = variables
        recordSteps = includeSteps
        steps.clear()
        tokens = tokenize(source)
        position = 0
        val value = parseExpression()
        expect(TokenType.END)
        return when (value) {
            is Value.Exact -> CalculationResult(
                exactValue = value.number,
                displayValue = value.number.toMixedDisplayString(),
                decimalValue = value.number.toDecimalString(),
                steps = steps.toList()
            )
            is Value.Approximate -> CalculationResult(
                exactValue = null,
                displayValue = value.number.stripTrailingZeros().toPlainString(),
                decimalValue = value.number.stripTrailingZeros().toPlainString(),
                steps = steps.toList()
            )
        }
    }

    private sealed class Value {
        data class Exact(val number: Rational) : Value()
        data class Approximate(val number: BigDecimal) : Value()
    }

    private enum class TokenType { NUMBER, IDENTIFIER, PLUS, MINUS, MULTIPLY, DIVIDE, POWER, PERCENT, LEFT, RIGHT, END }
    private data class Token(val type: TokenType, val text: String)

    private fun tokenize(source: String): List<Token> {
        val result = mutableListOf<Token>()
        var i = 0
        while (i < source.length) {
            val c = source[i]
            if (c.isWhitespace()) { i++; continue }
            when {
                c.isDigit() || c == '.' || c == ',' -> {
                    val start = i
                    var dots = 0
                    while (i < source.length && (source[i].isDigit() || source[i] == '.' || source[i] == ',')) {
                        if (source[i] == '.' || source[i] == ',') dots++
                        require(dots <= 1) { "Некорректное число" }
                        i++
                    }
                    result += Token(TokenType.NUMBER, source.substring(start, i).replace(',', '.'))
                }
                c.isLetter() || c == 'π' -> {
                    val start = i
                    while (i < source.length && (source[i].isLetter() || source[i] == 'π')) i++
                    val word = source.substring(start, i).lowercase()
                    result += Token(TokenType.IDENTIFIER, when (word) {
                        "π" -> "pi"
                        else -> word
                    })
                }
                else -> {
                    val type = when (c) {
                        '+' -> TokenType.PLUS
                        '-', '−' -> TokenType.MINUS
                        '*', '×' -> TokenType.MULTIPLY
                        '/', '÷' -> TokenType.DIVIDE
                        '^', '²' -> TokenType.POWER
                        '%' -> TokenType.PERCENT
                        '(' -> TokenType.LEFT
                        ')' -> TokenType.RIGHT
                        else -> throw IllegalArgumentException("Неизвестный символ: $c")
                    }
                    result += Token(type, c.toString())
                    i++
                }
            }
        }
        result += Token(TokenType.END, "")
        return result
    }

    private fun parseExpression(): Value {
        var value = parseTerm()
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            val op = take()
            val right = parseTerm()
            val before = value
            value = binary(value, right, op.type)
            if (recordSteps) steps += "${describe(before)} ${op.text} ${describe(right)} = ${describe(value)}"
        }
        return value
    }

    private fun parseTerm(): Value {
        var value = parseUnary()
        while (
            peek().type == TokenType.MULTIPLY ||
            peek().type == TokenType.DIVIDE ||
            startsImplicitFactor(peek())
        ) {
            val explicitOperator = peek().type == TokenType.MULTIPLY || peek().type == TokenType.DIVIDE
            val op = if (explicitOperator) take() else Token(TokenType.MULTIPLY, "×")
            val right = parseUnary()
            val before = value
            value = binary(value, right, op.type)
            if (recordSteps) steps += "${describe(before)} ${op.text} ${describe(right)} = ${describe(value)}"
        }
        return value
    }

    private fun startsImplicitFactor(token: Token): Boolean =
        token.type == TokenType.LEFT || token.type == TokenType.IDENTIFIER

    private fun parseUnary(): Value {
        return when (peek().type) {
            TokenType.PLUS -> { take(); parseUnary() }
            TokenType.MINUS -> {
                take()
                val v = parseUnary()
                when (v) {
                    is Value.Exact -> Value.Exact(-v.number)
                    is Value.Approximate -> Value.Approximate(v.number.negate())
                }
            }
            else -> parsePower()
        }
    }

    private fun parsePower(): Value {
        var value = parsePostfix()
        if (peek().type == TokenType.POWER) {
            take()
            val base = value
            val exponent = parseUnary()
            val exp = asExact(exponent)
            require(exp.denominator == java.math.BigInteger.ONE) {
                "Пока поддерживаются только целые показатели степени"
            }
            require(exp.numerator >= java.math.BigInteger.valueOf(-1000) &&
                exp.numerator <= java.math.BigInteger.valueOf(1000)) {
                "Показатель степени слишком большой"
            }
            val n = exp.numerator.toInt()
            value = when (base) {
                is Value.Exact -> Value.Exact(base.number.pow(n))
                is Value.Approximate -> {
                    if (n >= 0) {
                        Value.Approximate(base.number.pow(n, MC))
                    } else {
                        require(base.number.compareTo(BigDecimal.ZERO) != 0) { "Division by zero" }
                        Value.Approximate(BigDecimal.ONE.divide(base.number.pow(-n, MC), MC))
                    }
                }
            }
            if (recordSteps) steps += "${describe(base)}^$n = ${describe(value)}"
        }
        return value
    }

    private fun parsePostfix(): Value {
        var value = parsePrimary()
        while (peek().type == TokenType.PERCENT) {
            take()
            value = binary(value, Value.Exact(Rational.of(100)), TokenType.DIVIDE)
            if (recordSteps) steps += "Процент: ${describe(value)}"
        }
        return value
    }

    private fun parsePrimary(): Value {
        val token = take()
        return when (token.type) {
            TokenType.NUMBER -> Value.Exact(Rational.parseDecimal(token.text))
            TokenType.LEFT -> {
                val value = parseExpression()
                expect(TokenType.RIGHT)
                value
            }
            TokenType.IDENTIFIER -> parseIdentifier(token.text)
            else -> throw IllegalArgumentException("Ожидалось число или функция")
        }
    }

    private fun parseIdentifier(name: String): Value {
        variables[name]?.let { return Value.Approximate(it) }
        if (name == "pi") return Value.Approximate(BigDecimal(PI, MC))
        if (name == "e") return Value.Approximate(BigDecimal(E, MC))
        require(peek().type == TokenType.LEFT) { "После функции $name нужны скобки" }
        take()
        val argument = parseExpression()
        expect(TokenType.RIGHT)
        if (name == "abs" && argument is Value.Exact) {
            val exact = Rational.of(argument.number.numerator.abs(), argument.number.denominator)
            if (recordSteps) steps += "Модуль: |${describe(argument)}| = ${describe(Value.Exact(exact))}"
            return Value.Exact(exact)
        }
        if (name == "sqrt" && argument is Value.Exact) {
            require(argument.number.numerator.signum() >= 0) { "Корень из отрицательного числа" }
            val numeratorRoot = integerSquareRoot(argument.number.numerator)
            val denominatorRoot = integerSquareRoot(argument.number.denominator)
            if (numeratorRoot.multiply(numeratorRoot) == argument.number.numerator &&
                denominatorRoot.multiply(denominatorRoot) == argument.number.denominator) {
                val exact = Rational.of(numeratorRoot, denominatorRoot)
                if (recordSteps) steps += "√${describe(argument)} = ${describe(Value.Exact(exact))}"
                return Value.Exact(exact)
            }
        }
        val x = asDecimal(argument).toDouble()
        val answer = when (name) {
            "sqrt" -> { require(x >= 0.0) { "Корень из отрицательного числа" }; sqrt(x) }
            "sin" -> sin(toRadians(x))
            "cos" -> cos(toRadians(x))
            "tan" -> {
                val radians = toRadians(x)
                require(abs(cos(radians)) > 1e-12) { "Тангенс не определён для этого угла" }
                tan(radians)
            }
            "ln" -> { require(x > 0.0) { "Аргумент ln должен быть положительным" }; ln(x) }
            "log" -> { require(x > 0.0) { "Аргумент log должен быть положительным" }; log10(x) }
            "abs" -> abs(x)
            else -> throw IllegalArgumentException("Неизвестная функция: $name")
        }
        require(answer.isFinite()) { "Результат не является конечным числом" }
        val decimal = BigDecimal.valueOf(answer).round(MC)
        if (recordSteps) steps += "$name(${describe(argument)}) ≈ ${decimal.stripTrailingZeros().toPlainString()}"
        return Value.Approximate(decimal)
    }

    private fun integerSquareRoot(value: java.math.BigInteger): java.math.BigInteger {
        require(value.signum() >= 0)
        if (value < java.math.BigInteger.valueOf(2)) return value
        var x = java.math.BigInteger.ONE.shiftLeft((value.bitLength() + 1) / 2)
        while (true) {
            val next = x.add(value.divide(x)).shiftRight(1)
            if (next >= x) return x
            x = next
        }
    }

    private fun toRadians(value: Double) =
        if (angleMode == AngleMode.DEGREES) Math.toRadians(value) else value

    private fun binary(left: Value, right: Value, op: TokenType): Value {
        if (left is Value.Exact && right is Value.Exact) {
            val result = when (op) {
                TokenType.PLUS -> left.number + right.number
                TokenType.MINUS -> left.number - right.number
                TokenType.MULTIPLY -> left.number * right.number
                TokenType.DIVIDE -> left.number / right.number
                else -> error("Unsupported operator")
            }
            return Value.Exact(result)
        }
        val a = asDecimal(left)
        val b = asDecimal(right)
        val result = when (op) {
            TokenType.PLUS -> a.add(b, MC)
            TokenType.MINUS -> a.subtract(b, MC)
            TokenType.MULTIPLY -> a.multiply(b, MC)
            TokenType.DIVIDE -> a.divide(b, MC)
            else -> error("Unsupported operator")
        }
        return Value.Approximate(result)
    }

    private fun asExact(value: Value): Rational =
        (value as? Value.Exact)?.number ?: throw IllegalArgumentException("Для этой операции нужно точное целое число")

    private fun asDecimal(value: Value): BigDecimal = when (value) {
        is Value.Exact -> value.number.toBigDecimal(MC)
        is Value.Approximate -> value.number
    }

    private fun describe(value: Value): String = when (value) {
        is Value.Exact -> value.number.toDisplayString()
        is Value.Approximate -> value.number.stripTrailingZeros().toPlainString()
    }

    private fun peek() = tokens[position]
    private fun take() = tokens[position++]
    private fun expect(type: TokenType) {
        require(peek().type == type) { "Неправильная структура выражения" }
        take()
    }

    companion object {
        private val MC = MathContext(24, RoundingMode.HALF_EVEN)
    }
}
