package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidGlassPillPhysicsTest {
    @Test
    fun fixedSubstepsMakeEquivalentFramePartitionsDeterministic() {
        var split = 0.0 to 0.0
        repeat(4) {
            split = LiquidGlassPillPhysics.springStep(
                split.first, split.second, 1.0, 1.0 / 240.0, 280.0, 31.4
            )
        }

        val singleFrame = LiquidGlassPillPhysics.springStep(
            0.0, 0.0, 1.0, 1.0 / 60.0, 280.0, 31.4
        )

        assertEquals(split.first, singleFrame.first, 1e-8)
        assertEquals(split.second, singleFrame.second, 1e-8)
    }

    @Test
    fun allocationFreeStepperMatchesReferenceIntegrator() {
        var reference = 0.0 to 0.0
        val inPlace = SpringState(0.0, 0.0)
        repeat(120) {
            reference = LiquidGlassPillPhysics.springStep(
                reference.first, reference.second, 1.0, 1.0 / 120.0, 280.0, 31.4
            )
            LiquidGlassPillPhysics.springStepInPlace(
                inPlace, 1.0, 1.0 / 120.0, 280.0, 31.4
            )
        }
        assertEquals(reference.first, inPlace.position, 1e-10)
        assertEquals(reference.second, inPlace.velocity, 1e-10)
    }

    @Test
    fun allocationFreeLiftSnapMatchesReferenceSnap() {
        val reference = LiquidGlassPillPhysics.stepLift(0.9995 to 0.005, 1.0)
        val inPlace = SpringState(0.9995, 0.005)
        LiquidGlassPillPhysics.stepLiftInPlace(inPlace, 1.0)
        assertEquals(reference.first, inPlace.position, 0.0)
        assertEquals(reference.second, inPlace.velocity, 0.0)
    }

    @Test
    fun travelSpringConvergesWithoutNonFiniteValues() {
        var state = 0.0 to 0.0
        repeat(180) {
            state = LiquidGlassPillPhysics.springStep(
                state.first, state.second, 1.0, 1.0 / 60.0, 280.0, 31.4
            )
            assertTrue(state.first.isFinite())
            assertTrue(state.second.isFinite())
        }

        assertEquals(1.0, state.first, 0.003)
        assertEquals(0.0, state.second, 0.05)
    }

    @Test
    fun liftAxisSnapsWhenResidualMotionIsBelowThreshold() {
        val snapped = LiquidGlassPillPhysics.stepLift(0.9995 to 0.005, 1.0)
        assertEquals(1.0, snapped.first, 0.0)
        assertEquals(0.0, snapped.second, 0.0)
    }

    @Test
    fun liftAxisKeepsMeaningfulMotion() {
        val moving = 0.99 to 0.02
        assertEquals(moving, LiquidGlassPillPhysics.stepLift(moving, 1.0))
    }

    @Test
    fun invalidOrNegativeElapsedTimeDoesNotCorruptState() {
        val initial = 0.4 to -0.2
        assertEquals(
            initial,
            LiquidGlassPillPhysics.springStep(
                initial.first, initial.second, 1.0, -0.1, 280.0, 31.4
            )
        )
        assertEquals(
            initial,
            LiquidGlassPillPhysics.springStep(
                initial.first, initial.second, 1.0, Double.NaN, 280.0, 31.4
            )
        )
    }
}
