package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinearSystemSolverTest {
    private val solver = LinearSystemSolver()
    @Test fun solvesTwoVariableSystemExactly() {
        val r = solver.solve(listOf("x + y = 5", "x - y = 1"))
        assertEquals("unique", r.kind); assertEquals("x = 3, y = 2", r.answer)
        assertTrue(r.steps.any { it.contains("матрицу") })
    }
    @Test fun solvesThreeVariableSystemExactly() {
        val r = solver.solve(listOf("x + y + z = 6", "2x - y + z = 3", "x + 2y - z = 2"))
        assertEquals("unique", r.kind); assertTrue(r.answer.contains("x =")); assertTrue(r.answer.contains("y =")); assertTrue(r.answer.contains("z ="))
    }
    @Test fun detectsInconsistentSystem() {
        assertEquals("no_solution", solver.solve(listOf("x + y = 2", "x + y = 3")).kind)
    }
    @Test fun detectsInfiniteSolutions() {
        assertEquals("infinite", solver.solve(listOf("x + y = 2", "2x + 2y = 4")).kind)
    }
    @Test fun supportsParenthesesAndFractions() {
        val r = solver.solve(listOf("2(x + y) = 6", "x - y = 1/2"))
        assertEquals("unique", r.kind); assertEquals("x = 7/4, y = 5/4", r.answer)
    }
}
