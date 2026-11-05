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

    @Test fun rejectsCubicEquation() {
        try {
            solver.solve("x^3 = 8")
            throw AssertionError("Expected unsupported degree to fail")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test fun acceptsSuperscriptSquareAndUnicodeMinus() {
        val result = solver.solve("x² − 5x + 6 = 0")
        assertEquals("quadratic", result.kind)
        assertTrue(result.answer.contains("x₁ = 3"))
        assertTrue(result.answer.contains("x₂ = 2"))
    }
}
