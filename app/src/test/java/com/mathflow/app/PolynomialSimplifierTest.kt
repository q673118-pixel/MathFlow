package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PolynomialSimplifierTest {
    private val simplifier = PolynomialSimplifier()
    @Test fun combinesLikeTermsAndExpandsParentheses() { assertEquals("x + 6", simplifier.simplify("2(x + 3) - x").expression) }
    @Test fun simplifiesQuadraticPolynomial() { assertEquals("x^2 + 5x + 6", simplifier.simplify("(x + 2)(x + 3)").expression) }
    @Test fun keepsExactFractionCoefficients() { assertEquals("1/2x + 1/3", simplifier.simplify("x/2 + 1/3").expression) }
    @Test fun simplifiesToZero() { assertEquals("0", simplifier.simplify("3x - x - 2x").expression) }
}
