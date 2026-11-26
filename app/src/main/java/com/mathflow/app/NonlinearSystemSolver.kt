package com.mathflow.app

import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max

data class NonlinearSystemRoot(val x: Double, val y: Double, val residual: Double)
data class NonlinearSystemSolution(
    val roots: List<NonlinearSystemRoot>,
    val searchedSeeds: Int,
    val explanation: List<String>
)

/** Multi-start damped Newton solver for two nonlinear equations in two variables. */
class NonlinearSystemSolver {
    fun solve(
        firstEquation: String,
        secondEquation: String,
        searchMin: Double = -10.0,
        searchMax: Double = 10.0,
        seedCountPerAxis: Int = 9
    ): NonlinearSystemSolution {
        require(searchMin.isFinite() && searchMax.isFinite() && searchMin < searchMax)
        require(seedCountPerAxis in 3..21)
        val first = splitEquation(firstEquation)
        val second = splitEquation(secondEquation)
        val engine = ExpressionEngine(AngleMode.RADIANS)
        fun eval(pair: Pair<String, String>, x: Double, y: Double): Double? = runCatching {
            val vars = mapOf("x" to BigDecimal.valueOf(x), "y" to BigDecimal.valueOf(y))
            val a = engine.calculate(pair.first, vars, false).decimalValue.toDouble()
            val b = engine.calculate(pair.second, vars, false).decimalValue.toDouble()
            (a - b).takeIf { it.isFinite() }
        }.getOrNull()
        fun residual(x: Double, y: Double): Pair<Double, Double>? {
            val a = eval(first, x, y) ?: return null
            val b = eval(second, x, y) ?: return null
            return a to b
        }

        val roots = ArrayList<NonlinearSystemRoot>()
        var attempted = 0
        for (ix in 0 until seedCountPerAxis) {
            for (iy in 0 until seedCountPerAxis) {
                attempted++
                var x = searchMin + (searchMax - searchMin) * ix / (seedCountPerAxis - 1)
                var y = searchMin + (searchMax - searchMin) * iy / (seedCountPerAxis - 1)
                repeat(80) {
                    val f = residual(x, y) ?: return@repeat
                    val norm = max(abs(f.first), abs(f.second))
                    if (norm < 1.0e-9) return@repeat
                    val h = 1.0e-5 * max(1.0, max(abs(x), abs(y)))
                    val fx = residual(x + h, y) ?: return@repeat
                    val fy = residual(x, y + h) ?: return@repeat
                    val a = (fx.first - f.first) / h
                    val b = (fy.first - f.first) / h
                    val c = (fx.second - f.second) / h
                    val d = (fy.second - f.second) / h
                    val determinant = a * d - b * c
                    if (!determinant.isFinite() || abs(determinant) < 1.0e-12) return@repeat
                    val dx = (d * f.first - b * f.second) / determinant
                    val dy = (-c * f.first + a * f.second) / determinant
                    var factor = 1.0
                    var accepted = false
                    repeat(12) {
                        val nx = x - factor * dx
                        val ny = y - factor * dy
                        val next = residual(nx, ny)
                        if (next != null && max(abs(next.first), abs(next.second)) < norm) {
                            x = nx
                            y = ny
                            accepted = true
                            return@repeat
                        }
                        factor *= 0.5
                    }
                    if (!accepted) return@repeat
                }
                val final = residual(x, y) ?: continue
                val error = max(abs(final.first), abs(final.second))
                if (error <= 1.0e-7 && x.isFinite() && y.isFinite() &&
                    x in searchMin..searchMax && y in searchMin..searchMax &&
                    roots.none { abs(it.x - x) < 1.0e-5 && abs(it.y - y) < 1.0e-5 }) {
                    roots += NonlinearSystemRoot(x, y, error)
                }
            }
        }
        return NonlinearSystemSolution(
            roots.sortedWith(compareBy<NonlinearSystemRoot> { it.x }.thenBy { it.y }),
            attempted,
            listOf(
                "Использован метод Ньютона с численной матрицей Якоби и несколькими начальными приближениями.",
                "Область поиска: [" + searchMin + "; " + searchMax + "] для обеих переменных.",
                "Метод находит только корни, к которым сходятся выбранные начальные приближения; полнота решения не гарантируется."
            )
        )
    }

    private fun splitEquation(source: String): Pair<String, String> {
        val index = source.indexOf('=')
        require(index > 0 && source.indexOf('=', index + 1) < 0) {
            "В каждом уравнении должен быть ровно один знак ="
        }
        val left = source.substring(0, index).trim()
        val right = source.substring(index + 1).trim()
        require(left.isNotEmpty() && right.isNotEmpty())
        return left to right
    }
}
