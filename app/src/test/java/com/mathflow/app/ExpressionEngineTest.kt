package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class ExpressionEngineTest {
    private val engine = ExpressionEngine()

    @Test fun exactDecimalAndFractionArithmetic() {
        val result = engine.calculate("0.5 + 1/4")
        assertEquals("3/4", result.displayValue)
        assertEquals("0.75", result.decimalValue)
    }

    @Test fun respectsOperatorPrecedence() {
        assertEquals("14", engine.calculate("2 + 3 * 4").displayValue)
    }

    @Test fun handlesParentheses() {
        assertEquals("20", engine.calculate("(2 + 3) * 4").displayValue)
    }

    @Test fun supportsIntegerPowers() {
        assertEquals("81", engine.calculate("3^4").displayValue)
    }

    @Test fun supportsPostfixPercent() {
        assertEquals("1/4", engine.calculate("25%").displayValue)
    }

    @Test fun reportsDivisionByZero() {
        try {
            engine.calculate("1/0")
            throw AssertionError("Expected division by zero to fail")
        } catch (_: IllegalArgumentException) {
            // Expected
        }
    }

    @Test fun evaluatesGraphVariable() {
        val result = ExpressionEngine().calculate(
            "x^2 + 2x + 1",
            variables = mapOf("x" to java.math.BigDecimal.valueOf(3)),
            includeSteps = false
        )
        assertEquals("16", result.displayValue)
        assertTrue(result.steps.isEmpty())
    }

    @Test fun evaluatesGraphVariableInsideFunction() {
        val result = ExpressionEngine().calculate(
            "sin(x)",
            variables = mapOf("x" to java.math.BigDecimal.valueOf(30))
        )
        assertEquals(0.5, result.decimalValue.toDouble(), 1e-9)
    }

    @Test fun trigUsesDegreeModeByDefault() {
        val result = engine.calculate("sin(30)")
        assertEquals(0.5, result.decimalValue.toDouble(), 1e-9)
    }

    @Test
    fun implicitMultiplicationWithParentheses() {
        val result = ExpressionEngine().calculate("2(3+4)")
        assertEquals("14", result.displayValue)
    }

    @Test
    fun implicitMultiplicationWithConstant() {
        val result = ExpressionEngine().calculate("2pi")
        assertTrue(result.exactValue == null)
        assertEquals(2.0 * Math.PI, result.decimalValue.toDouble(), 1e-9)
    }

    @Test
    fun perfectSquareRootRemainsExact() {
        val result = ExpressionEngine().calculate("sqrt(81/16)")
        assertEquals("2 1/4", result.displayValue)
        assertEquals(Rational.of(9) / Rational.of(4), result.exactValue)
    }

    @Test
    fun absoluteValueRemainsExact() {
        val result = ExpressionEngine().calculate("abs(-7/3)")
        assertEquals("2 1/3", result.displayValue)
        assertEquals(Rational.of(7) / Rational.of(3), result.exactValue)
    }

    @Test
    fun approximateValueSupportsNegativeIntegerPower() {
        val result = ExpressionEngine().calculate("pi^-1")
        assertEquals(1.0 / Math.PI, result.decimalValue.toDouble(), 1e-9)
    }
}
