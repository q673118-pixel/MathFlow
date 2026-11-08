package com.mathflow.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class KeyAnimationStyle(val id: String, val titleKey: String) {
    SPRING("spring", "animation_spring"),
    PRESS("press", "animation_press"),
    SCALE("scale", "animation_scale"),
    FLASH("flash", "animation_flash"),
    RIPPLE("ripple", "animation_ripple"),
    BOUNCE("bounce", "animation_bounce"),
    ELASTIC("elastic", "animation_elastic"),
    MINIMAL("minimal", "animation_minimal");

    companion object {
        fun fromId(id: String) = values().firstOrNull { it.id == id } ?: SPRING
    }
}

internal data class KeyAnimationSettings(
    val style: KeyAnimationStyle = KeyAnimationStyle.SPRING,
    val durationMs: Int = 140,
    val opacity: Float = 0.72f,
    val intensity: Float = 0.10f,
    val colorEffectEnabled: Boolean = false,
    val pressColorArgb: Long = 0x00000000L
) {
    fun normalized() = copy(
        durationMs = durationMs.coerceIn(70, 400),
        opacity = opacity.coerceIn(0.20f, 1f),
        intensity = intensity.coerceIn(0.02f, 0.20f)
    )
}

internal enum class BottomNavAnimationStyle(val id: String, val title: String, val description: String) {
    FLUID_PILL("fluid_pill", "Fluid Pill", "Плавно перетекающая капсула"),
    SPRING_MOTION("spring_motion", "Spring Motion", "Пружинящее движение иконки"),
    LIGHT_TRAIL("light_trail", "Light Trail", "Световой след под вкладкой"),
    MORPH("morph", "Morph", "Плавное изменение формы индикатора"),
    EXPAND("expand", "Expand", "Раскрытие капсулы из точки"),
    SOFT_FOCUS("soft_focus", "Soft Focus", "Мягкое проявление выбранной вкладки"),
    ELASTIC_SLIDE("elastic_slide", "Elastic Slide", "Эластичное скольжение индикатора"),
    RIPPLE_BLOOM("ripple_bloom", "Ripple Bloom", "Волна раскрывается под иконкой");

    companion object {
        fun fromId(id: String) = values().firstOrNull { it.id == id } ?: FLUID_PILL
    }
}

internal data class BottomNavAnimationSettings(
    val enabled: Boolean = true,
    val style: BottomNavAnimationStyle = BottomNavAnimationStyle.FLUID_PILL,
    val durationMs: Int = 280,
    val intensity: Float = 0.65f,
    val iconScale: Float = 0.08f,
    val opacity: Float = 0.85f
) {
    fun normalized() = copy(
        durationMs = durationMs.coerceIn(100, 1000),
        intensity = intensity.coerceIn(0f, 1f),
        iconScale = iconScale.coerceIn(0f, 0.20f),
        opacity = opacity.coerceIn(0f, 1f)
    )
}

internal val LocalKeyAnimationSettings = staticCompositionLocalOf { KeyAnimationSettings() }

