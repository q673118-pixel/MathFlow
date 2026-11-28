package com.mathflow.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathFlowUiStyleTest {
    @Test
    fun onlySixCuratedStylesRemainAvailable() {
        assertEqualsExpected(true, MathFlowUiStyle.entries.size == 6)
        assertEqualsExpected(
            true,
            MathFlowUiStyle.entries.map { it.id }.toSet() ==
                setOf("material_you", "liquid_glass", "fluent", "one_ui", "minimal", "soft_ui")
        )
    }

    @Test
    fun retiredStyleIdsFallBackToMaterialYou() {
        listOf("pixel", "neo", "skeuomorphic", "retro_grid").forEach { oldId ->
            assertEqualsExpected(true, MathFlowUiStyle.fromId(oldId) == MathFlowUiStyle.MATERIAL_YOU)
        }
    }

    @Test
    fun onlyLiquidGlassStyleUsesRefractiveSelectionPill() {
        assertFalse(MathFlowUiStyle.MATERIAL_YOU.liquidGlassPill)
        assertFalse(MathFlowUiStyle.MATERIAL_YOU.liquidGlassNavigation)
        assertTrue(MathFlowUiStyle.LIQUID_GLASS.liquidGlassPill)
        assertTrue(MathFlowUiStyle.LIQUID_GLASS.liquidGlassNavigation)
        listOf(
            MathFlowUiStyle.FLUENT,
            MathFlowUiStyle.ONE_UI,
            MathFlowUiStyle.MINIMAL,
            MathFlowUiStyle.SOFT_UI
        ).forEach { style ->
            assertFalse(style.liquidGlassPill)
            assertFalse(style.liquidGlassNavigation)
        }
    }

    @Test
    fun onlyGlobalLiquidGlassStyleEnablesTheRefractiveNavigationRenderer() {
        MathFlowUiStyle.entries.forEach { style ->
            assertEqualsExpected(
                expected = style == MathFlowUiStyle.LIQUID_GLASS,
                actual = style.usesRefractiveNavigation(backdropAvailable = true)
            )
        }
    }

    @Test
    fun liquidGlassKeepsTextBearingSurfacesOpaque() {
        assertTrue(MathFlowUiStyle.LIQUID_GLASS.surfaceAlpha == 1f)
    }

    @Test
    fun missingBackdropDisablesRefractionWithoutChangingTheGlobalStyle() {
        MathFlowUiStyle.entries.forEach { style ->
            assertFalse(style.usesRefractiveNavigation(backdropAvailable = false))
        }
        assertTrue(MathFlowUiStyle.LIQUID_GLASS.liquidGlassNavigation)
    }

    private fun assertEqualsExpected(expected: Boolean, actual: Boolean) {
        if (expected) assertTrue(actual) else assertFalse(actual)
    }
}
