package com.mathflow.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
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
    val mScale = remember { Animatable(0f) }
    val startY = with(density) { (-450).dp.toPx() }
    val firstBounceY = with(density) { (-42).dp.toPx() }
    val secondBounceY = with(density) { (-14).dp.toPx() }
    val dotY = remember { Animatable(startY) }
    val dotAlpha = remember { Animatable(0f) }
    val dotScaleX = remember { Animatable(1f) }
    val dotScaleY = remember { Animatable(1f) }
    val splashAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(500L)
        mScale.animateTo(
            1.08f,
            tween(390, easing = androidx.compose.animation.core.Easing { f ->
                1f - (1f - f) * (1f - f) * (1f - f)
            })
        )
        mScale.animateTo(1f, spring(dampingRatio = 0.48f, stiffness = 430f))
        dotAlpha.snapTo(1f)

        // Run the ball trajectory as separate phases. Each impact is a real
        // direction change, so it cannot be skipped by reaching the final keyframe early.
        kotlinx.coroutines.coroutineScope {
            launch {
                // Gravity: accelerate downwards into the first impact.
                dotY.animateTo(
                    0f,
                    tween(420, easing = androidx.compose.animation.core.Easing { it * it })
                )
                // Stay at the contact point while the ball visibly compresses.
                delay(60L)
                // First rebound: upward, then gravity pulls the ball back down.
                dotY.animateTo(
                    firstBounceY,
                    tween(190, easing = androidx.compose.animation.core.Easing { 1f - (1f - it) * (1f - it) })
                )
                dotY.animateTo(
                    0f,
                    tween(170, easing = androidx.compose.animation.core.Easing { it * it })
                )
                // A shorter second impact, followed by a smaller rebound.
                delay(45L)
                dotY.animateTo(
                    secondBounceY,
                    tween(125, easing = androidx.compose.animation.core.Easing { 1f - (1f - it) * (1f - it) })
                )
                dotY.animateTo(
                    0f,
                    tween(115, easing = androidx.compose.animation.core.Easing { it * it })
                )
            }
            launch {
                // Keep the ball round during the fall; squash only at contact.
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

        // Let the final landing settle before the overlay begins to fade.
        delay(120L)
        splashAlpha.animateTo(
            0f,
            tween(1000, easing = LinearOutSlowInEasing)
        )
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
        Image(
            painter = painterResource(R.drawable.mark_m),
            contentDescription = null,
            modifier = Modifier.size(108.dp).graphicsLayer {
                scaleX = mScale.value
                scaleY = mScale.value
            },
            colorFilter = ColorFilter.tint(palette.accent)
        )
        Image(
            painter = painterResource(R.drawable.mark_dot),
            contentDescription = null,
            modifier = Modifier.size(108.dp).graphicsLayer {
                translationY = dotY.value
                alpha = dotAlpha.value
                scaleX = dotScaleX.value
                scaleY = dotScaleY.value
                transformOrigin = TransformOrigin(0.5f, 0.308f)
            },
            colorFilter = ColorFilter.tint(palette.keyEquals)
        )
    }
}

private enum class Page(val title: String, val icon: ImageVector) {
    HOME("Главная", Icons.Default.Calculate),
    TOOLS("Инструменты", Icons.Default.Build),
    SOLVER("Уравнения", Icons.Default.Functions),
    HISTORY("История", Icons.Default.History),
    SETTINGS("Настройки", Icons.Default.Settings)
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
    ))
)

private fun themeFor(id: String): ThemeOption =
    ThemeOptions.firstOrNull { it.id == id } ?: ThemeOptions.first()

