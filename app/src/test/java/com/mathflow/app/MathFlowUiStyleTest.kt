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
    fun missingBackdropDisablesRefractionWithoutChangingTheGlobalStyle() {
        MathFlowUiStyle.entries.forEach { style ->
            assertFalse(style.usesRefractiveNavigation(backdropAvailable = false))
        }
        assertTrue(MathFlowUiStyle.LIQUID_GLASS.glassSurface)
    }

    private fun assertEqualsExpected(expected: Boolean, actual: Boolean) {
        if (expected) assertTrue(actual) else assertFalse(actual)
    }
}
