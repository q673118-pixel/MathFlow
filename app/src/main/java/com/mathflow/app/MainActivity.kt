package com.mathflow.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MathFlowApp() }
    }
}

private enum class Page(val title: String) {
    HOME("Главная"), TOOLS("Инструменты"), SOLVER("Решатель"), HISTORY("История"), SETTINGS("Настройки")
}

@Composable
private fun MathFlowApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mathflow_settings", Context.MODE_PRIVATE) }
    var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
    var fontPath by remember { mutableStateOf(prefs.getString("font_path", null)) }
    var page by remember { mutableStateOf(Page.HOME) }
    var history by remember { mutableStateOf(HistoryStore.load(context)) }

    val customFont = remember(fontPath) {
        fontPath?.let { path ->
            runCatching {
                val file = File(path)
                if (file.exists() && file.length() > 0L) FontFamily(Font(file, FontWeight.Normal)) else null
            }.getOrNull()
        }
    }
    val baseDensity = LocalDensity.current
    val adjustedDensity = remember(baseDensity, fontScale) {
        Density(baseDensity.density, baseDensity.fontScale * fontScale)
    }

    val light = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF6750A4),
        primaryContainer = androidx.compose.ui.graphics.Color(0xFFEADDFF),
        onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF21005D),
        background = androidx.compose.ui.graphics.Color(0xFFF7F2FA),
        surface = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7E0EC)
    )
    val dark = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
        primaryContainer = androidx.compose.ui.graphics.Color(0xFF4F378B),
        onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFEADDFF),
        background = androidx.compose.ui.graphics.Color(0xFF111016),
        surface = androidx.compose.ui.graphics.Color(0xFF19161F),
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFF302B38)
    )

    CompositionLocalProvider(
        LocalDensity provides adjustedDensity,
        LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = customFont ?: FontFamily.Default)
    ) {
        MaterialTheme(colorScheme = if (darkMode) dark else light) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar {
                        listOf(Page.HOME, Page.TOOLS, Page.SOLVER, Page.HISTORY, Page.SETTINGS).forEach { destination ->
                            NavigationBarItem(
                                selected = page == destination,
                                onClick = { page = destination },
                                icon = {
                                    Icon(
                                        when (destination) {
                                            Page.HOME -> Icons.Default.Calculate
                                            Page.TOOLS, Page.SOLVER -> Icons.Default.Functions
                                            Page.HISTORY -> Icons.Default.History
                                            Page.SETTINGS -> Icons.Default.Settings
                                        },
                                        contentDescription = destination.title
                                    )
                                },
                                label = { Text(destination.title, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            ) { insets ->
                Column(Modifier.fillMaxSize().padding(insets).padding(horizontal = 18.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MathFlow", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                            Text("Математика без ограничений", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            onClick = {
                                darkMode = !darkMode
                                prefs.edit().putBoolean("dark_mode", darkMode).apply()
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (darkMode) "☀" else "☾", fontSize = 22.sp)
                            }
                        }
                    }
                    AnimatedContent(
                        targetState = page,
                        transitionSpec = { androidx.compose.animation.fadeIn(spring()) togetherWith androidx.compose.animation.fadeOut(spring()) },
                        label = "page"
                    ) { selected ->
                        when (selected) {
                            Page.HOME -> CalculatorHome(
                                onCalculationSaved = { expression, result, steps ->
                                    history = HistoryStore.add(context, expression, result, steps)
                                }
                            )
                            Page.TOOLS -> ToolsPage()
                            Page.SOLVER -> EquationSolverPage()
                            Page.HISTORY -> HistoryPage(
                                entries = history,
                                onClear = {
                                    HistoryStore.clear(context)
                                    history = emptyList()
                                }
                            )
                            Page.SETTINGS -> SettingsPage(
                                darkMode = darkMode,
                                fontScale = fontScale,
                                fontPath = fontPath,
                                onThemeChange = {
                                    darkMode = !darkMode
                                    prefs.edit().putBoolean("dark_mode", darkMode).apply()
                                },
                                onFontScaleChange = {
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
        }
    }
}

@Composable
private fun CalculatorHome(onCalculationSaved: (String, String, List<String>) -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var explanation by remember { mutableStateOf("Введите математическое выражение") }
    var calculationSteps by remember { mutableStateOf(emptyList<String>()) }
    val keys = listOf(
        "sin", "cos", "tan", "ln",
        "log", "abs", "e", "AC",
        "(", ")", "⌫", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "−",
        "1", "2", "3", "+",
        "%", "0", ".", "=",
        "√", "x²", "π"
    )
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.End) {
                Text("ВЫРАЖЕНИЕ", Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(expression.ifBlank { "0" }, Modifier.fillMaxWidth().padding(top = 12.dp), fontSize = 27.sp, maxLines = 2)
                Text(result, Modifier.fillMaxWidth().padding(top = 7.dp), fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text("Решение", Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(explanation, Modifier.fillMaxWidth().padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                if (calculationSteps.isNotEmpty()) {
                    calculationSteps.forEachIndexed { index, step ->
                        Text(
                            "${index + 1}. $step",
                            Modifier.fillMaxWidth().padding(top = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Клавиатура", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("НАУЧНЫЙ РЕЖИМ", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        keys.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { key ->
                    val operator = key in listOf("÷", "×", "−", "+", "=")
                    val utility = key in listOf("AC", "⌫", "√", "x²", "π", "%", "(", ")", "sin", "cos", "tan", "ln", "log", "abs", "e")
                    val background = when {
                        key == "=" -> MaterialTheme.colorScheme.primary
                        operator -> MaterialTheme.colorScheme.primaryContainer
                        utility -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val foreground = if (key == "=") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    Surface(
                        modifier = Modifier.weight(1f).height(52.dp).clickable {
                            when (key) {
                                "AC" -> {
                                    expression = ""
                                    result = "0"
                                    explanation = "Введите математическое выражение"
                                    calculationSteps = emptyList()
                                }
                                "⌫" -> expression = expression.dropLast(1)
                                "=" -> {
                                    try {
                                        val calculation = ExpressionEngine().calculate(expression)
                                        result = calculation.displayValue
                                        calculationSteps = calculation.steps
                                        onCalculationSaved(expression, calculation.displayValue, calculation.steps)
                                        explanation = if (calculation.steps.isEmpty())
                                            "$expression = ${calculation.displayValue}"
                                        else "Промежуточные вычисления:"
                                    } catch (error: IllegalArgumentException) {
                                        result = "Ошибка"
                                        explanation = error.message ?: "Проверьте выражение"
                                        calculationSteps = emptyList()
                                    } catch (_: Exception) {
                                        result = "Ошибка"
                                        explanation = "Не удалось вычислить выражение"
                                        calculationSteps = emptyList()
                                    }
                                }
                                "√" -> expression += "sqrt("
                                "x²" -> expression += "^2"
                                "π" -> expression += "pi"
                                " " -> Unit
                                "e" -> expression += "e"
                                "sin", "cos", "tan", "ln", "log", "abs" -> expression += "$key("
                                else -> expression += key
                            }
                        },
                        shape = RoundedCornerShape(19.dp),
                        color = background
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(key, fontSize = if (key.length > 2) 13.sp else 19.sp, fontWeight = FontWeight.Medium, color = foreground)
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun EquationSolverPage() {
    var equation by remember { mutableStateOf("2x + 3 = 11") }
    var answer by remember { mutableStateOf("Введите уравнение и нажмите «Решить»") }
    var steps by remember { mutableStateOf(emptyList<String>()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Решатель уравнений", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(
            "Линейные и квадратные уравнения с одной переменной x",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        OutlinedTextField(
            value = equation,
            onValueChange = { equation = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Уравнение") },
            placeholder = { Text("Например: x² − 5x + 6 = 0") },
            singleLine = true
        )
        Button(
            onClick = {
                try {
                    val solved = AlgebraSolver().solve(equation)
                    answer = solved.answer
                    steps = solved.steps
                    error = null
                } catch (e: IllegalArgumentException) {
                    answer = "Не удалось решить"
                    steps = emptyList()
                    error = e.message ?: "Проверьте запись"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Решить уравнение")
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Ответ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(answer, color = MaterialTheme.colorScheme.primary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                if (steps.isNotEmpty()) {
                    HorizontalDivider(Modifier.padding(vertical = 5.dp))
                    Text("Пошаговое решение", fontWeight = FontWeight.SemiBold)
                    steps.forEachIndexed { index, step ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("${index + 1}.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(step, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
        Text(
            "Поддерживаются x, числа, скобки, +, −, ×, ÷ и степени до x². Деление на выражение с x и уравнения выше второй степени пока не поддерживаются.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ToolsPage() {
    var fractionA by remember { mutableStateOf("3/4") }
    var fractionB by remember { mutableStateOf("2/5") }
    var fractionResult by remember { mutableStateOf("Выберите операцию") }
    var fractionError by remember { mutableStateOf<String?>(null) }

    var percentMode by remember { mutableStateOf("Процент от числа") }
    var percentValue by remember { mutableStateOf("15") }
    var percentBase by remember { mutableStateOf("240") }
    var percentResult by remember { mutableStateOf("Результат появится здесь") }
    var percentError by remember { mutableStateOf<String?>(null) }

    var geometryMode by remember { mutableStateOf("Прямоугольник") }
    var sideA by remember { mutableStateOf("5") }
    var sideB by remember { mutableStateOf("3") }
    var sideC by remember { mutableStateOf("4") }
    var geometryResult by remember { mutableStateOf("Введите размеры фигуры") }
    var geometryError by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Математические инструменты", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(
            "Отдельные калькуляторы для дробей, процентов и геометрии.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Обыкновенные дроби", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Вводите дроби как 3/4 или десятичные числа.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = fractionA, onValueChange = { fractionA = it }, label = { Text("Первая дробь") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fractionB, onValueChange = { fractionB = it }, label = { Text("Вторая дробь") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("+" , "−", "×", "÷").forEach { op ->
                        FilledTonalButton(
                            onClick = {
                                runCatching {
                                    fractionResult = FractionTools.calculate(fractionA, fractionB, op)
                                    fractionError = null
                                }.onFailure {
                                    fractionError = it.message ?: "Проверьте дроби"
                                    fractionResult = "Ошибка"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) { Text(op, fontSize = 18.sp) }
                    }
                }
                ToolResult(fractionResult)
                fractionError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        }

        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Проценты", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Три режима: найти процент, долю от целого или изменить число на процент.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Процент от числа", "Какой процент", "Изменить число").forEach { mode ->
                        if (percentMode == mode) {
                            Button(onClick = { percentMode = mode }, modifier = Modifier.fillMaxWidth()) {
                                Text(mode)
                            }
                        } else {
                            OutlinedButton(onClick = { percentMode = mode }, modifier = Modifier.fillMaxWidth()) {
                                Text(mode)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = percentValue,
                    onValueChange = { percentValue = it },
                    label = {
                        Text(
                            when (percentMode) {
                                "Какой процент" -> "Часть"
                                "Изменить число" -> "Исходное число"
                                else -> "Процент (%)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = percentBase,
                    onValueChange = { percentBase = it },
                    label = {
                        Text(
                            when (percentMode) {
                                "Какой процент" -> "Целое"
                                "Изменить число" -> "Процент изменения (%)"
                                else -> "Число"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        runCatching {
                            percentResult = when (percentMode) {
                                "Какой процент" -> PercentTools.whatPercent(percentValue, percentBase)
                                "Изменить число" -> {
                                    val rate = percentBase.trim().replace(',', '.').toBigDecimal()
                                    when {
                                        rate.signum() < 0 -> PercentTools.changeByPercent(percentValue, rate.abs().toPlainString(), false)
                                        else -> PercentTools.changeByPercent(percentValue, percentBase, true)
                                    }
                                }
                                else -> PercentTools.percentOf(percentValue, percentBase)
                            }
                            percentError = null
                        }.onFailure {
                            percentError = it.message ?: "Проверьте значения"
                            percentResult = "Ошибка"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Вычислить") }
                ToolResult(percentResult)
                percentError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        }

        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Геометрия", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Прямоугольник", "Круг", "Треугольник").forEach { mode ->
                        val selected = geometryMode == mode
                        if (selected) {
                            Button(onClick = { geometryMode = mode }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
                                Text(mode, fontSize = 10.sp, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(onClick = { geometryMode = mode }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)) {
                                Text(mode, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
                OutlinedTextField(value = sideA, onValueChange = { sideA = it }, label = { Text(if (geometryMode == "Круг") "Радиус" else "Сторона a") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (geometryMode != "Круг") {
                    OutlinedTextField(value = sideB, onValueChange = { sideB = it }, label = { Text(if (geometryMode == "Прямоугольник") "Сторона b" else "Сторона b") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                if (geometryMode == "Треугольник") {
                    OutlinedTextField(value = sideC, onValueChange = { sideC = it }, label = { Text("Сторона c") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                Button(
                    onClick = {
                        runCatching {
                            geometryResult = when (geometryMode) {
                                "Прямоугольник" -> GeometryTools.rectangle(sideA, sideB)
                                "Круг" -> GeometryTools.circle(sideA)
                                else -> GeometryTools.triangle(sideA, sideB, sideC)
                            }
                            geometryError = null
                        }.onFailure {
                            geometryError = it.message ?: "Проверьте размеры"
                            geometryResult = "Ошибка"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Рассчитать") }
                ToolResult(geometryResult)
                geometryError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                Text("Единицы измерения результата: квадратные и линейные единицы исходных размеров.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ToolResult(value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            value,
            Modifier.padding(13.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun HistoryPage(
    entries: List<HistoryEntry>,
    onClear: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("История вычислений", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear, enabled = entries.isNotEmpty()) { Text("Очистить") }
        }
        if (entries.isEmpty()) {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("История пока пуста", fontWeight = FontWeight.SemiBold)
                    Text("Результаты обычного калькулятора будут автоматически сохраняться здесь.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            entries.forEach { entry ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(entry.expression, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text("= ${entry.result}", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(entry.dateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        if (entry.steps.isNotEmpty()) {
                            var expanded by remember(entry.id) { mutableStateOf(false) }
                            TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                                Text(if (expanded) "Скрыть решение" else "Показать решение")
                            }
                            if (expanded) {
                                entry.steps.forEachIndexed { index, step ->
                                    Text("${index + 1}. $step", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            Text("Хранятся последние 100 вычислений.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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
                val steps = (0 until stepsJson.length()).map { stepsJson.optString(it) }
                HistoryEntry(
                    id = item.optLong("id"),
                    expression = item.optString("expression"),
                    result = item.optString("result"),
                    timestamp = item.optLong("timestamp"),
                    steps = steps
                )
            }.sortedByDescending { it.timestamp }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, expression: String, result: String, steps: List<String>): List<HistoryEntry> {
        val entries = (
            listOf(HistoryEntry(System.currentTimeMillis(), expression, result, System.currentTimeMillis(), steps)) +
                load(context)
            ).take(LIMIT)
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

@Composable
private fun SettingsPage(
    darkMode: Boolean,
    fontScale: Float,
    fontPath: String?,
    onThemeChange: () -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onFontSelected: (String) -> Unit,
    onResetFont: () -> Unit
) {
    val context = LocalContext.current
    var fontMessage by remember { mutableStateOf<String?>(null) }
    val fontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (column >= 0 && cursor.moveToFirst()) cursor.getString(column) else null
                } ?: "custom_font.ttf"
                val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                require(extension == "ttf" || extension == "otf") { "Выберите файл TTF или OTF" }
                val fontDir = File(context.filesDir, "fonts").apply { mkdirs() }
                val target = File(fontDir, "mathflow_custom.$extension")
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Не удалось открыть файл шрифта" }
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                require(target.length() > 0L) { "Файл шрифта пустой" }
                // Validate that Android can parse the selected font before applying it.
                android.graphics.Typeface.createFromFile(target)
                onFontSelected(target.absolutePath)
                fontMessage = "Шрифт установлен: $displayName"
            }.onFailure {
                fontMessage = it.message ?: "Не удалось установить шрифт"
            }
        }
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Text("Настройки", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onThemeChange).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Тёмная тема", fontWeight = FontWeight.SemiBold)
                    Text(if (darkMode) "Включена" else "Выключена", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Switch(checked = darkMode, onCheckedChange = { onThemeChange() })
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Шрифт приложения", fontWeight = FontWeight.SemiBold)
                Text(
                    fontPath?.let { "Пользовательский шрифт установлен" } ?: "Используется системный шрифт",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { fontPicker.launch(arrayOf("font/ttf", "font/otf", "application/octet-stream")) }) {
                        Text("Выбрать TTF / OTF")
                    }
                    if (fontPath != null) {
                        OutlinedButton(onClick = {
                            onResetFont()
                            fontMessage = "Возвращён системный шрифт"
                        }) { Text("Сбросить") }
                    }
                }
                fontMessage?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) }
                Text("Файл копируется во внутреннюю память приложения. Доступ к хранилищу целиком не запрашивается.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Размер текста", fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        fontScale < 0.95f -> "Маленький"
                        fontScale > 1.05f -> "Большой"
                        else -> "Стандартный"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Slider(
                    value = fontScale,
                    onValueChange = onFontScaleChange,
                    valueRange = 0.85f..1.20f,
                    steps = 6
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Меньше", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Больше", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("О приложении", fontWeight = FontWeight.SemiBold)
                Text("MathFlow 0.5.0", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Настройки сохраняются после закрытия приложения. История ограничена последними 100 вычислениями.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}
