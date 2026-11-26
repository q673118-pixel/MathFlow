package com.mathflow.app

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sqrt

data class CoordinatePoint(val x: Double, val y: Double)
data class GeometryVector(val x: Double, val y: Double)
data class LineEquation(val a: Double, val b: Double, val c: Double) {
    fun evaluate(point: CoordinatePoint): Double = a * point.x + b * point.y + c
}
data class CircleEquation(val center: CoordinatePoint, val radius: Double)
data class TriangleMetrics(
    val perimeter: Double,
    val area: Double,
    val angleA: Double,
    val angleB: Double,
    val angleC: Double
)

class CoordinateGeometry {
    fun distance(a: CoordinatePoint, b: CoordinatePoint): Double = hypot(b.x - a.x, b.y - a.y)

    fun midpoint(a: CoordinatePoint, b: CoordinatePoint) =
        CoordinatePoint((a.x + b.x) / 2.0, (a.y + b.y) / 2.0)

    fun vector(from: CoordinatePoint, to: CoordinatePoint) =
        GeometryVector(to.x - from.x, to.y - from.y)

    fun add(a: GeometryVector, b: GeometryVector) = GeometryVector(a.x + b.x, a.y + b.y)
    fun subtract(a: GeometryVector, b: GeometryVector) = GeometryVector(a.x - b.x, a.y - b.y)
    fun scale(vector: GeometryVector, factor: Double) = GeometryVector(vector.x * factor, vector.y * factor)
    fun dot(a: GeometryVector, b: GeometryVector): Double = a.x * b.x + a.y * b.y
    fun magnitude(vector: GeometryVector): Double = hypot(vector.x, vector.y)

    fun angleDegrees(a: GeometryVector, b: GeometryVector): Double {
        val denominator = magnitude(a) * magnitude(b)
        require(denominator > 0.0) { "У нулевого вектора нет направления" }
        return Math.toDegrees(acos((dot(a, b) / denominator).coerceIn(-1.0, 1.0)))
    }

    fun lineThrough(a: CoordinatePoint, b: CoordinatePoint): LineEquation {
        require(distance(a, b) > 1.0e-12) { "Для прямой нужны две разные точки" }
        val aa = b.y - a.y
        val bb = a.x - b.x
        val cc = -(aa * a.x + bb * a.y)
        val norm = hypot(aa, bb)
        return LineEquation(aa / norm, bb / norm, cc / norm)
    }

    fun intersection(first: LineEquation, second: LineEquation): CoordinatePoint? {
        val determinant = first.a * second.b - second.a * first.b
        if (abs(determinant) < 1.0e-12) return null
        return CoordinatePoint(
            (first.b * second.c - second.b * first.c) / determinant,
            (second.a * first.c - first.a * second.c) / determinant
        )
    }

    fun circleThrough(center: CoordinatePoint, pointOnCircle: CoordinatePoint): CircleEquation {
        val radius = distance(center, pointOnCircle)
        require(radius > 0.0) { "Радиус должен быть положительным" }
        return CircleEquation(center, radius)
    }

    fun circleContains(circle: CircleEquation, point: CoordinatePoint, tolerance: Double = 1.0e-9): Boolean =
        distance(circle.center, point) <= circle.radius + tolerance

    fun triangle(a: CoordinatePoint, b: CoordinatePoint, c: CoordinatePoint): TriangleMetrics {
        val sideA = distance(b, c)
        val sideB = distance(a, c)
        val sideC = distance(a, b)
        require(min(sideA, min(sideB, sideC)) > 1.0e-12) { "Стороны треугольника должны быть положительными" }
        require(sideA + sideB > sideC && sideA + sideC > sideB && sideB + sideC > sideA) {
            "Точки не образуют треугольник"
        }
        val semiperimeter = (sideA + sideB + sideC) / 2.0
        val area = sqrt(semiperimeter * (semiperimeter - sideA) *
            (semiperimeter - sideB) * (semiperimeter - sideC))
        fun angle(opposite: Double, adjacentA: Double, adjacentB: Double): Double =
            Math.toDegrees(acos(((adjacentA * adjacentA + adjacentB * adjacentB - opposite * opposite) /
                (2.0 * adjacentA * adjacentB)).coerceIn(-1.0, 1.0)))
        return TriangleMetrics(
            sideA + sideB + sideC,
            area,
            angle(sideA, sideB, sideC),
            angle(sideB, sideA, sideC),
            angle(sideC, sideA, sideB)
        )
    }

    fun slope(a: CoordinatePoint, b: CoordinatePoint): Double? =
        if (abs(b.x - a.x) < 1.0e-12) null else (b.y - a.y) / (b.x - a.x)
}