internal val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { themeFor("vanilla").palette }

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
private fun MathFlowApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mathflow_settings", Context.MODE_PRIVATE) }
    var themeId by remember {
        mutableStateOf(prefs.getString("app_theme", if (prefs.getBoolean("dark_mode", true)) "vanilla" else "white") ?: "vanilla")
    }
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
                opacity = prefs.getFloat("nav_animation_opacity", 0.85f)
            ).normalized()
        )
    }
    var configProfiles by remember { mutableStateOf(MathFlowConfigProfiles.load(context)) }
    var activeConfigId by remember {
        mutableStateOf(prefs.getString("mathflow_active_config", null)?.takeIf { id -> configProfiles.any { it.id == id } })
    }
    LaunchedEffect(themeId, selectedIconId, fontScale, fontPath, appLanguage, measurementSystem,
        keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat, activeConfigId) {
        val activeId = activeConfigId
        if (activeId != null) {
            val snapshot = makeConfigSnapshot(themeId, selectedIconId, fontScale, fontPath,
                appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat)
            configProfiles = configProfiles.map { profile ->
                if (profile.id == activeId) profile.copy(settings = snapshot) else profile
            }
            MathFlowConfigProfiles.save(context, configProfiles)
        }
    }

    var page by remember { mutableStateOf(Page.HOME) }
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
    val palette = remember(themeId) { themeFor(themeId).palette }
    var showWelcome by remember { mutableStateOf(prefs.getInt("welcome_version", 0) < 3) }
    var splashFinished by remember { mutableStateOf(false) }
    // The first page must be fully laid out behind the intro; only later tab changes animate.
    var initialPageRendered by remember { mutableStateOf(false) }

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
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                            // Keep only one full page tree composed on low-memory devices.
                            key(page) {
                                pageStateHolder.SaveableStateProvider(page) {
                                    val enterProgress = remember { Animatable(0f) }
                                    LaunchedEffect(Unit) {
                                        if (!initialPageRendered) {
                                            enterProgress.snapTo(1f)
                                            initialPageRendered = true
                                        } else {
                                            enterProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(PAGE_ENTER_DURATION_MS, easing = FastOutSlowInEasing)
                                            )
                                        }
                                    }
                                    Box(
                                        Modifier.weight(1f).fillMaxWidth().graphicsLayer {
                                            alpha = enterProgress.value
                                            translationX = (1f - enterProgress.value) * 8.dp.toPx()
                                        }
                                    ) {
                                        when (page) {
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
                                            Page.SETTINGS -> SettingsPage(
                                                currentConfig = makeConfigSnapshot(themeId, selectedIconId, fontScale, fontPath,
                                                    appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat),
                                                configProfiles = configProfiles,
                                                activeConfigId = activeConfigId,
                                                onCreateConfig = { name ->
                                                    val profile = MathFlowConfigProfiles.create(name, makeConfigSnapshot(themeId, selectedIconId, fontScale,
                                                        fontPath, appLanguage, measurementSystem, keyAnimationSettings, bottomNavAnimationSettings, calculatorResultFormat))
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
                                                        cfg.getDouble("navIconScale").toFloat(), cfg.getDouble("navOpacity").toFloat()
                                                    ).normalized()
                                                    prefs.edit()
                                                        .putString("app_theme", themeId).putString("app_icon", selectedIconId)
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
                                                },
                                                selectedThemeId = themeId,
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
                                                        .apply()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            BottomNav(page, bottomNavAnimationSettings) { page = it }
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
    Dialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp).heightIn(max = 760.dp),
            shape = RoundedCornerShape(30.dp),
            color = p.surface,
            contentColor = p.text,
            shadowElevation = 14.dp
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(16.dp), color = p.key, modifier = Modifier.size(48.dp)) {
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
                        shape = RoundedCornerShape(18.dp),
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
                            Icon(Icons.Default.ArrowDropDown, null, tint = p.accent)
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
                            shape = RoundedCornerShape(16.dp),
                            color = optionBackground,
                            border = androidx.compose.foundation.BorderStroke(if (selected) 1.4.dp else 1.dp, optionBorder)
                        ) {
                            Row(Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(description, fontSize = 10.sp, color = p.muted)
                                }
                                if (selected) Icon(Icons.Default.Check, null, tint = p.accent)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("appearance", language), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("03", color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    ThemeOptions.chunked(2).forEach { rowThemes ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            rowThemes.forEach { option ->
                                ThemeOptionCard(
                                    option = option,
                                    selected = themeId == option.id,
                                    applied = themeId == option.id,
                                    modifier = Modifier.weight(1f)
                                ) { onThemeSelected(option.id) }
                            }
                            if (rowThemes.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tx("app_icons"), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(tx("icon_hint"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                    AppIconOptions.chunked(2).forEach { rowIcons ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowIcons.forEach { option ->
                                IconOptionCard(
                                    option = option,
                                    selected = pendingIconId == option.id,
                                    applied = selectedIconId == option.id,
                                    modifier = Modifier.weight(1f)
                                ) { pendingIconId = option.id }
                            }
                            if (rowIcons.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(tx("fonts"), color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
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
                                modifier = Modifier.weight(1f).animateContentSize(tween(180))
                                    .mathFlowClickable { onFontSelected(id) },
                                shape = RoundedCornerShape(16.dp),
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
                    modifier = Modifier.fillMaxWidth().mathFlowClickable {
                        if (pendingIconId != selectedIconId) onIconSelected(pendingIconId)
                        onConfirm()
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = p.accent
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(measurementText("continue", language), modifier = Modifier.weight(1f), color = if (p.dark) Color(0xFF17131F) else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Check, null, tint = if (p.dark) Color(0xFF17131F) else Color.White)
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 620.dp),
                shape = RoundedCornerShape(26.dp),
                color = p.surface,
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
                        shape = RoundedCornerShape(15.dp)
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
                                shape = RoundedCornerShape(15.dp),
                                color = languageCardColor,
                                border = androidx.compose.foundation.BorderStroke(if (selected) 1.3.dp else 1.dp, languageBorderColor)
                            ) {
                                Row(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(10.dp), color = p.surface, modifier = Modifier.size(38.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(item.tag.uppercase(Locale.ROOT), color = p.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column(Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(item.nativeName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(item.englishName, fontSize = 11.sp, color = p.muted)
                                    }
                                    if (selected) Icon(Icons.Default.Check, null, tint = p.accent)
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
private const val PAGE_ENTER_DURATION_MS = 260

@Composable
private fun BottomNav(
    current: Page,
    settings: BottomNavAnimationSettings,
    preview: Boolean = false,
    onSelect: (Page) -> Unit
) {
    val p = LocalPalette.current
    val style = settings.style
    val duration = if (settings.enabled) settings.durationMs else 0
    val springMotion = settings.enabled && style in setOf(
        BottomNavAnimationStyle.FLUID_PILL,
        BottomNavAnimationStyle.SPRING_MOTION,
        BottomNavAnimationStyle.ELASTIC_SLIDE
    )
    // Compose springs do not have a fixed duration. Map the duration control to
    // spring stiffness so slower settings remain visibly slower in spring styles.
    val springStiffness = bottomNavSpringStiffness(duration)
    Box(
        Modifier.fillMaxWidth()
            .then(if (preview) Modifier else Modifier.navigationBarsPadding())
            .padding(horizontal = 28.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(26.dp), color = p.surface) {
            BoxWithConstraints(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                val itemWidth = maxWidth / Page.values().size
                val density = LocalDensity.current
                val layoutDirection = LocalLayoutDirection.current
                val visualIndex = if (layoutDirection == LayoutDirection.Rtl) {
                    Page.values().lastIndex - current.ordinal
                } else current.ordinal
                val indicatorWidth = minOf(
                    when (style) {
                        BottomNavAnimationStyle.LIGHT_TRAIL -> 38.dp
                        BottomNavAnimationStyle.SOFT_FOCUS -> 54.dp
                        BottomNavAnimationStyle.RIPPLE_BLOOM -> 66.dp
                        else -> 72.dp
                    },
                    itemWidth * 0.92f
                )
                val trailWidth = minOf(62.dp, itemWidth * 0.96f)
                val indicatorHeight = when (style) {
                    BottomNavAnimationStyle.LIGHT_TRAIL -> 4.dp
                    BottomNavAnimationStyle.MORPH -> 38.dp
                    BottomNavAnimationStyle.SOFT_FOCUS -> 38.dp
                    else -> 42.dp
                }
                val targetOffset = with(density) {
                    (itemWidth * visualIndex + (itemWidth - indicatorWidth) / 2).toPx()
                }
                // The indicator always follows the duration slider. Spring-like character
                // is retained by the stretch and icon-scale effects below.
                val offsetSpec = tween<Float>(
                    durationMillis = duration,
                    easing = FastOutSlowInEasing
                )
                val indicatorOffset by animateFloatAsState(
                    targetValue = targetOffset,
                    animationSpec = offsetSpec,
                    label = "MathFlowNavIndicatorPosition"
                )
                val indicatorScaleX by animateFloatAsState(
                    targetValue = when {
                        !settings.enabled -> 1f
                        style == BottomNavAnimationStyle.EXPAND -> 1f + settings.intensity * 0.12f
                        style == BottomNavAnimationStyle.ELASTIC_SLIDE -> 1f + settings.intensity * 0.20f
                        style == BottomNavAnimationStyle.LIGHT_TRAIL -> 1f + settings.intensity * 0.08f
                        else -> 1f
                    },
                    animationSpec = tween(duration, easing = FastOutSlowInEasing),
                    label = "MathFlowNavIndicatorScale"
                )
                val indicatorAlpha by animateFloatAsState(
                    targetValue = if (style == BottomNavAnimationStyle.SOFT_FOCUS) {
                        settings.opacity * 0.62f
                    } else settings.opacity,
                    animationSpec = tween(duration),
                    label = "MathFlowNavIndicatorAlpha"
                )
                val bloom = remember(current, style) { Animatable(0.72f) }
                LaunchedEffect(current, style, settings.enabled, duration, settings.intensity) {
                    if (settings.enabled && style in setOf(BottomNavAnimationStyle.RIPPLE_BLOOM, BottomNavAnimationStyle.EXPAND)) {
                        bloom.snapTo(
                            if (style == BottomNavAnimationStyle.RIPPLE_BLOOM) 0.08f + settings.intensity * 0.34f
                            else 0.38f + settings.intensity * 0.36f
                        )
                        bloom.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
                    } else {
                        bloom.snapTo(1f)
                    }
                }
                val travelStretch = remember(current, style) { Animatable(1f) }
                LaunchedEffect(current, style, settings.enabled, duration, settings.intensity) {
                    if (settings.enabled && style in setOf(BottomNavAnimationStyle.FLUID_PILL, BottomNavAnimationStyle.ELASTIC_SLIDE)) {
                        travelStretch.snapTo(
                            1f + settings.intensity * if (style == BottomNavAnimationStyle.FLUID_PILL) 0.28f else 0.38f
                        )
                        travelStretch.animateTo(
                            1f,
                            spring(
                                dampingRatio = if (style == BottomNavAnimationStyle.FLUID_PILL) 0.72f else 0.48f,
                                stiffness = springStiffness * if (style == BottomNavAnimationStyle.FLUID_PILL) 0.95f else 1.05f
                            )
                        )
                    } else {
                        travelStretch.snapTo(1f)
                    }
                }
                if (style != BottomNavAnimationStyle.SOFT_FOCUS || !settings.enabled) {
                    Box(
                        Modifier.align(Alignment.CenterStart)
                            .graphicsLayer {
                                translationX = indicatorOffset - if (style == BottomNavAnimationStyle.LIGHT_TRAIL) {
                                    with(density) { ((trailWidth - indicatorWidth) / 2).toPx() }
                                } else 0f
                                scaleX = if (style == BottomNavAnimationStyle.RIPPLE_BLOOM || style == BottomNavAnimationStyle.EXPAND) bloom.value else indicatorScaleX * travelStretch.value
                                alpha = indicatorAlpha
                            }
                            .offset(y = if (style == BottomNavAnimationStyle.LIGHT_TRAIL) 14.dp else 0.dp)
                            .size(
                                width = if (style == BottomNavAnimationStyle.LIGHT_TRAIL) trailWidth else indicatorWidth,
                                height = if (style == BottomNavAnimationStyle.LIGHT_TRAIL) 12.dp else indicatorHeight
                            )
                    ) {
                        if (style == BottomNavAnimationStyle.LIGHT_TRAIL && settings.enabled) {
                            Box(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(percent = 50))
                                    .background(p.accent.copy(alpha = 0.16f))
                            )
                            Box(
                                Modifier.align(Alignment.Center).size(width = indicatorWidth, height = 3.dp)
                                    .clip(RoundedCornerShape(percent = 50)).background(p.accent)
                            )
                        } else {
                            Box(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(percent = 50))
                                    .background(if (style == BottomNavAnimationStyle.LIGHT_TRAIL) p.accent else p.navActive)
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Page.values().forEach { item ->
                        val selected = item == current
                        val tint by animateColorAsState(
                            targetValue = if (selected) p.accent else p.muted,
                            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconTint"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (settings.enabled && selected &&
                                style in setOf(
                                    BottomNavAnimationStyle.SPRING_MOTION,
                                    BottomNavAnimationStyle.MORPH,
                                    BottomNavAnimationStyle.SOFT_FOCUS,
                                    BottomNavAnimationStyle.ELASTIC_SLIDE
                                )
                            ) 1f + settings.iconScale else 1f,
                            animationSpec = if (springMotion) spring(
                                dampingRatio = 0.88f - settings.intensity * 0.38f,
                                stiffness = springStiffness
                            ) else tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconScale"
                        )
                        val morphY by animateFloatAsState(
                            targetValue = if (settings.enabled && selected && style == BottomNavAnimationStyle.MORPH) {
                                1f - settings.iconScale * (0.35f + settings.intensity * 0.45f)
                            } else 1f,
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavigationIconMorph"
                        )
                        val focusAlpha by animateFloatAsState(
                            targetValue = if (settings.enabled && selected && style == BottomNavAnimationStyle.SOFT_FOCUS) {
                                settings.opacity * 0.72f
                            } else 0f,
                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                            label = "MathFlowNavSoftFocusAlpha"
                        )
                        Box(
                            Modifier.weight(1f).height(48.dp).mathFlowClickable {
                                if (!selected) onSelect(item)
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
                            Icon(
                                imageVector = item.icon,
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
            }
        }
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
        Surface(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(28.dp), color = p.surface) {
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
                    Modifier.align(Alignment.TopEnd).size(40.dp).clip(RoundedCornerShape(20.dp))
                        .mathFlowClickable { press("⌫") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, tx("delete"), tint = p.muted, modifier = Modifier.size(22.dp))
                }
                val decimalFormatLabel = tx("decimal_format")
                val fractionFormatLabel = tx("fraction_format")
                Box(Modifier.align(Alignment.TopStart).padding(top = 54.dp).width(220.dp)) {
                    Segmented(
                        listOf(decimalFormatLabel, fractionFormatLabel),
                        if (resultFormat == "decimal") decimalFormatLabel else fractionFormatLabel
                    ) { selected ->
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
            Surface(Modifier.fillMaxWidth().heightIn(max = 96.dp), shape = RoundedCornerShape(20.dp), color = p.surface) {
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
    Box(
        Modifier.height(32.dp).clip(RoundedCornerShape(16.dp))
            .background(if (active) p.navActive else p.key)
            .mathFlowClickable(onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(displayText(label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (active) p.accent else p.muted)
    }
}

@Composable
private fun CalcKey(key: String, modifier: Modifier, compact: Boolean, onClick: () -> Unit) {
    MathFlowKeyButton(
        label = key,
        modifier = modifier,
        compact = compact,
        onClick = onClick
    )
}

// ---------------------------------------------------------------- shared pieces

@Composable
private fun PageColumn(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 14.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(displayText(title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
        content()
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    Surface(
        modifier.animateContentSize(tween(180)).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = p.surface
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

@Composable
private fun Segmented(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val p = LocalPalette.current
    BoxWithConstraints(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(p.key).padding(3.dp)
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
                .clip(RoundedCornerShape(19.dp))
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
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(displayText(label), maxLines = 1) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
    )
}

@Composable
private fun ActionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    Surface(
        modifier = modifier.fillMaxWidth().height(50.dp).mathFlowClickable(onClick = onClick),
        shape = RoundedCornerShape(25.dp),
        color = p.keyEquals
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(displayText(text), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = p.onKeyEquals)
        }
    }
}

@Composable
private fun ResultBox(text: String, error: String?) {
    val p = LocalPalette.current
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = p.key) {
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
        Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(20.dp)).background(p.key)
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
                    shape = RoundedCornerShape(18.dp),
                    color = p.surface
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

private enum class SettingsSection { MAIN, ICONS, THEMES, LANGUAGE, MEASUREMENTS, ANIMATIONS, FONTS, CONFIGS }

private fun makeConfigSnapshot(
    theme: String, icon: String, fontScale: Float, fontPath: String?, language: String,
    measurement: String, key: KeyAnimationSettings, nav: BottomNavAnimationSettings,
    resultFormat: String
): JSONObject = JSONObject()
    .put("schemaVersion", 2)
    .put("theme", theme)
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

private fun validateConfigSnapshot(input: JSONObject): JSONObject {
    require(input.optInt("schemaVersion", -1) in 1..2) { "Unsupported settings schema" }
    val theme = input.optString("theme", "vanilla")
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
        input.optDouble("navOpacity", 0.85).toFloat()
    ).normalized()
    return makeConfigSnapshot(theme, icon, fontScale, input.optString("fontPath").takeIf { it.isNotBlank() && it != "null" },
        language, measurement, key, nav, resultFormat)
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
    onImportConfig: suspend (MathFlowConfigArchive.ImportedConfig) -> Unit,
    selectedThemeId: String,
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
    var pendingFontScale by remember(fontScale) { mutableFloatStateOf(fontScale) }
    var pendingLanguage by remember(selectedLanguage) { mutableStateOf(selectedLanguage) }
    var pendingMeasurementSystem by remember(selectedMeasurementSystem) { mutableStateOf(selectedMeasurementSystem) }
    var pendingAnimationSettings by remember(appliedAnimationSettings) { mutableStateOf(appliedAnimationSettings) }
    var pendingNavAnimationSettings by remember(appliedNavAnimationSettings) { mutableStateOf(appliedNavAnimationSettings) }
    var navPreviewPage by rememberSaveable { mutableStateOf(Page.HOME) }
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
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
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
                        onImportConfig(imported.getOrThrow())
                        configMessage = profileImportedMessage
                    } catch (_: Exception) {
                        configMessage = profileImportFailedMessage
                    }
                } else {
                    configMessage = profileImportFailedMessage
                }
            }
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
        pendingFontScale != fontScale ||
        pendingIconId != selectedIconId ||
        pendingThemeId != selectedThemeId ||
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
                    SettingsEntry(
                        title = tx("app_icons"),
                        subtitle = tx("icon_entry_subtitle"),
                        icon = Icons.Default.Build,
                        onClick = { pendingIconId = selectedIconId; section = SettingsSection.ICONS }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = tx("themes"),
                        subtitle = tx("theme_entry_subtitle"),
                        icon = Icons.Default.Settings,
                        onClick = { pendingThemeId = selectedThemeId; section = SettingsSection.THEMES }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = tx("app_language"),
                        subtitle = AppLanguages.firstOrNull { it.tag == selectedLanguage }?.nativeName ?: "Русский",
                        icon = Icons.Default.Functions,
                        onClick = { section = SettingsSection.LANGUAGE }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = measurementText("measurements", LocalLanguage.current),
                        subtitle = measurementText("measurements_subtitle", LocalLanguage.current),
                        icon = Icons.Default.Functions,
                        onClick = { pendingMeasurementSystem = selectedMeasurementSystem; section = SettingsSection.MEASUREMENTS }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = tx("animations"),
                        subtitle = tx("animation_entry_subtitle"),
                        icon = Icons.Default.Settings,
                        onClick = {
                            pendingAnimationSettings = appliedAnimationSettings
                            pendingNavAnimationSettings = appliedNavAnimationSettings
                            section = SettingsSection.ANIMATIONS
                        }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = tx("configurations"),
                        subtitle = tx("configurations_subtitle"),
                        icon = Icons.Default.Build,
                        onClick = { selectedConfigId = activeConfigId ?: configProfiles.firstOrNull()?.id.orEmpty(); section = SettingsSection.CONFIGS }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = tx("fonts"),
                        subtitle = tx("font_library_hint"),
                        icon = Icons.Default.Functions,
                        onClick = {
                            pendingFontPath = fontPath
                            fontMessage = null
                            section = SettingsSection.FONTS
                        }
                    )
                }
                Panel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("text_size"), fontSize = 15.sp, color = p.muted)
                        Text("${(fontScale * 100).toInt()}%", fontSize = 15.sp, color = p.text)
                    }
                    Slider(
                        value = pendingFontScale,
                        onValueChange = { pendingFontScale = Math.round(it * 20f) / 20f },
                        valueRange = 0.85f..1.25f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = p.accent, activeTrackColor = p.accent,
                            inactiveTrackColor = p.key, activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent
                        )
                    )
                }
                Panel {
                    Text("MathFlow", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Version $version", fontSize = 14.sp, color = p.accent)
                    Text(
                        tx("about_description"),
                        fontSize = 13.sp, color = p.muted, lineHeight = 18.sp
                    )
                }
            }

            SettingsSection.ICONS -> PageColumn(tx("app_icons")) {
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(tx("icon_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                }
                val baseIcons = AppIconOptions.filter { it.id in setOf("vanilla", "black", "white") }
                val colorIcons = AppIconOptions.filterNot { it.id in setOf("vanilla", "black", "white") }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(displayText("ОСНОВНЫЕ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    baseIcons.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { option ->
                                IconOptionCard(option, pendingIconId == option.id, selectedIconId == option.id,
                                    Modifier.weight(1f)) { pendingIconId = option.id }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(displayText("ЦВЕТОВЫЕ ИКОНКИ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    colorIcons.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { option ->
                                IconOptionCard(option, pendingIconId == option.id, selectedIconId == option.id,
                                    Modifier.weight(1f)) { pendingIconId = option.id }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
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
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
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
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
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
                            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = p.accent)
                        }
                    }
                }
            }

            SettingsSection.ANIMATIONS -> PageColumn(tx("animations")) {
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
                Text(tx("animation_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                KeyAnimationStyle.values().toList().chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { style ->
                            val selected = pendingAnimationSettings.style == style
                            val shape = RoundedCornerShape(17.dp)
                            Column(
                                Modifier.weight(1f).clip(shape)
                                    .background(if (selected) p.navActive else p.surface)
                                    .border(if (selected) 2.dp else 1.dp, if (selected) p.accent else p.key, shape)
                                    .mathFlowClickable { pendingAnimationSettings = pendingAnimationSettings.copy(style = style) }
                                    .padding(horizontal = 13.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(tx(style.titleKey), color = if (selected) p.accent else p.text,
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (selected) tx("selected") else " ",
                                    color = p.muted, fontSize = 10.sp, maxLines = 1
                                )
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
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
                Panel {
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
                    BottomNavAnimationStyle.values().toList().chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            pair.forEach { style ->
                                val selected = pendingNavAnimationSettings.style == style
                                val shape = RoundedCornerShape(15.dp)
                                Column(
                                    Modifier.weight(1f).clip(shape)
                                        .background(if (selected) p.navActive else p.surface)
                                        .border(if (selected) 1.5.dp else 1.dp, if (selected) p.accent else p.key, shape)
                                        .mathFlowClickable {
                                            pendingNavAnimationSettings = pendingNavAnimationSettings.copy(style = style)
                                        }
                                        .padding(horizontal = 11.dp, vertical = 11.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(style.title, color = if (selected) p.accent else p.text,
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(tx(style.descriptionKey), color = p.muted, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                    Panel {
                        Text(tx("nav_preview"), color = p.muted, fontSize = 13.sp)
                        BottomNav(navPreviewPage, pendingNavAnimationSettings, preview = true) { navPreviewPage = it }
                    }
                    Text("${tx("nav_duration")}: ${pendingNavAnimationSettings.durationMs} ms", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.durationMs.toFloat(),
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(durationMs = it.toInt()) },
                        valueRange = 100f..1000f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("${tx("animation_intensity")}: ${(pendingNavAnimationSettings.intensity * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.intensity,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(intensity = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("${tx("nav_icon_scale")}: ${(pendingNavAnimationSettings.iconScale * 100).toInt()}%", color = p.text, fontSize = 13.sp)
                    Slider(
                        value = pendingNavAnimationSettings.iconScale,
                        onValueChange = { pendingNavAnimationSettings = pendingNavAnimationSettings.copy(iconScale = it) },
                        valueRange = 0f..0.20f,
                        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent, inactiveTrackColor = p.key)
                    )
                    Text("${tx("nav_indicator_opacity")}: ${(pendingNavAnimationSettings.opacity * 100).toInt()}%", color = p.text, fontSize = 13.sp)
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
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
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
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
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
                                if (selected) Icon(Icons.Default.Calculate, contentDescription = null, tint = p.accent)
                            }
                        }
                    }
                }
                Panel {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .mathFlowClickable { pendingFontPath = null }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(tx("system_font"), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(if (pendingFontPath == null) tx("selected") else tx("font_system_subtitle"), color = p.muted, fontSize = 11.sp)
                        }
                        if (pendingFontPath == null) Icon(Icons.Default.Calculate, contentDescription = null, tint = p.accent)
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
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
                Text(tx("configurations_description"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Field(configName, { configName = it.take(48) }, tx("profile_name_placeholder"), Modifier.weight(1f), numeric = false)
                    ActionButton(tx("create_profile"), Modifier.width(104.dp)) { if (configName.isNotBlank()) { onCreateConfig(configName); configMessage = profileCreatedMessage } }
                }
                if (configProfiles.isEmpty()) {
                    Panel { Text(tx("no_profiles_hint"), color = p.muted, fontSize = 13.sp) }
                }
                configProfiles.forEach { profile ->
                    val selected = selectedConfigId == profile.id
                    Panel {
                        Row(Modifier.fillMaxWidth().mathFlowClickable { selectedConfigId = profile.id },
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(profile.name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(if (activeConfigId == profile.id) tx("active_profile") else tx("saved_profile"),
                                    color = p.muted, fontSize = 12.sp)
                            }
                            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = p.accent)
                        }
                        if (selected) {
                            Field(renameValue.ifBlank { profile.name }, { renameValue = it.take(48) }, tx("profile_name_placeholder"), numeric = false)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(tx("apply_profile"), Modifier.weight(1f)) { onApplyConfig(profile); configMessage = profileAppliedMessage }
                                ActionButton(tx("export_profile"), Modifier.weight(1f)) { exportLauncher.launch(profile.name.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".mf") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton(tx("rename_profile"), Modifier.weight(1f)) {
                                    val name = renameValue.trim()
                                    if (name.isNotEmpty()) { onRenameConfig(profile.id, name); renameValue = ""; configMessage = profileRenamedMessage }
                                }
                                ActionButton(tx("delete_profile"), Modifier.weight(1f)) { onDeleteConfig(profile.id); configMessage = profileDeletedMessage }
                            }
                        }
                    }
                }
                ActionButton(tx("import_profile")) { importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) }
                configMessage?.let { Text(it, color = p.accent, fontSize = 12.sp) }
            }

            SettingsSection.LANGUAGE -> PageColumn(tx("app_language")) {
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
                Text(tx("language_hint"), color = p.muted, fontSize = 13.sp)
                OutlinedTextField(
                    value = languageQuery,
                    onValueChange = { languageQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(tx("search_languages")) },
                    shape = RoundedCornerShape(16.dp)
                )
                val visibleLanguages = AppLanguages.filter { language ->
                    language.nativeName.contains(languageQuery, ignoreCase = true) ||
                        language.englishName.contains(languageQuery, ignoreCase = true)
                }
                if (visibleLanguages.isEmpty()) Text(tx("no_languages_found"), color = p.muted, fontSize = 13.sp)
                visibleLanguages.forEach { language ->
                    val selected = pendingLanguage == language.tag
                    val shape = RoundedCornerShape(18.dp)
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
                            Icon(Icons.Default.Calculate, contentDescription = tx("selected"), tint = p.accent)
                        }
                    }
                }
                Text(tx("languages_available"), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            }

            SettingsSection.THEMES -> PageColumn(tx("themes")) {
                MathFlowTextButton("← ${tx("settings")}", p.accent) { section = SettingsSection.MAIN }
                Text(tx("theme_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                val baseThemes = ThemeOptions.filter { it.id in setOf("vanilla", "black", "white") }
                val colorThemes = ThemeOptions.filterNot { it.id in setOf("vanilla", "black", "white") }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(displayText("ОСНОВНЫЕ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    baseThemes.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { option ->
                                ThemeOptionCard(option, pendingThemeId == option.id, selectedThemeId == option.id,
                                    Modifier.weight(1f)) { pendingThemeId = option.id }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(displayText("ЦВЕТОВЫЕ ТЕМЫ"), color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                    colorThemes.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { option ->
                                ThemeOptionCard(option, pendingThemeId == option.id, selectedThemeId == option.id,
                                    Modifier.weight(1f)) { pendingThemeId = option.id }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                val selected = themeFor(pendingThemeId)
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(selected.palette.background).border(1.dp, p.key, RoundedCornerShape(16.dp)))
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
        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
            .padding(bottom = 28.dp).offset(y = 32.dp),
        enter = androidx.compose.animation.fadeIn(tween(170)) +
            androidx.compose.animation.slideInVertically(tween(240, easing = FastOutSlowInEasing)) { it },
        exit = androidx.compose.animation.fadeOut(tween(120)) +
            androidx.compose.animation.slideOutVertically(tween(180, easing = FastOutSlowInEasing)) { it }
    ) {
        SettingsApplyBar {
            if (pendingFontScale != fontScale) onFontScale(pendingFontScale)
            if (pendingIconId != selectedIconId) onIconSelected(pendingIconId)
            if (pendingThemeId != selectedThemeId) onThemeSelected(pendingThemeId)
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
    icon: ImageVector,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).mathFlowClickable(onClick = onClick).padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(p.key), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(23.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(displayText(title), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = p.text)
            Text(displayText(subtitle), fontSize = 12.sp, color = p.muted, lineHeight = 16.sp)
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
    val shape = RoundedCornerShape(19.dp)
    val borderColor by animateColorAsState(
        if (selected) p.accent else p.key,
        animationSpec = tween(190, easing = FastOutSlowInEasing),
        label = "IconCardBorder"
    )
    val cardContainer by animateColorAsState(
        if (selected) p.navActive else p.surface,
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
            Modifier.fillMaxWidth().height(62.dp).clip(RoundedCornerShape(13.dp))
                .background(option.previewBackground.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            LauncherIconPreview(
                drawable = option.drawable,
                contentDescription = option.title,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp))
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(option.title, modifier = Modifier.weight(1f), fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (selected) {
                Box(Modifier.size(19.dp).clip(CircleShape).background(p.accent), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = tx("selected"), tint = p.onKeyEquals, modifier = Modifier.size(13.dp))
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
    val shape = RoundedCornerShape(19.dp)
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
            .background(if (selected) p.navActive else p.surface)
            .border(if (selected) 1.8.dp else 1.dp, borderColor, shape)
            .mathFlowClickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Tiny interface preview communicates the actual palette more clearly than swatches alone.
        Row(
            Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(10.dp))
                .background(option.palette.background).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(0.34f).fillMaxSize().clip(RoundedCornerShape(7.dp)).background(option.palette.surface)) {
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
                        Box(Modifier.weight(1f).height(13.dp).clip(RoundedCornerShape(5.dp)).background(option.palette.key))
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
                    Icon(Icons.Default.Check, contentDescription = tx("selected"), tint = p.onKeyEquals, modifier = Modifier.size(13.dp))
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
    val shape = RoundedCornerShape(28.dp)
    Surface(
        modifier = Modifier.widthIn(min = 176.dp, max = 220.dp).height(52.dp).clip(shape)
            .mathFlowClickable(onClick = onClick),
        shape = shape,
        color = p.keyEquals,
        shadowElevation = 10.dp
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = p.onKeyEquals, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(9.dp))
            Text(tx("apply"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = p.onKeyEquals)
        }
    }
}
