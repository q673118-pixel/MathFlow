package com.mathflow.app

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.sqrt

private val TOOL_CONTEXT = MathContext(16, RoundingMode.HALF_UP)

private fun parseToolNumber(source: String): BigDecimal {
    val normalized = source.trim().replace(',', '.')
    require(normalized.isNotEmpty()) { "Заполните все поля" }
    return try {
        BigDecimal(normalized)
    } catch (_: NumberFormatException) {
        throw IllegalArgumentException("Некорректное число: $source")
    }
}

private fun formatToolNumber(value: BigDecimal): String =
    value.round(TOOL_CONTEXT).stripTrailingZeros().toPlainString()

object FractionTools {
    private fun parse(source: String): Rational {
        val text = source.trim().replace(',', '.')
        require(text.isNotEmpty()) { "Введите обе дроби" }
        val slash = text.indexOf('/')
        if (slash < 0) return Rational.parseDecimal(text)
        require(text.indexOf('/', slash + 1) < 0) { "В дроби должен быть только один знак /" }
        val numerator = Rational.parseDecimal(text.substring(0, slash).trim())
        val denominator = Rational.parseDecimal(text.substring(slash + 1).trim())
        require(denominator.numerator.signum() != 0) { "Знаменатель не может быть равен нулю" }
        return numerator / denominator
    }

    fun calculate(first: String, second: String, operation: String): String {
        val a = parse(first)
        val b = parse(second)
        val result = when (operation) {
            "+" -> a + b
            "−", "-" -> a - b
            "×", "*" -> a * b
            "÷", "/" -> a / b
            else -> throw IllegalArgumentException("Неизвестная операция")
        }
        return buildString {
            append("Точный ответ: ${result.toMixedDisplayString()}")
            append("\nДесятичный вид: ${result.toDecimalString(12)}")
            append("\nСокращённая дробь: ${result.toDisplayString()}")
        }
    }
}

object PercentTools {
    fun percentOf(percent: String, amount: String): String {
        val p = parseToolNumber(percent)
        val a = parseToolNumber(amount)
        val result = a.multiply(p).divide(BigDecimal("100"), TOOL_CONTEXT)
        return "$percent% от $amount = ${formatToolNumber(result)}"
    }

    fun whatPercent(part: String, whole: String): String {
        val p = parseToolNumber(part)
        val w = parseToolNumber(whole)
        require(w.compareTo(BigDecimal.ZERO) != 0) { "Целое значение не должно быть равно нулю" }
        val result = p.multiply(BigDecimal("100")).divide(w, TOOL_CONTEXT)
        return "$part составляет ${formatToolNumber(result)}% от $whole"
    }

    fun changeByPercent(amount: String, percent: String, increase: Boolean): String {
        val value = parseToolNumber(amount)
        val rate = parseToolNumber(percent).divide(BigDecimal("100"), TOOL_CONTEXT)
        val factor = if (increase) BigDecimal.ONE.add(rate) else BigDecimal.ONE.subtract(rate)
        val result = value.multiply(factor, TOOL_CONTEXT)
        val label = if (increase) "После увеличения" else "После уменьшения"
        return "$label на $percent%: ${formatToolNumber(result)}"
    }
}

object GeometryTools {
    fun rectangle(aText: String, bText: String): String {
        val a = parseToolNumber(aText)
        val b = parseToolNumber(bText)
        require(a > BigDecimal.ZERO && b > BigDecimal.ZERO) { "Стороны должны быть больше нуля" }
        val area = a.multiply(b, TOOL_CONTEXT)
        val perimeter = a.add(b).multiply(BigDecimal("2"), TOOL_CONTEXT)
        return "Площадь: ${formatToolNumber(area)}\nПериметр: ${formatToolNumber(perimeter)}"
    }

    fun circle(radiusText: String): String {
        val radius = parseToolNumber(radiusText)
        require(radius > BigDecimal.ZERO) { "Радиус должен быть больше нуля" }
        val r = radius.toDouble()
        val area = PI * r * r
        val circumference = 2.0 * PI * r
        return "Площадь: ${formatToolNumber(BigDecimal.valueOf(area))}\nДлина окружности: ${formatToolNumber(BigDecimal.valueOf(circumference))}"
    }

    fun triangle(aText: String, bText: String, cText: String): String {
        val a = parseToolNumber(aText).toDouble()
        val b = parseToolNumber(bText).toDouble()
        val c = parseToolNumber(cText).toDouble()
        require(a > 0 && b > 0 && c > 0) { "Стороны должны быть больше нуля" }
        require(a + b > c && a + c > b && b + c > a) { "Такие стороны не образуют треугольник" }
        val perimeter = a + b + c
        val semiperimeter = perimeter / 2.0
        val area = sqrt(semiperimeter * (semiperimeter - a) * (semiperimeter - b) * (semiperimeter - c))
        require(area.isFinite()) { "Не удалось вычислить площадь" }
        return "Периметр: ${formatToolNumber(BigDecimal.valueOf(perimeter))}\nПолупериметр: ${formatToolNumber(BigDecimal.valueOf(semiperimeter))}\nПлощадь (формула Герона): ${formatToolNumber(BigDecimal.valueOf(area))}"
    }
}
