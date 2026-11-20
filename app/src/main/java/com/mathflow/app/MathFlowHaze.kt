package com.mathflow.app

import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

/** Shared capture state for backdrop effects in the active app window. */
internal val LocalMathFlowHazeState = staticCompositionLocalOf { HazeState() }
