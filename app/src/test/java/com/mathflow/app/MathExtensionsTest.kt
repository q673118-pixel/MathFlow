package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class MathExtensionsTest {
    @Test fun expressionAstSupportsVariablesFunctionsAndImplicitMultiplication() {
        val expression = MathExpression.parse("2x + sin(pi/2) + |y|")
        assertEquals(8.0, expression.evaluate(mapOf("x" to 3.0, "y" to 1.0)), 1e-9)
    }

    @Test fun expressionAstSupportsInverseTrigonometricAndCotangentFunctions() {
        val expression = MathExpression.parse("asin(1)+acos(1)+atan(0)+cot(pi/4)")
        assertEquals(Math.PI / 2.0 + 1.0, expression.evaluate(), 1e-9)
    }

    @Test fun expressionAstRejectsUndefinedDomains() {
        val expression = MathExpression.parse("sqrt(x)")
        try {
            expression.evaluate(mapOf("x" to -1.0))
            throw AssertionError("Expected domain validation")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test fun graphEngineSamplesFunctionsAndBreaksAtPoles() {
        val engine = MathGraphEngine()
        val points = engine.sampleFunction("x^2", GraphViewport(-2.0, 2.0, -1.0, 5.0), 40)
        assertTrue(points.size >= 41)
        assertEquals(4.0, points.last().y!!, 1e-6)
        val reciprocal = engine.sampleFunction("1/x", GraphViewport(-2.0, 2.0, -10.0, 10.0), 40)
        assertTrue(reciprocal.any { it.y == null })
    }

    @Test fun graphEngineFindsImplicitCircleContour() {
        val segments = MathGraphEngine().sampleImplicit(
            "x^2+y^2-1",
            GraphViewport(-2.0, 2.0, -2.0, 2.0),
            50,
            50
        )
        assertTrue(segments.size > 20)
    }

    @Test fun graphEngineBuildsInequalityRegionMask() {
        val cells = MathGraphEngine().sampleInequality(
            "x^2+y^2-1", "<=", GraphViewport(-2.0, 2.0, -2.0, 2.0), 32, 32
        )
        assertTrue(cells.isNotEmpty())
        assertTrue(cells.size < 32 * 32)
    }

    @Test fun statisticsCalculateDescriptiveAndFrequencyMeasures() {
        val result = StatisticsEngine().analyze(listOf(1.0, 2.0, 2.0, 3.0), 2)
        assertEquals(4, result.count)
        assertEquals(2.0, result.mean, 1e-9)
        assertEquals(2.0, result.median, 1e-9)
        assertEquals(listOf(2.0), result.mode)
        assertEquals(0.5, result.variance, 1e-9)
        assertEquals(4, result.cumulativeFrequencies.last().second)
        assertEquals(4, result.histogram.sumOf { it.frequency })
    }

    @Test fun statisticsParseDecimalCommaAndDelimitedValues() {
        val engine = StatisticsEngine()
        assertEquals(listOf(1.5), engine.parseValues("1,5"))
        assertEquals(listOf(1.0, 2.5, 3.0), engine.parseValues("1; 2,5; 3"))
        assertEquals(listOf(1.0, 2.0, 3.0), engine.parseValues("1,2,3"))
    }

    @Test fun coordinateGeometryComputesDistanceMidpointAndLineIntersection() {
        val geometry = CoordinateGeometry()
        val a = CoordinatePoint(0.0, 0.0)
        val b = CoordinatePoint(3.0, 4.0)
        assertEquals(5.0, geometry.distance(a, b), 1e-9)
        assertEquals(CoordinatePoint(1.5, 2.0), geometry.midpoint(a, b))
        val vertical = geometry.lineThrough(CoordinatePoint(2.0, 0.0), CoordinatePoint(2.0, 4.0))
        val horizontal = geometry.lineThrough(CoordinatePoint(0.0, 3.0), CoordinatePoint(4.0, 3.0))
        assertEquals(CoordinatePoint(2.0, 3.0), geometry.intersection(vertical, horizontal))
    }

    @Test fun coordinateGeometrySupportsPolygonAreaAndTransformations() {
        val geometry = CoordinateGeometry()
        val square = listOf(
            CoordinatePoint(0.0, 0.0), CoordinatePoint(2.0, 0.0),
            CoordinatePoint(2.0, 2.0), CoordinatePoint(0.0, 2.0)
        )
        assertEquals(4.0, geometry.polygonArea(square), 1e-9)
        assertEquals(8.0, geometry.polygonPerimeter(square), 1e-9)
        assertEquals(CoordinatePoint(0.0, 1.0), geometry.rotate(CoordinatePoint(1.0, 0.0), CoordinatePoint(0.0, 0.0), 90.0))
        assertEquals(CoordinatePoint(-1.0, 2.0), geometry.homothety(CoordinatePoint(1.0, 2.0), CoordinatePoint(0.0, 2.0), -1.0))
        assertEquals(PI, geometry.sectorArea(2.0, 90.0), 1e-9)
    }

    @Test fun coordinateGeometryComputesTriangleMetrics() {
        val result = CoordinateGeometry().triangle(
            CoordinatePoint(0.0, 0.0),
            CoordinatePoint(4.0, 0.0),
            CoordinatePoint(0.0, 3.0)
        )
        assertEquals(12.0, result.perimeter, 1e-9)
        assertEquals(6.0, result.area, 1e-9)
        assertEquals(90.0, result.angleA, 1e-9)
    }

    @Test fun inequalitySolverKeepsIsolatedRepeatedRootForNonStrictRelation() {
        val result = InequalitySolver().solve("x^2", "<=", "0", -5.0, 5.0, 2000)
        assertEquals(1, result.intervals.size)
        assertEquals(result.intervals.first().left, result.intervals.first().right, 1e-3)
        assertTrue(result.intervals.first().leftIncluded)
        assertTrue(result.intervals.first().rightIncluded)
    }

    @Test fun inequalitySolverSplitsAtRationalPolesWithoutTreatingThemAsRoots() {
        val result = InequalitySolver().solve("1/(x-1)", ">", "0", -5.0, 5.0, 2000)
        assertEquals(1, result.intervals.size)
        assertEquals(1.0, result.intervals.first().left, 1e-3)
        assertFalse(result.intervals.first().leftIncluded)
        assertTrue(result.intervals.first().rightIncluded)
        assertTrue(result.domainExclusions.any { kotlin.math.abs(it - 1.0) < 1e-3 })
    }

    @Test fun inequalitySolverMergesIntervalsWhenTheSharedRootIsIncluded() {
        val result = InequalitySolver().solve("x^2", ">=", "0", -5.0, 5.0, 2000)
        assertEquals(1, result.intervals.size)
        assertEquals(-5.0, result.intervals.first().left, 1e-3)
        assertEquals(5.0, result.intervals.first().right, 1e-3)
    }

    @Test fun inequalitySolverFindsBoundedSolutionIntervals() {
        val result = InequalitySolver().solve("x^2", "<=", "4", -5.0, 5.0, 2000)
        assertEquals(1, result.intervals.size)
        assertEquals(-2.0, result.intervals.first().left, 1e-3)
        assertEquals(2.0, result.intervals.first().right, 1e-3)
    }

    @Test fun nonlinearSystemSolverFindsBothCircleLineIntersections() {
        val result = NonlinearSystemSolver().solve(
            "x^2+y^2=1",
            "x-y=0",
            -2.0,
            2.0,
            9
        )
        assertEquals(2, result.roots.size)
        assertTrue(result.roots.any { it.x > 0.0 && it.y > 0.0 })
        assertTrue(result.roots.any { it.x < 0.0 && it.y < 0.0 })
        assertFalse(result.roots.any { it.residual > 1e-7 })
    }
}
