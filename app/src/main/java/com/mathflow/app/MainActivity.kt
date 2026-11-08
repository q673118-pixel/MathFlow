package com.mathflow.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MathFlowApp() }
    }
}

private enum class Page(val title: String, val icon: ImageVector) {
    HOME("Главная", Icons.Default.Calculate),
    TOOLS("Инструменты", Icons.Default.Build),
    SOLVER("Решатель", Icons.Default.Functions),
    HISTORY("История", Icons.Default.History),
    SETTINGS("Настройки", Icons.Default.Settings)
}

// ---------------------------------------------------------------- palette

private class Palette(
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

private val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { themeFor("vanilla").palette }

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

@Composable
private fun MathFlowApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mathflow_settings", Context.MODE_PRIVATE) }
    var themeId by remember {
        mutableStateOf(prefs.getString("app_theme", if (prefs.getBoolean("dark_mode", true)) "vanilla" else "white") ?: "vanilla")
    }
    var selectedIconId by remember { mutableStateOf(prefs.getString("app_icon", "vanilla") ?: "vanilla") }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
    var fontPath by remember { mutableStateOf(prefs.getString("font_path", null)) }
    var appLanguage by remember { mutableStateOf(prefs.getString("app_language", "ru") ?: "ru") }
    var page by remember { mutableStateOf(Page.HOME) }
    var history by remember { mutableStateOf(HistoryStore.load(context)) }
    val pageStateHolder = rememberSaveableStateHolder()
    val palette = remember(themeId) { themeFor(themeId).palette }

    LaunchedEffect(Unit) {
        setLauncherIcon(context, selectedIconId)
    }

    val customFont: FontFamily? = remember(fontPath) {
        fontPath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.length() > 0L) FontFamily(Font(file, FontWeight.Normal)) else null
            }.getOrNull()
        }
    }
    val family = customFont ?: FontFamily.Default

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
    }

    CompositionLocalProvider(LocalLanguage provides appLanguage, LocalDensity provides density, LocalPalette provides palette) {
        MaterialTheme(colorScheme = colors, typography = typography) {
            // Text() reads LocalTextStyle, not the theme typography, so the custom
            // family has to be provided here too for it to reach every label.
            CompositionLocalProvider(
                androidx.compose.material3.LocalTextStyle provides TextStyle(fontFamily = family)
            ) {
                Surface(Modifier.fillMaxSize(), color = palette.background, contentColor = palette.text) {
                    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                        AnimatedContent(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            targetState = page,
                            transitionSpec = {
                                val duration = NAV_TRANSITION_DURATION_MS
                                val easing = FastOutSlowInEasing
                                if (targetState.ordinal > initialState.ordinal) {
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = duration, easing = easing),
                                        initialOffsetX = { fullWidth -> fullWidth }
                                    ) togetherWith slideOutHorizontally(
                                        animationSpec = tween(durationMillis = duration, easing = easing),
                                        targetOffsetX = { fullWidth -> -fullWidth }
                                    )
                                } else {
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = duration, easing = easing),
                                        initialOffsetX = { fullWidth -> -fullWidth }
                                    ) togetherWith slideOutHorizontally(
                                        animationSpec = tween(durationMillis = duration, easing = easing),
                                        targetOffsetX = { fullWidth -> fullWidth }
                                    )
                                }
                            },
                            label = "MathFlowScreenTransition"
                        ) { targetPage ->
                            pageStateHolder.SaveableStateProvider(targetPage) {
                                Box(Modifier.fillMaxSize()) {
                                    when (targetPage) {
                                    Page.HOME -> CalculatorHome { expression, result, steps ->
                                        history = HistoryStore.add(context, expression, result, steps)
                                    }
                                    Page.TOOLS -> ToolsPage()
                                    Page.SOLVER -> EquationSolverPage()
                                    Page.HISTORY -> HistoryPage(history) {
                                        HistoryStore.clear(context)
                                        history = emptyList()
                                    }
                                    Page.SETTINGS -> SettingsPage(
                                        selectedThemeId = themeId,
                                        selectedIconId = selectedIconId,
                                        onThemeSelected = { id ->
                                            themeId = id
                                            prefs.edit().putString("app_theme", id).apply()
                                        },
                                        onIconSelected = { id ->
                                            selectedIconId = id
                                            prefs.edit().putString("app_icon", id).apply()
                                            setLauncherIcon(context, id)
                                        },
                                        fontScale = fontScale,
                                        fontPath = fontPath,
                                        selectedLanguage = appLanguage,
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
                                            prefs.edit().remove("font_path").apply()
                                        }
                                    )
                                    }
                                }
                            }
                        }
                        BottomNav(page) { page = it }
                    }
                }
            }
        }
    }
}