@Composable
internal fun MathFlowKeyButton(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    settings: KeyAnimationSettings = LocalKeyAnimationSettings.current,
    onClick: () -> Unit
) {
    val palette = LocalPalette.current
    val normalized = settings.normalized()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val amount = normalized.intensity

    val targetScaleX = when (normalized.style) {
        KeyAnimationStyle.SCALE -> if (pressed) 1f + amount else 1f
        KeyAnimationStyle.ELASTIC -> if (pressed) 1f + amount else 1f
        KeyAnimationStyle.MINIMAL -> if (pressed) 1f - amount * 0.25f else 1f
        KeyAnimationStyle.PRESS -> if (pressed) 1f - amount * 0.45f else 1f
        KeyAnimationStyle.BOUNCE -> if (pressed) 1f - amount * 0.60f else 1f
        KeyAnimationStyle.SPRING -> if (pressed) 1f - amount else 1f
        KeyAnimationStyle.FLASH -> if (pressed) 1f - amount * 0.12f else 1f
        KeyAnimationStyle.RIPPLE -> 1f
    }
    val targetScaleY = when (normalized.style) {
        KeyAnimationStyle.ELASTIC -> if (pressed) 1f - amount * 1.25f else 1f
        KeyAnimationStyle.PRESS -> if (pressed) 1f - amount else 1f
        KeyAnimationStyle.BOUNCE -> if (pressed) 1f - amount * 0.60f else 1f
        KeyAnimationStyle.SPRING -> if (pressed) 1f - amount else 1f
        KeyAnimationStyle.MINIMAL -> if (pressed) 1f - amount * 0.25f else 1f
        KeyAnimationStyle.SCALE -> if (pressed) 1f + amount else 1f
        KeyAnimationStyle.FLASH, KeyAnimationStyle.RIPPLE -> 1f
    }
    val springStyle = normalized.style == KeyAnimationStyle.SPRING ||
        normalized.style == KeyAnimationStyle.BOUNCE ||
        normalized.style == KeyAnimationStyle.ELASTIC
    // Spring animations have no fixed duration; map the duration control to stiffness
    // so the same slider still changes their perceived speed.
    val durationFraction = ((normalized.durationMs - 70) / 330f).coerceIn(0f, 1f)
    val springStiffness = 1200f - durationFraction * 1080f
    val scaleSpec = if (springStyle) {
        spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = springStiffness)
    } else {
        tween<Float>(durationMillis = normalized.durationMs)
    }
    val scaleX by animateFloatAsState(targetScaleX, scaleSpec, label = "MathFlowKeyScaleX")
    val scaleY by animateFloatAsState(targetScaleY, scaleSpec, label = "MathFlowKeyScaleY")

    val operator = label in setOf("÷", "×", "−", "+")
    val equals = label == "="
    val baseColor = when {
        equals -> palette.keyEquals
        operator || label == "AC" -> palette.keyAccent
        else -> palette.key
    }
    val baseText = if (equals) palette.onKeyEquals else palette.text
    val background = baseColor
    val foreground = baseText
    val overlayAlpha by animateFloatAsState(
        if (pressed && normalized.colorEffectEnabled) {
            when (normalized.style) {
                KeyAnimationStyle.FLASH -> normalized.opacity
                KeyAnimationStyle.RIPPLE -> normalized.opacity * 0.34f
                else -> normalized.opacity * 0.22f
            }
        } else 0f,
        tween(normalized.durationMs),
        label = "MathFlowKeyOverlay"
    )
    val rippleProgress by animateFloatAsState(
        if (pressed && normalized.style == KeyAnimationStyle.RIPPLE) 1f else 0f,
        tween(normalized.durationMs),
        label = "MathFlowKeyRippleProgress"
    )
    val overlayColor = if (normalized.pressColorArgb != 0L) Color(normalized.pressColorArgb) else palette.accent
    val shape = RoundedCornerShape(50)

    Surface(
        modifier = modifier.graphicsLayer {
            this.scaleX = scaleX
            this.scaleY = scaleY
        }.clip(shape).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ),
        shape = shape,
        color = background,
        contentColor = foreground
    ) {
        Box(
            Modifier.fillMaxSize().clip(shape),
            contentAlignment = Alignment.Center
        ) {
            if (normalized.style == KeyAnimationStyle.RIPPLE) {
                Canvas(Modifier.fillMaxSize()) {
                    val rippleReach = 0.45f + normalized.intensity * 3.3f
                    val radius = size.maxDimension * rippleReach * rippleProgress
                    if (radius > 0f) {
                        drawCircle(
                            color = palette.accent.copy(alpha = overlayAlpha),
                            radius = radius,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxSize()
                        .background(
                            (if (normalized.style == KeyAnimationStyle.FLASH) palette.accent else overlayColor)
                                .copy(alpha = overlayAlpha)
                        )
                )
            }
            Text(
                label,
                fontSize = when {
                    label.length > 1 && label != "AC" -> if (compact) 18.sp else 20.sp
                    else -> if (compact) 22.sp else 28.sp
                },
                fontWeight = FontWeight.Normal,
                color = foreground,
                maxLines = 1
            )
        }
    }
}
