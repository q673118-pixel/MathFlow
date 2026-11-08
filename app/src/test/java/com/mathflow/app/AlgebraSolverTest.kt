package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlgebraSolverTest {
    private val solver = AlgebraSolver()

    @Test fun solvesLinearEquation() {
        val result = solver.solve("2x + 3 = 11")
        assertEquals("linear", result.kind)
        assertTrue(result.answer.startsWith("x = 4"))
    }

    @Test fun solvesLinearEquationWithParentheses() {
        val result = solver.solve("3(x - 2) = 9")
        assertTrue(result.answer.startsWith("x = 5"))
    }

    @Test fun solvesQuadraticWithExactRoots() {
        val result = solver.solve("x^2 - 5x + 6 = 0")
        assertEquals("quadratic", result.kind)
        assertTrue(result.answer.contains("x₁ = 3"))
        assertTrue(result.answer.contains("x₂ = 2"))
    }

    @Test fun identifiesDoubleRoot() {
        val result = solver.solve("x^2 - 4x + 4 = 0")
        assertEquals("quadratic_double", result.kind)
        assertTrue(result.answer.contains("x = 2"))
    }

    @Test fun identifiesNoRealRoots() {
        val result = solver.solve("x^2 + 1 = 0")
        assertEquals("no_real_roots", result.kind)
    }

    @Test fun identifiesIdentityAndContradiction() {
        assertEquals("identity", solver.solve("2x + 2 = 2(x + 1)").kind)
        assertEquals("no_solution", solver.solve("x + 1 = x + 2").kind)
    }

    @Test fun solvesCubicEquationNumerically() {
        val result = solver.solve("x^3 = 8")
        assertEquals("polynomial_numeric", result.kind)
        assertTrue(result.answer.contains("2"))
    }

    @Test fun solvesQuarticWithFourRealRoots() {
        val result = solver.solve("x^4 - 5x^2 + 4 = 0")
        assertEquals("polynomial_numeric", result.kind)
        assertTrue(result.answer.contains("-2"))
        assertTrue(result.answer.contains("-1"))
        assertTrue(result.answer.contains("1"))
        assertTrue(result.answer.contains("2"))
    }

    @Test fun findsRepeatedQuarticRoots() {
        val result = solver.solve("x^4 - 2x^2 + 1 = 0")
        assertEquals("polynomial_numeric", result.kind)
        assertTrue(result.answer.contains("-1"))
        assertTrue(result.answer.contains("1"))
    }

    @Test fun identifiesQuarticWithoutRealRoots() {
        assertEquals("no_real_roots", solver.solve("x^4 + 1 = 0").kind)
    }

    @Test fun acceptsSuperscriptCubicInput() {
        val result = solver.solve("x³ = 8")
        assertEquals("polynomial_numeric", result.kind)
        assertTrue(result.answer.contains("2"))
    }

    @Test fun rejectsPolynomialDegreeAboveFourExplicitly() {
        try {
            solver.solve("x^5 = 1")
            throw AssertionError("Expected degree above four to be rejected")
        } catch (_: IllegalArgumentException) {
            // The solver must report the unsupported degree rather than guessing.
        }
    }

    @Test fun acceptsSuperscriptSquareAndUnicodeMinus() {
        val result = solver.solve("x² − 5x + 6 = 0")
        assertEquals("quadratic", result.kind)
        assertTrue(result.answer.contains("x₁ = 3"))
        assertTrue(result.answer.contains("x₂ = 2"))
    }
    @Test fun solvesSimpleRationalEquationAndChecksDomain() {
        val result = solver.solve("1/(x - 1) = 2")
        assertEquals("linear", result.kind)
        assertTrue(result.answer.contains("1.5"))
        assertTrue(result.steps.any { it.contains("ОДЗ") })
    }

    @Test fun excludesExtraneousRootAfterClearingDenominator() {
        val result = solver.solve("(x² - 1)/(x - 1) = 0")
        assertEquals("linear", result.kind)
        assertTrue(result.answer.contains("-1"))
        assertTrue(!result.answer.contains("1"))
    }

    @Test fun keepsConstantDenominatorScale() {
        val result = solver.solve("x/2 = 3")
        assertEquals("linear", result.kind)
        assertTrue(result.answer.contains("6"))
    }

    @Test fun supportsNegativePowersAsRationalExpressions() {
        val result = solver.solve("x^-1 = 0.5")
        assertEquals("linear", result.kind)
        assertTrue(result.answer.contains("2"))
    }

    @Test fun identifiesRationalIdentityWithExcludedPoint() {
        val result = solver.solve("(x - 1)/(x - 1) = 1")
        assertEquals("identity", result.kind)
        assertTrue(result.answer.contains("1"))
        assertTrue(result.steps.any { it.contains("ОДЗ") })
    }

    @Test fun excludesDenominatorRootFromQuadraticSolutions() {
        val result = solver.solve("(x² - 1)/(x - 1) = 0")
        assertTrue(result.answer.contains("-1"))
        assertTrue(!result.answer.contains("x₂"))
    }

}
