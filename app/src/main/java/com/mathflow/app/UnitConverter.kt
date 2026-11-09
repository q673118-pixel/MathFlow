package com.mathflow.app

import androidx.compose.ui.draw.clip

import androidx.compose.animation.core.FastOutSlowInEasing

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal data class UnitDefinition(val id: String, val label: String, val factorToBase: BigDecimal = BigDecimal.ONE)
internal data class UnitCategory(val id: String, val title: String, val units: List<UnitDefinition>)

/** Offline unit conversion using BigDecimal; temperature is normalized to Celsius. */
internal object UnitConverter {
    private val mc = MathContext(34, RoundingMode.HALF_EVEN)
    private val five = BigDecimal("5")
    private val nine = BigDecimal("9")
    private val offset = BigDecimal("273.15")

    private fun u(id: String, label: String, factor: String = "1") =
        UnitDefinition(id, label, BigDecimal(factor))

    val categories = listOf(
        UnitCategory("length", "Длина", listOf(
            u("mm","mm","0.001"), u("cm","cm","0.01"),
            u("m","m"), u("km","km","1000"),
            u("in","in","0.0254"), u("ft","ft","0.3048"),
            u("yd","yd","0.9144"), u("mi","mi","1609.344")
        )),
        UnitCategory("mass", "Масса", listOf(
            u("mg","mg","0.000001"), u("g","g","0.001"),
            u("kg","kg"), u("t","t","1000"),
            u("oz","oz","0.028349523125"), u("lb","lb","0.45359237")
        )),
        UnitCategory("area", "Площадь", listOf(
            u("mm2","mm²","0.000001"), u("cm2","cm²","0.0001"), u("m2","m²"),
            u("ha","ha","10000"), u("km2","km²","1000000"), u("acre","ac","4046.8564224")
        )),
        UnitCategory("volume", "Объём", listOf(
            u("ml","mL","0.001"), u("l","L"),
            u("m3","m³","1000"), u("tsp","tsp","0.00492892159375"),
            u("cup","cup","0.2365882365"), u("gal","gal","3.785411784")
        )),
        UnitCategory("temperature", "Температура", listOf(
            u("c","°C"), u("f","°F"), u("k","K")
        )),
        UnitCategory("speed", "Скорость", listOf(
            u("ms","m/s"),
            u("kmh","km/h","0.2777777777777777777777777777777778"),
            u("mph","mph","0.44704"),
            u("kn","kn","0.5144444444444444444444444444444444")
        )),
        UnitCategory("time", "Время", listOf(
            u("ms","ms","0.001"), u("s","s"),
            u("min","min","60"), u("h","h","3600"), u("day","d","86400")
        )),
        UnitCategory("pressure", "Давление", listOf(
            u("pa","Pa"), u("kpa","kPa","1000"),
            u("mpa","MPa","1000000"), u("bar","bar","100000"),
            u("atm","atm","101325"), u("mmhg","mmHg","133.322387415")
        )),
        UnitCategory("energy", "Энергия", listOf(
            u("j","J"), u("kj","kJ","1000"),
            u("cal","cal","4.184"), u("kcal","kcal","4184"),
            u("wh","Wh","3600"), u("kwh","kWh","3600000")
        )),
        UnitCategory("data", "Данные", listOf(
            u("b","B"), u("kb","kB","1000"),
            u("mb","MB","1000000"), u("gb","GB","1000000000"),
            u("tb","TB","1000000000000"), u("kib","KiB","1024"),
            u("mib","MiB","1048576"), u("gib","GiB","1073741824")
        ))
    )

    fun convert(input: String, categoryId: String, fromUnitId: String, toUnitId: String): String {
        val value = input.trim().replace(',', '.').toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Введите корректное число")
        val category = categories.firstOrNull { it.id == categoryId }
            ?: throw IllegalArgumentException("Неизвестная категория")
        val from = category.units.firstOrNull { it.id == fromUnitId }
            ?: throw IllegalArgumentException("Неизвестная исходная единица")
        val to = category.units.firstOrNull { it.id == toUnitId }
            ?: throw IllegalArgumentException("Неизвестная целевая единица")

        val base = if (categoryId == "temperature") toCelsius(value, from.id)
            else value.multiply(from.factorToBase, mc)
        require(categoryId != "temperature" || base >= offset.negate()) {
            "Температура ниже абсолютного нуля"
        }
        val converted = if (categoryId == "temperature") fromCelsius(base, to.id)
            else base.divide(to.factorToBase, mc)
        val result = converted.round(mc).stripTrailingZeros()
        return if (result.compareTo(BigDecimal.ZERO) == 0) "0" else result.toPlainString()
    }

    private fun toCelsius(value: BigDecimal, unit: String): BigDecimal = when (unit) {
        "c" -> value
        "f" -> value.subtract(BigDecimal("32"), mc).multiply(five, mc).divide(nine, mc)
        "k" -> value.subtract(offset, mc)
        else -> throw IllegalArgumentException("Неизвестная единица температуры")
    }

