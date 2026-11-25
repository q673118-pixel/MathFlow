package com.mathflow.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathFlowUiStyleTest {
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
