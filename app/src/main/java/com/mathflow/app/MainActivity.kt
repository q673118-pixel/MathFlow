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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
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

private val DarkPalette = Palette(
    background = Color(0xFF0C0B10),
    surface = Color(0xFF16141C),
    key = Color(0xFF211F29),
    keyAccent = Color(0xFF54447F),
    keyEquals = Color(0xFF6A5AA3),
    onKeyEquals = Color(0xFFFFFFFF),
    text = Color(0xFFE9E5F3),
    muted = Color(0xFF9A94AB),
    accent = Color(0xFFB9A8F0),
    navActive = Color(0xFF2E2942),
    error = Color(0xFFFF8A80),
    dark = true
)

private val LightPalette = Palette(
    background = Color(0xFFF3F0F8),
    surface = Color(0xFFFFFFFF),
    key = Color(0xFFE4DFEC),
    keyAccent = Color(0xFFD3C7F0),
    keyEquals = Color(0xFF6750A4),
    onKeyEquals = Color(0xFFFFFFFF),
    text = Color(0xFF1D1A24),
    muted = Color(0xFF625B71),
    accent = Color(0xFF6750A4),
    navActive = Color(0xFFE2D9F5),
    error = Color(0xFFB3261E),
    dark = false
)

private val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { DarkPalette }

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
    var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
    var fontPath by remember { mutableStateOf(prefs.getString("font_path", null)) }
    var page by remember { mutableStateOf(Page.HOME) }
    var history by remember { mutableStateOf(HistoryStore.load(context)) }

    val customFont: FontFamily? = remember(fontPath) {
        fontPath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.length() > 0L) FontFamily(Font(file, FontWeight.Normal)) else null
            }.getOrNull()
        }
    }
    val family = customFont ?: FontFamily.Default
    val palette = if (darkMode) DarkPalette else LightPalette

    val baseDensity = LocalDensity.current
    val density = remember(baseDensity, fontScale) {
        Density(baseDensity.density, baseDensity.fontScale * fontScale)
    }

    val colors = if (darkMode) {
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

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkMode
        controller.isAppearanceLightNavigationBars = !darkMode
    }

    CompositionLocalProvider(LocalDensity provides density, LocalPalette provides palette) {
        MaterialTheme(colorScheme = colors, typography = typographyFor(family)) {
            // Text() reads LocalTextStyle, not the theme typography, so the custom
            // family has to be provided here too for it to reach every label.
            CompositionLocalProvider(
                androidx.compose.material3.LocalTextStyle provides TextStyle(fontFamily = family)
            ) {
                Surface(Modifier.fillMaxSize(), color = palette.background, contentColor = palette.text) {
                    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            when (page) {
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
                                    darkMode = darkMode,
                                    fontScale = fontScale,
                                    fontPath = fontPath,
                                    onTheme = { dark ->
                                        darkMode = dark
                                        prefs.edit().putBoolean("dark_mode", dark).apply()
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
                        BottomNav(page) { page = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNav(current: Page, onSelect: (Page) -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 28.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(26.dp), color = p.surface) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Page.values().forEach { item ->
                    val selected = item == current
                    Box(
                        Modifier
                            .size(width = 52.dp, height = 38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(if (selected) p.navActive else Color.Transparent)
                            .clickable { onSelect(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            tint = if (selected) p.accent else p.muted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- calculator

private val OPERATORS = setOf("÷", "×", "−", "+")

@Composable
private fun CalculatorHome(onCalculationSaved: (String, String, List<String>) -> Unit) {
    val p = LocalPalette.current
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var message by remember { mutableStateOf<String?>(null) }
    var steps by remember { mutableStateOf(emptyList<String>()) }
    var showSteps by remember { mutableStateOf(true) }
    var scientific by remember { mutableStateOf(false) }
    var degrees by remember { mutableStateOf(true) }
    var finished by remember { mutableStateOf(false) }
    var continuation by remember { mutableStateOf("") }

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

    val mainRows = listOf(
        listOf("AC", "(", ")", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("%", "0", ".", "=")
    )
    val sciRows = listOf(
        listOf("sin", "cos", "tan", "ln"),
        listOf("log", "abs", "π", "e"),
        listOf("√", "x²", "xʸ", "⌫")
    )

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
                    if (steps.isNotEmpty()) Chip("Решение", showSteps) { showSteps = !showSteps }
                }
                Box(
                    Modifier.align(Alignment.TopEnd).size(40.dp).clip(RoundedCornerShape(20.dp))
                        .clickable { press("⌫") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, "Удалить", tint = p.muted, modifier = Modifier.size(22.dp))
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

        val rows = if (scientific) sciRows + mainRows else mainRows
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
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
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
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(p.key).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(19.dp))
                    .background(if (active) p.keyEquals else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option, fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Medium,
                    color = if (active) p.onKeyEquals else p.muted
                )
            }
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, numeric: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, maxLines = 1) },
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
    var tool by remember { mutableStateOf("Дроби") }

    var fractionA by remember { mutableStateOf("3/4") }
    var fractionB by remember { mutableStateOf("2/5") }
    var fractionResult by remember { mutableStateOf("") }
    var fractionError by remember { mutableStateOf<String?>(null) }

    var percentMode by remember { mutableStateOf("% от числа") }
    var percentA by remember { mutableStateOf("15") }
    var percentB by remember { mutableStateOf("240") }
    var percentResult by remember { mutableStateOf("") }
    var percentError by remember { mutableStateOf<String?>(null) }

    var shape by remember { mutableStateOf("Прямоугольник") }
    var sideA by remember { mutableStateOf("5") }
    var sideB by remember { mutableStateOf("3") }
    var sideC by remember { mutableStateOf("4") }
    var geometryResult by remember { mutableStateOf("") }
    var geometryError by remember { mutableStateOf<String?>(null) }

    PageColumn("Инструменты") {
        Segmented(listOf("Дроби", "Проценты", "Геометрия"), tool) { tool = it }
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
                                        .onFailure { fractionError = friendly(it, "Проверьте дроби"); fractionResult = "" }
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
                    ActionButton("Вычислить") {
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
                            .onFailure { percentError = friendly(it, "Проверьте значения"); percentResult = "" }
                    }
                    if (percentResult.isNotEmpty() || percentError != null) ResultBox(percentResult, percentError)
                }
                else -> {
                    Segmented(listOf("Прямоугольник", "Круг", "Треугольник"), shape) { shape = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(sideA, { sideA = it }, if (shape == "Круг") "Радиус" else "Сторона a", Modifier.weight(1f))
                        if (shape != "Круг") Field(sideB, { sideB = it }, "Сторона b", Modifier.weight(1f))
                        if (shape == "Треугольник") Field(sideC, { sideC = it }, "Сторона c", Modifier.weight(1f))
                    }
                    ActionButton("Рассчитать") {
                        runCatching {
                            when (shape) {
                                "Прямоугольник" -> GeometryTools.rectangle(sideA, sideB)
                                "Круг" -> GeometryTools.circle(sideA)
                                else -> GeometryTools.triangle(sideA, sideB, sideC)
                            }
                        }.onSuccess { geometryResult = it; geometryError = null }
                            .onFailure { geometryError = friendly(it, "Проверьте размеры"); geometryResult = "" }
                    }
                    if (geometryResult.isNotEmpty() || geometryError != null) ResultBox(geometryResult, geometryError)
                }
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
    var equation by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<String?>(null) }
    var steps by remember { mutableStateOf(emptyList<String>()) }
    var error by remember { mutableStateOf<String?>(null) }

    PageColumn("Решатель") {
        Panel {
            Field(equation, { equation = it }, "Например: x² − 5x + 6 = 0", Modifier.fillMaxWidth(), numeric = false)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("x", "x²", "(", ")", "+", "−", "×", "÷", "=").forEach { symbol ->
                    Chip(symbol, false) { equation += symbol }
                }
            }
            ActionButton("Решить") {
                try {
                    val solved = AlgebraSolver().solve(equation)
                    answer = solved.answer
                    steps = solved.steps
                    error = null
                } catch (e: IllegalArgumentException) {
                    answer = null
                    steps = emptyList()
                    error = friendly(e, "Проверьте запись уравнения")
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
            "Линейные и квадратные уравнения с одной переменной x. Деление на выражение с x и степени выше второй не поддерживаются.",
            color = p.muted, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

// ---------------------------------------------------------------- history

@Composable
private fun HistoryPage(entries: List<HistoryEntry>, onClear: () -> Unit) {
    val p = LocalPalette.current
    PageColumn("История") {
        if (entries.isEmpty()) {
            Text("Здесь появятся результаты основного калькулятора.", color = p.muted, fontSize = 14.sp)
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
                                entry.dateLabel + if (entry.steps.isNotEmpty()) (if (expanded) "  ·  скрыть решение" else "  ·  показать решение") else "",
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
                Text("Очистить историю", color = p.accent)
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

@Composable
private fun SettingsPage(
    darkMode: Boolean,
    fontScale: Float,
    fontPath: String?,
    onTheme: (Boolean) -> Unit,
    onFontScale: (Float) -> Unit,
    onFontSelected: (String) -> Unit,
    onResetFont: () -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var fontMessage by remember { mutableStateOf<String?>(null) }
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
                require(extension == "ttf" || extension == "otf") { "Выберите файл TTF или OTF" }
                val fontDir = File(context.filesDir, "fonts").apply { mkdirs() }
                val target = File(fontDir, "mathflow_custom_${System.currentTimeMillis()}.$extension")
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Не удалось открыть файл шрифта" }
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                require(target.length() > 0L) { "Файл шрифта пустой" }
                // Make sure Android can actually parse the file before applying it.
                android.graphics.Typeface.createFromFile(target)
                fontDir.listFiles()?.filter { it != target }?.forEach { it.delete() }
                onFontSelected(target.absolutePath)
                fontMessage = "Шрифт применён: $displayName"
            }.onFailure {
                fontMessage = it.message ?: "Не удалось установить шрифт"
            }
        }
    }

    PageColumn("Настройки") {
        Panel {
            Text("Тема", fontSize = 15.sp, color = p.muted)
            Segmented(listOf("Тёмная", "Светлая"), if (darkMode) "Тёмная" else "Светлая") { onTheme(it == "Тёмная") }
        }
        Panel {
            Text("Шрифт", fontSize = 15.sp, color = p.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    ActionButton("Выбрать TTF / OTF") {
                        fontPicker.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "application/octet-stream"))
                    }
                }
                if (fontPath != null) {
                    Surface(
                        onClick = { onResetFont(); fontMessage = "Возвращён стандартный шрифт" },
                        modifier = Modifier.height(50.dp), shape = RoundedCornerShape(25.dp), color = p.key
                    ) {
                        Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                            Text("Сброс", fontSize = 15.sp, color = p.text)
                        }
                    }
                }
            }
            fontMessage?.let { Text(it, fontSize = 13.sp, color = p.accent) }
        }
        Panel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Размер текста", fontSize = 15.sp, color = p.muted)
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
            Text("Версия $version", fontSize = 14.sp, color = p.accent)
            Text(
                "Научный калькулятор, дроби, проценты, геометрия и решатель уравнений. Всё считается на устройстве, без интернета.",
                fontSize = 13.sp, color = p.muted, lineHeight = 18.sp
            )
        }
    }
}
