package com.mathflow.app

import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class MathToolsTest {
    @Test
    fun fractionAdditionIsExactAndReduced() {
        val result = FractionTools.calculate("3/4", "2/5", "+")
        assertTrue(result.contains("Точный ответ: 1 3/20"))
        assertTrue(result.contains("Сокращённая дробь: 23/20"))
    }

    @Test
    fun fractionDivisionRejectsZero() {
        assertThrows(IllegalArgumentException::class.java) {
            FractionTools.calculate("1/2", "0", "÷")
        }
    }

    @Test
    fun percentOfNumber() {
        assertTrue(PercentTools.percentOf("15", "240").endsWith("36"))
    }

    @Test
    fun percentageOfWhole() {
        assertTrue(PercentTools.whatPercent("25", "200").contains("12.5%"))
    }

    @Test
    fun percentDecrease() {
        assertTrue(PercentTools.changeByPercent("100", "15", false).endsWith("85"))
    }

    @Test
    fun rectangleAreaAndPerimeter() {
        val result = GeometryTools.rectangle("5", "3")
        assertTrue(result.contains("Площадь: 15"))
        assertTrue(result.contains("Периметр: 16"))
    }

    @Test
    fun circleUsesRadius() {
        val result = GeometryTools.circle("1")
        assertTrue(result.contains("Площадь: 3.14159265358979"))
    }

    @Test
    fun triangleRejectsImpossibleSides() {
        assertThrows(IllegalArgumentException::class.java) {
            GeometryTools.triangle("1", "2", "8")
        }
    }

    @Test
    fun triangleUsesHeronFormula() {
        val result = GeometryTools.triangle("3", "4", "5")
        assertTrue(result.contains("Площадь (формула Герона): 6"))
    }

    @Test
    fun percentIncrease() {
        assertTrue(PercentTools.changeByPercent("200", "12.5", true).endsWith("225"))
    }

    @Test
    fun decimalFractionInputRemainsExact() {
        val result = FractionTools.calculate("0.5", "1/4", "+")
        assertTrue(result.contains("Точный ответ: 3/4"))
    }

    @Test
    fun rectangleRejectsZeroSide() {
        assertThrows(IllegalArgumentException::class.java) {
            GeometryTools.rectangle("0", "5")
        }
    }
}
