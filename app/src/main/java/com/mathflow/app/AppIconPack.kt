package com.mathflow.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.sharp.Backspace
import androidx.compose.material.icons.automirrored.twotone.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * App-wide icon families. MathFlow uses its own navigation glyph geometry;
 * supporting utility glyphs use Compose vectors so they remain crisp at every density.
 */
internal enum class AppIconPack(
    val id: String,
    val title: String,
    val description: String
) {
    MATERIAL("material", "Material", "Залитые формы и привычная геометрия Material."),
    MATHFLOW("mathflow", "MathFlow", "Авторские контурные и геометрические символы MathFlow."),
    ROUNDED("rounded", "Rounded", "Мягкие формы и округлённые окончания."),
    SHARP("sharp", "Sharp", "Строгие силуэты и чёткие углы."),
    DUOTONE("duotone", "Duotone", "Два уровня заполнения для большей глубины.");

    companion object {
        fun fromId(id: String): AppIconPack = when (id) {
            "ios" -> MATHFLOW
            else -> entries.firstOrNull { it.id == id } ?: MATERIAL
        }
    }
}

internal enum class AppGlyph {
    HOME, TOOLS, SOLVER, HISTORY, SETTINGS,
    CALCULATOR, BUILD, FUNCTIONS, CHECK, DROPDOWN,
    BACKSPACE, PALETTE, TUNE, TEXT, LANGUAGE, GLOBE
}

internal val LocalAppIconPack = staticCompositionLocalOf { AppIconPack.MATERIAL }

