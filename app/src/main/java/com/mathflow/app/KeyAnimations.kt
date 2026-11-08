package com.mathflow.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
    val intensity: Float = 0.10f
) {
    fun normalized() = copy(
        durationMs = durationMs.coerceIn(70, 400),
        opacity = opacity.coerceIn(0.20f, 1f),
        intensity = intensity.coerceIn(0.02f, 0.20f)
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
        KeyAnimationStyle.FLASH -> if (pressed) 0.985f else 1f
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
    val scaleSpec = if (springStyle) {
        spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
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
    val flash = normalized.style == KeyAnimationStyle.FLASH && pressed
    val background = baseColor
    val foreground by animateColorAsState(
        if (flash) palette.background else baseText,
        tween(normalized.durationMs),
        label = "MathFlowKeyFlashText"
    )
    val overlayAlpha by animateFloatAsState(
        if (pressed) {
            when (normalized.style) {
                KeyAnimationStyle.FLASH -> normalized.opacity
                KeyAnimationStyle.RIPPLE -> normalized.opacity * 0.12f
                else -> normalized.opacity * 0.22f
            }
        } else 0f,
        tween(normalized.durationMs),
        label = "MathFlowKeyOverlay"
    )
    val overlayColor = when (normalized.style) {
        KeyAnimationStyle.FLASH, KeyAnimationStyle.RIPPLE -> palette.accent
        else -> if (palette.dark) Color.White else Color.Black
    }
    val shape = RoundedCornerShape(50)

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            this.scaleX = scaleX
            this.scaleY = scaleY
        },
        shape = shape,
        color = background,
        contentColor = foreground,
        interactionSource = interactionSource
    ) {
        Box(
            Modifier.fillMaxSize().clip(shape)
                .background(overlayColor.copy(alpha = overlayAlpha)),
            contentAlignment = Alignment.Center
        ) {
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
