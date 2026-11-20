package com.mathflow.app

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Global visual systems. A UI style describes component geometry and surface
 * treatment only; it never owns colors. All colors continue to come from Palette,
 * so every style can be combined with every color theme.
 */
internal enum class MathFlowUiStyle(
    val id: String,
    val title: String,
    val description: String,
    val cardRadiusDp: Int,
    val controlRadiusDp: Int,
    val buttonRadiusDp: Int,
    val surfaceAlpha: Float,
    val outlineAlpha: Float,
    val elevationDp: Float,
    val glassSurface: Boolean = false,
    val compact: Boolean = false
) {
    MATERIAL_YOU(
        "material_you", "Material You",
        "Выразительные поверхности и мягкие скругления.",
        24, 16, 22, 1f, 0.0f, 0f
    ),
    LIQUID_GLASS(
        "liquid_glass", "Liquid Glass",
        "Размытие фона, полупрозрачные поверхности и световые границы.",
        28, 19, 24, 0.78f, 0.42f, 2f, glassSurface = true
    ),
    FLUENT(
        "fluent", "Fluent",
        "Лёгкие поверхности, тонкие контуры и сдержанная глубина.",
        18, 13, 16, 0.88f, 0.30f, 1f
    ),
    ONE_UI(
        "one_ui", "One UI",
        "Крупные зоны взаимодействия и спокойная геометрия.",
        26, 20, 22, 1f, 0.12f, 0f
    ),
    MINIMAL(
        "minimal", "Minimal",
        "Плоские поверхности, тонкие разделители и минимум декора.",
        14, 10, 12, 1f, 0.22f, 0f, compact = true
    ),
    SOFT_UI(
        "soft_ui", "Soft UI",
        "Мягкая глубина и деликатное разделение поверхностей.",
        25, 18, 22, 0.96f, 0.10f, 4f
    ),
    PIXEL(
        "pixel", "Pixel",
        "Чёткая геометрия, строгие контуры и компактные элементы.",
        12, 9, 12, 1f, 0.38f, 0f, compact = true
    );

    /**
     * Calculator keys retain generous rounding in every visual system.
     * This token is intentionally independent from generic control geometry.
     */
    val calculatorKeyRadiusDp: Int
        get() = 28

    companion object {
        fun fromId(id: String): MathFlowUiStyle =
            entries.firstOrNull { it.id == id } ?: MATERIAL_YOU
    }
}

/** The active visual system is provided once at the app root. */
internal val LocalMathFlowUiStyle = staticCompositionLocalOf {
    MathFlowUiStyle.MATERIAL_YOU
}
