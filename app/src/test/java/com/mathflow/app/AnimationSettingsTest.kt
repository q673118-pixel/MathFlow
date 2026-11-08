package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationSettingsTest {
    @Test
    fun buttonColorEffectIsDisabledByDefault() {
        assertFalse(KeyAnimationSettings().colorEffectEnabled)
    }

    @Test
    fun bottomNavigationDefaultsToFluidPill() {
        val settings = BottomNavAnimationSettings()
        assertTrue(settings.enabled)
        assertEquals(BottomNavAnimationStyle.FLUID_PILL, settings.style)
    }

    @Test
    fun bottomNavigationSettingsAreClamped() {
        val normalized = BottomNavAnimationSettings(
            durationMs = 5000,
            intensity = 1.5f,
            iconScale = -0.5f,
            opacity = 2f
        ).normalized()

        assertEquals(1000, normalized.durationMs)
        assertEquals(1f, normalized.intensity)
        assertEquals(0f, normalized.iconScale)
        assertEquals(1f, normalized.opacity)
    }

    @Test
    fun unknownBottomNavigationStyleFallsBackSafely() {
        assertEquals(
            BottomNavAnimationStyle.FLUID_PILL,
            BottomNavAnimationStyle.fromId("not_a_real_style")
        )
    }
}
