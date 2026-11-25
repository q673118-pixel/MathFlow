package com.mathflow.app.navigation

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

/**
 * RX Playa dock integration for MathFlow.
 *
 * The tab-bar structure, state transitions, selection physics, top-layer pill,
 * and drag behaviour live in the source-adapted FloatingTabBar/GlassSelectionPill.
 * Only destination keys, glyphs, palette and app navigation callbacks are supplied here.
 */
val LocalDockAppBackdrop = compositionLocalOf<Backdrop> { error("MathFlow dock backdrop not provided") }
internal val LocalDockGlassExport = compositionLocalOf<LayerBackdrop?> { null }
internal val LocalDockReduceMotion = compositionLocalOf { false }
internal val LocalDockGlassEnabled = compositionLocalOf { true }
internal val LocalDockHaptic = compositionLocalOf<(Boolean) -> Unit> { {} }

data class RxDockItem(
    val key: Any,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun MathFlowRxDock(
    items: List<RxDockItem>,
    selectedKey: Any,
    standalone: RxDockItem,
    scrollConnection: FloatingTabBarScrollConnection,
    pageBackdrop: Backdrop,
    dark: Boolean,
    accent: Color,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    useSystemInsets: Boolean = true,
    onSelect: (Any) -> Unit
) {
    val view = LocalView.current
    val currentOnSelect by rememberUpdatedState(onSelect)
    val shape = remember { RoundedCornerShape(percent = 50) }
    val haptic: (Boolean) -> Unit = { tick ->
        view.performHapticFeedback(
            if (tick) HapticFeedbackConstants.CLOCK_TICK else HapticFeedbackConstants.KEYBOARD_TAP
        )
    }
    val glassEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val glassContentTint = if (dark) Color.White else Color.Black
    val glassSurface: @Composable () -> Modifier = {
        rxDockGlass(shape, pageBackdrop, dark)
    }
    val stableContentKey = remember(selectedKey, items.map { it.key }, standalone.key) {
        listOf(selectedKey, items.map { it.key }, standalone.key)
    }

    CompositionLocalProvider(
        LocalDockAppBackdrop provides pageBackdrop,
        LocalDockReduceMotion provides reduceMotion,
        LocalDockGlassEnabled provides glassEnabled,
        LocalDockHaptic provides haptic
    ) {
        FloatingTabBar(
            selectedTabKey = selectedKey,
            scrollConnection = scrollConnection,
            modifier = modifier
                .then(if (useSystemInsets) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
                .padding(horizontal = 28.dp)
                .padding(bottom = 7.dp)
                .fillMaxWidth(),
            tabBarContentModifier = glassSurface,
            colors = FloatingTabBarDefaults.colors(
                backgroundColor = Color.Transparent,
                accessoryBackgroundColor = Color.Transparent,
                indicatorColor = if (dark) Color.White.copy(alpha = 0.14f) else accent.copy(alpha = 0.16f)
            ),
            elevations = FloatingTabBarDefaults.elevations(
                inlineElevation = 0.dp,
                expandedElevation = 0.dp
            ),
            sizes = FloatingTabBarDefaults.sizes(
                tabBarContentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                tabInlineContentPadding = PaddingValues(10.dp),
                tabExpandedContentPadding = PaddingValues(vertical = 6.dp, horizontal = 0.dp),
                componentSpacing = 8.dp,
                tabSpacing = 0.dp,
                inlineComponentSpacing = 14.dp,
                inlineBarPadding = PaddingValues(start = 0.dp, end = 0.dp, bottom = 7.dp)
            ),
            contentKey = stableContentKey
        ) {
            items.forEach { item ->
                tab(
                    key = item.key,
                    title = {},
                    icon = item.icon,
                    onClick = {
                        if (item.key != selectedKey) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            currentOnSelect(item.key)
                        }
                    }
                )
            }
            standaloneTab(
                key = standalone.key,
                icon = standalone.icon,
                onClick = {
                    if (standalone.key != selectedKey) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        currentOnSelect(standalone.key)
                    }
                }
            )
        }
    }
}

@Composable
private fun rxDockGlass(
    shape: Shape,
    pageBackdrop: Backdrop,
    dark: Boolean
): Modifier {
    val exported = LocalDockGlassExport.current
    val tint = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return Modifier.clip(shape).background(tint.copy(alpha = 0.94f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    }
    return Modifier.drawBackdrop(
        backdrop = pageBackdrop,
        shape = { shape },
        effects = {
            colorControls(saturation = 1.5f)
            blur(8.dp.toPx() * 0.33f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lens(
                    refractionHeight = 24.dp.toPx() * 0.33f,
                    refractionAmount = 24.dp.toPx() * 0.33f,
                    depthEffect = true,
                    chromaticAberration = true
                )
            }
        },
        highlight = { Highlight.Default },
        shadow = { Shadow.Default },
        onDrawSurface = { drawRect(tint.copy(alpha = 0.4f)) },
        exportedBackdrop = exported,
        backdropScale = 0.33f
    )
}
