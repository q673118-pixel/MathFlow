package com.mathflow.app

import kotlin.math.roundToInt

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val pendingConfigUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        pendingConfigUri.value = extractConfigUri(intent)
        enableEdgeToEdge()
        setContent {
            MathFlowApp(
                externalConfigUri = pendingConfigUri.value,
                onExternalConfigHandled = { pendingConfigUri.value = null }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingConfigUri.value = extractConfigUri(intent)
    }

    @Suppress("DEPRECATION")
    private fun extractConfigUri(source: android.content.Intent?): Uri? {
        if (source == null) return null
        return source.data ?: if (android.os.Build.VERSION.SDK_INT >= 33) {
            source.getParcelableExtra(android.content.Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            source.getParcelableExtra(android.content.Intent.EXTRA_STREAM) as? Uri
        }
    }
}


@Composable
private fun MathFlowIntroSplash(palette: Palette, onFinished: () -> Unit) {
    val density = LocalDensity.current
    val startY = with(density) { (-450).dp.toPx() }
    val firstBounceY = with(density) { (-42).dp.toPx() }
    val secondBounceY = with(density) { (-14).dp.toPx() }
    val mProgress = remember { Animatable(0f) }
    val dotY = remember { Animatable(startY) }
    val dotAlpha = remember { Animatable(0f) }
    val dotScaleX = remember { Animatable(1f) }
    val dotScaleY = remember { Animatable(1f) }
    val splashAlpha = remember { Animatable(1f) }

    // Keep the existing intro timing and bouncing-dot choreography. Only replace
    // the old bitmap M + separate dot with the exact Ocean vector geometry.
    LaunchedEffect(Unit) {
        delay(500L)
        mProgress.animateTo(
            1f,
            tween(760, easing = androidx.compose.animation.core.Easing { f ->
                1f - (1f - f) * (1f - f) * (1f - f)
            })
        )
        dotAlpha.snapTo(1f)

        kotlinx.coroutines.coroutineScope {
            launch {
                dotY.animateTo(0f, tween(420, easing = androidx.compose.animation.core.Easing { it * it }))
                delay(60L)
                dotY.animateTo(firstBounceY, tween(190, easing = androidx.compose.animation.core.Easing { 1f - (1f - it) * (1f - it) }))
                dotY.animateTo(0f, tween(170, easing = androidx.compose.animation.core.Easing { it * it }))
                delay(45L)
                dotY.animateTo(secondBounceY, tween(125, easing = androidx.compose.animation.core.Easing { 1f - (1f - it) * (1f - it) }))
                dotY.animateTo(0f, tween(115, easing = androidx.compose.animation.core.Easing { it * it }))
            }
            launch {
                delay(420L)
                dotScaleX.animateTo(1.18f, tween(60))
                dotScaleX.animateTo(0.90f, tween(190))
                dotScaleX.animateTo(1.02f, tween(170))
                dotScaleX.animateTo(1.12f, tween(45))
                dotScaleX.animateTo(0.96f, tween(125))
                dotScaleX.animateTo(1f, tween(115))
            }
            launch {
                delay(420L)
                dotScaleY.animateTo(0.78f, tween(60))
                dotScaleY.animateTo(1.12f, tween(190))
                dotScaleY.animateTo(0.98f, tween(170))
                dotScaleY.animateTo(0.88f, tween(45))
                dotScaleY.animateTo(1.04f, tween(125))
                dotScaleY.animateTo(1f, tween(115))
            }
        }

        delay(120L)
        splashAlpha.animateTo(0f, tween(2200, easing = LinearOutSlowInEasing))
        onFinished()
    }

    Box(
        Modifier.fillMaxSize()
            .background(palette.background)
            .graphicsLayer { alpha = splashAlpha.value }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(108.dp)) {
            val scaleX = size.width / 108f
            val scaleY = size.height / 108f
            val oceanM = Path().apply {
                moveTo(28f, 78f)
                lineTo(28f, 37f)
                quadraticTo(28f, 28f, 36f, 37f)
                lineTo(54f, 65f)
                lineTo(72f, 37f)
                quadraticTo(80f, 28f, 80f, 37f)
                lineTo(80f, 78f)
            }
            // The full Ocean contour is about 170 px long in this 108-unit
            // coordinate space. Keep the visible dash longer than the contour:
            // otherwise the last stroke segment falls into the dash gap.
            val fullContourDash = 240f
            drawContext.transform.scale(scaleX, scaleY, pivot = Offset.Zero)
            drawPath(
                path = oceanM,
                color = palette.accent,
                style = Stroke(
                    width = 10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf((fullContourDash * mProgress.value).coerceAtLeast(0.001f), fullContourDash * 2f),
                        0f
                    )
                )
            )
            drawCircle(
                color = palette.accent,
                radius = 4.5f * dotScaleX.value,
                center = Offset(54f, 25f + (dotY.value / scaleY)),
                alpha = dotAlpha.value
            )
        }
    }
}

private enum class Page(val title: String, val glyph: AppGlyph) {
    HOME("Главная", AppGlyph.HOME),
    TOOLS("Инструменты", AppGlyph.TOOLS),
    SOLVER("Уравнения", AppGlyph.SOLVER),
    HISTORY("История", AppGlyph.HISTORY),
    SETTINGS("Настройки", AppGlyph.SETTINGS)
}

// ---------------------------------------------------------------- palette

internal class Palette(
    val background: Color,
    val surface: Color,
    val key: Color,
    val keyAccent: Color,
    val keyEquals: Color,
    val onKeyEquals: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val navActive: Color,
    val error: Color,
    val dark: Boolean
)

private data class ThemeOption(
    val id: String,
    val title: String,
    val description: String,
    val palette: Palette
)

private val ThemeOptions = listOf(
    ThemeOption("vanilla", "Vanilla", "Фирменная фиолетовая палитра MathFlow.", Palette(
        Color(0xFF0C0B10), Color(0xFF16141C), Color(0xFF211F29), Color(0xFF54447F),
        Color(0xFF6A5AA3), Color.White, Color(0xFFE9E5F3), Color(0xFF9A94AB),
        Color(0xFFB9A8F0), Color(0xFF2E2942), Color(0xFFFF8A80), true
    )),
    ThemeOption("black", "Black", "Почти чёрный интерфейс с нейтральными поверхностями.", Palette(
        Color(0xFF08090D), Color(0xFF12141A), Color(0xFF20232B), Color(0xFF343945),
        Color(0xFFB7A0FF), Color(0xFF1A1327), Color(0xFFF4F2FA), Color(0xFF9296A3),
        Color(0xFFC9B8FF), Color(0xFF252833), Color(0xFFFF8A80), true
    )),
    ThemeOption("white", "White", "Светлая тема с чистыми белыми поверхностями.", Palette(
        Color(0xFFF7F6FA), Color.White, Color(0xFFECEAF2), Color(0xFFE1D8F3),
        Color(0xFF6A1B9A), Color.White, Color(0xFF17131F), Color(0xFF686274),
        Color(0xFF7428B2), Color(0xFFE8DDF5), Color(0xFFB3261E), false
    )),
    ThemeOption("purple", "Purple", "Глубокий фиолетовый с насыщенными акцентами.", Palette(
        Color(0xFF10051B), Color(0xFF1C0C2B), Color(0xFF2C1240), Color(0xFF511C70),
        Color(0xFF8A2CC2), Color.White, Color(0xFFF5E9FF), Color(0xFFB59BBF),
        Color(0xFFD49AFF), Color(0xFF321343), Color(0xFFFF8A80), true
    )),
    ThemeOption("ocean", "Ocean", "Холодные синие оттенки и яркий голубой акцент.", Palette(
        Color(0xFF04142F), Color(0xFF081E43), Color(0xFF102D58), Color(0xFF075C9B),
        Color(0xFF139EE0), Color.White, Color(0xFFEAF7FF), Color(0xFF8FB7D4),
        Color(0xFF39C7FF), Color(0xFF0B3157), Color(0xFFFF8A80), true
    )),
    ThemeOption("emerald", "Emerald", "Тёмно-зелёная палитра с мятными акцентами.", Palette(
        Color(0xFF031D17), Color(0xFF082B22), Color(0xFF104336), Color(0xFF14664C),
        Color(0xFF13A67A), Color.White, Color(0xFFE8FFF6), Color(0xFF90C9B3),
        Color(0xFF4BE2B2), Color(0xFF0D3A2C), Color(0xFFFF8A80), true
    )),
    ThemeOption("sunset", "Sunset", "Тёплые оттенки заката: коралл, оранжевый и розовый.", Palette(
        Color(0xFF24101C), Color(0xFF351729), Color(0xFF4C2035), Color(0xFFB34B37),
        Color(0xFFFF6D49), Color.White, Color(0xFFFFF2EA), Color(0xFFD3A6A0),
        Color(0xFFFF9A66), Color(0xFF51243B), Color(0xFFFFB4AB), true
    )),
    ThemeOption("rose", "Rose", "Мягкая розовая тема с ярким малиновым акцентом.", Palette(
        Color(0xFFFFF3F8), Color.White, Color(0xFFF8DDEB), Color(0xFFF5BBD5),
        Color(0xFFE50070), Color.White, Color(0xFF39182B), Color(0xFF9C6B83),
        Color(0xFFD60069), Color(0xFFF9D7E9), Color(0xFFB3261E), false
    )),
    ThemeOption("midnight", "Midnight", "Ночная фиолетовая палитра с мягкими лавандовыми акцентами.", Palette(
        Color(0xFF100521), Color(0xFF1A0B31), Color(0xFF2A124A), Color(0xFF6331A1),
        Color(0xFFA66BFF), Color.White, Color(0xFFF6EDFF), Color(0xFFB5A0D1),
        Color(0xFFC69AFF), Color(0xFF321957), Color(0xFFFF8A80), true
    )),
    ThemeOption("graphite", "Graphite", "Спокойные графитовые оттенки с холодным светлым акцентом.", Palette(
        Color(0xFF222431), Color(0xFF2D3040), Color(0xFF3A3D4F), Color(0xFF4A4D62),
        Color(0xFFAEB4D8), Color(0xFF222431), Color(0xFFF1F2FF), Color(0xFFB0B4C9),
        Color(0xFFD1D5FF), Color(0xFF3C4056), Color(0xFFFF8A80), true
    )),
    ThemeOption("gray", "Gray", "Нейтральная графитово-серая палитра без цветового оттенка.", Palette(
        Color(0xFF17191E), Color(0xFF22252C), Color(0xFF2E323A), Color(0xFF515965),
        Color(0xFFD4D8E0), Color(0xFF20232A), Color(0xFFF2F3F5), Color(0xFFAEB3BD),
        Color(0xFFDCE0E8), Color(0xFF393E48), Color(0xFFFF8A80), true
    )),
    ThemeOption("transparent", "Transparent", "Полупрозрачные поверхности и холодные стеклянные оттенки.", Palette(
        Color(0xFF10151C), Color(0xC01C2633), Color(0xA02C3948), Color(0xB34D6379),
        Color(0xFFDCEBFA), Color(0xFF16202B), Color(0xFFF4F8FC), Color(0xFFB5C2D0),
        Color(0xFFB7D9F5), Color(0x803B536B), Color(0xFFFF8A80), true
    ))
)

private fun themeFor(id: String): ThemeOption =
    ThemeOptions.firstOrNull { it.id == id } ?: ThemeOptions.first()

@Composable
private fun animatePalette(target: Palette): Palette {
    val spec = tween<Color>(durationMillis = 520, easing = FastOutSlowInEasing)
    val background by animateColorAsState(target.background, spec, label = "ThemeBackground")
    val surface by animateColorAsState(target.surface, spec, label = "ThemeSurface")
    val key by animateColorAsState(target.key, spec, label = "ThemeKey")
    val keyAccent by animateColorAsState(target.keyAccent, spec, label = "ThemeKeyAccent")
    val keyEquals by animateColorAsState(target.keyEquals, spec, label = "ThemeEquals")
    val onKeyEquals by animateColorAsState(target.onKeyEquals, spec, label = "ThemeOnEquals")
    val text by animateColorAsState(target.text, spec, label = "ThemeText")
    val muted by animateColorAsState(target.muted, spec, label = "ThemeMuted")
    val accent by animateColorAsState(target.accent, spec, label = "ThemeAccent")
    val navActive by animateColorAsState(target.navActive, spec, label = "ThemeNavActive")
    val error by animateColorAsState(target.error, spec, label = "ThemeError")
    return remember(target.dark, background, surface, key, keyAccent, keyEquals, onKeyEquals,
        text, muted, accent, navActive, error) {
        Palette(background, surface, key, keyAccent, keyEquals, onKeyEquals, text, muted, accent, navActive, error, target.dark)
    }
}

internal val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { themeFor("vanilla").palette }
internal val LocalSettingsCornerRadius = androidx.compose.runtime.staticCompositionLocalOf<Int?> { null }
internal val LocalBottomNavGlassSettings = androidx.compose.runtime.staticCompositionLocalOf { BottomNavAnimationSettings() }

private fun typographyFor(family: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family)
    )
}