@Composable
internal fun AppIcon(
    glyph: AppGlyph,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color,
    pack: AppIconPack? = null
) {
    val activePack = pack ?: LocalAppIconPack.current
    if (activePack == AppIconPack.MATHFLOW && glyph in setOf(
            AppGlyph.HOME, AppGlyph.FUNCTIONS, AppGlyph.TOOLS, AppGlyph.SOLVER,
            AppGlyph.HISTORY, AppGlyph.SETTINGS, AppGlyph.TUNE
        )
    ) {
        MathFlowNavigationGlyph(glyph, modifier, tint)
    } else {
        Icon(
            imageVector = iconVector(activePack, glyph),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}

private fun iconVector(pack: AppIconPack, glyph: AppGlyph): ImageVector {
    return when (pack) {
        AppIconPack.MATERIAL -> when (glyph) {
            AppGlyph.HOME, AppGlyph.CALCULATOR -> Icons.Filled.Calculate
            AppGlyph.TOOLS, AppGlyph.BUILD -> Icons.Filled.Build
            AppGlyph.SOLVER, AppGlyph.FUNCTIONS -> Icons.Filled.Functions
            AppGlyph.HISTORY -> Icons.Filled.History
            AppGlyph.SETTINGS, AppGlyph.TUNE -> Icons.Filled.Tune
            AppGlyph.CHECK -> Icons.Filled.Check
            AppGlyph.DROPDOWN -> Icons.Filled.ArrowDropDown
            AppGlyph.BACKSPACE -> Icons.AutoMirrored.Filled.Backspace
            AppGlyph.PALETTE -> Icons.Filled.ColorLens
            AppGlyph.TEXT -> Icons.Filled.TextFields
            AppGlyph.LANGUAGE -> Icons.Filled.Language
            AppGlyph.GLOBE -> Icons.Filled.Public
        }
        AppIconPack.MATHFLOW -> when (glyph) {
            AppGlyph.HOME, AppGlyph.FUNCTIONS -> Icons.Outlined.Functions
            AppGlyph.SOLVER -> Icons.Outlined.Calculate
            AppGlyph.TOOLS -> Icons.Outlined.GridView
            AppGlyph.HISTORY -> Icons.Outlined.History
            AppGlyph.SETTINGS, AppGlyph.TUNE -> Icons.Outlined.Tune
            AppGlyph.CALCULATOR -> Icons.Outlined.Calculate
            AppGlyph.BUILD -> Icons.Outlined.Build
            AppGlyph.CHECK -> Icons.Outlined.Check
            AppGlyph.DROPDOWN -> Icons.Outlined.ArrowDropDown
            AppGlyph.BACKSPACE -> Icons.AutoMirrored.Outlined.Backspace
            AppGlyph.PALETTE -> Icons.Outlined.ColorLens
            AppGlyph.TEXT -> Icons.Outlined.TextFields
            AppGlyph.LANGUAGE -> Icons.Outlined.Language
            AppGlyph.GLOBE -> Icons.Outlined.Public
        }
        AppIconPack.ROUNDED -> when (glyph) {
            AppGlyph.HOME, AppGlyph.CALCULATOR -> Icons.Rounded.Calculate
            AppGlyph.TOOLS, AppGlyph.BUILD -> Icons.Rounded.Build
            AppGlyph.SOLVER, AppGlyph.FUNCTIONS -> Icons.Rounded.Functions
            AppGlyph.HISTORY -> Icons.Rounded.History
            AppGlyph.SETTINGS, AppGlyph.TUNE -> Icons.Rounded.Tune
            AppGlyph.CHECK -> Icons.Rounded.Check
            AppGlyph.DROPDOWN -> Icons.Rounded.ArrowDropDown
            AppGlyph.BACKSPACE -> Icons.AutoMirrored.Rounded.Backspace
            AppGlyph.PALETTE -> Icons.Rounded.ColorLens
            AppGlyph.TEXT -> Icons.Rounded.TextFields
            AppGlyph.LANGUAGE -> Icons.Rounded.Language
            AppGlyph.GLOBE -> Icons.Rounded.Public
        }
        AppIconPack.SHARP -> when (glyph) {
            AppGlyph.HOME, AppGlyph.CALCULATOR -> Icons.Sharp.Calculate
            AppGlyph.TOOLS, AppGlyph.BUILD -> Icons.Sharp.Build
            AppGlyph.SOLVER, AppGlyph.FUNCTIONS -> Icons.Sharp.Functions
            AppGlyph.HISTORY -> Icons.Sharp.History
            AppGlyph.SETTINGS, AppGlyph.TUNE -> Icons.Sharp.Tune
            AppGlyph.CHECK -> Icons.Sharp.Check
            AppGlyph.DROPDOWN -> Icons.Sharp.ArrowDropDown
            AppGlyph.BACKSPACE -> Icons.AutoMirrored.Sharp.Backspace
            AppGlyph.PALETTE -> Icons.Sharp.ColorLens
            AppGlyph.TEXT -> Icons.Sharp.TextFields
            AppGlyph.LANGUAGE -> Icons.Sharp.Language
            AppGlyph.GLOBE -> Icons.Sharp.Public
        }
        AppIconPack.DUOTONE -> when (glyph) {
            AppGlyph.HOME, AppGlyph.CALCULATOR -> Icons.TwoTone.Calculate
            AppGlyph.TOOLS, AppGlyph.BUILD -> Icons.TwoTone.Build
            AppGlyph.SOLVER, AppGlyph.FUNCTIONS -> Icons.TwoTone.Functions
            AppGlyph.HISTORY -> Icons.TwoTone.History
            AppGlyph.SETTINGS, AppGlyph.TUNE -> Icons.TwoTone.Tune
            AppGlyph.CHECK -> Icons.TwoTone.Check
            AppGlyph.DROPDOWN -> Icons.TwoTone.ArrowDropDown
            AppGlyph.BACKSPACE -> Icons.AutoMirrored.TwoTone.Backspace
            AppGlyph.PALETTE -> Icons.TwoTone.ColorLens
            AppGlyph.TEXT -> Icons.TwoTone.TextFields
            AppGlyph.LANGUAGE -> Icons.TwoTone.Language
            AppGlyph.GLOBE -> Icons.TwoTone.Public
        }
    }
}


/** Original MathFlow navigation glyphs; independent of platform SF Symbols. */
@Composable
private fun MathFlowNavigationGlyph(glyph: AppGlyph, modifier: Modifier, tint: Color) {
    when (glyph) {
        AppGlyph.HOME, AppGlyph.FUNCTIONS -> Text(
            text = "ƒ(x)",
            modifier = modifier,
            color = tint,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            maxLines = 1
        )
        else -> Canvas(modifier) {
            val s = size.minDimension
            val stroke = s * 0.085f
            val left = size.width * 0.12f
            val right = size.width * 0.88f
            val top = size.height * 0.12f
            val bottom = size.height * 0.88f
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val line = Stroke(width = stroke, cap = StrokeCap.Round)
            when (glyph) {
                AppGlyph.TOOLS -> {
                    val cell = s * 0.34f
                    val gap = s * 0.12f
                    val startX = (size.width - (cell * 2f + gap)) / 2f
                    val startY = (size.height - (cell * 2f + gap)) / 2f
                    for (row in 0..1) for (col in 0..1) {
                        drawRoundRect(
                            tint,
                            Offset(startX + col * (cell + gap), startY + row * (cell + gap)),
                            Size(cell, cell),
                            androidx.compose.ui.geometry.CornerRadius(s * 0.055f),
                            style = line
                        )
                    }
                }
                AppGlyph.SOLVER -> {
                    drawLine(tint, Offset(size.width * 0.22f, centerY - s * 0.16f), Offset(size.width * 0.78f, centerY - s * 0.16f), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(tint, Offset(size.width * 0.22f, centerY + s * 0.16f), Offset(size.width * 0.78f, centerY + s * 0.16f), strokeWidth = stroke, cap = StrokeCap.Round)
                }
                AppGlyph.HISTORY -> {
                    drawArc(tint, 42f, 300f, false,
                        Offset(size.width * 0.18f, size.height * 0.18f),
                        Size(size.width * 0.64f, size.height * 0.64f), style = line)
                    val arrow = Path().apply {
                        moveTo(size.width * 0.08f, size.height * 0.28f)
                        lineTo(size.width * 0.10f, size.height * 0.08f)
                        lineTo(size.width * 0.30f, size.height * 0.12f)
                    }
                    drawPath(arrow, tint, style = line)
                    drawLine(tint, Offset(centerX, centerY), Offset(centerX, centerY - s * 0.19f), strokeWidth = stroke, cap = StrokeCap.Round)
                    drawLine(tint, Offset(centerX, centerY), Offset(centerX + s * 0.16f, centerY + s * 0.10f), strokeWidth = stroke, cap = StrokeCap.Round)
                }
                AppGlyph.SETTINGS, AppGlyph.TUNE -> {
                    val ys = listOf(size.height * 0.25f, centerY, size.height * 0.75f)
                    val knobs = listOf(size.width * 0.38f, size.width * 0.67f, size.width * 0.31f)
                    ys.forEachIndexed { i, y ->
                        drawLine(tint, Offset(left, y), Offset(right, y), strokeWidth = stroke, cap = StrokeCap.Round)
                        drawCircle(tint, s * 0.105f, Offset(knobs[i], y), style = line)
                    }
                }
                else -> Unit
            }
        }
    }
}