    private fun fromCelsius(value: BigDecimal, unit: String): BigDecimal = when (unit) {
        "c" -> value
        "f" -> value.multiply(nine, mc).divide(five, mc).add(BigDecimal("32"), mc)
        "k" -> value.add(offset, mc)
        else -> throw IllegalArgumentException("Неизвестная единица температуры")
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun UnitConverterContent() {
    val p = LocalPalette.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val categories = UnitConverter.categories
    var categoryId by rememberSaveable { mutableStateOf("length") }
    var input by rememberSaveable { mutableStateOf("1") }
    var fromId by rememberSaveable { mutableStateOf("km") }
    var toId by rememberSaveable { mutableStateOf("m") }
    var selectorSide by remember { mutableStateOf<String?>(null) }

    val category = categories.first { it.id == categoryId }
    val from = category.units.firstOrNull { it.id == fromId } ?: category.units.first()
    val to = category.units.firstOrNull { it.id == toId } ?: category.units.getOrElse(1) { category.units.first() }
    val result = remember(input, categoryId, from.id, to.id) {
        if (input.isBlank()) null else runCatching { UnitConverter.convert(input, categoryId, from.id, to.id) }
    }
    val converted = result?.getOrNull()
    val fieldShape = RoundedCornerShape(19.dp)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            tx("converter_title"),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.text
        )

        // Scrollable category rail. Selection colors come from the active MathFlow palette.
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { item ->
                val selected = item.id == categoryId
                val chipColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (selected) p.keyEquals else p.key,
                    animationSpec = androidx.compose.animation.core.tween(220, easing = FastOutSlowInEasing),
                    label = "ConverterCategoryColor"
                )
                val textColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (selected) p.onKeyEquals else p.muted,
                    animationSpec = androidx.compose.animation.core.tween(220, easing = FastOutSlowInEasing),
                    label = "ConverterCategoryText"
                )
                Surface(
                    onClick = {
                        if (!selected) {
                            categoryId = item.id
                            fromId = item.units.first().id
                            toId = item.units.getOrElse(1) { item.units.first() }.id
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = chipColor,
                    contentColor = textColor
                ) {
                    Text(
                        displayText(item.title),
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(tx("value"), fontSize = 12.sp, color = p.muted, fontWeight = FontWeight.Medium)
            Row(
                Modifier.fillMaxWidth()
                    .clip(fieldShape)
                    .background(p.key)
                    .then(
                        if (input.isNotEmpty()) Modifier.border(1.dp, p.accent.copy(alpha = 0.58f), fieldShape)
                        else Modifier.border(1.dp, p.muted.copy(alpha = 0.22f), fieldShape)
                    )
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = p.text,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(p.accent),
                    decorationBox = { inner ->
                        Box {
                            if (input.isEmpty()) {
                                Text("0", color = p.muted.copy(alpha = 0.55f), fontSize = 23.sp)
                            }
                            inner()
                        }
                    }
                )
                if (input.isNotEmpty()) {
                    Text(
                        from.label,
                        color = p.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            ConverterUnitButton(
                label = tx("from"),
                unit = from,
                modifier = Modifier.weight(1f),
                onClick = { selectorSide = "from" }
            )
            Surface(
                onClick = {
                    val previous = from.id
                    fromId = to.id
                    toId = previous
                },
                shape = CircleShape,
                color = p.key,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = tx("swap_units"),
                        tint = p.accent,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
            ConverterUnitButton(
                label = tx("to"),
                unit = to,
                modifier = Modifier.weight(1f),
                onClick = { selectorSide = "to" }
            )
        }

        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = p.key
        ) {
            Column(
                Modifier.padding(horizontal = 17.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        tx("result").uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = p.muted,
                        modifier = Modifier.weight(1f)
                    )
                    if (converted != null) {
                        Surface(
                            onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(converted)) },
                            shape = RoundedCornerShape(12.dp),
                            color = p.surface
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.ContentCopy,
                                    contentDescription = tx("copy"),
                                    tint = p.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(tx("copy"), color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                when {
                    input.isBlank() -> Text(tx("enter_value"), color = p.muted, fontSize = 17.sp)
                    converted != null -> {
                        Text(
                            converted,
                            fontSize = 29.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.accent,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${from.label}  →  ${to.label}",
                            fontSize = 12.sp,
                            color = p.muted
                        )
                    }
                    else -> Text(
                        tx("conversion_error"),
                        color = p.error,
                        fontSize = 14.sp
                    )
                }
            }
        }
        Text(
            tx("offline_conversion"),
            color = p.muted,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }

    selectorSide?.let { side ->
        val selectedUnit = if (side == "from") from else to
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectorSide = null }) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = p.surface,
                contentColor = p.text,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (side == "from") tx("from") else tx("to"),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.text
                    )
                    Text(
                        displayText(category.title),
                        fontSize = 12.sp,
                        color = p.muted
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(category.units.size) { index ->
                            val unit = category.units[index]
                            val active = unit.id == selectedUnit.id
                            Surface(
                                onClick = {
                                    if (side == "from") fromId = unit.id else toId = unit.id
                                    selectorSide = null
                                },
                                shape = RoundedCornerShape(15.dp),
                                color = if (active) p.navActive else p.key
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        unit.label,
                                        modifier = Modifier.weight(1f),
                                        color = if (active) p.accent else p.text,
                                        fontSize = 15.sp,
                                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                    if (active) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = p.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { selectorSide = null }, modifier = Modifier.align(Alignment.End)) {
                        Text(tx("cancel"), color = p.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConverterUnitButton(
    label: String,
    unit: UnitDefinition,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        color = p.key
    ) {
        Column(
            Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(label, color = p.muted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    unit.label,
                    modifier = Modifier.weight(1f),
                    color = p.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
