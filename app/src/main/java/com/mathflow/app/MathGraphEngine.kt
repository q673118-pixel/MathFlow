package com.mathflow.app

import kotlin.math.abs
import kotlin.math.max

data class GraphPoint(val x: Double, val y: Double?)
data class GraphCurve(val expression: String, val points: List<GraphPoint>)
data class GraphSegment(val startX: Double, val startY: Double, val endX: Double, val endY: Double)
data class GraphRegionCell(val left: Double, val top: Double, val right: Double, val bottom: Double, val included: Boolean)
data class GraphIntersection(val x: Double, val y: Double)

data class GraphViewport(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0
) {
    init {
        require(xMin.isFinite() && xMax.isFinite() && yMin.isFinite() && yMax.isFinite())
        require(xMin < xMax && yMin < yMax) { "Некорректные границы графика" }
    }
}

/**
 * CPU-only graph sampler shared by function, implicit-equation and inequality views.
 * Expression evaluation is performed off the UI thread by callers.
 */
class MathGraphEngine {
    fun sampleFunctions(
        expressions: List<String>,
        viewport: GraphViewport,
        targetSegments: Int = 240,
        maxPointsPerCurve: Int = 5000
    ): List<GraphCurve> {
        require(expressions.size in 1..12) { "Можно построить от 1 до 12 графиков" }
        return expressions.map { expression ->
            GraphCurve(
                expression = expression,
                points = sampleFunction(expression, viewport, targetSegments, maxPointsPerCurve)
            )
        }
    }

    fun sampleFunction(
        expression: String,
        viewport: GraphViewport,
        targetSegments: Int = 240,
        maxPoints: Int = 5000
    ): List<GraphPoint> {
        require(expression.isNotBlank()) { "Введите функцию" }
        require(targetSegments in 16..2000 && maxPoints in 64..10000)
        val evaluator = MathExpression.parse(normalizeFunctionExpression(expression))
        val cache = HashMap<Long, Double?>()
        fun evaluate(x: Double): Double? {
            val key = java.lang.Double.doubleToLongBits(x)
            if (cache.containsKey(key)) return cache[key]
            val value = runCatching {
                evaluator.evaluate(mapOf("x" to x))
            }.getOrNull()?.takeIf { it.isFinite() && abs(it) <= 1.0e12 }
            cache[key] = value
            return value
        }

        val output = ArrayList<GraphPoint>(minOf(maxPoints, targetSegments * 2))
        val pixelYScale = 900.0 / (viewport.yMax - viewport.yMin)
        val maxVisibleJump = (viewport.yMax - viewport.yMin) * 1.8
        fun append(x: Double, y: Double?) {
            if (output.size >= maxPoints) return
            val previous = output.lastOrNull()
            if (previous != null && previous.y != null && y != null) {
                val jump = abs(y - previous.y)
                if (jump > maxVisibleJump || jump * pixelYScale > 1600.0) {
                    output += GraphPoint(x, null)
                }
            }
            output += GraphPoint(x, y?.takeIf { it in (viewport.yMin - maxVisibleJump)..(viewport.yMax + maxVisibleJump) })
        }

        fun refine(x0: Double, y0: Double?, x1: Double, y1: Double?, depth: Int) {
            if (output.size >= maxPoints - 2) {
                append(x1, y1)
                return
            }
            val xm = x0 + (x1 - x0) * 0.5
            val ym = evaluate(xm)
            val shouldSplit = if (depth < 10 && output.size < maxPoints - 2) {
                if (y0 == null || y1 == null || ym == null) {
                    depth < 5 && (y0 != null || y1 != null || ym != null)
                } else {
                    val linearMid = (y0 + y1) * 0.5
                    val errorPx = abs(ym - linearMid) * pixelYScale
                    val visible = max(abs(y0), max(abs(y1), abs(ym))) < max(abs(viewport.yMin), abs(viewport.yMax)) * 40.0 + 100.0
                    visible && errorPx > 1.4
                }
            } else false
            if (shouldSplit) {
                refine(x0, y0, xm, ym, depth + 1)
                refine(xm, ym, x1, y1, depth + 1)
            } else {
                append(x1, y1)
            }
        }

        val initialCount = targetSegments.coerceAtMost(600)
        var x0 = viewport.xMin
        var y0 = evaluate(x0)
        append(x0, y0)
        for (index in 1..initialCount) {
            val x1 = viewport.xMin + (viewport.xMax - viewport.xMin) * index / initialCount
            val y1 = evaluate(x1)
            refine(x0, y0, x1, y1, 0)
            x0 = x1
            y0 = y1
            if (output.size >= maxPoints) break
        }
        return output
    }

