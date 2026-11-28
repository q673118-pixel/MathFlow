package com.mathflow.app

import kotlin.math.abs

/**
 * Deterministic numerical core for the RX-style pill springs.
 *
 * Kept free of Compose state and frame clocks so the integrator can be tested
 * independently from rendering and coroutine scheduling.
 */
internal class SpringState(
    var position: Double,
    var velocity: Double
)

internal object LiquidGlassPillPhysics {
    private const val MAX_STEP_SECONDS = 1.0 / 240.0
    private const val POSITION_SNAP_EPSILON = 0.0008
    private const val VELOCITY_SNAP_EPSILON = 0.01

    /** Semi-implicit Euler integration with fixed maximum substeps. */
    fun springStep(
        position: Double,
        velocity: Double,
        target: Double,
        elapsedSeconds: Double,
        stiffness: Double,
        damping: Double
    ): Pair<Double, Double> {
        if (!elapsedSeconds.isFinite() || elapsedSeconds <= 0.0) {
            return position to velocity
        }
        if (!position.isFinite() || !velocity.isFinite() || !target.isFinite() ||
            !stiffness.isFinite() || !damping.isFinite() ||
            stiffness < 0.0 || damping < 0.0
        ) {
            return position to velocity
        }

        var remaining = elapsedSeconds
        var x = position
        var v = velocity
        while (remaining > 0.0) {
            val step = minOf(remaining, MAX_STEP_SECONDS)
            val acceleration = -stiffness * (x - target) - damping * v
            v += acceleration * step
            x += v * step
            remaining -= step
        }
        return x to v
    }

    /** Allocation-free spring update for the per-frame animation path. */
    fun springStepInPlace(
        state: SpringState,
        target: Double,
        elapsedSeconds: Double,
        stiffness: Double,
        damping: Double
    ) {
        if (!elapsedSeconds.isFinite() || elapsedSeconds <= 0.0 ||
            !state.position.isFinite() || !state.velocity.isFinite() || !target.isFinite() ||
            !stiffness.isFinite() || !damping.isFinite() || stiffness < 0.0 || damping < 0.0
        ) return

        var remaining = elapsedSeconds
        while (remaining > 0.0) {
            val step = minOf(remaining, MAX_STEP_SECONDS)
            val acceleration = -stiffness * (state.position - target) - damping * state.velocity
            state.velocity += acceleration * step
            state.position += state.velocity * step
            remaining -= step
        }
    }

    /** Allocation-free snap for a settled lift axis. */
    fun stepLiftInPlace(state: SpringState, target: Double) {
        if (abs(state.position - target) < POSITION_SNAP_EPSILON &&
            abs(state.velocity) < VELOCITY_SNAP_EPSILON
        ) {
            state.position = target
            state.velocity = 0.0
        }
    }

    /** Snap a nearly settled lift axis to eliminate residual sub-pixel motion. */
    fun stepLift(
        result: Pair<Double, Double>,
        target: Double
    ): Pair<Double, Double> =
        if (abs(result.first - target) < POSITION_SNAP_EPSILON &&
            abs(result.second) < VELOCITY_SNAP_EPSILON
        ) {
            target to 0.0
        } else {
            result
        }
}
