package com.mathflow.app

import java.util.Locale
import kotlin.math.abs

data class ParabolaTransformationResult(
    val formula: String,
    val steps: List<String>
)

/**
 * Explains transformations from the parent graph y = x² to
 * y = a(x - h)² + k.
 */
object ParabolaTransformation {
    fun explain(a: Double, h: Double, k: Double): ParabolaTransformationResult {
        require(a.isFinite() && h.isFinite() && k.isFinite())
        require(a != 0.0) { "Коэффициент a не должен быть равен нулю" }

        fun number(value: Double): String =
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.').let {
                if (it == "-0" || it.isEmpty()) "0" else it
            }

        val formula = "y = ${number(a)}(x - (${number(h)}))² + (${number(k)})"
        val steps = mutableListOf<String>()
        if (a < 0.0) {
            steps += "Отразить параболу y = x² относительно оси Ox: ветви будут направлены вниз."
        }
        when {
            abs(a) > 1.0 -> steps += "Растянуть график по вертикали в ${number(abs(a))} раза: парабола станет уже."
            abs(a) < 1.0 -> steps += "Сжать график по вертикали в ${number(1.0 / abs(a))} раза: парабола станет шире."
            else -> steps += "Ширина параболы не меняется: |a| = 1."
        }
        if (h > 0.0) steps += "Сдвинуть график вправо на ${number(h)}."
        if (h < 0.0) steps += "Сдвинуть график влево на ${number(abs(h))}."
        if (h == 0.0) steps += "По горизонтали сдвига нет."
        if (k > 0.0) steps += "Сдвинуть график вверх на ${number(k)}."
        if (k < 0.0) steps += "Сдвинуть график вниз на ${number(abs(k))}."
        if (k == 0.0) steps += "По вертикали сдвига нет."
        return ParabolaTransformationResult(formula, steps)
    }
}