private const val NAV_TRANSITION_DURATION_MS = 280

@Composable
private fun BottomNav(current: Page, onSelect: (Page) -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(26.dp), color = p.surface) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                val itemWidth = maxWidth / Page.values().size
                // Wide, softly rounded active capsule (the original MathFlow shape),
                // centered inside each tab rather than a small circular dot.
                val indicatorWidth = 72.dp
                val indicatorHeight = 44.dp
                val density = LocalDensity.current
                val indicatorOffsetPx by animateFloatAsState(
                    targetValue = with(density) {
                        (itemWidth * current.ordinal + (itemWidth - indicatorWidth) / 2).toPx()
                    },
                    animationSpec = tween(
                        durationMillis = NAV_TRANSITION_DURATION_MS,
                        easing = FastOutSlowInEasing
                    ),
                    label = "MathFlowActiveTabPosition"
                )

                // Animate only the draw-layer translation, avoiding a layout pass
                // on every animation frame. Taps themselves have no ripple.
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer { translationX = indicatorOffsetPx }
                        .size(width = indicatorWidth, height = indicatorHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(p.navActive)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Page.values().forEach { item ->
                        val selected = item == current
                        val tint by animateColorAsState(
                            targetValue = if (selected) p.accent else p.muted,
                            animationSpec = tween(
                                durationMillis = NAV_TRANSITION_DURATION_MS,
                                easing = FastOutSlowInEasing
                            ),
                            label = "MathFlowNavigationIconTint"
                        )
                        val interactionSource = remember(item) { MutableInteractionSource() }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { if (!selected) onSelect(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = displayText(item.title),
                                tint = tint,
                                modifier = Modifier.size(26.dp)
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
private fun CalculatorHome(onCalculationSaved: (String, String, List<String>) -> Unit) {
    val p = LocalPalette.current
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("0") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var steps by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showSteps by rememberSaveable { mutableStateOf(true) }
    var scientific by rememberSaveable { mutableStateOf(false) }
    var degrees by rememberSaveable { mutableStateOf(true) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var continuation by rememberSaveable { mutableStateOf("") }

    fun clearAll() {
        expression = ""; result = "0"; message = null; steps = emptyList()
        finished = false; continuation = ""
    }

    fun append(text: String, continuesResult: Boolean) {
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
        try {
            var source = expression
            val open = source.count { it == '(' } - source.count { it == ')' }
            if (open > 0) source += ")".repeat(open)
            val r = ExpressionEngine(if (degrees) AngleMode.DEGREES else AngleMode.RADIANS).calculate(source)
            result = r.displayValue
            steps = r.steps
            message = null
            continuation = r.exactValue?.let {
                if (it.isInteger()) it.numerator.toString() else "(${it.numerator}/${it.denominator})"
            } ?: r.displayValue
            finished = true
            onCalculationSaved(source, r.displayValue, r.steps)
            expression = source
        } catch (e: IllegalArgumentException) {
            result = "Ошибка"
            message = when {
                e.message?.contains("Division by zero") == true -> "Деление на ноль"
                else -> e.message ?: "Проверьте выражение"
            }
            steps = emptyList()
        } catch (_: Exception) {
            result = "Ошибка"
            message = "Не удалось вычислить выражение"
            steps = emptyList()
        }
    }

    fun press(key: String) {
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
                    if (scientific) Chip(if (degrees) "DEG" else "RAD", false) { degrees = !degrees }
                    if (steps.isNotEmpty()) Chip(tx("steps"), showSteps) { showSteps = !showSteps }
                }
                Box(
                    Modifier.align(Alignment.TopEnd).size(40.dp).clip(RoundedCornerShape(20.dp))
                        .clickable { press("⌫") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, tx("delete"), tint = p.muted, modifier = Modifier.size(22.dp))
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
                    Text(
                        result,
                        fontSize = when {
                            result.length > 16 -> 24.sp
                            result.length > 11 -> 32.sp
                            result.length > 8 -> 40.sp
                            else -> 52.sp
                        },
                        fontWeight = FontWeight.Medium,
                        color = if (result == "Ошибка") p.error else p.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    message?.let {
                        Text(it, fontSize = 13.sp, color = p.error, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (steps.isNotEmpty() && showSteps) {
            Surface(Modifier.fillMaxWidth().heightIn(max = 96.dp), shape = RoundedCornerShape(20.dp), color = p.surface) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp)) {
                    steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", fontSize = 13.sp, color = p.muted, lineHeight = 18.sp)
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
            .clickable(onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (active) p.accent else p.muted)
    }
}

@Composable
private fun CalcKey(key: String, modifier: Modifier, compact: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    val operator = key in OPERATORS
    val equals = key == "="
    val background = when {
        equals -> p.keyEquals
        operator || key == "AC" -> p.keyAccent
        else -> p.key
    }
    val foreground = if (equals) p.onKeyEquals else p.text
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(50), color = background, contentColor = foreground) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                key,
                fontSize = when {
                    key.length > 1 && key != "AC" -> if (compact) 18.sp else 20.sp
                    else -> if (compact) 22.sp else 28.sp
                },
                fontWeight = FontWeight.Normal,
                color = foreground,
                maxLines = 1
            )
        }
    }
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
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = p.surface) {
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
                val interaction = remember(option) { MutableInteractionSource() }
                Box(
                    Modifier.weight(1f).height(38.dp)
                        .clickable(interactionSource = interaction, indication = null) {
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
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp), color = p.keyEquals) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = p.onKeyEquals)
        }
    }
}

@Composable
private fun ResultBox(text: String, error: String?) {
    val p = LocalPalette.current
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = p.key) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (error != null) Text(error, color = p.error, fontSize = 14.sp)
            else Text(text, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
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
        Segmented(listOf("Дроби", "Проценты", "Геометрия", "Конвертер"), tool) { tool = it }
        Panel {
            when (tool) {
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
                    if (fractionResult.isNotEmpty() || fractionError != null) ResultBox(fractionResult, fractionError)
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
                    if (percentResult.isNotEmpty() || percentError != null) ResultBox(percentResult, percentError)
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
                    if (geometryResult.isNotEmpty() || geometryError != null) ResultBox(geometryResult, geometryError)
                }
                "Конвертер" -> UnitConverterContent()
            }
        }
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
    var equation by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf<String?>(null) }
    var steps by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val equationFallback = tx("equation_error")

    PageColumn(tx("solver")) {
        Panel {
            Field(equation, { equation = it }, tx("example_equation"), Modifier.fillMaxWidth(), numeric = false)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("x", "x²", "(", ")", "+", "−", "×", "÷", "=").forEach { symbol ->
                    Chip(symbol, false) { equation += symbol }
                }
            }
            ActionButton(tx("solve")) {
                try {
                    val solved = AlgebraSolver().solve(equation)
                    answer = solved.answer
                    steps = solved.steps
                    error = null
                } catch (e: IllegalArgumentException) {
                    answer = null
                    steps = emptyList()
                    error = friendly(e, equationFallback)
                }
            }
        }
        error?.let { Text(it, color = p.error, fontSize = 14.sp) }
        answer?.let { text ->
            Panel {
                Text(text, color = p.accent, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                if (steps.isNotEmpty()) {
                    HorizontalDivider(color = p.key)
                    steps.forEachIndexed { index, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${index + 1}.", color = p.accent, fontSize = 14.sp)
                            Text(step, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        }
        Text(
            tx("equation_help"),
            color = p.muted, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

// ---------------------------------------------------------------- history

@Composable
private fun HistoryPage(entries: List<HistoryEntry>, onClear: () -> Unit) {
    val p = LocalPalette.current
    PageColumn(tx("history")) {
        if (entries.isEmpty()) {
            Text(tx("history_empty"), color = p.muted, fontSize = 14.sp)
        } else {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = p.surface) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    entries.forEachIndexed { index, entry ->
                        var expanded by remember(entry.id) { mutableStateOf(false) }
                        Column(
                            Modifier.fillMaxWidth()
                                .clickable(enabled = entry.steps.isNotEmpty()) { expanded = !expanded }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(entry.expression, fontSize = 15.sp, color = p.muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("= ${entry.result}", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = p.text)
                            Text(
                                entry.dateLabel + if (entry.steps.isNotEmpty()) (if (expanded) tx("hide_solution") else tx("show_solution")) else "",
                                fontSize = 12.sp, color = p.muted
                            )
                            if (expanded) {
                                entry.steps.forEachIndexed { i, step ->
                                    Text("${i + 1}. $step", fontSize = 13.sp, color = p.muted, lineHeight = 18.sp)
                                }
                            }
                        }
                        if (index != entries.lastIndex) HorizontalDivider(color = p.key, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
            TextButton(onClick = onClear, modifier = Modifier.align(Alignment.End)) {
                Text(tx("clear_history"), color = p.accent)
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
    val aliases = AppIconOptions
    val selectedComponent = android.content.ComponentName(context, "${context.packageName}.Launcher${selected.id.replaceFirstChar { it.uppercase(Locale.ROOT) }}")
    packageManager.setComponentEnabledSetting(
        selectedComponent,
        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        android.content.pm.PackageManager.DONT_KILL_APP
    )
    aliases.filterNot { it.id == selected.id }.forEach { option ->
        val component = android.content.ComponentName(context, "${context.packageName}.Launcher${option.id.replaceFirstChar { it.uppercase(Locale.ROOT) }}")
        packageManager.setComponentEnabledSetting(
            component,
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            android.content.pm.PackageManager.DONT_KILL_APP
        )
    }
}

private enum class SettingsSection { MAIN, ICONS, THEMES, LANGUAGE }

@Composable
private fun SettingsPage(
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
    onLanguageSelected: (String) -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var section by rememberSaveable { mutableStateOf(SettingsSection.MAIN) }
    var pendingIconId by rememberSaveable(selectedIconId) { mutableStateOf(selectedIconId) }
    var pendingThemeId by rememberSaveable(selectedThemeId) { mutableStateOf(selectedThemeId) }
    var fontMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val fontFileTypeError = tx("font_file_type")
    val fontOpenError = tx("font_open_error")
    val fontEmptyError = tx("font_empty")
    val fontInstallError = tx("font_install_error")
    val fontAppliedLabel = tx("font_applied")
    val defaultFontLabel = tx("default_font")
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (column >= 0 && cursor.moveToFirst()) cursor.getString(column) else null
                } ?: "custom_font.ttf"
                val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                require(extension == "ttf" || extension == "otf") { fontFileTypeError }
                val fontDir = File(context.filesDir, "fonts").apply { mkdirs() }
                val target = File(fontDir, "mathflow_custom_${System.currentTimeMillis()}.$extension")
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { fontOpenError }
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                require(target.length() > 0L) { fontEmptyError }
                android.graphics.Typeface.createFromFile(target)
                fontDir.listFiles()?.filter { it != target }?.forEach { it.delete() }
                onFontSelected(target.absolutePath)
                fontMessage = "$fontAppliedLabel: $displayName"
            }.onFailure {
                fontMessage = fontInstallError
            }
        }
    }

    var horizontalDrag by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 64.dp.toPx() }
    androidx.compose.animation.AnimatedContent(
        modifier = Modifier.fillMaxSize().pointerInput(section) {
            detectHorizontalDragGestures(
                onDragStart = { horizontalDrag = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    horizontalDrag += dragAmount
                    change.consume()
                },
                onDragEnd = {
                    if (section != SettingsSection.MAIN && horizontalDrag > swipeThreshold) {
                        section = SettingsSection.MAIN
                    }
                    horizontalDrag = 0f
                },
                onDragCancel = { horizontalDrag = 0f }
            )
        },
        targetState = section,
        transitionSpec = {
            val forward = initialState == SettingsSection.MAIN && targetState != SettingsSection.MAIN
            if (forward) {
                slideInHorizontally(
                    animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
                ) { it } togetherWith slideOutHorizontally(
                    animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
                ) { -it }
            } else {
                slideInHorizontally(
                    animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
                ) { -it } togetherWith slideOutHorizontally(
                    animationSpec = tween(NAV_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)
                ) { it }
            }
        },
        label = "SettingsSubpageTransition"
    ) { target ->
        when (target) {
            SettingsSection.MAIN -> PageColumn(tx("settings")) {
                Panel {
                    SettingsEntry(
                        title = "Иконки приложения",
                        subtitle = "Выберите значок MathFlow на рабочем столе",
                        icon = Icons.Default.Build,
                        onClick = { pendingIconId = selectedIconId; section = SettingsSection.ICONS }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = "Темы оформления",
                        subtitle = "Цвета интерфейса, независимо от иконки",
                        icon = Icons.Default.Settings,
                        onClick = { pendingThemeId = selectedThemeId; section = SettingsSection.THEMES }
                    )
                }
                Panel {
                    SettingsEntry(
                        title = "Язык приложения",
                        subtitle = AppLanguages.firstOrNull { it.tag == selectedLanguage }?.nativeName ?: "Русский",
                        icon = Icons.Default.Functions,
                        onClick = { section = SettingsSection.LANGUAGE }
                    )
                }
                Panel {
                    Text(tx("font"), fontSize = 15.sp, color = p.muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            ActionButton(tx("choose_font")) {
                                fontPicker.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream"))
                            }
                        }
                        if (fontPath != null) {
                            Surface(
                                onClick = { onResetFont(); fontMessage = defaultFontLabel },
                                modifier = Modifier.height(50.dp), shape = RoundedCornerShape(25.dp), color = p.key
                            ) {
                                Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                                    Text(tx("reset"), fontSize = 15.sp, color = p.text)
                                }
                            }
                        }
                    }
                    fontMessage?.let { Text(it, fontSize = 13.sp, color = p.accent) }
                }
                Panel {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tx("text_size"), fontSize = 15.sp, color = p.muted)
                        Text("${(fontScale * 100).toInt()}%", fontSize = 15.sp, color = p.text)
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { onFontScale(Math.round(it * 20f) / 20f) },
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
                TextButton(onClick = { section = SettingsSection.MAIN }) { Text("← ${tx("settings")}", color = p.accent) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(tx("icon_hint"), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                }
                AppIconOptions.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { option ->
                            IconOptionCard(
                                option = option,
                                selected = pendingIconId == option.id,
                                applied = selectedIconId == option.id,
                                modifier = Modifier.weight(1f),
                                onClick = { pendingIconId = option.id }
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
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

            SettingsSection.LANGUAGE -> PageColumn(tx("app_language")) {
                TextButton(onClick = { section = SettingsSection.MAIN }) { Text("← ${tx("settings")}", color = p.accent) }
                Text(tx("language_hint"), color = p.muted, fontSize = 13.sp)
                AppLanguages.forEach { language ->
                    val selected = selectedLanguage == language.tag
                    val shape = RoundedCornerShape(18.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape)
                            .background(if (selected) p.navActive else p.surface)
                            .border(if (selected) 1.5.dp else 1.dp, if (selected) p.accent else p.key, shape)
                            .clickable { onLanguageSelected(language.tag) }
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
                TextButton(onClick = { section = SettingsSection.MAIN }) { Text("← ${tx("settings")}", color = p.accent) }
                Text(tx("theme_hint"), color = p.muted, fontSize = 13.sp)
                ThemeOptions.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { option ->
                            ThemeOptionCard(
                                option = option,
                                selected = pendingThemeId == option.id,
                                applied = selectedThemeId == option.id,
                                modifier = Modifier.weight(1f),
                                onClick = { pendingThemeId = option.id }
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 5.dp),
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
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.clip(shape)
            .background(p.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) p.accent else p.key, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        LauncherIconPreview(
            drawable = option.drawable,
            contentDescription = option.title,
            modifier = Modifier.size(70.dp)
        )
        Text(option.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1)
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
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.clip(shape)
            .background(p.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) p.accent else p.key, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(option.palette.background, option.palette.surface, option.palette.keyEquals, option.palette.accent).forEach { color ->
                Box(Modifier.size(18.dp).clip(CircleShape).background(color))
            }
        }
        Text(option.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1)
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
    val p = LocalPalette.current
    Surface(
        modifier = Modifier.fillMaxWidth().height(50.dp).then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(25.dp),
        color = if (enabled) p.keyEquals else p.key
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (enabled) p.onKeyEquals else p.muted)
        }
    }
}
