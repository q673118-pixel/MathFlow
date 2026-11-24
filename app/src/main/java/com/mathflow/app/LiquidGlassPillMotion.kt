package com.mathflow.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

/**
 * Frame-driven selection motion for the refractive navigation indicator.
 *
 * The model is deliberately independent from Compose layout and rendering:
 * it publishes only the values consumed by graphicsLayer/draw lambdas.
 * Navigation selection remains owned by BottomNav and its caller.
 */
@Stable
internal class LiquidGlassPillMotion(
    private val scope: CoroutineScope,
    initialIndex: Int
) {
    var position by mutableFloatStateOf(initialIndex.toFloat())
        private set
    var liftX by mutableFloatStateOf(0f)
        private set
    var liftY by mutableFloatStateOf(0f)
        private set
    var material by mutableFloatStateOf(0f)
        private set
    var deviation by mutableFloatStateOf(0f)
        private set
    var glassPresence by mutableFloatStateOf(0f)
        private set
    var isFlat by mutableStateOf(true)
        private set

    /** Width of one navigation cell in dp; squash sampling matches RX Playa's dp model. */
    var tabStepDp: Float = 0f
    var reduceMotion: Boolean = false

    private var targetIndex = initialIndex.toDouble()
    private var travelPosition = targetIndex
    private var travelVelocity = 0.0
    private var travelStart = targetIndex
    private var travelActive = false

    private var dragging = false
    private var dragTarget = targetIndex
    private var dragPosition = targetIndex

    private var lifted = false
    private var liftXPosition = 0.0
    private var liftXVelocity = 0.0
    private var liftYPosition = 0.0
    private var liftYVelocity = 0.0
    private var materialPosition = 0.0
    private var materialVelocity = 0.0

    private var handoff = 1.0
    private var squashValue = 0.0
    private var travelDirection = 0.0
    private var easedDirection = 0.0

    // Keep the complete 300 ms window at every refresh rate. A fixed 48-frame
    // ring silently shortened this window on 240 Hz displays.
    private val sampleHistory = ArrayList<Pair<Double, Double>>()

    private var frameJob: Job? = null
    private var generation = 0

    fun animateTo(index: Int) {
        if (reduceMotion) {
            snapTo(index)
            return
        }
        if (index.toDouble() == targetIndex && !dragging) return
        targetIndex = index.toDouble()
        travelStart = travelPosition
        travelActive = true
        lifted = true
        travelDirection = directionOf(targetIndex - travelStart)
        isFlat = false
        startFrames()
    }

    fun snapTo(index: Int) {
        generation++
        frameJob?.cancel()
        frameJob = null
        targetIndex = index.toDouble()
        travelPosition = targetIndex
        travelVelocity = 0.0
        travelStart = targetIndex
        travelActive = false
        dragging = false
        dragPosition = targetIndex
        dragTarget = targetIndex
        lifted = false
        liftXPosition = 0.0
        liftXVelocity = 0.0
        liftYPosition = 0.0
        liftYVelocity = 0.0
        materialPosition = 0.0
        materialVelocity = 0.0
        handoff = 1.0
        squashValue = 0.0
        travelDirection = 0.0
        easedDirection = 0.0
        clearSamples()
        publish()
        isFlat = true
    }

    fun startDrag() {
        dragging = true
        travelActive = false
        lifted = !reduceMotion
        travelVelocity = 0.0
        dragPosition = travelPosition
        dragTarget = dragPosition
        travelDirection = 0.0
        clearSamples()
        isFlat = false
        startFrames()
    }

    fun dragTo(indexPosition: Float) {
        if (dragging) {
            dragTarget = indexPosition.toDouble()
            if (reduceMotion) dragPosition = dragTarget
        }
    }

    fun release(index: Int) {
        if (!dragging) return
        if (reduceMotion) {
            snapTo(index)
            return
        }
        dragging = false
        targetIndex = index.toDouble()
        travelPosition = dragPosition
        travelVelocity = 0.0
        travelStart = travelPosition
        travelActive = true
        travelDirection = directionOf(targetIndex - travelStart)
        startFrames()
    }

    private fun startFrames() {
        if (frameJob?.isActive == true) return
        val token = ++generation
        frameJob = scope.launch {
            var previousFrame = 0L
            try {
                while (true) {
                    val frame = withFrameNanos { it }
                    val rawDelta = if (previousFrame == 0L) 0.0 else (frame - previousFrame) / 1_000_000_000.0
                    previousFrame = frame
                    val delta = rawDelta.coerceIn(0.0, MAX_FRAME_DELTA)
                    if (!advance(delta, frame / 1_000_000_000.0)) break
                }
            } finally {
                if (generation == token) frameJob = null
            }
        }
    }

    private fun advance(deltaSeconds: Double, nowSeconds: Double): Boolean {
        val dt = deltaSeconds
        var travelSettled = !travelActive
        if (travelActive) {
            val result = springAxis(
                travelPosition, travelVelocity, targetIndex, dt,
                TRAVEL_STIFFNESS,
                TRAVEL_DAMPING
            )
            travelPosition = result.first
            travelVelocity = result.second
            travelSettled = abs(travelPosition - targetIndex) < 0.003 &&
                abs(travelVelocity) < 0.05
            if (travelSettled) {
                travelPosition = targetIndex
                travelVelocity = 0.0
            }
        }

        if (dragging) {
            if (reduceMotion) {
                dragPosition = dragTarget
            } else {
                val follow = 1.0 - exp(-dt / DRAG_FOLLOW_TIME)
                dragPosition += (dragTarget - dragPosition) * follow
            }
        }

        if (!dragging && (travelSettled || travelProgress() >= LIFT_RELEASE_POINT)) {
            lifted = false
        }

        val liftTarget = if (lifted) 1.0 else 0.0
        springAxis(liftXPosition, liftXVelocity, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_X)
            .also { liftXPosition = snapAxis(it, liftTarget).first; liftXVelocity = snapAxis(it, liftTarget).second }
        springAxis(liftYPosition, liftYVelocity, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_Y)
            .also { liftYPosition = snapAxis(it, liftTarget).first; liftYVelocity = snapAxis(it, liftTarget).second }
        springAxis(materialPosition, materialVelocity, liftTarget, dt, MATERIAL_STIFFNESS, MATERIAL_DAMPING)
            .also { materialPosition = snapAxis(it, liftTarget).first; materialVelocity = snapAxis(it, liftTarget).second }

        val liftSettled = !lifted &&
            liftXPosition == 0.0 && liftYPosition == 0.0 && materialPosition == 0.0

        if (travelActive && travelSettled && liftSettled && !dragging) {
            travelActive = false
        }

        val drawnPosition = if (dragging) dragPosition else travelPosition
        if (tabStepDp > 0f) {
            recordSample(drawnPosition * tabStepDp, nowSeconds)
            val raw = (averageAcceleration() * ACCELERATION_GAIN)
                .coerceIn(-MAX_SQUASH, MAX_SQUASH)
            val response = (dt / SQUASH_RESPONSE_TIME).coerceIn(0.0, 1.0)
            squashValue += (raw - squashValue) * response
            if (easedDirection == 0.0) {
                easedDirection = travelDirection
            } else if (easedDirection != travelDirection) {
                val blend = 1.0 - exp(-dt / DIRECTION_BLEND_TIME)
                easedDirection += (travelDirection - easedDirection) * blend
                if (abs(travelDirection - easedDirection) < 0.01) easedDirection = travelDirection
            }
            if (easedDirection != 0.0) {
                squashValue = squashValue * (1.0 - abs(easedDirection)) -
                    easedDirection * abs(squashValue)
            }
        }

        val handoffTarget = if (lifted) 0.0 else 1.0
        val handoffTime = if (handoffTarget > handoff) HANDOFF_IN_TIME else HANDOFF_OUT_TIME
        handoff += (handoffTarget - handoff) * (1.0 - exp(-dt / handoffTime))
        if (abs(handoffTarget - handoff) < 0.002) handoff = handoffTarget

        val settled = !travelActive && !dragging && liftSettled &&
            abs(squashValue) < 0.0005 && handoff >= 1.0
        if (settled) {
            squashValue = 0.0
            travelDirection = 0.0
            easedDirection = 0.0
            clearSamples()
        }

        publish()
        isFlat = settled
        return !settled
    }

    private fun travelProgress(): Double {
        val distance = abs(targetIndex - travelStart)
        if (distance < 0.000001) return 1.0
        return (1.0 - abs(targetIndex - travelPosition) / distance).coerceIn(0.0, 1.0)
    }

    private fun recordSample(positionDp: Double, time: Double) {
        sampleHistory.add(positionDp to time)
        val cutoff = time - ACCELERATION_WINDOW
        sampleHistory.removeAll { it.second < cutoff }
    }

    private fun averageAcceleration(): Double {
        if (sampleHistory.size < 3) return 0.0
        val velocities = ArrayList<Pair<Double, Double>>(sampleHistory.size)
        for (i in 1 until sampleHistory.size) {
            val previous = sampleHistory[i - 1]
            val current = sampleHistory[i]
            val elapsed = current.second - previous.second
            if (elapsed <= 0.0) continue
            velocities.add(
                (current.first - previous.first) / elapsed to
                    (current.second + previous.second) * 0.5
            )
        }
        if (velocities.size < 2) return 0.0
        var totalAcceleration = 0.0
        var count = 0
        for (i in 1 until velocities.size) {
            val elapsed = velocities[i].second - velocities[i - 1].second
            if (elapsed <= 0.0) continue
            totalAcceleration += (velocities[i].first - velocities[i - 1].first) / elapsed
            count++
        }
        return if (count == 0) 0.0 else totalAcceleration / count
    }

    private fun clearSamples() {
        sampleHistory.clear()
    }

    private fun publish() {
        position = (if (dragging) dragPosition else travelPosition).toFloat()
        liftX = liftXPosition.toFloat()
        liftY = liftYPosition.toFloat()
        material = materialPosition.coerceIn(0.0, 1.0).toFloat()
        deviation = squashValue.toFloat()
        glassPresence = (1.0 - handoff).coerceIn(0.0, 1.0).toFloat()
    }

    private fun directionOf(distance: Double): Double =
        if (abs(distance) < 0.000001) 0.0 else sign(distance)

    private fun springAxis(
        position: Double,
        velocity: Double,
        target: Double,
        elapsed: Double,
        stiffness: Double,
        damping: Double
    ): Pair<Double, Double> {
        return LiquidGlassPillPhysics.springStep(
            position, velocity, target, elapsed, stiffness, damping
        )
    }

    private fun snapAxis(
        result: Pair<Double, Double>,
        target: Double
    ): Pair<Double, Double> =
        LiquidGlassPillPhysics.stepLift(result, target)

    private companion object {
        const val TRAVEL_STIFFNESS = 280.0
        const val TRAVEL_DAMPING = 31.4
        const val LIFT_STIFFNESS = 250.0
        const val LIFT_DAMPING_X = 19.0
        const val LIFT_DAMPING_Y = 22.1
        const val MATERIAL_STIFFNESS = 1000.0
        const val MATERIAL_DAMPING = 63.3

        const val LIFT_RELEASE_POINT = 0.92
        const val DRAG_FOLLOW_TIME = 0.05
        const val HANDOFF_IN_TIME = 0.09
        const val HANDOFF_OUT_TIME = 0.05
        const val DIRECTION_BLEND_TIME = 0.25
        const val ACCELERATION_WINDOW = 0.30
        const val ACCELERATION_GAIN = 0.00007
        const val MAX_SQUASH = 0.12
        const val SQUASH_RESPONSE_TIME = 0.18

        const val MAX_FRAME_DELTA = 0.05
    }
}