    /** Marching-squares contour for an implicit equation F(x,y)=0. */
    fun sampleImplicit(
        expression: String,
        viewport: GraphViewport,
        columns: Int = 80,
        rows: Int = 80
    ): List<GraphSegment> {
        require(expression.isNotBlank())
        require(columns in 8..240 && rows in 8..240)
        val engine = MathExpression.parse(normalizeImplicitExpression(expression))
        val dx = (viewport.xMax - viewport.xMin) / columns
        val dy = (viewport.yMax - viewport.yMin) / rows
        fun value(x: Double, y: Double): Double? = runCatching {
            engine.evaluate(mapOf("x" to x, "y" to y))
        }.getOrNull()?.takeIf { it.isFinite() && abs(it) < 1.0e12 }

        val segments = ArrayList<GraphSegment>()
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val left = viewport.xMin + column * dx
                val top = viewport.yMin + row * dy
                val p = listOf(
                    left to top,
                    (left + dx) to top,
                    (left + dx) to (top + dy),
                    left to (top + dy)
                )
                val v = p.map { value(it.first, it.second) }
                if (v.any { it == null }) continue
                val edges = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 0)
                val crossings = edges.mapNotNull { (a, b) ->
                    val va = v[a]!!
                    val vb = v[b]!!
                    if ((va < 0.0) == (vb < 0.0)) null
                    else {
                        val t = (va / (va - vb)).coerceIn(0.0, 1.0)
                        (p[a].first + (p[b].first - p[a].first) * t) to
                            (p[a].second + (p[b].second - p[a].second) * t)
                    }
                }
                if (crossings.size >= 2) {
                    for (i in 0 until crossings.size - 1 step 2) {
                        val a = crossings[i]
                        val b = crossings[i + 1]
                        segments += GraphSegment(a.first, a.second, b.first, b.second)
                    }
                }
            }
        }
        return segments
    }

    /** Grid mask for F(x,y) relation 0; cells are suitable for translucent Canvas fills. */
    fun sampleInequality(
        expression: String,
        relation: String,
        viewport: GraphViewport,
        columns: Int = 64,
        rows: Int = 64
    ): List<GraphRegionCell> {
        require(relation in setOf("<", "<=", ">", ">=")) { "Неизвестный знак неравенства" }
        require(columns in 8..160 && rows in 8..160)
        val engine = MathExpression.parse(expression)
        val dx = (viewport.xMax - viewport.xMin) / columns
        val dy = (viewport.yMax - viewport.yMin) / rows
        val result = ArrayList<GraphRegionCell>()
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val x = viewport.xMin + (column + 0.5) * dx
                val y = viewport.yMin + (row + 0.5) * dy
                val value = runCatching {
                    engine.evaluate(mapOf("x" to x, "y" to y))
                }.getOrNull()?.takeIf { it.isFinite() } ?: continue
                val included = when (relation) {
                    "<" -> value < 0.0
                    "<=" -> value <= 0.0
                    ">" -> value > 0.0
                    else -> value >= 0.0
                }
                if (included) {
                    result += GraphRegionCell(
                        viewport.xMin + column * dx,
                        viewport.yMin + row * dy,
                        viewport.xMin + (column + 1) * dx,
                        viewport.yMin + (row + 1) * dy,
                        true
                    )
                }
            }
        }
        return result
    }
    private fun normalizeFunctionExpression(source: String): String {
        val text = source.trim()
        val equals = text.indexOf('=')
        if (equals < 0) return text
        require(text.indexOf('=', equals + 1) < 0) { "В функции допускается только один знак =" }
        val left = text.substring(0, equals).trim()
        val right = text.substring(equals + 1).trim()
        require(left == "y" || Regex("[A-Za-z][A-Za-z0-9]*\\\\(x\\\\)").matches(left)) {
            "Функцию вводите как y = выражение или f(x) = выражение"
        }
        require(right.isNotEmpty()) { "После знака = нет выражения" }
        return right
    }

    private fun normalizeImplicitExpression(source: String): String {
        val text = source.trim()
        val equals = text.indexOf('=')
        if (equals < 0) return text
        require(text.indexOf('=', equals + 1) < 0) { "В неявном уравнении допускается один знак =" }
        val left = text.substring(0, equals).trim()
        val right = text.substring(equals + 1).trim()
        require(left.isNotEmpty() && right.isNotEmpty()) { "Заполните обе части уравнения" }
        return "(" + left + ")-(" + right + ")"
    }

    fun findIntersections(
        firstExpression: String,
        secondExpression: String,
        viewport: GraphViewport,
        sampleCount: Int = 1200
    ): List<GraphIntersection> {
        require(sampleCount in 100..5000)
        val first = MathExpression.parse(normalizeFunctionExpression(firstExpression))
        val second = MathExpression.parse(normalizeFunctionExpression(secondExpression))
        fun valuesAt(x: Double): Pair<Double, Double>? = runCatching {
            val variables = mapOf("x" to x)
            val a = first.evaluate(variables)
            val b = second.evaluate(variables)
            if (a.isFinite() && b.isFinite()) a to b else null
        }.getOrNull()
        fun differenceAt(x: Double): Double? =
            valuesAt(x)?.let { (a, b) -> (a - b).takeIf { it.isFinite() } }

        val step = (viewport.xMax - viewport.xMin) / sampleCount
        val differences = Array(sampleCount + 1) { index ->
            differenceAt(viewport.xMin + index * step)
        }
        val roots = ArrayList<Double>()
        fun addRoot(x: Double) {
            val pair = valuesAt(x) ?: return
            if (abs(pair.first - pair.second) <= 1.0e-6 &&
                roots.none { abs(it - x) < step * 0.2 }
            ) roots += x
        }

        for (index in 0 until sampleCount) {
            val a = differences[index] ?: continue
            val b = differences[index + 1] ?: continue
            val x0 = viewport.xMin + index * step
            val x1 = x0 + step
            if (abs(a) < 1.0e-10) addRoot(x0)
            if ((a < 0.0) != (b < 0.0)) {
                var low = x0
                var high = x1
                var lowValue = a
                repeat(60) {
                    val middle = (low + high) * 0.5
                    val middleValue = differenceAt(middle)
                    if (middleValue == null) high = middle
                    else if ((middleValue < 0.0) == (lowValue < 0.0)) {
                        low = middle
                        lowValue = middleValue
                    } else high = middle
                }
                addRoot((low + high) * 0.5)
            }
        }

        // A tangent intersection has no sign change, so minimize |f(x)-g(x)| near local minima.
        for (index in 1 until sampleCount) {
            val before = differences[index - 1] ?: continue
            val current = differences[index] ?: continue
            val after = differences[index + 1] ?: continue
            if (abs(current) > abs(before) || abs(current) > abs(after) || abs(current) > 0.02) continue
            var low = viewport.xMin + (index - 1) * step
            var high = viewport.xMin + (index + 1) * step
            repeat(40) {
                val leftProbe = low + (high - low) / 3.0
                val rightProbe = high - (high - low) / 3.0
                val leftValue = abs(differenceAt(leftProbe) ?: Double.POSITIVE_INFINITY)
                val rightValue = abs(differenceAt(rightProbe) ?: Double.POSITIVE_INFINITY)
                if (leftValue <= rightValue) high = rightProbe else low = leftProbe
            }
            addRoot((low + high) * 0.5)
        }

        if (abs(differences.last() ?: Double.POSITIVE_INFINITY) < 1.0e-10) addRoot(viewport.xMax)
        return roots.sorted().mapNotNull { x ->
            valuesAt(x)?.let { (a, b) -> GraphIntersection(x, (a + b) * 0.5) }
        }
    }

    fun evaluateFunction(expression: String, x: Double): Double? = runCatching {
        MathExpression.parse(normalizeFunctionExpression(expression)).evaluate(mapOf("x" to x))
    }.getOrNull()?.takeIf { it.isFinite() }

}
