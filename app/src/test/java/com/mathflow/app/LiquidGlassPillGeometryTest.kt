package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Test

class LiquidGlassPillGeometryTest {
    @Test
    fun ltrAndRtlUseOppositeVisualTabOrder() {
        assertEquals(20f, LiquidGlassPillGeometry.center(0f, 40f, 48f, 5, false).first, 0f)
        assertEquals(180f, LiquidGlassPillGeometry.center(0f, 40f, 48f, 5, true).first, 0f)
        assertEquals(100f, LiquidGlassPillGeometry.center(2f, 40f, 48f, 5, false).first, 0f)
        assertEquals(100f, LiquidGlassPillGeometry.center(2f, 40f, 48f, 5, true).first, 0f)
    }

    @Test
    fun liftedPillPreservesCellAspectRatio() {
        val lifted = LiquidGlassPillGeometry.liftedSize(
            restWidth = 60f,
            restHeight = 48f,
            topInset = 5f,
            bottomInset = 5f,
            growth = 9f
        )
        assertEquals(67f, lifted.second, 0f)
        assertEquals(60f * 67f / 48f, lifted.first, 1e-5f)
    }

    @Test
    fun dragMapsPhysicalDistanceToLogicalTabsInBothDirections() {
        assertEquals(
            2f,
            LiquidGlassPillGeometry.dragTargetIndex(1f, 48f, 48f, 5, false),
            0f
        )
        assertEquals(
            0f,
            LiquidGlassPillGeometry.dragTargetIndex(1f, 48f, 48f, 5, true),
            0f
        )
        assertEquals(
            4f,
            LiquidGlassPillGeometry.dragTargetIndex(3.5f, 96f, 48f, 5, false),
            0f
        )
    }

    @Test
    fun dragWithInvalidStepDoesNotDivideByZero() {
        assertEquals(
            4f,
            LiquidGlassPillGeometry.dragTargetIndex(2f, 100f, 0f, 5, false),
            0f
        )
    }

    @Test
    fun zeroHeightDoesNotDivideByZero() {
        assertEquals(30f to 0f, LiquidGlassPillGeometry.liftedSize(30f, 0f, 5f, 5f, 9f))
    }
}
