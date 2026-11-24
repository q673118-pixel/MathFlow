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
    private data class FrameState(
        val position: Float,
        val liftX: Float,
        val liftY: Float,
        val material: Float,
        val deviation: Float,
        val glassPresence: Float
    )

    // Publish one immutable frame snapshot instead of six independent snapshot writes.
    // This limits invalidation to the draw/layer nodes that actually consume motion.
    private var frameState by mutableStateOf(
        FrameState(initialIndex.toFloat(), 0f, 0f, 0f, 0f, 0f)
    )
    val position: Float get() = frameState.position
    val liftX: Float get() = frameState.liftX
    val liftY: Float get() = frameState.liftY
    val material: Float get() = frameState.material
    val deviation: Float get() = frameState.deviation
    val glassPresence: Float get() = frameState.glassPresence

    var isFlat by mutableStateOf(true)
        private set

    /** Width of one navigation cell in dp; squash sampling matches RX Playa's dp model. */
    var tabStepDp: Float = 0f
    var reduceMotion: Boolean = false

    private var targetIndex = initialIndex.toDouble()
    private val travelSpring = SpringState(targetIndex, 0.0)
    private var travelStart = targetIndex
    private var travelActive = false

    private var dragging = false
    private var dragTarget = targetIndex
    private var dragPosition = targetIndex

    private var lifted = false
    private val liftXSpring = SpringState(0.0, 0.0)
    private val liftYSpring = SpringState(0.0, 0.0)
    private val materialSpring = SpringState(0.0, 0.0)

    private var handoff = 1.0
    private var squashValue = 0.0
    private var travelDirection = 0.0
    private var easedDirection = 0.0

    // Primitive ring buffer: the acceleration window is sampled every frame.
    // Avoid Pair/ArrayList allocation and removeAll scans on the animation hot path.
    // 128 samples cover the full 300 ms window up to 426 Hz.
    private val samplePositions = DoubleArray(SAMPLE_CAPACITY)
    private val sampleTimes = DoubleArray(SAMPLE_CAPACITY)
    private var sampleStart = 0
    private var sampleCount = 0

    private var frameJob: Job? = null
    private var generation = 0

    fun animateTo(index: Int) {
        if (reduceMotion) {
            snapTo(index)
            return
        }
        if (index.toDouble() == targetIndex && !dragging) return
        targetIndex = index.toDouble()
        travelStart = travelSpring.position
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
        travelSpring.position = targetIndex
        travelSpring.velocity = 0.0
        travelStart = targetIndex
        travelActive = false
        dragging = false
        dragPosition = targetIndex
        dragTarget = targetIndex
        lifted = false
        liftXSpring.position = 0.0
        liftXSpring.velocity = 0.0
        liftYSpring.position = 0.0
        liftYSpring.velocity = 0.0
        materialSpring.position = 0.0
        materialSpring.velocity = 0.0
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
        travelSpring.velocity = 0.0
        dragPosition = travelSpring.position
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
        travelSpring.position = dragPosition
        travelSpring.velocity = 0.0
        travelStart = travelSpring.position
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
                    // Preserve RX timing for normal and dropped frames. If the frame
                    // clock resumes after a long lifecycle pause, skip that suspended
                    // interval instead of integrating thousands of stale substeps.
                    val delta = if (rawDelta > MAX_RESUMED_FRAME_GAP) 0.0
                        else rawDelta.coerceAtLeast(0.0)
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
            LiquidGlassPillPhysics.springStepInPlace(
                travelSpring, targetIndex, dt, TRAVEL_STIFFNESS, TRAVEL_DAMPING
            )
            travelSettled = abs(travelSpring.position - targetIndex) < 0.003 &&
                abs(travelSpring.velocity) < 0.05
            if (travelSettled) {
                travelSpring.position = targetIndex
                travelSpring.velocity = 0.0
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
        LiquidGlassPillPhysics.springStepInPlace(
            liftXSpring, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_X
        )
        LiquidGlassPillPhysics.stepLiftInPlace(liftXSpring, liftTarget)
        LiquidGlassPillPhysics.springStepInPlace(
            liftYSpring, liftTarget, dt, LIFT_STIFFNESS, LIFT_DAMPING_Y
        )
        LiquidGlassPillPhysics.stepLiftInPlace(liftYSpring, liftTarget)
        LiquidGlassPillPhysics.springStepInPlace(
            materialSpring, liftTarget, dt, MATERIAL_STIFFNESS, MATERIAL_DAMPING
        )
        LiquidGlassPillPhysics.stepLiftInPlace(materialSpring, liftTarget)

        val liftSettled = !lifted &&
            liftXSpring.position == 0.0 && liftYSpring.position == 0.0 &&
            materialSpring.position == 0.0

        if (travelActive && travelSettled && liftSettled && !dragging) {
            travelActive = false
        }

        val drawnPosition = if (dragging) dragPosition else travelSpring.position
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
        return (1.0 - abs(targetIndex - travelSpring.position) / distance).coerceIn(0.0, 1.0)
    }

    private fun recordSample(positionDp: Double, time: Double) {
        if (sampleCount == SAMPLE_CAPACITY) {
            sampleStart = (sampleStart + 1) % SAMPLE_CAPACITY
            sampleCount--
        }
        val writeIndex = (sampleStart + sampleCount) % SAMPLE_CAPACITY
        samplePositions[writeIndex] = positionDp
        sampleTimes[writeIndex] = time
        sampleCount++

        val cutoff = time - ACCELERATION_WINDOW
        while (sampleCount > 0 && sampleTimes[sampleStart] < cutoff) {
            sampleStart = (sampleStart + 1) % SAMPLE_CAPACITY
            sampleCount--
        }
    }

    private fun averageAcceleration(): Double {
        if (sampleCount < 3) return 0.0

        var previousVelocity = 0.0
        var previousMidpoint = 0.0
        var hasPreviousVelocity = false
        var totalAcceleration = 0.0
        var accelerationCount = 0

        for (i in 1 until sampleCount) {
            val previousIndex = (sampleStart + i - 1) % SAMPLE_CAPACITY
            val currentIndex = (sampleStart + i) % SAMPLE_CAPACITY
            val elapsed = sampleTimes[currentIndex] - sampleTimes[previousIndex]
            if (elapsed <= 0.0) continue

            val velocity = (samplePositions[currentIndex] - samplePositions[previousIndex]) / elapsed
            val midpoint = (sampleTimes[currentIndex] + sampleTimes[previousIndex]) * 0.5
            if (hasPreviousVelocity) {
                val velocityElapsed = midpoint - previousMidpoint
                if (velocityElapsed > 0.0) {
                    totalAcceleration += (velocity - previousVelocity) / velocityElapsed
                    accelerationCount++
                }
            }
            previousVelocity = velocity
            previousMidpoint = midpoint
            hasPreviousVelocity = true
        }
        return if (accelerationCount == 0) 0.0 else totalAcceleration / accelerationCount
    }

    private fun clearSamples() {
        sampleStart = 0
        sampleCount = 0
    }

    private fun publish() {
        frameState = FrameState(
            position = (if (dragging) dragPosition else travelSpring.position).toFloat(),
            liftX = liftXSpring.position.toFloat(),
            liftY = liftYSpring.position.toFloat(),
            material = materialSpring.position.coerceIn(0.0, 1.0).toFloat(),
            deviation = squashValue.toFloat(),
            glassPresence = (1.0 - handoff).coerceIn(0.0, 1.0).toFloat()
        )
    }

    private fun directionOf(distance: Double): Double =
        if (abs(distance) < 0.000001) 0.0 else sign(distance)

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

        const val MAX_RESUMED_FRAME_GAP = 0.25
        const val SAMPLE_CAPACITY = 128
    }
}
