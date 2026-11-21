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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon

/**
 * App-wide icon families. The iOS family follows the SF Symbols visual language
 * used by the native iOS branch; the remaining families use Compose Material
 * icon variants so every glyph stays crisp and accessible at any density.
 */
internal enum class AppIconPack(
    val id: String,
    val title: String,
    val description: String
) {
    MATERIAL("material", "Material", "Залитые формы и привычная геометрия Material."),
    IOS("ios", "iOS", "Контурные символы в духе SF Symbols."),
    ROUNDED("rounded", "Rounded", "Мягкие формы и округлённые окончания."),
    SHARP("sharp", "Sharp", "Строгие силуэты и чёткие углы."),
    DUOTONE("duotone", "Duotone", "Два уровня заполнения для большей глубины.");

    companion object {
        fun fromId(id: String): AppIconPack = entries.firstOrNull { it.id == id } ?: MATERIAL
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
    Icon(
        imageVector = iconVector(pack ?: LocalAppIconPack.current, glyph),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
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
        AppIconPack.IOS -> when (glyph) {
            AppGlyph.HOME, AppGlyph.SOLVER, AppGlyph.FUNCTIONS -> Icons.Outlined.Functions
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
