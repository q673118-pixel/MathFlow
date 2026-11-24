package com.mathflow.app

/** Pure layout math for the RX-style moving navigation pill. */
internal object LiquidGlassPillGeometry {
    fun visualPosition(position: Float, tabCount: Int, isRtl: Boolean): Float {
        if (tabCount <= 0) return 0f
        return if (isRtl) tabCount - 1f - position else position
    }

    fun center(
        position: Float,
        cellWidth: Float,
        rowHeight: Float,
        tabCount: Int,
        isRtl: Boolean
    ): Pair<Float, Float> {
        val visual = visualPosition(position, tabCount, isRtl)
        return (visual + 0.5f) * cellWidth to rowHeight * 0.5f
    }

    /**
     * Convert a physical horizontal drag into a clamped logical tab position.
     * Logical order stays stable in RTL; only the physical drag direction flips.
     */
    fun dragTargetIndex(
        startIndex: Float,
        dragDistancePx: Float,
        tabStepPx: Float,
        tabCount: Int,
        isRtl: Boolean
    ): Float {
        if (tabCount <= 0) return 0f
        val step = tabStepPx.takeIf { it.isFinite() && it > 0f } ?: 1f
        val direction = if (isRtl) -1f else 1f
        return (startIndex + dragDistancePx * direction / step)
            .coerceIn(0f, (tabCount - 1).toFloat())
    }

    /** Preserve the resting tab-cell aspect ratio while adding RX's vertical lift. */
    fun liftedSize(
        restWidth: Float,
        restHeight: Float,
        topInset: Float,
        bottomInset: Float,
        growth: Float
    ): Pair<Float, Float> {
        if (restHeight <= 0f) return restWidth to restHeight
        val height = restHeight + topInset + bottomInset + growth
        return height * restWidth / restHeight to height
    }
}