// ---------------------------------------------------------------- root

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MathFlowApp(externalConfigUri: Uri? = null, onExternalConfigHandled: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mathflow_settings", Context.MODE_PRIVATE) }
    var themeId by remember {
        mutableStateOf(prefs.getString("app_theme", if (prefs.getBoolean("dark_mode", true)) "vanilla" else "white") ?: "vanilla")
    }
    var uiStyleId by remember {
        mutableStateOf(MathFlowUiStyle.fromId(prefs.getString("ui_style", "material_you") ?: "material_you").id)
    }
    var iconPackId by remember {
        mutableStateOf(AppIconPack.fromId(prefs.getString("app_icon_pack", "material") ?: "material").id)
    }
    var settingsCornerRadiusDp by remember { mutableStateOf(prefs.getInt("settings_corner_radius_dp", 18).coerceIn(0, 40)) }
    var selectedIconId by remember { mutableStateOf(prefs.getString("app_icon", "vanilla") ?: "vanilla") }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
    val initialFontPath = remember {
        if (!prefs.getBoolean("font_selection_initialized", false)) {
            // Before the font selector had a separate bundled-font entry, a missing
            // selection meant the app's default font. Migrate that state to Artegra.
            val storedPath = prefs.getString("font_path", null) ?: BUILTIN_ARTEGRA_FONT
            prefs.edit()
                .putBoolean("font_selection_initialized", true)
                .putString("font_path", storedPath)
                .apply()
            storedPath
        } else {
            prefs.getString("font_path", null)
        }
    }
    var fontPath by remember { mutableStateOf(initialFontPath) }
    var appLanguage by remember { mutableStateOf(prefs.getString("app_language", "ru") ?: "ru") }
    var measurementSystem by remember { mutableStateOf(prefs.getString("measurement_system", "auto") ?: "auto") }
    var calculatorResultFormat by remember { mutableStateOf(prefs.getString("calculator_result_format", "fraction") ?: "fraction") }
    var keyAnimationSettings by remember {
        mutableStateOf(
            KeyAnimationSettings(
                style = KeyAnimationStyle.fromId(prefs.getString("key_animation_style", "spring") ?: "spring"),
                durationMs = prefs.getInt("key_animation_duration", 140),
                opacity = prefs.getFloat("key_animation_opacity", 0.72f),
                intensity = prefs.getFloat("key_animation_intensity", 0.10f),
                colorEffectEnabled = prefs.getBoolean("key_animation_color_enabled", false),
                pressColorArgb = prefs.getLong("key_animation_press_color", 0L)
            ).normalized()
        )
    }
    var bottomNavAnimationSettings by remember {
        mutableStateOf(
            BottomNavAnimationSettings(
                enabled = prefs.getBoolean("nav_animation_enabled", true),
                style = BottomNavAnimationStyle.fromId(prefs.getString("nav_animation_style", "fluid_pill") ?: "fluid_pill"),
                durationMs = prefs.getInt("nav_animation_duration", 280),
                intensity = prefs.getFloat("nav_animation_intensity", 0.65f),
                iconScale = prefs.getFloat("nav_animation_icon_scale", 0.08f),
                opacity = prefs.getFloat("nav_animation_opacity", 0.85f),
                refractionStrength = prefs.getFloat("nav_refraction_strength", 0.35f),
                dropletRoundness = prefs.getFloat("nav_droplet_roundness", 0.96f),
                glassBlur = prefs.getFloat("nav_glass_blur", 8f),
                glassDepth = prefs.getFloat("nav_glass_depth", 0.70f),
                highlightStrength = prefs.getFloat("nav_highlight_strength", 0.90f),
                shadowStrength = prefs.getFloat("nav_shadow_strength", 0.42f),
                surfaceMaterial = BottomNavSurfaceMaterial.fromId(prefs.getString("nav_surface_material", "frosted") ?: "frosted"),
                matteOpacity = prefs.getFloat("matte_glass_opacity", 0.72f),
                matteBlur = prefs.getFloat("matte_glass_blur", 12f),
                matteDepth = prefs.getFloat("matte_glass_depth", 0.38f),
                matteHighlight = prefs.getFloat("matte_glass_highlight", 0.58f),
                matteShadow = prefs.getFloat("matte_glass_shadow", 0.32f)
            ).normalized()
        )
    }
    var configProfiles by remember { mutableStateOf(MathFlowConfigProfiles.load(context)) }
    var activeConfigId by remember {
        mutableStateOf(prefs.getString("mathflow_active_config", null)?.takeIf { id -> configProfiles.any { it.id == id } })
    }
    LaunchedEffect(themeId, selectedIconId, iconPackId, fontScale, fontPath, appLanguage, measurementSystem,
        keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat, uiStyleId, activeConfigId) {
        val activeId = activeConfigId
        if (activeId != null) {
            val snapshot = makeConfigSnapshot(themeId, selectedIconId, fontScale, fontPath,
                appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat, uiStyleId, iconPackId)
            configProfiles = configProfiles.map { profile ->
                if (profile.id == activeId) profile.copy(settings = snapshot) else profile
            }
            MathFlowConfigProfiles.save(context, configProfiles)
        }
    }

    var page by rememberSaveable { mutableStateOf(Page.HOME) }
    var history by remember { mutableStateOf(emptyList<HistoryEntry>()) }
    val historyScope = rememberCoroutineScope()
    val historyMutex = remember { Mutex() }
    LaunchedEffect(context) {
        historyMutex.lock()
        try {
            history = withContext(Dispatchers.IO) { HistoryStore.load(context) }
        } finally {
            historyMutex.unlock()
        }
    }
    val pageStateHolder = rememberSaveableStateHolder()
    val targetPalette = themeFor(themeId).palette
    val palette = animatePalette(targetPalette)
    // Record an opaque page backdrop, including the theme background. Without
    // this base fill, transparent pixels bypass blur and remain razor-sharp.
    val backdropBackground = palette.background
    val backdropDraw: androidx.compose.ui.graphics.drawscope.ContentDrawScope.() -> Unit =
        remember(backdropBackground) {
            { drawRect(backdropBackground); drawContent() }
        }
    val backdrop = rememberLayerBackdrop(onDraw = backdropDraw)
    // The full page capture is only consumed by the Liquid Glass navigation.
    // Avoid recording the entire page for every other global visual style.
    val capturePageBackdrop = MathFlowUiStyle.fromId(uiStyleId).glassSurface
    var showWelcome by remember { mutableStateOf(prefs.getInt("welcome_version", 0) < 3) }
    LaunchedEffect(externalConfigUri, showWelcome) {
        if (externalConfigUri != null && !showWelcome) page = Page.SETTINGS
    }
    var splashFinished by remember { mutableStateOf(false) }

    var customFont by remember { mutableStateOf<FontFamily?>(null) }
    LaunchedEffect(fontPath) {
        customFont = fontPath?.takeUnless { it == BUILTIN_ARTEGRA_FONT }?.let { path ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(path)
                    if (file.exists() && file.length() > 0L) FontFamily(Font(file, FontWeight.Normal)) else null
                }.getOrNull()
            }
        }
    }
    val family = when (fontPath) {
        null -> FontFamily.Default
        BUILTIN_ARTEGRA_FONT -> FontFamily(Font(R.font.artegra_soft_global_bold_rounded, FontWeight.Normal))
        else -> customFont ?: FontFamily.Default
    }

    val baseDensity = LocalDensity.current
    val density = remember(baseDensity, fontScale) {
        Density(baseDensity.density, baseDensity.fontScale * fontScale)
    }

    val colors = remember(palette) { if (palette.dark) {
        darkColorScheme(
            primary = palette.accent, onPrimary = Color(0xFF1D1530),
            background = palette.background, onBackground = palette.text,
            surface = palette.surface, onSurface = palette.text,
            surfaceVariant = palette.key, onSurfaceVariant = palette.muted,
            outline = palette.muted, outlineVariant = palette.key, error = palette.error
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = Color.White,
            background = palette.background, onBackground = palette.text,
            surface = palette.surface, onSurface = palette.text,
            surfaceVariant = palette.key, onSurfaceVariant = palette.muted,
            outline = palette.muted, outlineVariant = palette.key, error = palette.error
        )
    }

    }

    val typography = remember(family) { typographyFor(family) }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !palette.dark
        controller.isAppearanceLightNavigationBars = !palette.dark
        // Keep both system bars genuinely transparent; Android 10+ contrast scrims
        // can otherwise make the edge-to-edge area look unexpectedly opaque.
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }

    CompositionLocalProvider(
        LocalLanguage provides appLanguage,
        LocalMeasurementSystem provides resolveMeasurementSystem(measurementSystem),
        LocalDensity provides density,
        LocalPalette provides palette,
        LocalMathFlowUiStyle provides MathFlowUiStyle.fromId(uiStyleId),
        LocalAppIconPack provides AppIconPack.fromId(iconPackId),
        LocalBottomNavGlassSettings provides bottomNavAnimationSettings,
        // One persisted radius controls settings controls and calculator keys consistently.
        LocalSettingsCornerRadius provides settingsCornerRadiusDp,
        LocalKeyAnimationSettings provides keyAnimationSettings,
        // Disable all global press indications: no ripple, droplets, particles, or color overlay.
        LocalIndication provides MathFlowNoIndication,
        LocalRippleConfiguration provides RippleConfiguration(
            color = Color.Transparent,
            rippleAlpha = androidx.compose.material.ripple.RippleAlpha(
                draggedAlpha = 0f,
                focusedAlpha = 0f,
                hoveredAlpha = 0f,
                pressedAlpha = 0f
            )
        ),
        LocalLayoutDirection provides if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        MaterialTheme(colorScheme = colors, typography = typography) {
            // Text() reads LocalTextStyle, not the theme typography, so the custom
            // family has to be provided here too for it to reach every label.
            CompositionLocalProvider(
                androidx.compose.material3.LocalTextStyle provides TextStyle(fontFamily = family)
            ) {
                Surface(Modifier.fillMaxSize(), color = palette.background, contentColor = palette.text) {
                    val themeBloom = remember { Animatable(1f) }
                    LaunchedEffect(themeId) {
                        themeBloom.snapTo(0f)
                        themeBloom.animateTo(1f, animationSpec = tween(680, easing = FastOutSlowInEasing))
                    }
                    Box(Modifier.fillMaxSize()) {
                        // Dedicated source for the calculator's glass display.
                        // This layer contains only the page base, never the display itself.
                        Box(
                            Modifier.fillMaxSize()
                                .layerBackdrop(homeBackdrop)
                                .background(backdropBackground)
                        )
                        Canvas(Modifier.fillMaxSize()) {
                            val progress = themeBloom.value
                            if (progress < 1f) {
                                val radius = size.maxDimension * (0.18f + progress * 0.92f)
                                drawCircle(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(palette.accent.copy(alpha = 0.10f * (1f - progress)), Color.Transparent),
                                        center = center,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = center
                                )
                            }
                        }
                        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                            androidx.compose.animation.AnimatedContent(
                                targetState = page,
                                contentKey = { it },
                                transitionSpec = {
                                    val forward = targetState.ordinal > initialState.ordinal
                                    if (!ValueAnimator.areAnimatorsEnabled()) {
                                        androidx.compose.animation.EnterTransition.None togetherWith
                                            androidx.compose.animation.ExitTransition.None
                                    } else {
                                        (
                                            slideInHorizontally(
                                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                                initialOffsetX = { width -> if (forward) width / 10 else -width / 10 }
                                            ) + androidx.compose.animation.fadeIn(tween(150))
                                        ) togetherWith (
                                            slideOutHorizontally(
                                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                                targetOffsetX = { width -> if (forward) -width / 12 else width / 12 }
                                            ) + androidx.compose.animation.fadeOut(tween(130))
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f).fillMaxWidth()
                                    .then(if (capturePageBackdrop) Modifier.layerBackdrop(backdrop) else Modifier)
                                    .clipToBounds(),
                                label = "MathFlowPageTransition"
                            ) { destinationPage ->
                                pageStateHolder.SaveableStateProvider(destinationPage) {
                                    Box(Modifier.fillMaxSize()) {
                                        when (destinationPage) {
                                            Page.HOME -> CalculatorHome(
                                                resultFormat = calculatorResultFormat,
                                                onResultFormatChange = { format ->
                                                    calculatorResultFormat = format
                                                    prefs.edit().putString("calculator_result_format", format).apply()
                                                }
                                            ) { expression, result, steps ->
                                                historyScope.launch {
                                                    historyMutex.lock()
                                                    try {
                                                        history = withContext(Dispatchers.IO) {
                                                            HistoryStore.add(context, expression, result, steps)
                                                        }
                                                    } finally {
                                                        historyMutex.unlock()
                                                    }
                                                }
                                            }
                                            Page.TOOLS -> ToolsPage()
                                            Page.SOLVER -> EquationSolverPage()
                                            Page.HISTORY -> HistoryPage(history) {
                                                historyScope.launch {
                                                    historyMutex.lock()
                                                    try {
                                                        withContext(Dispatchers.IO) {
                                                            HistoryStore.clear(context)
                                                        }
                                                        history = emptyList()
                                                    } finally {
                                                        historyMutex.unlock()
                                                    }
                                                }
                                            }
                                            Page.SETTINGS -> CompositionLocalProvider(LocalSettingsCornerRadius provides settingsCornerRadiusDp) {
                                            SettingsPage(
                                                currentConfig = makeConfigSnapshot(themeId, selectedIconId, fontScale, fontPath,
                                                    appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat, uiStyleId, iconPackId),
                                                configProfiles = configProfiles,
                                                activeConfigId = activeConfigId,
                                                onCreateConfig = { name ->
                                                    val profile = MathFlowConfigProfiles.create(name, makeConfigSnapshot(themeId, selectedIconId, fontScale,
                                                        fontPath, appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat, uiStyleId, iconPackId))
                                                    configProfiles = configProfiles + profile
                                                    activeConfigId = profile.id
                                                    prefs.edit().putString("mathflow_active_config", profile.id).apply()
                                                    MathFlowConfigProfiles.save(context, configProfiles)
                                                },
                                                onRenameConfig = { id, name ->
                                                    configProfiles = configProfiles.map { if (it.id == id) it.copy(name = name.trim().take(48)) else it }
                                                    MathFlowConfigProfiles.save(context, configProfiles)
                                                },
                                                onDeleteConfig = { id ->
                                                    configProfiles = configProfiles.filterNot { it.id == id }
                                                    if (activeConfigId == id) {
                                                        activeConfigId = null
                                                        prefs.edit().remove("mathflow_active_config").apply()
                                                    }
                                                    MathFlowConfigProfiles.save(context, configProfiles)
                                                },
                                                onApplyConfig = { profile ->
                                                    val cfg = validateConfigSnapshot(profile.settings)
                                                    themeId = cfg.getString("theme")
                                                    iconPackId = AppIconPack.fromId(cfg.optString("iconPack", "material")).id
                                                    uiStyleId = MathFlowUiStyle.fromId(cfg.optString("uiStyle", "material_you")).id
                                                    selectedIconId = cfg.getString("icon")
                                                    fontScale = cfg.getDouble("fontScale").toFloat()
                                                    appLanguage = cfg.getString("language")
                                                    measurementSystem = cfg.getString("measurement")
                                                    calculatorResultFormat = cfg.getString("resultFormat")
                                                    val importedFontPath = cfg.optString("fontPath").takeIf { it.isNotBlank() && it != "null" }
                                                    fontPath = importedFontPath
                                                    keyAnimationSettings = KeyAnimationSettings(
                                                        KeyAnimationStyle.fromId(cfg.getString("keyStyle")), cfg.getInt("keyDuration"),
                                                        cfg.getDouble("keyOpacity").toFloat(), cfg.getDouble("keyIntensity").toFloat(),
                                                        cfg.getBoolean("keyColorEnabled"), cfg.getLong("keyPressColor")
                                                    ).normalized()
                                                    bottomNavAnimationSettings = BottomNavAnimationSettings(
                                                        cfg.getBoolean("navEnabled"), BottomNavAnimationStyle.fromId(cfg.getString("navStyle")),
                                                        cfg.getInt("navDuration"), cfg.getDouble("navIntensity").toFloat(),
                                                        cfg.getDouble("navIconScale").toFloat(), cfg.getDouble("navOpacity").toFloat(),
                                                        cfg.optDouble("navRefraction", 0.35).toFloat(),
                                                        cfg.optDouble("navRoundness", 0.96).toFloat(),
                                                        cfg.optDouble("navBlur", 8.0).toFloat(),
                                                        cfg.optDouble("navDepth", 0.70).toFloat(),
                                                        cfg.optDouble("navHighlight", 0.90).toFloat(),
                                                        cfg.optDouble("navShadow", 0.42).toFloat(),
                                                        BottomNavSurfaceMaterial.fromId(cfg.optString("navSurfaceMaterial", "frosted")),
                                                        cfg.optDouble("matteOpacity", 0.72).toFloat(),
                                                        cfg.optDouble("matteBlur", 12.0).toFloat(),
                                                        cfg.optDouble("matteDepth", 0.38).toFloat(),
                                                        cfg.optDouble("matteHighlight", 0.58).toFloat(),
                                                        cfg.optDouble("matteShadow", 0.32).toFloat()
                                                    ).normalized()
                                                    prefs.edit()
                                                        .putString("app_theme", themeId).putString("ui_style", uiStyleId)
                                                        .putString("app_icon", selectedIconId)
                                                        .putString("app_icon_pack", iconPackId)
                                                        .putFloat("font_scale", fontScale).putString("app_language", appLanguage)
                                                        .putString("measurement_system", measurementSystem)
                                                        .putString("font_path", fontPath)
                                                        .putBoolean("font_selection_initialized", true)
                                                        .putString("key_animation_style", keyAnimationSettings.style.id)
                                                        .putInt("key_animation_duration", keyAnimationSettings.durationMs)
                                                        .putFloat("key_animation_opacity", keyAnimationSettings.opacity)
                                                        .putFloat("key_animation_intensity", keyAnimationSettings.intensity)
                                                        .putBoolean("key_animation_color_enabled", keyAnimationSettings.colorEffectEnabled)
                                                        .putLong("key_animation_press_color", keyAnimationSettings.pressColorArgb)
                                                        .putBoolean("nav_animation_enabled", bottomNavAnimationSettings.enabled)
                                                        .putString("nav_animation_style", bottomNavAnimationSettings.style.id)
                                                        .putInt("nav_animation_duration", bottomNavAnimationSettings.durationMs)
                                                        .putFloat("nav_animation_intensity", bottomNavAnimationSettings.intensity)
                                                        .putFloat("nav_animation_icon_scale", bottomNavAnimationSettings.iconScale)
                                                        .putFloat("nav_animation_opacity", bottomNavAnimationSettings.opacity)
                                                        .putFloat("nav_refraction_strength", bottomNavAnimationSettings.refractionStrength)
                                                        .putFloat("nav_droplet_roundness", bottomNavAnimationSettings.dropletRoundness)
                                                        .putFloat("nav_glass_blur", bottomNavAnimationSettings.glassBlur)
                                                        .putFloat("nav_glass_depth", bottomNavAnimationSettings.glassDepth)
                                                        .putFloat("nav_highlight_strength", bottomNavAnimationSettings.highlightStrength)
                                                        .putFloat("nav_shadow_strength", bottomNavAnimationSettings.shadowStrength)
                                                        .putString("nav_surface_material", bottomNavAnimationSettings.surfaceMaterial.id)
                                                        .putFloat("matte_glass_opacity", bottomNavAnimationSettings.matteOpacity)
                                                        .putFloat("matte_glass_blur", bottomNavAnimationSettings.matteBlur)
                                                        .putFloat("matte_glass_depth", bottomNavAnimationSettings.matteDepth)
                                                        .putFloat("matte_glass_highlight", bottomNavAnimationSettings.matteHighlight)
                                                        .putFloat("matte_glass_shadow", bottomNavAnimationSettings.matteShadow)
                                                        .putString("calculator_result_format", calculatorResultFormat).apply()
                                                    runCatching { setLauncherIcon(context, selectedIconId) }
                                                    activeConfigId = profile.id
                                                    prefs.edit().putString("mathflow_active_config", profile.id).apply()
                                                },
                                                onImportConfig = { imported ->
                                                    val profile = withContext(Dispatchers.IO) {
                                                        val cfg = validateConfigSnapshot(imported.settings)
                                                        val importedFontSetting = cfg.optString("fontPath")
                                                        cfg.put("fontPath", if (importedFontSetting == BUILTIN_ARTEGRA_FONT) BUILTIN_ARTEGRA_FONT else JSONObject.NULL)
                                                        val installedPath = MathFlowConfigProfiles.installFont(context, imported.fontFileName, imported.fontBytes)
                                                        if (installedPath != null) cfg.put("fontPath", installedPath)
                                                        MathFlowConfigProfiles.create(imported.name, cfg)
                                                    }
                                                    configProfiles = configProfiles + profile
                                                    MathFlowConfigProfiles.save(context, configProfiles)
                                                    profile
                                                },
                                                externalConfigUri = externalConfigUri.takeUnless { showWelcome },
                                                onExternalConfigHandled = onExternalConfigHandled,
                                                onDuplicateConfig = { source ->
                                                    val duplicate = MathFlowConfigProfiles.create(source.name + " copy", source.settings)
                                                    configProfiles = configProfiles + duplicate
                                                    MathFlowConfigProfiles.save(context, configProfiles)
                                                },
                                                selectedThemeId = themeId,
                                                selectedUiStyleId = uiStyleId,
                                                selectedIconPackId = iconPackId,
                                                onIconPackSelected = { id ->
                                                    iconPackId = AppIconPack.fromId(id).id
                                                    prefs.edit().putString("app_icon_pack", iconPackId).apply()
                                                },
                                                settingsCornerRadiusDp = settingsCornerRadiusDp,
                                                onSettingsCornerRadiusChange = { radius ->
                                                    settingsCornerRadiusDp = radius.coerceIn(0, 40)
                                                    prefs.edit().putInt("settings_corner_radius_dp", settingsCornerRadiusDp).apply()
                                                },
                                                onUiStyleSelected = { id ->
                                                    uiStyleId = MathFlowUiStyle.fromId(id).id
                                                    prefs.edit().putString("ui_style", uiStyleId).apply()
                                                },
                                                selectedIconId = selectedIconId,
                                                onThemeSelected = { id ->
                                                    themeId = id
                                                    prefs.edit().putString("app_theme", id).apply()
                                                },
                                                onIconSelected = { id ->
                                                    runCatching { setLauncherIcon(context, id) }
                                                    selectedIconId = id
                                                    prefs.edit().putString("app_icon", id).apply()
                                                },
                                                fontScale = fontScale,
                                                fontPath = fontPath,
                                                selectedLanguage = appLanguage,
                                                selectedMeasurementSystem = measurementSystem,
                                                onMeasurementSystemSelected = { mode ->
                                                    measurementSystem = mode
                                                    prefs.edit().putString("measurement_system", mode).apply()
                                                },
                                                onLanguageSelected = { tag ->
                                                    appLanguage = tag
                                                    prefs.edit().putString("app_language", tag).apply()
                                                },
                                                onFontScale = {
                                                    fontScale = it
                                                    prefs.edit().putFloat("font_scale", it).apply()
                                                },
                                                onFontSelected = { path ->
                                                    fontPath = path
                                                    prefs.edit().putString("font_path", path).apply()
                                                },
                                                onResetFont = {
                                                    fontPath = null
                                                    prefs.edit().remove("font_path")
                                                        .putBoolean("font_selection_initialized", true)
                                                        .apply()
                                                },
                                                appliedAnimationSettings = keyAnimationSettings,
                                                onAnimationSettingsApplied = { settings ->
                                                    val normalized = settings.normalized()
                                                    keyAnimationSettings = normalized
                                                    prefs.edit()
                                                        .putString("key_animation_style", normalized.style.id)
                                                        .putInt("key_animation_duration", normalized.durationMs)
                                                        .putFloat("key_animation_opacity", normalized.opacity)
                                                        .putFloat("key_animation_intensity", normalized.intensity)
                                                        .putBoolean("key_animation_color_enabled", normalized.colorEffectEnabled)
                                                        .putLong("key_animation_press_color", normalized.pressColorArgb)
                                                        .apply()
                                                },
                                                appliedNavAnimationSettings = bottomNavAnimationSettings,
                                                onNavAnimationSettingsApplied = { settings ->
                                                    val normalized = settings.normalized()
                                                    bottomNavAnimationSettings = normalized
                                                    prefs.edit()
                                                        .putBoolean("nav_animation_enabled", normalized.enabled)
                                                        .putString("nav_animation_style", normalized.style.id)
                                                        .putInt("nav_animation_duration", normalized.durationMs)
                                                        .putFloat("nav_animation_intensity", normalized.intensity)
                                                        .putFloat("nav_animation_icon_scale", normalized.iconScale)
                                                        .putFloat("nav_animation_opacity", normalized.opacity)
                                                         .putFloat("nav_refraction_strength", normalized.refractionStrength)
                                                         .putFloat("nav_droplet_roundness", normalized.dropletRoundness)
                                                         .putFloat("nav_glass_blur", normalized.glassBlur)
                                                         .putFloat("nav_glass_depth", normalized.glassDepth)
                                                         .putFloat("nav_highlight_strength", normalized.highlightStrength)
                                                         .putFloat("nav_shadow_strength", normalized.shadowStrength)
                                                        .putString("nav_surface_material", normalized.surfaceMaterial.id)
                                                        .putFloat("matte_glass_opacity", normalized.matteOpacity)
                                                        .putFloat("matte_glass_blur", normalized.matteBlur)
                                                        .putFloat("matte_glass_depth", normalized.matteDepth)
                                                        .putFloat("matte_glass_highlight", normalized.matteHighlight)
                                                        .putFloat("matte_glass_shadow", normalized.matteShadow)
                                                        .apply()
                                                }
                                            )
                                            }
                                        }
                                    }
                                }
                            }
                            BottomNav(page, bottomNavAnimationSettings, backdrop = backdrop) { page = it }
                        }
                        if (showWelcome && splashFinished) {
                            FirstLaunchWelcome(
                                palette = palette,
                                language = appLanguage,
                                measurementSystem = measurementSystem,
                                themeId = themeId,
                                selectedIconId = selectedIconId,
                                fontPath = fontPath,
                                fontScale = fontScale,
                                onLanguageSelected = { tag ->
                                    appLanguage = tag
                                    prefs.edit().putString("app_language", tag).apply()
                                },
                                onMeasurementSystemSelected = { mode ->
                                    measurementSystem = mode
                                    prefs.edit().putString("measurement_system", mode).apply()
                                },
                                onThemeSelected = { id ->
                                    themeId = id
                                    prefs.edit().putString("app_theme", id).apply()
                                },
                                onIconSelected = { id ->
                                    runCatching { setLauncherIcon(context, id) }
                                    selectedIconId = id
                                    prefs.edit().putString("app_icon", id).apply()
                                },
                                onFontSelected = { selected ->
                                    fontPath = selected
                                    prefs.edit().putString("font_path", selected).putBoolean("font_selection_initialized", true).apply()
                                },
                                onFontScaleSelected = { scale ->
                                    fontScale = scale
                                    prefs.edit().putFloat("font_scale", scale).apply()
                                },
                                onConfirm = {
                                    prefs.edit().putBoolean("welcome_seen", true).putInt("welcome_version", 3).apply()
                                    showWelcome = false
                                }
                            )
                        }
                        if (!splashFinished) {
                            MathFlowIntroSplash(palette = palette, onFinished = { splashFinished = true })
                        }
                    }
                }
            }
        }
    }
}

 
@Composable
private fun FirstLaunchWelcome(
    palette: Palette,
    language: String,
    measurementSystem: String,
    themeId: String,
    selectedIconId: String,
    fontPath: String?,
    fontScale: Float,
    onLanguageSelected: (String) -> Unit,
    onMeasurementSystemSelected: (String) -> Unit,
    onThemeSelected: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onFontSelected: (String?) -> Unit,
    onFontScaleSelected: (Float) -> Unit,
    onConfirm: () -> Unit
) {
    var languagePickerOpen by remember { mutableStateOf(false) }
    var languageQuery by rememberSaveable { mutableStateOf("") }
    var pendingIconId by rememberSaveable { mutableStateOf(selectedIconId) }
    val activeLanguage = AppLanguages.firstOrNull { it.tag == language } ?: AppLanguages.first()
    val p = palette
    val uiStyle = LocalMathFlowUiStyle.current
    Dialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp).heightIn(max = 760.dp)
                .clip(RoundedCornerShape(uiStyle.cardRadiusDp.dp))
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(p.surface.copy(alpha = 0.88f), p.surface.copy(alpha = 0.74f))
                ))
                .border(1.dp, p.text.copy(alpha = 0.12f), RoundedCornerShape(uiStyle.cardRadiusDp.dp)),
            shape = RoundedCornerShape(uiStyle.cardRadiusDp.dp),
            color = Color.Transparent,
            contentColor = p.text,
            shadowElevation = 14.dp
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp), color = p.key, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(painterResource(R.drawable.mark_black), null, Modifier.size(29.dp), colorFilter = ColorFilter.tint(p.accent))
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("MATHFLOW", color = p.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(measurementText("welcome_title", language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(measurementText("welcome_desc", language), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("language", language), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("01", color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().mathFlowClickable { languagePickerOpen = true },
                        shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
                        color = p.key,
                        border = androidx.compose.foundation.BorderStroke(1.dp, p.accent.copy(alpha = 0.34f))
                    ) {
                        Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = p.navActive, modifier = Modifier.size(38.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(activeLanguage.tag.uppercase(Locale.ROOT), color = p.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(activeLanguage.nativeName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(activeLanguage.englishName, fontSize = 11.sp, color = p.muted)
                            }
                            AppIcon(AppGlyph.DROPDOWN, null, tint = p.accent)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("measurements", language), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("02", color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    listOf("auto", "metric", "us").forEach { mode ->
                        val selected = measurementSystem == mode
                        val optionBackground by animateColorAsState(
                            if (selected) p.navActive else p.key,
                            animationSpec = tween(190, easing = FastOutSlowInEasing),
                            label = "MeasurementOptionBackground"
                        )
                        val optionBorder by animateColorAsState(
                            if (selected) p.accent else Color.Transparent,
                            animationSpec = tween(190, easing = FastOutSlowInEasing),
                            label = "MeasurementOptionBorder"
                        )
                        val title = when (mode) {
                            "auto" -> measurementText("automatic", language)
                            "metric" -> measurementText("metric", language)
                            else -> measurementText("us_customary", language)
                        }
                        val description = when (mode) {
                            "auto" -> measurementText("automatic_desc", language)
                            "metric" -> measurementText("metric_desc", language)
                            else -> measurementText("us_desc", language)
                        }
                        Surface(
                            Modifier.fillMaxWidth().mathFlowClickable { onMeasurementSystemSelected(mode) },
                            shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
                            color = optionBackground,
                            border = androidx.compose.foundation.BorderStroke(if (selected) 1.4.dp else 1.dp, optionBorder)
                        ) {
                            Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(description, fontSize = 10.sp, color = p.muted)
                                }
                                if (selected) AppIcon(AppGlyph.CHECK, null, tint = p.accent)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("appearance", language), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("03", color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    ThemeOptions.forEach { option ->
                        ThemeOptionCard(
                            option = option,
                            selected = themeId == option.id,
                            applied = themeId == option.id,
                            modifier = Modifier.fillMaxWidth()
                        ) { onThemeSelected(option.id) }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tx("app_icons"), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(tx("icon_hint"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    AppIconOptions.forEach { option ->
                        IconOptionCard(
                            option = option,
                            selected = pendingIconId == option.id,
                            applied = selectedIconId == option.id,
                            modifier = Modifier.fillMaxWidth()
                        ) { pendingIconId = option.id }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tx("fonts"), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(BUILTIN_ARTEGRA_FONT to "Artegra Soft Global", null to "System").forEach { (id, title) ->
                            val selected = fontPath == id
                            val fontCardColor by animateColorAsState(
                                if (selected) p.navActive else p.key,
                                animationSpec = tween(190, easing = FastOutSlowInEasing),
                                label = "FontCardColor"
                            )
                            val fontBorderColor by animateColorAsState(
                                if (selected) p.accent else Color.Transparent,
                                animationSpec = tween(190, easing = FastOutSlowInEasing),
                                label = "FontCardBorder"
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth().animateContentSize(tween(180))
                                    .mathFlowClickable { onFontSelected(id) },
                                shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
                                color = fontCardColor,
                                border = androidx.compose.foundation.BorderStroke(if (selected) 1.5.dp else 1.dp, fontBorderColor)
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text("Aa", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = p.accent)
                                    Text(title, fontSize = 11.sp, color = p.text, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tx("text_size"), color = p.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("${(fontScale * 100).toInt()}%", color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { onFontScaleSelected((kotlin.math.round(it * 20f) / 20f).coerceIn(0.85f, 1.25f)) },
                        valueRange = 0.85f..1.25f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(uiStyle.controlRadiusDp.dp))
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(p.accent.copy(alpha = 0.94f), p.accent.copy(alpha = 0.72f))
                        ))
                        .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(uiStyle.controlRadiusDp.dp))
                        .mathFlowClickable {
                            if (pendingIconId != selectedIconId) onIconSelected(pendingIconId)
                            onConfirm()
                        },
                    shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
                    color = Color.Transparent
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("continue", language), modifier = Modifier.weight(1f), color = if (p.dark) Color(0xFF17131F) else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        AppIcon(AppGlyph.CHECK, null, tint = if (p.dark) Color(0xFF17131F) else Color.White)
                    }
                }
            }
        }
    }

    if (languagePickerOpen) {
        Dialog(
            onDismissRequest = { languagePickerOpen = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 620.dp)
                    .clip(RoundedCornerShape(uiStyle.cardRadiusDp.dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(p.surface.copy(alpha = 0.90f), p.surface.copy(alpha = 0.76f))
                    ))
                    .border(1.dp, p.text.copy(alpha = 0.12f), RoundedCornerShape(uiStyle.cardRadiusDp.dp)),
                shape = RoundedCornerShape(uiStyle.cardRadiusDp.dp),
                color = Color.Transparent,
                contentColor = p.text
            ) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(measurementText("language", language), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(measurementText("languages_available", language), color = p.muted, fontSize = 11.sp)
                        }
                        Text(activeLanguage.tag.uppercase(Locale.ROOT), color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = languageQuery,
                        onValueChange = { languageQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(tx("search_languages")) },
                        shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp)
                    )
                    val filtered = AppLanguages.filter {
                        it.nativeName.contains(languageQuery, ignoreCase = true) ||
                            it.englishName.contains(languageQuery, ignoreCase = true) ||
                            it.tag.contains(languageQuery, ignoreCase = true)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f, fill = false)) {
                        items(filtered) { item ->
                            val selected = item.tag == language
                            val languageCardColor by animateColorAsState(
                                if (selected) p.navActive else p.key,
                                animationSpec = tween(180, easing = FastOutSlowInEasing),
                                label = "LanguageCardColor"
                            )
                            val languageBorderColor by animateColorAsState(
                                if (selected) p.accent else Color.Transparent,
                                animationSpec = tween(180, easing = FastOutSlowInEasing),
                                label = "LanguageCardBorder"
                            )
                            Surface(
                                modifier = Modifier.fillMaxWidth().animateContentSize(tween(180))
                                    .mathFlowClickable {
                                    onLanguageSelected(item.tag)
                                    languageQuery = ""
                                    languagePickerOpen = false
                                },
                                shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
                                color = languageCardColor,
                                border = androidx.compose.foundation.BorderStroke(if (selected) 1.3.dp else 1.dp, languageBorderColor)
                            ) {
                                Row(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp), color = p.surface, modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(item.tag.uppercase(Locale.ROOT), color = p.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column(Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(item.nativeName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(item.englishName, fontSize = 11.sp, color = p.muted)
                                    }
                                    if (selected) AppIcon(AppGlyph.CHECK, null, tint = p.accent)
                                }
                            }
                        }
                    }
                    if (filtered.isEmpty()) Text(tx("no_languages_found"), color = p.muted, fontSize = 13.sp)
                    MathFlowTextButton(tx("cancel"), p.accent, Modifier.align(Alignment.End)) {
                        languageQuery = ""
                        languagePickerOpen = false
                    }
                }
            }
        }
    }
}

private const val NAV_TRANSITION_DURATION_MS = 180

@Composable
private fun BottomNav(
    current: Page,
    settings: BottomNavAnimationSettings,
    backdrop: Backdrop? = null,
    preview: Boolean = false,
    onSelect: (Page) -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    // Liquid Glass navigation is active only when Liquid Glass is the global UI style.
    // Every other style keeps its own existing navigation surface and indicator.
    val navLiquidPanel = uiStyle.glassSurface
    // The preview uses the same global style gate as the real navigation bar.
    // It receives a backdrop only to render Liquid Glass when that style is selected.
    val useRefractiveLens = uiStyle.usesRefractiveNavigation(backdrop != null)
    val style = settings.style
    val duration = if (settings.enabled) settings.durationMs else 0

    val springMotion = settings.enabled && (
        useRefractiveLens || style in setOf(
            BottomNavAnimationStyle.FLUID_PILL,
            BottomNavAnimationStyle.SPRING_MOTION,
            BottomNavAnimationStyle.ELASTIC_SLIDE
        )
    )
    // Compose springs do not have a fixed duration. Map the duration control to
    // spring stiffness so slower settings remain visibly slower in spring styles.
    val springStiffness = bottomNavSpringStiffness(duration)
    Box(
        Modifier.fillMaxWidth()
            // Only the real app navigation observes the Android system inset.
            // A preview lives inside a bounded card and must remain self-contained.
            .then(if (preview) Modifier else Modifier.navigationBarsPadding())
            .padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val panelShape = RoundedCornerShape(if (navLiquidPanel) 35.dp else uiStyle.cardRadiusDp.dp)
        val barGlassBackdrop = if (useRefractiveLens) rememberLayerBackdrop() else null
        // The panel and its content are siblings: the moving lens must not inherit
        // Surface's rounded clipping and must be able to rise above the panel.
        Box {
        Surface(
            modifier = Modifier
                .then(
                    if (navLiquidPanel && useRefractiveLens) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop!!,
                            shape = { panelShape },
                            effects = {
                                // Fixed RX Playa 1.9.1 panel recipe; settings are
                                // intentionally not wired into this renderer pass.
                                colorControls(saturation = 1.5f)
                                // RX Playa renders the panel backdrop at 1/3 resolution.
                                // Scale pixel-based effects by the same factor so their
                                // apparent dp size remains unchanged after upscaling.
                                blur(8.dp.toPx() * 0.33f)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    lens(
                                        24.dp.toPx() * 0.33f,
                                        24.dp.toPx() * 0.33f,
                                        depthEffect = true,
                                        chromaticAberration = false
                                    )
                                }
                            },
                            highlight = { Highlight.Default },
                            shadow = { Shadow.Default },
                            onDrawSurface = {
                                val tint = if (p.dark) Color(0xFF121212) else Color(0xFFFAFAFA)
                                drawRect(tint.copy(alpha = 0.4f))
                            },
                            exportedBackdrop = barGlassBackdrop,
                            backdropScale = 0.33f
                        )
                    } else {
                        Modifier.clip(panelShape).background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    p.surface.copy(alpha = 0.46f * settings.opacity),
                                    p.surface.copy(alpha = 0.30f * settings.opacity)
                                )
                            )
                        )
                    }
                )
                .then(
                    if (useRefractiveLens) Modifier
                    else Modifier.border(
                        if (navLiquidPanel) 0.8.dp else 1.dp,
                        p.text.copy(alpha = if (navLiquidPanel) settings.highlightStrength * 0.22f else settings.matteHighlight * 0.24f),
                        panelShape
                    )
                )
                .fillMaxWidth()
                .height(58.dp),
            shape = panelShape,
            color = if (navLiquidPanel) Color.Transparent else p.surface.copy(alpha = uiStyle.surfaceAlpha),
            // Keep the floating glass capsule clean: the panel itself must not cast
            // a broad grey halo. The selected droplet retains its own local depth.
            shadowElevation = if (navLiquidPanel) 0.dp else uiStyle.elevationDp.dp
        ) { }
            BoxWithConstraints(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                val itemWidth = maxWidth / Page.entries.size
                // One capture for the complete tab row replaces five per-icon
                // captures. The lens samples the glyphs as a single layer, matching
                // RX Playa's composition with substantially less render-target work.
                val tabRowBackdrop = if (useRefractiveLens) rememberLayerBackdrop() else null
                val density = LocalDensity.current
                val layoutDirection = LocalLayoutDirection.current
                val visualIndex = if (layoutDirection == LayoutDirection.Rtl) {
                    Page.entries.lastIndex - current.ordinal
                } else current.ordinal
                val indicatorWidth = minOf(
                    when {
                        navLiquidPanel -> itemWidth
                        style == BottomNavAnimationStyle.SOFT_FOCUS -> 54.dp
                        style == BottomNavAnimationStyle.RIPPLE_BLOOM -> 66.dp
                        else -> 72.dp
                    },
                    itemWidth * if (navLiquidPanel) 1f else 0.92f
                )
                val indicatorHeight = when {
                    navLiquidPanel -> 48.dp
                    style == BottomNavAnimationStyle.MORPH -> 38.dp
                    style == BottomNavAnimationStyle.SOFT_FOCUS -> 38.dp
                    else -> 42.dp
                }
                val targetOffset = with(density) {
                    (itemWidth * visualIndex + (itemWidth - indicatorWidth) / 2).toPx()
                }
                // The indicator always follows the duration slider. Spring-like character
                // is retained by the stretch and icon-scale effects below.
                val offsetSpec = if (springMotion) {
                    spring<Float>(dampingRatio = 0.82f, stiffness = springStiffness)
                } else {
                    tween<Float>(
                        durationMillis = duration.coerceAtLeast(1),
                        easing = FastOutSlowInEasing
                    )
                }
                // The refractive pill has its own RX spring. Do not run a second
                // Compose offset animation in parallel with that frame loop.
                val indicatorOffset = animateFloatAsState(
                    targetValue = if (useRefractiveLens) 0f else targetOffset,
                    animationSpec = offsetSpec,
                    label = "MathFlowNavIndicatorPosition"
                )
                val navScope = rememberCoroutineScope()
                val pillMotion = remember { LiquidGlassPillMotion(navScope, current.ordinal) }
                pillMotion.reduceMotion = !settings.enabled || !ValueAnimator.areAnimatorsEnabled()
                // Match RX's compositing order without re-recording the parent page:
                // page -> glass panel -> selected fill -> glyph row.
                val indicatorBackdrop = if (
                    useRefractiveLens && barGlassBackdrop != null && tabRowBackdrop != null
                ) {
                    rememberSelectionPillBackdrop(
                        backdrop!!, barGlassBackdrop, tabRowBackdrop, p.navActive
                    ) { pillMotion.material }
                } else null
                val tabStepPx = with(density) { itemWidth.toPx() }.coerceAtLeast(1f)
                pillMotion.tabStepDp = itemWidth.value
                LaunchedEffect(current, useRefractiveLens, settings.enabled) {
                    if (useRefractiveLens && settings.enabled) {
                        pillMotion.animateTo(current.ordinal)
                    } else {
                        pillMotion.snapTo(current.ordinal)
                    }
                }
                // Resting glass stays spatially stable. Motion is reserved for
                // touch, drag, and the existing navigation transition.
                val indicatorScaleX = animateFloatAsState(
                    targetValue = when {
                        useRefractiveLens || !settings.enabled -> 1f
                        style == BottomNavAnimationStyle.EXPAND -> 1f + settings.intensity * 0.12f
                        style == BottomNavAnimationStyle.ELASTIC_SLIDE -> 1f
                        else -> 1f
                    },
                    animationSpec = tween(duration, easing = FastOutSlowInEasing),
                    label = "MathFlowNavIndicatorScale"
                )
                val indicatorAlpha = animateFloatAsState(
                    targetValue = if (useRefractiveLens) 1f else if (style == BottomNavAnimationStyle.SOFT_FOCUS) {
                        settings.opacity * 0.62f
                    } else settings.opacity,
                    animationSpec = tween(duration),
                    label = "MathFlowNavIndicatorAlpha"
                )
                val bloom = remember(current, style) { Animatable(0.72f) }
                LaunchedEffect(current, style, settings.enabled, duration, settings.intensity, useRefractiveLens) {
                    if (!useRefractiveLens && settings.enabled && style in setOf(BottomNavAnimationStyle.RIPPLE_BLOOM, BottomNavAnimationStyle.EXPAND)) {
                        bloom.snapTo(
                            if (style == BottomNavAnimationStyle.RIPPLE_BLOOM) 0.08f + settings.intensity * 0.34f
                            else 0.38f + settings.intensity * 0.36f
                        )
                        bloom.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
                    } else {
                        bloom.snapTo(1f)
                    }
                }
                // Restore the original droplet-like elastic travel. The lens and
                // capsule stretch together, then settle with a soft spring rebound.
                val stretchAmount = when (style) {
                    BottomNavAnimationStyle.FLUID_PILL -> 0.28f
                    BottomNavAnimationStyle.SPRING_MOTION -> 0.16f
                    BottomNavAnimationStyle.MORPH -> 0.46f
                    BottomNavAnimationStyle.EXPAND -> 0.22f
                    BottomNavAnimationStyle.SOFT_FOCUS -> 0.08f
                    BottomNavAnimationStyle.ELASTIC_SLIDE -> 0.38f
                    BottomNavAnimationStyle.RIPPLE_BLOOM -> 0.18f
                }
                val travelStretch = remember(current, style) { Animatable(1f) }
                LaunchedEffect(current, style, settings.enabled, duration, settings.intensity, useRefractiveLens) {
                    if (!useRefractiveLens && settings.enabled && stretchAmount > 0f) {
                        travelStretch.snapTo(1f + settings.intensity * stretchAmount)
                        travelStretch.animateTo(
                            1f,
                            if (springMotion) {
                                spring<Float>(dampingRatio = 0.72f, stiffness = springStiffness * 0.72f)
                            } else {
                                tween(durationMillis = duration.coerceAtLeast(1), easing = FastOutSlowInEasing)
                            }
                        )
                    } else {
                        travelStretch.snapTo(1f)
                    }
                }
                if (!useRefractiveLens &&
                    (style != BottomNavAnimationStyle.SOFT_FOCUS || !settings.enabled)) {
                    Box(
                        Modifier.align(Alignment.CenterStart)
                            .graphicsLayer {
                                translationX = indicatorOffset.value
                                scaleX = if (style == BottomNavAnimationStyle.RIPPLE_BLOOM || style == BottomNavAnimationStyle.EXPAND) bloom.value else indicatorScaleX.value * travelStretch.value
                                alpha = indicatorAlpha.value
                            }
                            .size(
                                width = indicatorWidth,
                                height = indicatorHeight
                            )
                    ) {
                        Box(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(percent = 50))
                                .background(p.navActive)
                        )
                    }
                }
                if (useRefractiveLens && pillMotion.isFlat) {
                    Box(
                        Modifier.align(Alignment.CenterStart)
                            .graphicsLayer {
                                val visualPosition = if (layoutDirection == LayoutDirection.Rtl) {
                                    Page.entries.lastIndex - pillMotion.position
                                } else pillMotion.position
                                val cellWidthPx = with(density) { itemWidth.toPx() }
                                val pillWidthPx = with(density) { indicatorWidth.toPx() }
                                translationX = visualPosition * cellWidthPx + (cellWidthPx - pillWidthPx) * 0.5f
                            }
                            .size(width = indicatorWidth, height = indicatorHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(p.navActive)
                    )
                }
                Row(
                    Modifier.fillMaxWidth()
                        .then(if (tabRowBackdrop != null) Modifier.layerBackdrop(tabRowBackdrop) else Modifier)
                        .then(
                            if (useRefractiveLens) {
                                // RX keeps the gesture detector alive for the whole row.
                                // Updated values flow through state holders, so a page
                                // recomposition cannot cancel a drag midway through.
                                val currentSelectedIndex by rememberUpdatedState(current.ordinal)
                                val currentLayoutDirection by rememberUpdatedState(layoutDirection)
                                val currentOnSelect by rememberUpdatedState(onSelect)
                                Modifier.pointerInput(Unit) {
                                    var dragStartIndex = currentSelectedIndex.toFloat()
                                    var dragDistance = 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            dragStartIndex = pillMotion.position
                                            dragDistance = 0f
                                            pillMotion.startDrag()
                                        },
                                        onDragEnd = {
                                            val logicalIndex = LiquidGlassPillGeometry.dragTargetIndex(
                                                startIndex = dragStartIndex,
                                                dragDistancePx = dragDistance,
                                                tabStepPx = currentTabStepPx,
                                                tabCount = Page.entries.size,
                                                isRtl = currentLayoutDirection == LayoutDirection.Rtl
                                            ).roundToInt()
                                            pillMotion.release(logicalIndex)
                                            if (logicalIndex != currentSelectedIndex) {
                                                currentOnSelect(Page.entries[logicalIndex])
                                            }
                                        },
                                        onDragCancel = {
                                            pillMotion.release(currentSelectedIndex)
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragDistance += dragAmount
                                            pillMotion.dragTo(
                                                LiquidGlassPillGeometry.dragTargetIndex(
                                                    startIndex = dragStartIndex,
                                                    dragDistancePx = dragDistance,
                                                    tabStepPx = currentTabStepPx,
                                                    tabCount = Page.entries.size,
                                                    isRtl = currentLayoutDirection == LayoutDirection.Rtl
                                                )
                                            )
                                        }
                                    )
                                }
                            } else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Page.entries.forEach { item ->
                        val isSelected = item == current
                        val tint by animateColorAsState(
                            targetValue = if (isSelected) p.accent else p.accent.copy(alpha = 0.74f),
                            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconTint"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = when {
                                useRefractiveLens && isSelected -> 1.12f
                                settings.enabled && isSelected -> 1f + settings.iconScale * (0.55f + settings.intensity * 0.25f)
                                else -> 1f
                            },
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconScale"
                        )
                        val morphY by animateFloatAsState(
                            targetValue = if (settings.enabled && isSelected && !useRefractiveLens && style == BottomNavAnimationStyle.MORPH) {
                                1f - settings.iconScale * (0.18f + settings.intensity * 0.22f)
                            } else 1f,
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconMorph"
                        )
                        val focusAlpha by animateFloatAsState(
                            targetValue = if (settings.enabled && isSelected && style == BottomNavAnimationStyle.SOFT_FOCUS) {
                                settings.opacity * 0.72f
                            } else 0f,
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavSoftFocusAlpha"
                        )
                        Box(
                            Modifier.weight(1f).height(48.dp)
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                }
                                .mathFlowClickable {
                                    if (!isSelected) onSelect(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (style == BottomNavAnimationStyle.SOFT_FOCUS && settings.enabled) {
                                Box(
                                    Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 5.dp)
                                        .graphicsLayer { alpha = focusAlpha }
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(p.navActive)
                                )
                            }
                            // Fade the sharp glyph according to the moving droplet's actual
                            // overlap. This crossfades continuously instead of removing it
                            // as soon as the destination tab becomes isSelected.
                            AppIcon(
                                glyph = item.glyph,
                                contentDescription = displayText(item.title),
                                tint = tint,
                                modifier = Modifier.size(26.dp).graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale * morphY
                                }
                            )
                        }
                    }
                }
                if (useRefractiveLens && !pillMotion.isFlat) {
                    Box(
                        Modifier.align(Alignment.CenterStart)
                            .size(width = itemWidth, height = indicatorHeight)
                            // Keep the measured bounds constant during motion. The old
                            // custom layout changed constraints every frame, forcing a
                            // measure/layout pass while the lens was also being redrawn.
                            // A graphics-layer transform preserves the same lifted shape
                            // while keeping the animation on the render layer.
                            .graphicsLayer {
                                val cellWidthPx = with(density) { itemWidth.toPx() }
                                val restWidthPx = cellWidthPx.coerceAtLeast(1f)
                                val restHeightPx = with(density) { indicatorHeight.toPx() }.coerceAtLeast(1f)
                                val visualPosition = if (layoutDirection == LayoutDirection.Rtl) {
                                    Page.entries.lastIndex - pillMotion.position
                                } else pillMotion.position
                                val lifted = LiquidGlassPillGeometry.liftedSize(
                                    restWidthPx,
                                    restHeightPx,
                                    with(density) { 5.dp.toPx() },
                                    with(density) { 5.dp.toPx() },
                                    with(density) { 9.dp.toPx() }
                                )
                                val deviation = pillMotion.deviation
                                translationX = visualPosition * cellWidthPx
                                scaleX = (1f + (lifted.first / restWidthPx - 1f) * pillMotion.liftX) *
                                    (1f + deviation)
                                scaleY = (1f + (lifted.second / restHeightPx - 1f) * pillMotion.liftY) *
                                    (1f - deviation)
                            }
                            .drawBackdrop(
                                backdrop = indicatorBackdrop ?: backdrop!!,
                                shape = {
                                     RoundedCornerShape(percent = 50)
                                },
                                effects = {
                                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                         lens(
                                             12.dp.toPx() * pillMotion.glassPresence,
                                             14.dp.toPx() * pillMotion.material,
                                             chromaticAberration = true
                                         )
                                     }
                                 },
                                 highlight = {
                                     val rim = ((pillMotion.glassPresence - 0.45f) / 0.55f).coerceIn(0f, 1f)
                                     Highlight.Default.copy(
                                         alpha = pillMotion.material * rim
                                     )
                                 },
                                 shadow = {
                                     val rim = ((pillMotion.glassPresence - 0.45f) / 0.55f).coerceIn(0f, 1f)
                                     Shadow(
                                          radius = 12.dp,
                                          offset = DpOffset(0.dp, 2.dp),
                                          color = Color.Black.copy(alpha = 0.3f)
                                      ).copy(alpha = pillMotion.material * rim)
                                 },
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberSelectionPillBackdrop(
    page: Backdrop,
    panel: Backdrop,
    tabs: Backdrop,
    fillColor: Color,
    material: () -> Float
): Backdrop {
    val currentFill by rememberUpdatedState(fillColor)
    return remember(page, panel, tabs) {
        SelectionPillBackdrop(page, panel, tabs) {
            val color = currentFill
            color.copy(alpha = color.alpha * (1f - material()))
        }
    }
}

/** RX-style ordered sampling: page, panel glass, fading selection fill, then glyphs. */
private class SelectionPillBackdrop(
    private val page: Backdrop,
    private val panel: Backdrop,
    private val tabs: Backdrop,
    private val fill: () -> Color
) : Backdrop {
    override val isCoordinatesDependent: Boolean = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        with(page) { drawBackdrop(density, coordinates, layerBlock) }
        with(panel) { drawBackdrop(density, coordinates, layerBlock) }
        val color = fill()
        if (color.alpha > 0f) {
            drawRect(
                color = color,
                topLeft = Offset(-size.width, -size.height),
                size = Size(size.width * 3f, size.height * 3f)
            )
        }
        with(tabs) { drawBackdrop(density, coordinates, layerBlock) }
    }
}

// ---------------------------------------------------------------- calculator

private val OPERATORS = setOf("÷", "×", "−", "+")
private val CALCULATOR_MAIN_ROWS = listOf(
    listOf("AC", "(", ")", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("%", "0", ".", "=")
)
private val CALCULATOR_SCIENTIFIC_ROWS = listOf(
    listOf("sin", "cos", "tan", "ln"),
    listOf("log", "abs", "π", "e"),
    listOf("√", "x²", "xʸ", "⌫")
) + CALCULATOR_MAIN_ROWS

@Composable
private fun CalculatorHome(
    resultFormat: String,
    onResultFormatChange: (String) -> Unit,
    onCalculationSaved: (String, String, List<String>) -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val calculationScope = rememberCoroutineScope()
    var calculationJob by remember { mutableStateOf<Job?>(null) }
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("0") }
    var decimalResult by rememberSaveable { mutableStateOf("0") }
    var fractionResult by rememberSaveable { mutableStateOf("0") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var steps by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showSteps by rememberSaveable { mutableStateOf(true) }
    var scientific by rememberSaveable { mutableStateOf(false) }
    var degrees by rememberSaveable { mutableStateOf(true) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var continuation by rememberSaveable { mutableStateOf("") }

    fun clearAll() {
        calculationJob?.cancel()
        calculationJob = null
        expression = ""; result = "0"; decimalResult = "0"; fractionResult = "0"
        message = null; steps = emptyList()
        finished = false; continuation = ""
    }

    fun append(text: String, continuesResult: Boolean) {
        calculationJob?.cancel()
        calculationJob = null
        if (finished) {
            expression = if (continuesResult && continuation.isNotEmpty()) continuation + text else text
            finished = false
            message = null
            steps = emptyList()
        } else {
            expression += text
        }
    }

    fun evaluate() {
        if (expression.isBlank()) return
        val input = expression
        val open = input.count { it == '(' } - input.count { it == ')' }
        val source = if (open > 0) input + ")".repeat(open) else input
        val angleMode = if (degrees) AngleMode.DEGREES else AngleMode.RADIANS
        calculationJob?.cancel()
        calculationJob = calculationScope.launch {
            try {
                val r = withContext(Dispatchers.Default) {
                    ExpressionEngine(angleMode).calculate(source)
                }
                fractionResult = r.displayValue
                decimalResult = r.decimalValue
                result = if (resultFormat == "decimal") decimalResult else fractionResult
                steps = r.steps
                message = null
                continuation = r.exactValue?.let {
                    if (it.isInteger()) it.numerator.toString() else "(${it.numerator}/${it.denominator})"
                } ?: r.displayValue
                finished = true
                onCalculationSaved(source, r.displayValue, r.steps)
                expression = source
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                result = "Ошибка"
                fractionResult = "Ошибка"
                decimalResult = "Ошибка"
                message = when {
                    e.message?.contains("Division by zero") == true -> "Деление на ноль"
                    else -> e.message ?: "Проверьте выражение"
                }
                steps = emptyList()
            } catch (_: Exception) {
                result = "Ошибка"
                fractionResult = "Ошибка"
                decimalResult = "Ошибка"
                message = "Не удалось вычислить выражение"
                steps = emptyList()
            }
        }
    }

    fun press(key: String) {
        if (key != "=") {
            calculationJob?.cancel()
            calculationJob = null
        }
        when (key) {
            "AC" -> clearAll()
            "⌫" -> {
                if (finished) clearAll() else {
                    expression = when {
                        expression.endsWith("sqrt(") -> expression.dropLast(5)
                        expression.endsWith("pi") -> expression.dropLast(2)
                        expression.length >= 4 && Regex("(sin|cos|tan|abs|log)\\($").containsMatchIn(expression) ->
                            expression.dropLast(4)
                        expression.endsWith("ln(") -> expression.dropLast(3)
                        else -> expression.dropLast(1)
                    }
                    if (expression.isEmpty()) { result = "0"; message = null }
                }
            }
            "=" -> evaluate()
            "√" -> append("sqrt(", false)
            "x²" -> append("^2", true)
            "xʸ" -> append("^", true)
            "π" -> append("pi", false)
            "%" -> append("%", true)
            "sin", "cos", "tan", "ln", "log", "abs" -> append("$key(", false)
            else -> append(key, key in OPERATORS)
        }
    }


    Column(
        Modifier.fillMaxSize().padding(horizontal = 12.dp).padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val displayShape = RoundedCornerShape(uiStyle.cardRadiusDp.dp)
        Surface(
            Modifier.fillMaxWidth().weight(1f),
            shape = displayShape,
            color = p.surface.copy(alpha = uiStyle.surfaceAlpha),
            shadowElevation = uiStyle.elevationDp.dp
        ) {
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    Modifier.align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chip("f(x)", scientific) { scientific = !scientific }
                    if (scientific) Chip(if (degrees) "DEG" else "RAD", false) {
                        calculationJob?.cancel()
                        calculationJob = null
                        degrees = !degrees
                    }
                    if (steps.isNotEmpty()) Chip(tx("steps"), showSteps) { showSteps = !showSteps }
                }
                Box(
                    Modifier.align(Alignment.TopEnd).size(40.dp).clip(RoundedCornerShape(uiStyle.controlRadiusDp.dp))
                        .mathFlowClickable { press("⌫") },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(AppGlyph.BACKSPACE, tx("delete"), tint = p.muted, modifier = Modifier.size(22.dp))
                }
                val decimalFormatLabel = tx("decimal_format")
                val fractionFormatLabel = tx("fraction_format")
                Box(Modifier.align(Alignment.TopStart).padding(top = 54.dp).width(220.dp)) {
                    val selectedLabel = if (resultFormat == "decimal") decimalFormatLabel else fractionFormatLabel
                    Segmented(listOf(decimalFormatLabel, fractionFormatLabel), selectedLabel) { selected ->
                        val selectedFormat = if (selected == decimalFormatLabel) "decimal" else "fraction"
                        onResultFormatChange(selectedFormat)
                        result = if (selectedFormat == "decimal") decimalResult else fractionResult
                    }
                }
                Column(Modifier.align(Alignment.BottomEnd).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState(), reverseScrolling = true), horizontalArrangement = Arrangement.End) {
                        Text(
                            expression.replace("sqrt", "√").replace("pi", "π").ifEmpty { " " },
                            fontSize = 24.sp,
                            color = p.muted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    androidx.compose.animation.AnimatedContent(
                        targetState = result,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(tween(220, delayMillis = 20)) +
                                androidx.compose.animation.slideInVertically(animationSpec = tween(220)) { it / 8 } +
                                androidx.compose.animation.scaleIn(initialScale = 0.975f, animationSpec = tween(220))) togetherWith
                                (androidx.compose.animation.fadeOut(tween(120)) +
                                    androidx.compose.animation.slideOutVertically(animationSpec = tween(120)) { -it / 10 })
                        },
                        label = "CalculatorResult"
                    ) { animatedResult ->
                        Text(
                            animatedResult,
                            fontSize = when {
                                animatedResult.length > 16 -> 24.sp
                                animatedResult.length > 11 -> 32.sp
                                animatedResult.length > 8 -> 40.sp
                                else -> 52.sp
                            },
                            fontWeight = FontWeight.Medium,
                            color = if (animatedResult == "Ошибка") p.error else p.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = message != null,
                        enter = androidx.compose.animation.fadeIn(tween(180)) +
                            androidx.compose.animation.expandVertically(tween(180)),
                        exit = androidx.compose.animation.fadeOut(tween(110)) +
                            androidx.compose.animation.shrinkVertically(tween(110))
                    ) {
                        message?.let {
                            Text(MathTextLocalization.translate(it, LocalLanguage.current), fontSize = 13.sp, color = p.error, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = steps.isNotEmpty() && showSteps,
            enter = androidx.compose.animation.fadeIn(tween(190)) +
                androidx.compose.animation.expandVertically(tween(190)),
            exit = androidx.compose.animation.fadeOut(tween(110)) +
                androidx.compose.animation.shrinkVertically(tween(110))
        ) {
            Surface(
                Modifier.fillMaxWidth().heightIn(max = 96.dp),
                shape = RoundedCornerShape(uiStyle.cardRadiusDp.dp),
                color = p.surface.copy(alpha = uiStyle.surfaceAlpha),
                shadowElevation = uiStyle.elevationDp.dp
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp)) {
                    steps.forEachIndexed { index, step ->
                        Text("${index + 1}. ${MathTextLocalization.translate(step, LocalLanguage.current)}", fontSize = 13.sp, color = p.muted, lineHeight = 18.sp)
                    }
                }
            }
        }

        val rows = if (scientific) CALCULATOR_SCIENTIFIC_ROWS else CALCULATOR_MAIN_ROWS
        Column(
            Modifier.fillMaxWidth().weight(if (scientific) 2.5f else 1.7f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        CalcKey(key, Modifier.weight(1f).fillMaxSize(), compact = scientific) { press(key) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val chipShape = RoundedCornerShape(uiStyle.controlRadiusDp.dp)
    Box(
        Modifier.height(32.dp).clip(chipShape)
            .background(if (active) p.navActive else p.key)
            .mathFlowClickable(onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(displayText(label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (active) p.accent else p.muted)
    }
}

// ---------------------------------------------------------------- shared pieces

@Composable
private fun PageColumn(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 14.dp, bottom = if (uiStyle.glassSurface) 112.dp else 30.dp),
        verticalArrangement = Arrangement.spacedBy(if (uiStyle.compact) 8.dp else 12.dp)
    ) {
        Text(displayText(title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
        content()
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val settingsRadius = LocalSettingsCornerRadius.current
    val neo = uiStyle.id == "neo"
    val skeuo = uiStyle.id == "skeuomorphic"
    val retro = uiStyle.id == "retro_grid"
    val shape = RoundedCornerShape(
        (if (neo || skeuo || retro) uiStyle.cardRadiusDp else settingsRadius ?: uiStyle.cardRadiusDp).dp
    )
    val borderWidth = when {
        neo -> 2.dp
        retro -> 1.5.dp
        else -> 1.dp
    }
    val borderColor = when {
        neo -> p.accent
        retro -> p.accent.copy(alpha = 0.78f)
        skeuo -> p.text.copy(alpha = 0.14f)
        else -> p.text.copy(alpha = uiStyle.outlineAlpha)
    }
    val panelColor = p.surface.copy(alpha = uiStyle.surfaceAlpha)
    Surface(
        modifier = modifier.fillMaxWidth()
            .background(panelColor, shape)
            .then(if (neo || retro || skeuo || uiStyle.outlineAlpha > 0f) {
                Modifier.border(borderWidth, borderColor, shape)
            } else Modifier),
        shape = shape,
        color = panelColor,
        shadowElevation = if (skeuo) 7.dp else uiStyle.elevationDp.dp
    ) {
        Box(Modifier.fillMaxWidth().clip(shape)) {
            Column(
                Modifier.fillMaxWidth().padding(when {
                    neo -> 13.dp
                    retro -> 11.dp
                    skeuo -> 16.dp
                    uiStyle.compact -> 12.dp
                    else -> 15.dp
                }),
                verticalArrangement = Arrangement.spacedBy(if (uiStyle.compact) 8.dp else 11.dp)
            ) { content() }
        }
    }
}

@Composable
private fun Segmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val segmentShape = RoundedCornerShape(uiStyle.controlRadiusDp.dp)
    BoxWithConstraints(
        Modifier.fillMaxWidth().clip(segmentShape)
            .background(p.key).padding(3.dp)
    ) {
        val itemWidth = maxWidth / options.size
        val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
        val density = LocalDensity.current
        val indicatorOffset by animateFloatAsState(
            targetValue = with(density) { (itemWidth * selectedIndex).toPx() },
            animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "SegmentedIndicatorPosition"
        )
        Box(
            Modifier.align(Alignment.CenterStart)
                .graphicsLayer { translationX = indicatorOffset }
                .size(width = itemWidth, height = 38.dp)
                .clip(RoundedCornerShape(uiStyle.buttonRadiusDp.dp))
                .background(p.keyEquals)
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val active = option == selected
                val tint by animateColorAsState(
                    targetValue = if (active) p.onKeyEquals else p.muted,
                    animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
                    label = "SegmentedLabelColor"
                )
                Box(
                    Modifier.weight(1f).height(38.dp)
                        .mathFlowClickable {
                            if (!active) onSelect(option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(displayText(option), fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Medium, color = tint)
                }
            }
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, numeric: Boolean = true) {
    val uiStyle = LocalMathFlowUiStyle.current
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(displayText(label), maxLines = 1) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(uiStyle.controlRadiusDp.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
    )
}

@Composable
private fun ActionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val shape = RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.buttonRadiusDp).dp)
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 620f),
        label = "GlassActionButtonPress"
    )
    Surface(
        modifier = modifier.fillMaxWidth().height(if (uiStyle.compact) 46.dp else 52.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }

            .pointerInput(onClick) {
                detectTapGestures(onPress = {
                    pressed = true
                    val released = tryAwaitRelease()
                    pressed = false
                    if (released) onClick()
                })
            },
        shape = shape,
        color = p.keyEquals,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(displayText(text), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = p.onKeyEquals)
        }
    }
}

@Composable
private fun ResultBox(text: String, error: String?) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val resultShape = RoundedCornerShape(uiStyle.cardRadiusDp.dp)
    Surface(
        Modifier.fillMaxWidth(),
        shape = resultShape,
        color = p.key,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = androidx.compose.animation.fadeIn(tween(190)) +
                    androidx.compose.animation.expandVertically(tween(190)),
                exit = androidx.compose.animation.fadeOut(tween(110)) +
                    androidx.compose.animation.shrinkVertically(tween(110))
            ) {
                error?.let { AnimatedAnswerText(MathTextLocalization.translate(it, LocalLanguage.current), color = p.error, fontSize = 14.sp) }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = error == null && text.isNotBlank(),
                enter = androidx.compose.animation.fadeIn(tween(190)) +
                    androidx.compose.animation.expandVertically(tween(190)),
                exit = androidx.compose.animation.fadeOut(tween(110)) +
                    androidx.compose.animation.shrinkVertically(tween(110))
            ) {
                AnimatedAnswerText(
                    MathTextLocalization.translate(text, LocalLanguage.current),
                    color = p.text,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---------------------------------------------------------------- tools

@Composable
private fun ToolsPage() {
    var tool by rememberSaveable { mutableStateOf("Дроби") }

    var fractionA by rememberSaveable { mutableStateOf("3/4") }
    var fractionB by rememberSaveable { mutableStateOf("2/5") }
    var fractionResult by rememberSaveable { mutableStateOf("") }
    var fractionError by rememberSaveable { mutableStateOf<String?>(null) }

    var percentMode by rememberSaveable { mutableStateOf("% от числа") }
    var percentA by rememberSaveable { mutableStateOf("15") }
    var percentB by rememberSaveable { mutableStateOf("240") }
    var percentResult by rememberSaveable { mutableStateOf("") }
    var percentError by rememberSaveable { mutableStateOf<String?>(null) }

    var shape by rememberSaveable { mutableStateOf("Прямоугольник") }
    var sideA by rememberSaveable { mutableStateOf("5") }
    var sideB by rememberSaveable { mutableStateOf("3") }
    var sideC by rememberSaveable { mutableStateOf("4") }
    var geometryResult by rememberSaveable { mutableStateOf("") }
    var geometryError by rememberSaveable { mutableStateOf<String?>(null) }
    val fractionFallback = tx("fraction_error")
    val valuesFallback = tx("values_error")
    val sizesFallback = tx("sizes_error")

    PageColumn(tx("tools")) {
        if (tool == "График") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(tx("tools"), false) { tool = "Дроби" }
                Chip(tx("graph"), true) { tool = "График" }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Дроби", "Проценты", "Геометрия", "Конвертер", "График").forEach { option ->
                    Chip(option, tool == option) { tool = option }
                }
            }
        }
        Panel {
            androidx.compose.animation.AnimatedContent(
                targetState = tool,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(tween(190)) +
                        androidx.compose.animation.slideInVertically(animationSpec = tween(190)) { it / 12 }) togetherWith
                        (androidx.compose.animation.fadeOut(tween(110)) +
                            androidx.compose.animation.slideOutVertically(animationSpec = tween(110)) { -it / 12 })
                },
                label = "MathFlowToolContent"
            ) { activeTool ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (activeTool) {
                "Дроби" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(fractionA, { fractionA = it }, "Дробь 1", Modifier.weight(1f), numeric = false)
                        Field(fractionB, { fractionB = it }, "Дробь 2", Modifier.weight(1f), numeric = false)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("+", "−", "×", "÷").forEach { op ->
                            Box(Modifier.weight(1f)) {
                                ActionButton(op) {
                                    runCatching { FractionTools.calculate(fractionA, fractionB, op) }
                                        .onSuccess { fractionResult = it; fractionError = null }
                                        .onFailure { fractionError = friendly(it, fractionFallback); fractionResult = "" }
                                }
                            }
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = fractionResult.isNotEmpty() || fractionError != null,
                        enter = androidx.compose.animation.fadeIn(tween(190)) +
                            androidx.compose.animation.expandVertically(tween(190)),
                        exit = androidx.compose.animation.fadeOut(tween(110)) +
                            androidx.compose.animation.shrinkVertically(tween(110))
                    ) {
                        ResultBox(fractionResult, fractionError)
                    }
                }
                "Проценты" -> {
                    Segmented(listOf("% от числа", "Сколько %", "± на %"), percentMode) { percentMode = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(
                            percentA, { percentA = it },
                            when (percentMode) { "Сколько %" -> "Часть"; "± на %" -> "Число"; else -> "Процент" },
                            Modifier.weight(1f)
                        )
                        Field(
                            percentB, { percentB = it },
                            when (percentMode) { "Сколько %" -> "Целое"; "± на %" -> "Процент (− уменьшить)"; else -> "Число" },
                            Modifier.weight(1f)
                        )
                    }
                    ActionButton(tx("calculate")) {
                        runCatching {
                            when (percentMode) {
                                "Сколько %" -> PercentTools.whatPercent(percentA, percentB)
                                "± на %" -> {
                                    val rate = percentB.trim().replace(',', '.').toBigDecimalOrNull()
                                        ?: throw IllegalArgumentException("Некорректное число: $percentB")
                                    if (rate.signum() < 0) PercentTools.changeByPercent(percentA, rate.abs().toPlainString(), false)
                                    else PercentTools.changeByPercent(percentA, percentB, true)
                                }
                                else -> PercentTools.percentOf(percentA, percentB)
                            }
                        }.onSuccess { percentResult = it; percentError = null }
                            .onFailure { percentError = friendly(it, valuesFallback); percentResult = "" }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = percentResult.isNotEmpty() || percentError != null,
                        enter = androidx.compose.animation.fadeIn(tween(190)) +
                            androidx.compose.animation.expandVertically(tween(190)),
                        exit = androidx.compose.animation.fadeOut(tween(110)) +
                            androidx.compose.animation.shrinkVertically(tween(110))
                    ) {
                        ResultBox(percentResult, percentError)
                    }
                }
                "Геометрия" -> {
                    Segmented(listOf("Прямоугольник", "Круг", "Треугольник"), shape) { shape = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(sideA, { sideA = it }, if (shape == "Круг") "Радиус" else "Сторона a", Modifier.weight(1f))
                        if (shape != "Круг") Field(sideB, { sideB = it }, "Сторона b", Modifier.weight(1f))
                        if (shape == "Треугольник") Field(sideC, { sideC = it }, "Сторона c", Modifier.weight(1f))
                    }
                    ActionButton(tx("calculate")) {
                        runCatching {
                            when (shape) {
                                "Прямоугольник" -> GeometryTools.rectangle(sideA, sideB)
                                "Круг" -> GeometryTools.circle(sideA)
                                else -> GeometryTools.triangle(sideA, sideB, sideC)
                            }
                        }.onSuccess { geometryResult = it; geometryError = null }
                            .onFailure { geometryError = friendly(it, sizesFallback); geometryResult = "" }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = geometryResult.isNotEmpty() || geometryError != null,
                        enter = androidx.compose.animation.fadeIn(tween(190)) +
                            androidx.compose.animation.expandVertically(tween(190)),
                        exit = androidx.compose.animation.fadeOut(tween(110)) +
                            androidx.compose.animation.shrinkVertically(tween(110))
                    ) {
                        ResultBox(geometryResult, geometryError)
                    }
                }
                "Конвертер" -> UnitConverterContent()
                "График" -> GraphFunctionContent()
                    }
                }
            }
        }
    }
}
private data class GraphSample(val x: Double, val y: Double?)

@Composable
private fun GraphFunctionContent() {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    var expression by rememberSaveable { mutableStateOf("x^2") }
    var range by rememberSaveable { mutableStateOf(5) }
    var samples by remember { mutableStateOf(emptyList<GraphSample>()) }
    var selectedPoint by remember { mutableStateOf<GraphSample?>(null) }
    var isCalculating by remember { mutableStateOf(false) }
    var hasValidPoints by remember { mutableStateOf(false) }

    LaunchedEffect(expression, range) {
        selectedPoint = null
        if (expression.isBlank()) {
            samples = emptyList()
            hasValidPoints = false
            isCalculating = false
            return@LaunchedEffect
        }
        delay(140L)
        isCalculating = true
        val computed = withContext(Dispatchers.Default) {
            val engine = ExpressionEngine(AngleMode.RADIANS)
            (0..120).map { index ->
                val x = -range + (range * 2.0 * index / 120.0)
                val y = runCatching {
                    engine.calculate(expression, variables = mapOf("x" to BigDecimal.valueOf(x)), includeSteps = false).decimalValue.toDouble()
                }.getOrNull()?.takeIf { it.isFinite() && abs(it) < 1.0e7 }
                GraphSample(x, y)
            }
        }
        samples = computed
        hasValidPoints = computed.any { it.y != null }
        isCalculating = false
    }

    Field(expression, { expression = it }, tx("graph_input"), Modifier.fillMaxWidth(), numeric = false)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip("x²", false) { expression = "x^2" }
        Chip("sin(x)", false) { expression = "sin(x)" }
        Chip("1/x", false) { expression = "1/x" }
        Chip("2x+1", false) { expression = "2x+1" }
    }
    Text(tx("graph_range"), color = p.muted, fontSize = 12.sp)
    Segmented(listOf("±5", "±10", "±20"), "±$range") {
        range = it.removePrefix("±").toIntOrNull() ?: 5
    }
    Canvas(
        Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(uiStyle.cardRadiusDp.dp)).background(p.key)
            .pointerInput(samples, range) {
                detectTapGestures { tap ->
                    val fraction = tap.x.toDouble() / size.width.coerceAtLeast(1).toDouble()
                    val targetX = -range + fraction * range * 2.0
                    selectedPoint = samples.filter { it.y != null }.minByOrNull { abs(it.x - targetX) }
                }
            }
    ) {
        val validY = samples.mapNotNull { it.y }
        if (validY.isEmpty()) return@Canvas
        val rawMin = validY.minOrNull() ?: -1.0
        val rawMax = validY.maxOrNull() ?: 1.0
        val padding = ((rawMax - rawMin) * 0.08).coerceAtLeast(0.5)
        val yMin = rawMin - padding
        val yMax = rawMax + padding
        val ySpan = (yMax - yMin).coerceAtLeast(1.0)
        fun plotX(x: Double) = ((x + range) / (range * 2.0) * size.width.toDouble()).toFloat()
        fun plotY(y: Double) = (size.height.toDouble() - ((y - yMin) / ySpan * size.height.toDouble())).toFloat()
        for (i in 0..4) {
            val x = size.width * i / 4f
            val y = size.height * i / 4f
            drawLine(p.muted.copy(alpha = 0.18f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            drawLine(p.muted.copy(alpha = 0.18f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        if (yMin <= 0.0 && yMax >= 0.0) {
            val axisY = plotY(0.0)
            drawLine(p.muted.copy(alpha = 0.65f), Offset(0f, axisY), Offset(size.width, axisY), 1.dp.toPx())
        }
        val axisX = plotX(0.0)
        drawLine(p.muted.copy(alpha = 0.65f), Offset(axisX, 0f), Offset(axisX, size.height), 1.dp.toPx())
        val path = Path()
        var drawingSegment = false
        samples.forEach { sample ->
            val y = sample.y
            if (y == null) drawingSegment = false
            else {
                val point = Offset(plotX(sample.x), plotY(y))
                if (drawingSegment) path.lineTo(point.x, point.y) else path.moveTo(point.x, point.y)
                drawingSegment = true
            }
        }
        drawPath(path, p.accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        selectedPoint?.let { point -> point.y?.let { y ->
            val center = Offset(plotX(point.x), plotY(y))
            drawCircle(p.keyEquals, radius = 7.dp.toPx(), center = center)
            drawCircle(p.onKeyEquals, radius = 3.dp.toPx(), center = center)
        } }
    }
    when {
        isCalculating -> Text(tx("graph_calculating"), color = p.muted, fontSize = 12.sp)
        !hasValidPoints -> Text(tx("graph_no_points"), color = p.muted, fontSize = 12.sp)
        selectedPoint?.y != null -> androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.fadeIn(tween(180)) +
                androidx.compose.animation.expandVertically(tween(180))
        ) {
            selectedPoint?.let { point ->
                point.y?.let { y ->
                    AnimatedAnswerText(
                        "x = ${"%.2f".format(Locale.US, point.x)}   y = ${"%.2f".format(Locale.US, y)}",
                        color = p.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        else -> Text(tx("graph_hint"), color = p.muted, fontSize = 12.sp)
    }
}
private fun friendly(error: Throwable, fallback: String): String = when {
    error.message?.contains("Division by zero") == true -> "Деление на ноль"
    error is IllegalArgumentException || error is ArithmeticException -> error.message ?: fallback
    else -> fallback
}

// ---------------------------------------------------------------- solver

@Composable
private fun EquationSolverPage() {
    val p = LocalPalette.current
    val equationFallback = tx("equation_error")
    var mode by rememberSaveable { mutableStateOf("Уравнение") }
    var equation by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf<String?>(null) }
    var steps by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var systemCount by rememberSaveable { mutableStateOf(2) }
    var systemFirst by rememberSaveable { mutableStateOf("x + y = 5") }
    var systemSecond by rememberSaveable { mutableStateOf("x - y = 1") }
    var systemThird by rememberSaveable { mutableStateOf("x + y + z = 6") }
    var expressionToSimplify by rememberSaveable { mutableStateOf("2(x + 3) - x") }

    PageColumn(tx("solver")) {
        Segmented(listOf("Уравнение", "Система", "Упростить"), mode) {
            mode = it
            answer = null
            steps = emptyList()
            error = null
        }
        if (mode == "Уравнение") {
            Panel {
                Field(equation, { equation = it; answer = null; error = null }, tx("example_equation"), Modifier.fillMaxWidth(), numeric = false)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("x", "x²", "x³", "x⁴", "^", "(", ")", "√(", "abs(", "sin(", "cos(", "tan(", "ln(", "log(", "π", "e", "+", "−", "×", "÷", "=").forEach { symbol ->
                        Chip(symbol, false) { equation += symbol }
                    }
                }
                ActionButton(tx("solve")) {
                    runCatching { AlgebraSolver().solve(equation) }
                        .onSuccess { answer = it.answer; steps = it.steps; error = null }
                        .onFailure { answer = null; steps = emptyList(); error = it.message ?: equationFallback }
                }
            }
        } else if (mode == "Система") {
            Panel {
                Text(displayText("Линейная система: 2–3 неизвестных (x, y, z)"), color = p.muted, fontSize = 12.sp)
                Field(systemFirst, { systemFirst = it; answer = null; error = null }, "Уравнение 1", Modifier.fillMaxWidth(), numeric = false)
                Field(systemSecond, { systemSecond = it; answer = null; error = null }, "Уравнение 2", Modifier.fillMaxWidth(), numeric = false)
                if (systemCount == 3) {
                    Field(systemThird, { systemThird = it; answer = null; error = null }, "Уравнение 3", Modifier.fillMaxWidth(), numeric = false)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("2 уравнения", systemCount == 2) { systemCount = 2; answer = null; error = null }
                    Chip("3 уравнения", systemCount == 3) { systemCount = 3; answer = null; error = null }
                }
                ActionButton(displayText("Решить систему")) {
                    val equations = if (systemCount == 2) listOf(systemFirst, systemSecond) else listOf(systemFirst, systemSecond, systemThird)
                    runCatching { LinearSystemSolver().solve(equations) }
                        .onSuccess { answer = it.answer; steps = it.steps; error = null }
                        .onFailure { answer = null; steps = emptyList(); error = it.message ?: "Не удалось решить систему" }
                }
            }
            Text(displayText("Поддерживаются линейные выражения, скобки, дробные коэффициенты и точные рациональные ответы."), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
        } else {
            Panel {
                Text(displayText("Упрощение многочленов от одной переменной x (до 4-й степени)."), color = p.muted, fontSize = 12.sp)
                Field(expressionToSimplify, { expressionToSimplify = it; answer = null; error = null }, "Выражение", Modifier.fillMaxWidth(), numeric = false)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("x", "x²", "^", "(", ")", "+", "−", "×", "÷").forEach { symbol ->
                        Chip(symbol, false) { expressionToSimplify += symbol }
                    }
                }
                ActionButton(displayText("Упростить выражение")) {
                    runCatching { PolynomialSimplifier().simplify(expressionToSimplify) }
                        .onSuccess { answer = it.expression; steps = it.steps; error = null }
                        .onFailure { answer = null; steps = emptyList(); error = it.message ?: "Не удалось упростить выражение" }
                }
            }
            Text(displayText("Поддерживаются точные дробные коэффициенты, скобки, раскрытие произведений и приведение подобных слагаемых. Деление на выражение с x не поддерживается."), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = error != null,
            enter = androidx.compose.animation.fadeIn(tween(190)) + androidx.compose.animation.expandVertically(tween(190)),
            exit = androidx.compose.animation.fadeOut(tween(110)) + androidx.compose.animation.shrinkVertically(tween(110))
        ) { error?.let { AnimatedAnswerText(MathTextLocalization.translate(it, LocalLanguage.current), color = p.error, fontSize = 14.sp) } }
        androidx.compose.animation.AnimatedVisibility(
            visible = answer != null,
            enter = androidx.compose.animation.fadeIn(tween(210)) + androidx.compose.animation.expandVertically(tween(210)),
            exit = androidx.compose.animation.fadeOut(tween(120)) + androidx.compose.animation.shrinkVertically(tween(120))
        ) {
            answer?.let { text ->
                Panel {
                    AnimatedAnswerText(MathTextLocalization.translate(text, LocalLanguage.current), color = p.accent, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    if (steps.isNotEmpty()) {
                        HorizontalDivider(color = p.key)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            steps.forEachIndexed { index, step ->
                                Text("${index + 1}. ${MathTextLocalization.translate(step, LocalLanguage.current)}", color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
        if (mode == "Уравнение") Text(tx("equation_help"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ---------------------------------------------------------------- history

@Composable
private fun HistoryPage(entries: List<HistoryEntry>, onClear: () -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    // Compose only visible history rows instead of building the full list at once.
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 14.dp)
            .padding(top = 14.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "history_title") {
            Text(tx("history"), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
        }
        if (entries.isEmpty()) {
            item(key = "history_empty") {
                Text(tx("history_empty"), color = p.muted, fontSize = 14.sp)
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(uiStyle.cardRadiusDp.dp),
                    color = p.surface.copy(alpha = uiStyle.surfaceAlpha),
                    shadowElevation = uiStyle.elevationDp.dp
                ) {
                    Column(
                        Modifier.fillMaxWidth()
                            .mathFlowClickable(enabled = entry.steps.isNotEmpty()) { expanded = !expanded }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(entry.expression, fontSize = 15.sp, color = p.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("= ${MathTextLocalization.translate(entry.result, LocalLanguage.current)}", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = p.text)
                        Text(
                            entry.dateLabel + if (entry.steps.isNotEmpty()) (if (expanded) tx("hide_solution") else tx("show_solution")) else "",
                            fontSize = 12.sp, color = p.muted
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = expanded,
                            enter = androidx.compose.animation.fadeIn(tween(180)) +
                                androidx.compose.animation.expandVertically(tween(180)),
                            exit = androidx.compose.animation.fadeOut(tween(110)) +
                                androidx.compose.animation.shrinkVertically(tween(110))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                entry.steps.forEachIndexed { index, step ->
                                    Text("${index + 1}. ${MathTextLocalization.translate(step, LocalLanguage.current)}", fontSize = 13.sp, color = p.muted, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
            item(key = "clear_history") {
                MathFlowTextButton(tx("clear_history"), p.accent, modifier = Modifier.fillMaxWidth(), onClick = onClear)
            }
        }
    }
}

private data class HistoryEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long,
    val steps: List<String>
) {
    val dateLabel: String
        get() = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private object HistoryStore {
    private const val PREFS = "mathflow_history"
    private const val KEY = "entries"
    private const val LIMIT = 100

    fun load(context: Context): List<HistoryEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val stepsJson = item.optJSONArray("steps") ?: JSONArray()
                HistoryEntry(
                    id = item.optLong("id"),
                    expression = item.optString("expression"),
                    result = item.optString("result"),
                    timestamp = item.optLong("timestamp"),
                    steps = (0 until stepsJson.length()).map { stepsJson.optString(it) }
                )
            }.sortedByDescending { it.timestamp }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, expression: String, result: String, steps: List<String>): List<HistoryEntry> {
        val now = System.currentTimeMillis()
        val entries = (listOf(HistoryEntry(now, expression, result, now, steps)) + load(context)).take(LIMIT)
        save(context, entries)
        return entries
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }

    private fun save(context: Context, entries: List<HistoryEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val stepArray = JSONArray()
            entry.steps.forEach { stepArray.put(it) }
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("expression", entry.expression)
                    .put("result", entry.result)
                    .put("timestamp", entry.timestamp)
                    .put("steps", stepArray)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, array.toString()).apply()
    }
}

// ---------------------------------------------------------------- settings

private data class AppIconOption(
    val id: String,
    val title: String,
    val description: String,
    val drawable: Int,
    val previewBackground: Color
)

private val AppIconOptions = listOf(
    AppIconOption("vanilla", "Vanilla", "Оригинальная фиолетовая иконка MathFlow.", R.drawable.launcher_vanilla, Color(0xFF6A1B9A)),
    AppIconOption("black", "Black", "Чёрный фон и белый фирменный знак.", R.drawable.launcher_black, Color(0xFF090A0F)),
    AppIconOption("white", "White", "Белый фон с фиолетовым знаком.", R.drawable.launcher_white, Color(0xFFFFFFFF)),
    AppIconOption("purple", "Purple", "Тёмный фон с насыщенным фиолетовым знаком.", R.drawable.launcher_purple, Color(0xFF08070D)),
    AppIconOption("ocean", "Ocean", "Глубокий синий фон и яркий голубой знак.", R.drawable.launcher_ocean, Color(0xFF061A5B)),
    AppIconOption("emerald", "Emerald", "Тёмно-зелёный фон и мятный знак.", R.drawable.launcher_emerald, Color(0xFF003021)),
    AppIconOption("sunset", "Sunset", "Градиент заката с белым фирменным знаком.", R.drawable.launcher_sunset, Color(0xFFF04A66)),
    AppIconOption("rose", "Rose", "Нежно-розовый фон и малиновый знак.", R.drawable.launcher_rose, Color(0xFFF4A9D1)),
    AppIconOption("midnight", "Midnight", "Глубокий ночной фон и лавандовый знак.", R.drawable.launcher_midnight, Color(0xFF19002F)),
    AppIconOption("graphite", "Graphite", "Графитовый фон и светлый холодный знак.", R.drawable.launcher_graphite, Color(0xFF292B3A))
)

private fun setLauncherIcon(context: Context, iconId: String) {
    val packageManager = context.packageManager
    val selected = AppIconOptions.firstOrNull { it.id == iconId } ?: AppIconOptions.first()
    val changes = AppIconOptions.mapNotNull { option ->
        val component = android.content.ComponentName(
            context,
            "${context.packageName}.Launcher${option.id.replaceFirstChar { it.uppercase(Locale.ROOT) }}"
        )
        val desiredState = if (option.id == selected.id) {
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        if (packageManager.getComponentEnabledSetting(component) == desiredState) {
            null
        } else {
            component to desiredState
        }
    }

    if (changes.isEmpty()) return

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        // Android 13+: apply the alias changes as one package-manager operation.
        // This avoids the intermediate state where no launcher alias is enabled.
        val settings = changes.map { (component, state) ->
            android.content.pm.PackageManager.ComponentEnabledSetting(
                component,
                state,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        }
        packageManager.setComponentEnabledSettings(settings)
    } else {
        // Older Android versions have no atomic batch API. Enable the new entry
        // point first, then disable the remaining aliases.
        changes.firstOrNull { it.second == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED }
            ?.let { (component, state) ->
                packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            }
        changes.filter { it.second == android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED }
            .forEach { (component, state) ->
                packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            }
    }
}

private const val BUILTIN_ARTEGRA_FONT = "builtin:artegra_soft_global"

private enum class SettingsSection { MAIN, DISPLAY, ICONS, ICON_PACKS, THEMES, STYLES, LANGUAGE, MEASUREMENTS, ANIMATIONS, NAVIGATION, FONTS, CONFIGS }

private fun makeConfigSnapshot(
    theme: String, icon: String, fontScale: Float, fontPath: String?, language: String,
    measurement: String, key: KeyAnimationSettings, nav: BottomNavAnimationSettings,
    resultFormat: String,
    uiStyle: String = MathFlowUiStyle.MATERIAL_YOU.id,
    iconPack: String = "material"
): JSONObject = JSONObject()
    .put("schemaVersion", 6)
    .put("theme", theme)
    .put("uiStyle", MathFlowUiStyle.fromId(uiStyle).id)
    .put("iconPack", AppIconPack.fromId(iconPack).id)
    .put("icon", icon)
    .put("fontScale", fontScale.toDouble())
    .put("fontPath", fontPath ?: JSONObject.NULL)
    .put("language", language)
    .put("measurement", measurement)
    .put("resultFormat", resultFormat)
    .put("keyStyle", key.style.id)
    .put("keyDuration", key.durationMs)
    .put("keyOpacity", key.opacity.toDouble())
    .put("keyIntensity", key.intensity.toDouble())
    .put("keyColorEnabled", key.colorEffectEnabled)
    .put("keyPressColor", key.pressColorArgb)
    .put("navEnabled", nav.enabled)
    .put("navStyle", nav.style.id)
    .put("navDuration", nav.durationMs)
    .put("navIntensity", nav.intensity.toDouble())
    .put("navIconScale", nav.iconScale.toDouble())
    .put("navOpacity", nav.opacity.toDouble())
    .put("navRefraction", nav.refractionStrength.toDouble())
    .put("navRoundness", nav.dropletRoundness.toDouble())
    .put("navBlur", nav.glassBlur.toDouble())
    .put("navDepth", nav.glassDepth.toDouble())
    .put("navHighlight", nav.highlightStrength.toDouble())
    .put("navShadow", nav.shadowStrength.toDouble())
    .put("navSurfaceMaterial", nav.surfaceMaterial.id)
    .put("matteOpacity", nav.matteOpacity.toDouble())
    .put("matteBlur", nav.matteBlur.toDouble())
    .put("matteDepth", nav.matteDepth.toDouble())
    .put("matteHighlight", nav.matteHighlight.toDouble())
    .put("matteShadow", nav.matteShadow.toDouble())

private fun validateConfigSnapshot(input: JSONObject): JSONObject {
    require(input.optInt("schemaVersion", -1) in 1..6) { "Unsupported settings schema" }
    val iconPack = AppIconPack.fromId(input.optString("iconPack", "material")).id
    val theme = input.optString("theme", "vanilla")
    val uiStyle = MathFlowUiStyle.fromId(input.optString("uiStyle", "material_you")).id
    val icon = input.optString("icon", "vanilla")
    require(ThemeOptions.any { it.id == theme }) { "Unknown theme" }
    require(AppIconOptions.any { it.id == icon }) { "Unknown icon" }
    val fontScale = input.optDouble("fontScale", 1.0).toFloat()
    require(fontScale in 0.85f..1.25f) { "Invalid font scale" }
    val language = input.optString("language", "ru")
    require(AppLanguages.any { it.tag == language }) { "Unsupported language" }
    val measurement = input.optString("measurement", "auto")
    require(measurement in setOf("auto", "metric", "us")) { "Invalid measurement system" }
    val resultFormat = input.optString("resultFormat", "fraction")
    require(resultFormat in setOf("fraction", "decimal")) { "Invalid calculator result format" }
    val keyStyle = input.optString("keyStyle", "spring")
    require(KeyAnimationStyle.values().any { it.id == keyStyle }) { "Unknown key animation style" }
    val navStyle = input.optString("navStyle", "fluid_pill")
    require(BottomNavAnimationStyle.values().any { it.id == navStyle }) { "Unknown navigation animation style" }
    val key = KeyAnimationSettings(
        KeyAnimationStyle.fromId(keyStyle),
        input.optInt("keyDuration", 140),
        input.optDouble("keyOpacity", 0.72).toFloat(),
        input.optDouble("keyIntensity", 0.10).toFloat(),
        input.optBoolean("keyColorEnabled", false),
        input.optLong("keyPressColor", 0L)
    ).normalized()
    val nav = BottomNavAnimationSettings(
        input.optBoolean("navEnabled", true),
        BottomNavAnimationStyle.fromId(navStyle),
        input.optInt("navDuration", 280),
        input.optDouble("navIntensity", 0.65).toFloat(),
        input.optDouble("navIconScale", 0.08).toFloat(),
        input.optDouble("navOpacity", 0.85).toFloat(),
        input.optDouble("navRefraction", 0.35).toFloat(),
        input.optDouble("navRoundness", 0.96).toFloat(),
        input.optDouble("navBlur", 8.0).toFloat(),
        input.optDouble("navDepth", 0.70).toFloat(),
        input.optDouble("navHighlight", 0.90).toFloat(),
        input.optDouble("navShadow", 0.42).toFloat(),
        BottomNavSurfaceMaterial.fromId(input.optString("navSurfaceMaterial", "frosted")),
        input.optDouble("matteOpacity", 0.72).toFloat(),
        input.optDouble("matteBlur", 12.0).toFloat(),
        input.optDouble("matteDepth", 0.38).toFloat(),
        input.optDouble("matteHighlight", 0.58).toFloat(),
        input.optDouble("matteShadow", 0.32).toFloat()
    ).normalized()
    return makeConfigSnapshot(theme, icon, fontScale, input.optString("fontPath").takeIf { it.isNotBlank() && it != "null" },
        language, measurement, key, nav, resultFormat, uiStyle, iconPack)
}

@Composable
private fun SettingsPage(
    currentConfig: JSONObject,
    configProfiles: List<MathFlowConfigProfile>,
    activeConfigId: String?,
    onCreateConfig: (String) -> Unit,
    onRenameConfig: (String, String) -> Unit,
    onDeleteConfig: (String) -> Unit,
    onApplyConfig: (MathFlowConfigProfile) -> Unit,
    onImportConfig: suspend (MathFlowConfigArchive.ImportedConfig) -> MathFlowConfigProfile,
    externalConfigUri: Uri?,
    onExternalConfigHandled: () -> Unit,
    onDuplicateConfig: (MathFlowConfigProfile) -> Unit,
    selectedThemeId: String,
    selectedUiStyleId: String,
    selectedIconPackId: String,
    onIconPackSelected: (String) -> Unit,
    settingsCornerRadiusDp: Int,
    onSettingsCornerRadiusChange: (Int) -> Unit,
    onUiStyleSelected: (String) -> Unit,
    selectedIconId: String,
    onThemeSelected: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    fontScale: Float,
    fontPath: String?,
    onFontScale: (Float) -> Unit,
    onFontSelected: (String) -> Unit,
    onResetFont: () -> Unit,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    selectedMeasurementSystem: String,
    onMeasurementSystemSelected: (String) -> Unit,
    appliedAnimationSettings: KeyAnimationSettings,
    onAnimationSettingsApplied: (KeyAnimationSettings) -> Unit,
    appliedNavAnimationSettings: BottomNavAnimationSettings,
    onNavAnimationSettingsApplied: (BottomNavAnimationSettings) -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val context = LocalContext.current
    val ioScope = rememberCoroutineScope()
    val profileExportedMessage = tx("profile_exported")
    val profileExportFailedMessage = tx("profile_export_failed")
    val profileImportedMessage = tx("profile_imported")
    val profileImportFailedMessage = tx("profile_import_failed")
    val profileCreatedMessage = tx("profile_created")
    val profileAppliedMessage = tx("profile_applied")
    val profileRenamedMessage = tx("profile_renamed")
    val profileDeletedMessage = tx("profile_deleted")
    var section by rememberSaveable { mutableStateOf(SettingsSection.MAIN) }
    var pendingIconId by rememberSaveable(selectedIconId) { mutableStateOf(selectedIconId) }
    var pendingThemeId by rememberSaveable(selectedThemeId) { mutableStateOf(selectedThemeId) }
    var pendingUiStyleId by rememberSaveable(selectedUiStyleId) { mutableStateOf(selectedUiStyleId) }
    var pendingIconPackId by rememberSaveable(selectedIconPackId) { mutableStateOf(selectedIconPackId) }
    var pendingSettingsCornerRadiusDp by remember(settingsCornerRadiusDp) { mutableStateOf(settingsCornerRadiusDp) }
    var pendingFontScale by remember(fontScale) { mutableFloatStateOf(fontScale) }
    var pendingLanguage by remember(selectedLanguage) { mutableStateOf(selectedLanguage) }
    var pendingMeasurementSystem by remember(selectedMeasurementSystem) { mutableStateOf(selectedMeasurementSystem) }
    var pendingAnimationSettings by remember(appliedAnimationSettings) { mutableStateOf(appliedAnimationSettings) }
    var pendingNavAnimationSettings by remember(appliedNavAnimationSettings) { mutableStateOf(appliedNavAnimationSettings) }
    var navPreviewPage by rememberSaveable { mutableStateOf(Page.HOME) }
    val navPreviewBackdrop = rememberLayerBackdrop()
    var fontMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var savedFonts by remember {
        mutableStateOf<List<Pair<String, String>>>(
            listOf(BUILTIN_ARTEGRA_FONT to "Artegra Soft Global") +
                (File(context.filesDir, "fonts").listFiles()
                    ?.filter { it.isFile && (it.extension.equals("ttf", true) || it.extension.equals("otf", true)) }
                    ?.map { file ->
                        val storedName = file.name.substringAfter('_').substringAfter('_', file.name)
                        file.absolutePath to storedName
                    }
                    ?: emptyList())
        )
    }
    var pendingFontPath by remember(fontPath) { mutableStateOf(fontPath) }
    var configName by rememberSaveable { mutableStateOf("") }
    var renameValue by rememberSaveable { mutableStateOf("") }
    var selectedConfigId by rememberSaveable { mutableStateOf(activeConfigId ?: configProfiles.firstOrNull()?.id.orEmpty()) }
    var configMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingImportedProfile by remember { mutableStateOf<MathFlowConfigProfile?>(null) }

    // Android system Back navigates settings subpages; in the configuration manager
    // it first collapses the currently expanded profile.
    BackHandler(enabled = section != SettingsSection.MAIN) {
        if (section == SettingsSection.CONFIGS && selectedConfigId.isNotEmpty()) {
            selectedConfigId = ""
        } else {
            section = SettingsSection.MAIN
        }
    }
    LaunchedEffect(configProfiles, activeConfigId) {
        if (activeConfigId != null && configProfiles.any { it.id == activeConfigId }) {
            selectedConfigId = activeConfigId
        } else if (configProfiles.none { it.id == selectedConfigId }) {
            selectedConfigId = configProfiles.firstOrNull()?.id.orEmpty()
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val profile = configProfiles.firstOrNull { it.id == selectedConfigId }
        if (uri != null && profile != null) {
            ioScope.launch {
                val exported = withContext(Dispatchers.IO) {
                    runCatching {
                        val path = profile.settings.optString("fontPath").takeIf {
                            it.isNotBlank() && it != "null" && it != BUILTIN_ARTEGRA_FONT
                        }
                        val fontFile = path?.let { File(it).takeIf(File::isFile) }
                        require(path == null || fontFile != null) { "The selected custom font is missing" }
                        require(fontFile == null || fontFile.length() <= 8L * 1024L * 1024L)
                        val font = fontFile?.readBytes()
                        val fontName = if (font != null) fontFile!!.name else null
                        context.contentResolver.openOutputStream(uri).use { output ->
                            requireNotNull(output) { "Cannot open export destination" }
                            MathFlowConfigArchive.write(output, profile.name, profile.settings, fontName, font)
                        }
                    }.isSuccess
                }
                configMessage = if (exported) profileExportedMessage else profileExportFailedMessage
            }
        }
    }
    val importFromUri: (Uri) -> Unit = { uri ->
        ioScope.launch {
            val imported = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input) { "Cannot open selected file" }
                        MathFlowConfigArchive.read(input)
                    }
                }
            }
            if (imported.isSuccess) {
                try {
                    val profile = onImportConfig(imported.getOrThrow())
                    pendingImportedProfile = profile
                    selectedConfigId = profile.id
                    configMessage = profileImportedMessage
                } catch (_: Exception) {
                    configMessage = profileImportFailedMessage
                }
            } else {
                configMessage = profileImportFailedMessage
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importFromUri(uri)
    }
    LaunchedEffect(externalConfigUri) {
        externalConfigUri?.let { uri ->
            importFromUri(uri)
            onExternalConfigHandled()
        }
    }
    var languageQuery by rememberSaveable { mutableStateOf("") }
    val fontFileTypeError = tx("font_file_type")
    val fontOpenError = tx("font_open_error")
    val fontEmptyError = tx("font_empty")
    val fontInstallError = tx("font_install_error")
    val fontAddApplyHint = tx("font_add_apply_hint")
    val fontApplySuccess = tx("font_apply_success")
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ioScope.launch {
                var importedTarget: File? = null
                val imported = withContext(Dispatchers.IO) {
                    runCatching {
                        val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (column >= 0 && cursor.moveToFirst()) cursor.getString(column) else null
                        } ?: "custom_font.ttf"
                        val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                        require(extension == "ttf" || extension == "otf") { fontFileTypeError }
                        val fontDir = File(context.filesDir, "fonts").apply { mkdirs() }
                        val safeName = displayName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(64)
                        val target = File(fontDir, "mathflow_${System.currentTimeMillis()}_$safeName")
                        importedTarget = target
                        context.contentResolver.openInputStream(uri).use { input ->
                            requireNotNull(input) { fontOpenError }
                            target.outputStream().use { output ->
                                val buffer = ByteArray(8192)
                                var total = 0L
                                while (true) {
                                    val count = input.read(buffer)
                                    if (count < 0) break
                                    total += count
                                    require(total <= 8L * 1024L * 1024L) { fontInstallError }
                                    output.write(buffer, 0, count)
                                }
                            }
                        }
                        require(target.length() > 0L) { fontEmptyError }
                        android.graphics.Typeface.createFromFile(target)
                        target to displayName
                    }.onFailure { importedTarget?.delete() }
                }
                imported.onSuccess { (target, displayName) ->
                    savedFonts = (savedFonts + (target.absolutePath to displayName)).distinctBy { it.first }
                    pendingFontPath = target.absolutePath
                    fontMessage = "$displayName — $fontAddApplyHint"
                }.onFailure {
                    fontMessage = fontInstallError
                }
            }
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val hasPendingChanges =
        pendingSettingsCornerRadiusDp != settingsCornerRadiusDp ||
        pendingFontScale != fontScale ||
        pendingIconId != selectedIconId ||
        pendingThemeId != selectedThemeId ||
        pendingUiStyleId != selectedUiStyleId ||
        pendingIconPackId != selectedIconPackId ||
        pendingAnimationSettings != appliedAnimationSettings ||
        pendingNavAnimationSettings != appliedNavAnimationSettings ||
        pendingFontPath != fontPath ||
        pendingLanguage != selectedLanguage ||
        pendingMeasurementSystem != selectedMeasurementSystem

    Box(Modifier.fillMaxSize()) {
    androidx.compose.animation.AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = section,
        transitionSpec = {
            val forward = initialState == SettingsSection.MAIN && targetState != SettingsSection.MAIN
            val rtl = isRtl
            val enterFrom = if (forward != rtl) 1 else -1
            slideInHorizontally(
                animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) { it * enterFrom } togetherWith slideOutHorizontally(
                animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
            ) { -it * enterFrom }
        },
        label = "SettingsSubpageTransition"
    ) { target ->
        when (target) {
            SettingsSection.MAIN -> PageColumn(tx("settings")) {
                Panel {
                    Text("ПЕРСОНАЛИЗАЦИЯ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    SettingsEntry(tx("themes"), ThemeOptions.firstOrNull { it.id == selectedThemeId }?.title ?: tx("theme_entry_subtitle"), AppGlyph.PALETTE) {
                        pendingThemeId = selectedThemeId; section = SettingsSection.THEMES
                    }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry("Стиль интерфейса", MathFlowUiStyle.fromId(selectedUiStyleId).title, AppGlyph.TUNE) {
                        pendingUiStyleId = selectedUiStyleId; section = SettingsSection.STYLES
                    }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry("Внешний вид и размеры", "Скругления, масштаб текста и плотность", AppGlyph.SETTINGS) { section = SettingsSection.DISPLAY }
                }
                Panel {
                    Text("НАВИГАЦИЯ И АНИМАЦИЯ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    SettingsEntry("Нижняя панель", "Материал, форма, преломление и движение", AppGlyph.HISTORY) {
                        pendingNavAnimationSettings = appliedNavAnimationSettings; navPreviewPage = Page.HOME; section = SettingsSection.NAVIGATION
                    }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry("Анимация кнопок", tx("animation_entry_subtitle"), AppGlyph.SETTINGS) {
                        pendingAnimationSettings = appliedAnimationSettings; section = SettingsSection.ANIMATIONS
                    }
                }
                Panel {
                    Text("ПЕРСОНАЛИЗАЦИЯ ПРИЛОЖЕНИЯ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    SettingsEntry(tx("app_icons"), tx("icon_entry_subtitle"), AppGlyph.BUILD) { pendingIconId = selectedIconId; section = SettingsSection.ICONS }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry("Стиль системных иконок", AppIconPack.fromId(selectedIconPackId).title, AppGlyph.TUNE) { pendingIconPackId = selectedIconPackId; section = SettingsSection.ICON_PACKS }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry(tx("fonts"), tx("font_library_hint"), AppGlyph.TEXT) {
                        pendingFontPath = fontPath; fontMessage = null; section = SettingsSection.FONTS
                    }
                }
                Panel {
                    Text("РЕГИОН И ФОРМАТЫ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    SettingsEntry(tx("app_language"), AppLanguages.firstOrNull { it.tag == selectedLanguage }?.nativeName ?: "Русский", AppGlyph.LANGUAGE) { section = SettingsSection.LANGUAGE }
                    HorizontalDivider(color = p.key.copy(alpha = 0.55f))
                    SettingsEntry(measurementText("measurements", LocalLanguage.current), measurementText("measurements_subtitle", LocalLanguage.current), AppGlyph.GLOBE) {
                        pendingMeasurementSystem = selectedMeasurementSystem; section = SettingsSection.MEASUREMENTS
                    }
                }
                Panel {
                    Text("ДАННЫЕ И ПРОФИЛИ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    SettingsEntry(tx("configurations"), tx("configurations_subtitle"), AppGlyph.BUILD) {
                        selectedConfigId = activeConfigId ?: configProfiles.firstOrNull()?.id.orEmpty(); section = SettingsSection.CONFIGS
                    }
                }
                Panel {
                    Text("MathFlow", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                    Text("Version $version", fontSize = 13.sp, color = p.accent)
                    Text(tx("about_description"), fontSize = 12.sp, color = p.muted, lineHeight = 18.sp)
                }
            }

            SettingsSection.DISPLAY -> PageColumn("Внешний вид и размеры") {
                Panel {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Скругление элементов", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                                Text("Радиус углов карточек и элементов настроек", fontSize = 12.sp, color = p.muted)
                            }
                            Text("$pendingSettingsCornerRadiusDp dp", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.accent)
                        }
                        Slider(value = pendingSettingsCornerRadiusDp.toFloat(), onValueChange = { pendingSettingsCornerRadiusDp = it.toInt().coerceIn(0, 40) },
                            valueRange = 0f..40f, steps = 19,
                            colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key,
                                activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0 · Острые", fontSize = 11.sp, color = p.muted); Text("40 · Сильные", fontSize = 11.sp, color = p.muted)
                        }
                    }
                }
                Panel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("text_size"), fontSize = 14.sp, color = p.text)
                        Text("${(pendingFontScale * 100).toInt()}%", fontSize = 14.sp, color = p.accent)
                    }
                    Slider(value = pendingFontScale, onValueChange = { pendingFontScale = Math.round(it * 20f) / 20f },
                        valueRange = 0.85f..1.25f, steps = 7,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key,
                            activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent))
                    Text("Изменение размера текста во всём приложении", fontSize = 12.sp, color = p.muted)
                }
                Text("Изменения применяются через общую кнопку «Применить».", fontSize = 12.sp, color = p.muted)
            }

            SettingsSection.ICON_PACKS -> PageColumn("Стиль иконок") {
                Text("Выбери единый набор для нижней навигации, настроек, действий и системных элементов MathFlow.",
                    color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                AppIconPack.entries.forEach { pack ->
                    val selected = pendingIconPackId == pack.id
                    Panel {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(if (selected) p.navActive else Color.Transparent)
                                .mathFlowClickable { pendingIconPackId = pack.id }
                                .padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(13.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                listOf(AppGlyph.HOME, AppGlyph.TOOLS, AppGlyph.SOLVER, AppGlyph.HISTORY, AppGlyph.SETTINGS).forEach { glyph ->
                                    AppIcon(glyph, contentDescription = null, tint = if (selected) p.accent else p.muted,
                                        modifier = Modifier.size(23.dp), pack = pack)
                                }
                            }
                            if (selected) AppIcon(AppGlyph.CHECK, null, tint = p.accent)
                        }
                        Text(pack.title, color = if (selected) p.accent else p.text,
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(pack.description, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                Text(
                    if (pendingIconPackId == selectedIconPackId) "Этот набор уже используется." else "Нажми «Применить» в нижней панели, чтобы сохранить выбор.",
                    color = p.muted, fontSize = 12.sp, lineHeight = 17.sp
                )
            }

            SettingsSection.ICONS -> PageColumn(tx("app_icons")) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(tx("icon_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                }
                val baseIcons = AppIconOptions.filter { it.id in setOf("vanilla", "black", "white") }
                val colorIcons = AppIconOptions.filterNot { it.id in setOf("vanilla", "black", "white") }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(displayText("ОСНОВНЫЕ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    baseIcons.forEach { option ->
                        IconOptionCard(option, pendingIconId == option.id, selectedIconId == option.id,
                            Modifier.fillMaxWidth()) { pendingIconId = option.id }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(displayText("ЦВЕТОВЫЕ ИКОНКИ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    colorIcons.forEach { option ->
                        IconOptionCard(option, pendingIconId == option.id, selectedIconId == option.id,
                            Modifier.fillMaxWidth()) { pendingIconId = option.id }
                    }
                }
                val selected = AppIconOptions.firstOrNull { it.id == pendingIconId } ?: AppIconOptions.first()
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LauncherIconPreview(
                            drawable = selected.drawable,
                            contentDescription = selected.title,
                            modifier = Modifier.size(68.dp)
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(selected.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                            Text(tx("icon_option_description"), fontSize = 13.sp, lineHeight = 18.sp, color = p.muted)
                        }
                    }
                    ApplySelectionButton(
                        text = if (pendingIconId == selectedIconId) tx("already_applied") else tx("apply"),
                        enabled = pendingIconId != selectedIconId,
                        onClick = { onIconSelected(pendingIconId) }
                    )
                }
            }


            SettingsSection.MEASUREMENTS -> PageColumn(measurementText("measurements", LocalLanguage.current)) {
                Text(measurementText("units_note", LocalLanguage.current), color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                listOf("auto", "metric", "us").forEach { mode ->
                    val selected = pendingMeasurementSystem == mode
                    val title = when (mode) {
                        "auto" -> measurementText("automatic", LocalLanguage.current)
                        "metric" -> measurementText("metric", LocalLanguage.current)
                        else -> measurementText("us_customary", LocalLanguage.current)
                    }
                    val description = when (mode) {
                        "auto" -> measurementText("automatic_desc", LocalLanguage.current)
                        "metric" -> measurementText("metric_desc", LocalLanguage.current)
                        else -> measurementText("us_desc", LocalLanguage.current)
                    }
                    Panel {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp))
                                .background(if (selected) p.navActive else Color.Transparent)
                                .mathFlowClickable { pendingMeasurementSystem = mode }
                                .padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(title, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(description, color = p.muted, fontSize = 12.sp)
                            }
                            if (selected) AppIcon(AppGlyph.CHECK, contentDescription = null, tint = p.accent)
                        }
                    }
                }
            }

            SettingsSection.ANIMATIONS -> PageColumn(tx("animations")) {
                Text(tx("animation_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                KeyAnimationStyle.values().forEach { style ->
                    val selected = pendingAnimationSettings.style == style
                    val shape = RoundedCornerShape(LocalMathFlowUiStyle.current.cardRadiusDp.dp)
                    Column(
                        Modifier.fillMaxWidth().clip(shape)
                            .background(if (selected) p.navActive else p.surface)
                            .border(if (selected) 2.dp else 1.dp, if (selected) p.accent else p.key, shape)
                            .mathFlowClickable { pendingAnimationSettings = pendingAnimationSettings.copy(style = style) }
                            .padding(horizontal = 15.dp, vertical = 13.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(tx(style.titleKey), color = if (selected) p.accent else p.text,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (selected) tx("selected") else " ", color = p.muted, fontSize = 10.sp, maxLines = 1)
                    }
                }
                Panel {
                    Text(tx("animation_preview"), color = p.muted, fontSize = 13.sp)
                    MathFlowKeyButton(
                        label = "123",
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        settings = pendingAnimationSettings
                    ) { }
                }
                Panel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("animation_duration"), color = p.text, fontSize = 14.sp)
                        Text("${pendingAnimationSettings.durationMs} ms", color = p.accent, fontSize = 14.sp)
                    }
                    Slider(
                        value = pendingAnimationSettings.durationMs.toFloat(),
                        onValueChange = {
                            pendingAnimationSettings = pendingAnimationSettings.copy(durationMs = (it / 10f).toInt() * 10)
                        },
                        valueRange = 70f..400f,
                        steps = 32,
                        colors = SliderDefaults.colors(
                            thumbColor = p.accent, activeTrackColor = p.accent,
                            inactiveTrackColor = p.key, activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("animation_opacity"), color = p.text, fontSize = 14.sp)
                        Text("${(pendingAnimationSettings.opacity * 100).toInt()}%", color = p.accent, fontSize = 14.sp)
                    }
                    Slider(
                        value = pendingAnimationSettings.opacity,
                        onValueChange = {
                            pendingAnimationSettings = pendingAnimationSettings.copy(opacity = (it * 20f).toInt() / 20f)
                        },
                        valueRange = 0.20f..1f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = p.accent, activeTrackColor = p.accent,
                            inactiveTrackColor = p.key, activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("animation_intensity"), color = p.text, fontSize = 14.sp)
                        Text("${(pendingAnimationSettings.intensity * 100).toInt()}%", color = p.accent, fontSize = 14.sp)
                    }
                    Slider(
                        value = pendingAnimationSettings.intensity,
                        onValueChange = {
                            pendingAnimationSettings = pendingAnimationSettings.copy(intensity = (it * 100f).toInt() / 100f)
                        },
                        valueRange = 0.02f..0.20f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = p.accent, activeTrackColor = p.accent,
                            inactiveTrackColor = p.key, activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )
                }

                Panel {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tx("button_color_effect"), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (pendingAnimationSettings.colorEffectEnabled) tx("button_color_effect_on") else tx("button_color_effect_off"),
                                color = p.muted, fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = pendingAnimationSettings.colorEffectEnabled,
                            onCheckedChange = { enabled ->
                                pendingAnimationSettings = pendingAnimationSettings.copy(colorEffectEnabled = enabled)
                            }
                        )
                    }
                    Text(
                        tx("effect_color"),
                        color = if (pendingAnimationSettings.colorEffectEnabled) p.muted else p.muted.copy(alpha = 0.45f),
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0L, 0xFFB9A8F0L, 0xFF39C7FFL, 0xFF4BE2B2L, 0xFFFF9A66L).forEach { argb ->
                            val color = if (argb == 0L) p.accent else Color(argb.toInt())
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(color)
                                    .border(
                                        if (pendingAnimationSettings.pressColorArgb == argb) 2.dp else 1.dp,
                                        if (pendingAnimationSettings.pressColorArgb == argb) p.text else p.muted,
                                        CircleShape
                                    )
                                    .then(
                                        if (pendingAnimationSettings.colorEffectEnabled) Modifier.mathFlowClickable {
                                            pendingAnimationSettings = pendingAnimationSettings.copy(pressColorArgb = argb)
                                        } else Modifier
                                    )
                            )
                        }
                    }
                    ApplySelectionButton(
                        text = if (pendingAnimationSettings == appliedAnimationSettings) tx("already_applied") else tx("apply"),
                        enabled = pendingAnimationSettings != appliedAnimationSettings,
                        onClick = { onAnimationSettingsApplied(pendingAnimationSettings.normalized()) }
                    )
                }
            }

            SettingsSection.NAVIGATION -> PageColumn("Нижняя панель") {
                Text("Настрой внешний вид панели отдельно от анимации кнопок.", color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                Panel {
                    Text("ПОВЕДЕНИЕ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Row(
                        Modifier.fillMaxWidth().toggleable(
                            value = pendingNavAnimationSettings.enabled,
                            role = Role.Switch,
                            onValueChange = { enabled ->
                                pendingNavAnimationSettings = pendingNavAnimationSettings.copy(enabled = enabled)
                            }
                        ).padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tx("nav_animations"), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (pendingNavAnimationSettings.enabled) tx("nav_enabled") else tx("nav_disabled"),
                                color = p.muted, fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = pendingNavAnimationSettings.enabled,
                            onCheckedChange = null
                        )
                    }
                }
                Panel {
                    Text("СТИЛЬ ПАНЕЛИ НАВИГАЦИИ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Text(
                        if (uiStyle.glassSurface) {
                            "Используется Liquid Glass: преломление, размытие и физика капли."
                        } else {
                            "Панель и индикатор используют оформление выбранного глобального стиля: ${uiStyle.title}."
                        },
                        color = p.muted, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }
                Panel {
                    Text("МАТОВЫЕ ПОВЕРХНОСТИ", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Text("Матовое стекло · общие поверхности", color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Эти параметры управляют непрозрачностью, глубиной, бликами и тенями матовых поверхностей. Капля на панели настраивается отдельно.",
                        color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    Text("Непрозрачность: ${(pendingNavAnimationSettings.matteOpacity * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.matteOpacity,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(matteOpacity = it) },
                        valueRange = 0.25f..0.96f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("Глубина: ${(pendingNavAnimationSettings.matteDepth * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.matteDepth,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(matteDepth = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("Блики: ${(pendingNavAnimationSettings.matteHighlight * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.matteHighlight,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(matteHighlight = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("Тени: ${(pendingNavAnimationSettings.matteShadow * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.matteShadow,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(matteShadow = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                }
                Panel {
                    Text("АНИМАЦИЯ И ФОРМА", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    BottomNavAnimationStyle.values().forEach { style ->
                        val selected = pendingNavAnimationSettings.style == style
                        val shape = RoundedCornerShape(LocalMathFlowUiStyle.current.cardRadiusDp.dp)
                        Column(
                            Modifier.fillMaxWidth().clip(shape)
                                .background(if (selected) p.navActive else p.surface)
                                .border(if (selected) 1.5.dp else 1.dp, if (selected) p.accent else p.key, shape)
                                .mathFlowClickable {
                                    pendingNavAnimationSettings = pendingNavAnimationSettings.copy(style = style)
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(style.title, color = if (selected) p.accent else p.text,
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(tx(style.descriptionKey), color = p.muted, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
                Panel {
                    Text("ПРЕДПРОСМОТР", color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    Panel {
                        Text(tx("nav_preview"), color = p.muted, fontSize = 13.sp)
                        Box(
                            Modifier.fillMaxWidth().height(80.dp)
                                .clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Capture only the preview's static background. The glass
                            // navigation is a sibling and never samples itself.
                            Box(
                                Modifier.matchParentSize()
                                    .layerBackdrop(navPreviewBackdrop)
                                    .background(p.background)
                            )
                            BottomNav(
                                navPreviewPage,
                                pendingNavAnimationSettings,
                                backdrop = navPreviewBackdrop,
                                preview = true
                            ) { navPreviewPage = it }
                        }
                    }
                    Text("${tx("nav_duration")}: ${pendingNavAnimationSettings.durationMs} ms", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.durationMs.toFloat(),
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(durationMs = it.toInt()) },
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text(
                        "${if (uiStyle.glassSurface) "Эластичность капли" else "Интенсивность анимации"}: ${(pendingNavAnimationSettings.intensity * 100).toInt()}%",
                        color = p.text, fontSize = 13.sp
                    )
                    Slider(
                        value = pendingNavAnimationSettings.intensity,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(intensity = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("Масштаб иконки: ${(pendingNavAnimationSettings.iconScale * 500).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.iconScale,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(iconScale = it) },
                        valueRange = 0f..0.20f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("Сила преломления: ${(pendingNavAnimationSettings.refractionStrength * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.refractionStrength, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(refractionStrength = it) }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("Овальность капли: ${(pendingNavAnimationSettings.dropletRoundness * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.dropletRoundness, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(dropletRoundness = it) }, valueRange = 0.80f..1f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("Размытие фона: ${pendingNavAnimationSettings.glassBlur.toInt()} dp", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.glassBlur, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(glassBlur = it) }, valueRange = 0f..20f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("Глубина стекла: ${(pendingNavAnimationSettings.glassDepth * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.glassDepth, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(glassDepth = it) }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("Блик: ${(pendingNavAnimationSettings.highlightStrength * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.highlightStrength, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(highlightStrength = it) }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("Тень: ${(pendingNavAnimationSettings.shadowStrength * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(value = pendingNavAnimationSettings.shadowStrength, onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(shadowStrength = it) }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key))
                    Text("${if (uiStyle.glassSurface) "Прозрачность стекла" else tx("nav_indicator_opacity")}: ${(pendingNavAnimationSettings.opacity * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.opacity,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(opacity = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    ApplySelectionButton(
                        text = if (pendingNavAnimationSettings == appliedNavAnimationSettings) tx("already_applied") else tx("apply"),
                        enabled = pendingNavAnimationSettings != appliedNavAnimationSettings,
                        onClick = { onNavAnimationSettingsApplied(pendingNavAnimationSettings.normalized()) }
                    )
                }
            }

            SettingsSection.FONTS -> PageColumn(tx("fonts")) {
                Text(tx("font_library_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                Panel {
                    ActionButton(tx("choose_font")) {
                        fontPicker.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream"))
                    }
                    fontMessage?.let { Text(it, fontSize = 12.sp, color = p.accent, lineHeight = 17.sp) }
                }
                if (savedFonts.isEmpty()) {
                    Panel {
                        Text(tx("no_saved_fonts"), color = p.muted, fontSize = 13.sp)
                    }
                } else {
                    savedFonts.forEach { (path, name) ->
                        val selected = pendingFontPath == path
                        val active = fontPath == path
                        Panel {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp))
                                    .background(if (selected) p.navActive else Color.Transparent)
                                    .mathFlowClickable { pendingFontPath = path }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(name, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        when { active -> tx("applied"); selected -> tx("selected"); else -> tx("font_saved") },
                                        color = if (active || selected) p.accent else p.muted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (selected) AppIcon(AppGlyph.CALCULATOR, contentDescription = null, tint = p.accent)
                            }
                        }
                    }
                }
                Panel {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp))
                            .mathFlowClickable { pendingFontPath = null }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tx("system_font"), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(if (pendingFontPath == null) tx("selected") else tx("font_system_subtitle"), color = p.muted, fontSize = 11.sp)
                        }
                        if (pendingFontPath == null) AppIcon(AppGlyph.CALCULATOR, contentDescription = null, tint = p.accent)
                    }
                    ApplySelectionButton(
                        text = when {
                            pendingFontPath == fontPath -> tx("already_applied")
                            else -> tx("apply")
                        },
                        enabled = pendingFontPath != fontPath,
                        onClick = {
                            if (pendingFontPath == null) onResetFont() else onFontSelected(pendingFontPath!!)
                            fontMessage = fontApplySuccess
                        }
                    )
                }
            }

            SettingsSection.CONFIGS -> PageColumn(tx("configurations")) {
                Text(tx("configurations_description"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)).background(p.navActive), contentAlignment = Alignment.Center) {
                            AppIcon(AppGlyph.BUILD, null, tint = p.accent, modifier = Modifier.size(22.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tx("profile_manager_title"), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(tx("profile_manager_subtitle"), color = p.muted, fontSize = 11.sp)
                        }
                        Text(configProfiles.size.toString(), color = p.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(configName, { configName = it.take(48) }, tx("profile_name_placeholder"), Modifier.weight(1f), numeric = false)
                        ActionButton(tx("create_profile"), Modifier.width(104.dp)) {
                            if (configName.isNotBlank()) {
                                onCreateConfig(configName)
                                configName = ""
                                configMessage = profileCreatedMessage
                            }
                        }
                    }
                    ActionButton(tx("import_profile")) {
                        importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                    }
                }
                if (configProfiles.isEmpty()) {
                    Panel { Text(tx("no_profiles_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp) }
                } else {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(tx("my_configurations"), color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(configProfiles.size.toString(), color = p.muted, fontSize = 12.sp)
                    }
                }
                configProfiles.forEach { profile ->
                    val selected = selectedConfigId == profile.id
                    val cfg = runCatching { validateConfigSnapshot(profile.settings) }.getOrNull()
                    val profileTheme = cfg?.optString("theme", "vanilla") ?: "vanilla"
                    val profileIcon = cfg?.optString("icon", "vanilla") ?: "vanilla"
                    val themeOption = ThemeOptions.firstOrNull { it.id == profileTheme } ?: ThemeOptions.first()
                    val cardBorder by animateColorAsState(
                        if (selected) p.accent.copy(alpha = 0.72f) else p.key.copy(alpha = 0.72f),
                        animationSpec = tween(260, easing = FastOutSlowInEasing),
                        label = "ConfigCardBorder"
                    )
                    val cardBackground by animateColorAsState(
                        if (selected) p.navActive.copy(alpha = 0.45f) else Color.Transparent,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                        label = "ConfigCardBackground"
                    )
                    Column(
                        Modifier.fillMaxWidth().animateContentSize(tween(340, easing = FastOutSlowInEasing))
                    ) {
                    Panel(modifier = Modifier.border(if (selected) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.cardRadiusDp).dp))) {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp))
                                .background(cardBackground)
                                .animateContentSize(tween(300, easing = FastOutSlowInEasing))
                                .mathFlowClickable { selectedConfigId = if (selected) "" else profile.id }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Box(Modifier.size(46.dp).clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)).background(themeOption.palette.background)
                                .border(1.dp, themeOption.palette.accent.copy(alpha = 0.55f), RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)),
                                contentAlignment = Alignment.Center) {
                                AppIcon(AppGlyph.SETTINGS, null, tint = themeOption.palette.accent, modifier = Modifier.size(22.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(profile.name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
                                Text(
                                    (ThemeOptions.firstOrNull { it.id == profileTheme }?.title ?: profileTheme) +
                                        " · " + (AppIconOptions.firstOrNull { it.id == profileIcon }?.title ?: profileIcon),
                                    color = p.muted, fontSize = 11.sp
                                )
                                Text(if (activeConfigId == profile.id) tx("active_profile") else tx("saved_profile"),
                                    color = if (activeConfigId == profile.id) p.accent else p.muted, fontSize = 11.sp)
                            }
                            if (activeConfigId == profile.id) {
                                AppIcon(AppGlyph.CHECK, contentDescription = null, tint = p.accent)
                            } else if (selected) {
                                AppIcon(AppGlyph.DROPDOWN, contentDescription = null, tint = p.accent)
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = selected,
                            enter = androidx.compose.animation.fadeIn(tween(220, delayMillis = 35)) +
                                androidx.compose.animation.expandVertically(tween(300, easing = FastOutSlowInEasing), expandFrom = Alignment.Top),
                            exit = androidx.compose.animation.fadeOut(tween(130)) +
                                androidx.compose.animation.shrinkVertically(tween(240, easing = FastOutSlowInEasing), shrinkTowards = Alignment.Top)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalDivider(color = p.key.copy(alpha = 0.8f))
                                Field(renameValue.ifBlank { profile.name }, { renameValue = it.take(48) }, tx("profile_name_placeholder"), numeric = false)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ActionButton(tx("apply_profile"), Modifier.weight(1f)) {
                                        onApplyConfig(profile)
                                        configMessage = profileAppliedMessage
                                    }
                                    ActionButton(tx("export_profile"), Modifier.weight(1f)) {
                                        exportLauncher.launch(profile.name.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".mf")
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ActionButton(tx("duplicate_profile"), Modifier.weight(1f)) {
                                        onDuplicateConfig(profile)
                                        configMessage = profileCreatedMessage
                                    }
                                    ActionButton(tx("rename_profile"), Modifier.weight(1f)) {
                                        val name = renameValue.trim()
                                        if (name.isNotEmpty()) {
                                            onRenameConfig(profile.id, name)
                                            renameValue = ""
                                            configMessage = profileRenamedMessage
                                        }
                                    }
                                }
                                ActionButton(tx("delete_profile")) {
                                    onDeleteConfig(profile.id)
                                    configMessage = profileDeletedMessage
                                }
                            }
                        }
                    }
                    }
                }
                configMessage?.let { Text(it, color = p.accent, fontSize = 12.sp) }
            }

            SettingsSection.LANGUAGE -> PageColumn(tx("app_language")) {
                Text(tx("language_hint"), color = p.muted, fontSize = 13.sp)
                OutlinedTextField(
                    value = languageQuery,
                    onValueChange = { languageQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(tx("search_languages")) },
                    shape = RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)
                )
                val visibleLanguages = AppLanguages.filter { language ->
                    language.nativeName.contains(languageQuery, ignoreCase = true) ||
                        language.englishName.contains(languageQuery, ignoreCase = true)
                }
                if (visibleLanguages.isEmpty()) Text(tx("no_languages_found"), color = p.muted, fontSize = 13.sp)
                visibleLanguages.forEach { language ->
                    val selected = pendingLanguage == language.tag
                    val shape = RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape)
                            .background(if (selected) p.navActive else p.surface)
                            .border(if (selected) 1.5.dp else 1.dp, if (selected) p.accent else p.key, shape)
                            .mathFlowClickable { pendingLanguage = language.tag }
                            .padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(language.nativeName, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(language.englishName, color = p.muted, fontSize = 12.sp)
                        }
                        if (selected) {
                            AppIcon(AppGlyph.CALCULATOR, contentDescription = tx("selected"), tint = p.accent)
                        }
                    }
                }
                Text(tx("languages_available"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            }

            SettingsSection.STYLES -> PageColumn("Стиль интерфейса") {
                Text("Выберите визуальное оформление всего приложения. Цветовая тема останется независимой.", color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                MathFlowUiStyle.entries.forEach { style ->
                    val selected = pendingUiStyleId == style.id
                    val applied = selectedUiStyleId == style.id
                    val shape = RoundedCornerShape(style.cardRadiusDp.dp)
                    Panel {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(style.title, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(style.description, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                            if (selected) AppIcon(AppGlyph.CHECK, contentDescription = "Выбрано", tint = p.accent)
                        }
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(style.cardRadiusDp.dp))
                                .background(p.background).padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.weight(1f).height(38.dp).clip(shape)
                                    .background(p.surface.copy(alpha = style.surfaceAlpha))
                                    .border(1.dp, p.text.copy(alpha = style.outlineAlpha.coerceAtMost(0.45f)), shape)
                            )
                            Box(
                                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(style.buttonRadiusDp.dp))
                                    .background(p.keyEquals),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("123", color = p.onKeyEquals, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(
                            when {
                                applied -> "Применён"
                                selected -> "Выбран"
                                else -> "Нажмите, чтобы выбрать"
                            },
                            color = if (selected) p.accent else p.muted,
                            fontSize = 11.sp
                        )
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(style.buttonRadiusDp.dp))
                                .background(if (selected) p.navActive else p.key)
                                .mathFlowClickable { pendingUiStyleId = style.id }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (selected) "Выбрано" else "Выбрать стиль", color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            SettingsSection.THEMES -> PageColumn(tx("themes")) {
                Text(tx("theme_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                val baseThemes = ThemeOptions.filter { it.id in setOf("vanilla", "black", "white") }
                val colorThemes = ThemeOptions.filterNot { it.id in setOf("vanilla", "black", "white") }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(displayText("ОСНОВНЫЕ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    baseThemes.forEach { option ->
                        ThemeOptionCard(option, pendingThemeId == option.id, selectedThemeId == option.id,
                            Modifier.fillMaxWidth()) { pendingThemeId = option.id }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(displayText("ЦВЕТОВЫЕ ТЕМЫ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    colorThemes.forEach { option ->
                        ThemeOptionCard(option, pendingThemeId == option.id, selectedThemeId == option.id,
                            Modifier.fillMaxWidth()) { pendingThemeId = option.id }
                    }
                }
                val selected = themeFor(pendingThemeId)
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(54.dp).clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)).background(selected.palette.background).border(1.dp, p.key, RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(selected.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                            Text(tx("theme_option_description"), fontSize = 13.sp, lineHeight = 18.sp, color = p.muted)
                        }
                    }
                    ApplySelectionButton(
                        text = if (pendingThemeId == selectedThemeId) tx("already_applied") else tx("apply"),
                        enabled = pendingThemeId != selectedThemeId,
                        onClick = { onThemeSelected(pendingThemeId) }
                    )
                }
            }
        }
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = hasPendingChanges,
        // Settings content already sits above the app's bottom navigation.
        // Do not apply system-bar insets a second time or push the action below the viewport.
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
        enter = androidx.compose.animation.fadeIn(tween(170)) +
            androidx.compose.animation.slideInVertically(tween(240, easing = FastOutSlowInEasing)) { it },
        exit = androidx.compose.animation.fadeOut(tween(120)) +
            androidx.compose.animation.slideOutVertically(tween(180, easing = FastOutSlowInEasing)) { it }
    ) {
        SettingsApplyBar {
            if (pendingSettingsCornerRadiusDp != settingsCornerRadiusDp) {
                onSettingsCornerRadiusChange(pendingSettingsCornerRadiusDp)
            }
            if (pendingFontScale != fontScale) onFontScale(pendingFontScale)
            if (pendingIconId != selectedIconId) onIconSelected(pendingIconId)
            if (pendingThemeId != selectedThemeId) onThemeSelected(pendingThemeId)
            if (pendingUiStyleId != selectedUiStyleId) onUiStyleSelected(pendingUiStyleId)
            if (pendingIconPackId != selectedIconPackId) onIconPackSelected(pendingIconPackId)
            if (pendingAnimationSettings != appliedAnimationSettings) {
                onAnimationSettingsApplied(pendingAnimationSettings.normalized())
            }
            if (pendingNavAnimationSettings != appliedNavAnimationSettings) {
                onNavAnimationSettingsApplied(pendingNavAnimationSettings.normalized())
            }
            if (pendingFontPath != fontPath) {
                if (pendingFontPath == null) onResetFont() else onFontSelected(pendingFontPath!!)
                fontMessage = fontApplySuccess
            }
            if (pendingLanguage != selectedLanguage) onLanguageSelected(pendingLanguage)
            if (pendingMeasurementSystem != selectedMeasurementSystem) onMeasurementSystemSelected(pendingMeasurementSystem)
        }
    }
    }
    val previewProfile = pendingImportedProfile
    if (previewProfile != null) {
        val previewConfig = runCatching { validateConfigSnapshot(previewProfile.settings) }.getOrNull()
        val previewTheme = previewConfig?.optString("theme", "vanilla") ?: "vanilla"
        val previewThemeOption = ThemeOptions.firstOrNull { it.id == previewTheme } ?: ThemeOptions.first()
        Dialog(onDismissRequest = { pendingImportedProfile = null }) {
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.cardRadiusDp).dp))
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(p.surface.copy(alpha = 0.90f), p.surface.copy(alpha = 0.76f))
                    ))
                    .border(1.dp, p.text.copy(alpha = 0.12f),
                        RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.cardRadiusDp).dp)),
                shape = RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.cardRadiusDp).dp),
                color = Color.Transparent,
                contentColor = p.text
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp)).background(previewThemeOption.palette.background),
                            contentAlignment = Alignment.Center) {
                            AppIcon(AppGlyph.SETTINGS, null, tint = previewThemeOption.palette.accent)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tx("profile_preview"), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(previewProfile.name, color = p.muted, fontSize = 12.sp)
                        }
                    }
                    Text(tx("profile_saved_before_apply"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    HorizontalDivider(color = p.key)
                    Text(tx("theme_label") + ": " + previewThemeOption.title, fontSize = 13.sp)
                    Text(tx("app_icons") + ": " + (AppIconOptions.firstOrNull { it.id == (previewConfig?.optString("icon") ?: "") }?.title ?: "—"), fontSize = 13.sp)
                    Text(tx("app_language") + ": " + (previewConfig?.optString("language") ?: "—").uppercase(Locale.ROOT), fontSize = 13.sp)
                    Text(tx("animations") + ": " + (previewConfig?.optString("keyStyle") ?: "—") + " / " + (previewConfig?.optString("navStyle") ?: "—"), fontSize = 13.sp)
                    Text(tx("font") + ": " + if (previewConfig?.optString("fontPath")?.takeIf { it.isNotBlank() && it != "null" } != null) tx("custom_font") else tx("system_font"), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton(tx("save_for_later"), Modifier.weight(1f)) {
                            pendingImportedProfile = null
                            configMessage = profileImportedMessage
                        }
                        ActionButton(tx("apply_now"), Modifier.weight(1f)) {
                            onApplyConfig(previewProfile)
                            pendingImportedProfile = null
                            configMessage = profileAppliedMessage
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherIconPreview(
    drawable: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    // Launcher icons are layer-list drawables. Android ImageView supports them;
    // Compose painterResource does not support this drawable type.
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { context ->
            android.widget.ImageView(context).apply {
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                this.contentDescription = contentDescription
                setImageResource(drawable)
            }
        },
        update = { imageView ->
            imageView.contentDescription = contentDescription
            imageView.setImageResource(drawable)
        }
    )
}

@Composable
private fun SettingsEntry(
    title: String,
    subtitle: String,
    icon: AppGlyph,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val settingsRadius = LocalSettingsCornerRadius.current
    val controlRadius = (settingsRadius ?: uiStyle.controlRadiusDp).dp
    val tileShape = RoundedCornerShape(controlRadius)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 68.dp)
            .clip(tileShape).mathFlowClickable(onClick = onClick)
            .padding(horizontal = 1.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(44.dp).clip(tileShape)
                .background(p.key),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(23.dp))
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                displayText(title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = p.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                displayText(subtitle),
                fontSize = 12.sp,
                color = p.muted,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text("›", color = p.muted, fontSize = 27.sp)
    }
}

@Composable
private fun IconOptionCard(
    option: AppIconOption,
    selected: Boolean,
    applied: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val settingsRadius = LocalSettingsCornerRadius.current
    val shape = RoundedCornerShape((settingsRadius ?: uiStyle.cardRadiusDp).dp)
    val borderColor by animateColorAsState(
        if (selected) p.accent else p.key,
        animationSpec = tween(190, easing = FastOutSlowInEasing),
        label = "IconCardBorder"
    )
    val cardContainer by animateColorAsState(
        (if (selected) p.navActive else p.surface).copy(alpha = uiStyle.surfaceAlpha),
        animationSpec = tween(190, easing = FastOutSlowInEasing),
        label = "IconCardContainer"
    )
    val cardScale by animateFloatAsState(
        if (selected) 1f else 0.985f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
        label = "IconCardScale"
    )
    Column(
        modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .animateContentSize(tween(200, easing = FastOutSlowInEasing))
            .clip(shape)
            .background(cardContainer)
            .border(if (selected) 1.8.dp else 1.dp, borderColor, shape)
            .mathFlowClickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(uiStyle.controlRadiusDp.dp))
                .background(option.previewBackground.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            LauncherIconPreview(
                drawable = option.drawable,
                contentDescription = option.title,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(uiStyle.controlRadiusDp.dp))
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(option.title, modifier = Modifier.weight(1f), fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (selected) {
                Box(Modifier.size(19.dp).clip(CircleShape).background(p.accent), contentAlignment = Alignment.Center) {
                    AppIcon(AppGlyph.CHECK, contentDescription = tx("selected"), tint = p.onKeyEquals, modifier = Modifier.size(13.dp))
                }
            }
        }
        Text(
            when { applied -> tx("applied"); selected -> tx("selected"); else -> " " },
            fontSize = 10.sp,
            color = if (applied) p.accent else p.muted,
            maxLines = 1
        )
    }
}

@Composable
private fun ThemeOptionCard(
    option: ThemeOption,
    selected: Boolean,
    applied: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val shape = RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.cardRadiusDp).dp)
    val borderColor by animateColorAsState(
        if (selected) p.accent else p.key,
        animationSpec = tween(190, easing = FastOutSlowInEasing),
        label = "ThemeCardBorder"
    )
    val cardScale by animateFloatAsState(
        if (selected) 1f else 0.985f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 520f),
        label = "ThemeCardScale"
    )
    Column(
        modifier.graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clip(shape)
            .background((if (selected) p.navActive else p.surface).copy(alpha = uiStyle.surfaceAlpha))
            .border(if (selected) 1.8.dp else 1.dp, borderColor, shape)
            .mathFlowClickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Tiny interface preview communicates the actual palette more clearly than swatches alone.
        Row(
            Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape((LocalSettingsCornerRadius.current ?: uiStyle.controlRadiusDp).dp))
                .background(option.palette.background).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(0.34f).fillMaxSize().clip(RoundedCornerShape((uiStyle.controlRadiusDp / 2).dp)).background(option.palette.surface)) {
                Column(Modifier.fillMaxSize().padding(5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.fillMaxWidth(0.72f).height(4.dp).clip(CircleShape).background(option.palette.text.copy(alpha = 0.8f)))
                    Box(Modifier.fillMaxWidth(0.48f).height(3.dp).clip(CircleShape).background(option.palette.muted.copy(alpha = 0.8f)))
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.fillMaxWidth(0.62f).height(7.dp).clip(CircleShape).background(option.palette.accent))
                }
            }
            Box(Modifier.weight(0.66f).fillMaxSize().clip(RoundedCornerShape(7.dp)).background(option.palette.surface).padding(5.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.fillMaxWidth(0.52f).height(4.dp).clip(CircleShape).background(option.palette.text.copy(alpha = 0.8f)))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.weight(1f).height(13.dp).clip(RoundedCornerShape((uiStyle.controlRadiusDp / 3).dp)).background(option.palette.key))
                        Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(5.dp)).background(option.palette.keyEquals))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(option.title, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (selected) {
                Box(Modifier.size(19.dp).clip(CircleShape).background(p.accent), contentAlignment = Alignment.Center) {
                    AppIcon(AppGlyph.CHECK, contentDescription = tx("selected"), tint = p.onKeyEquals, modifier = Modifier.size(13.dp))
                }
            }
        }
        Text(
            when { applied -> tx("applied"); selected -> tx("selected"); else -> " " },
            fontSize = 10.sp,
            color = if (applied) p.accent else p.muted,
            maxLines = 1
        )
    }
}

@Composable
private fun ApplySelectionButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    // Legacy call sites are intentionally inert; the shared bottom bar applies changes.
}

@Composable
private fun SettingsApplyBar(onClick: () -> Unit) {
    val p = LocalPalette.current
    val uiStyle = LocalMathFlowUiStyle.current
    val shape = RoundedCornerShape(uiStyle.buttonRadiusDp.dp)
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 560f),
        label = "SettingsApplyGlassPress"
    )
    Surface(
        modifier = Modifier.widthIn(min = 176.dp, max = 220.dp).height(52.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }

            .pointerInput(onClick) {
                detectTapGestures(onPress = {
                    pressed = true
                    val released = tryAwaitRelease()
                    pressed = false
                    if (released) onClick()
                })
            },
        shape = shape,
        color = p.keyEquals,
        shadowElevation = 10.dp
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val applyContentColor = p.onKeyEquals
            AppIcon(AppGlyph.CHECK, contentDescription = null, tint = applyContentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(9.dp))
            Text(tx("apply"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = applyContentColor)
        }
    }
}
