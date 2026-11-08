package com.mathflow.app

import androidx.compose.foundation.horizontalScroll
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
            u("mm","Миллиметры (мм)","0.001"), u("cm","Сантиметры (см)","0.01"),
            u("m","Метры (м)"), u("km","Километры (км)","1000"),
            u("in","Дюймы (in)","0.0254"), u("ft","Футы (ft)","0.3048"),
            u("yd","Ярды (yd)","0.9144"), u("mi","Мили (mi)","1609.344")
        )),
        UnitCategory("mass", "Масса", listOf(
            u("mg","Миллиграммы (мг)","0.000001"), u("g","Граммы (г)","0.001"),
            u("kg","Килограммы (кг)"), u("t","Тонны (т)","1000"),
            u("oz","Унции (oz)","0.028349523125"), u("lb","Фунты (lb)","0.45359237")
        )),
        UnitCategory("area", "Площадь", listOf(
            u("mm2","мм²","0.000001"), u("cm2","см²","0.0001"), u("m2","м²"),
            u("ha","Гектары (га)","10000"), u("km2","км²","1000000"), u("acre","Акры","4046.8564224")
        )),
        UnitCategory("volume", "Объём", listOf(
            u("ml","Миллилитры (мл)","0.001"), u("l","Литры (л)"),
            u("m3","Кубические метры (м³)","1000"), u("tsp","Чайные ложки (US)","0.00492892159375"),
            u("cup","Чашки (US)","0.2365882365"), u("gal","Галлоны (US)","3.785411784")
        )),
        UnitCategory("temperature", "Температура", listOf(
            u("c","Цельсий (°C)"), u("f","Фаренгейт (°F)"), u("k","Кельвин (K)")
        )),
        UnitCategory("speed", "Скорость", listOf(
            u("ms","Метры в секунду (м/с)"),
            u("kmh","Километры в час (км/ч)","0.2777777777777777777777777777777778"),
            u("mph","Мили в час (mph)","0.44704"),
            u("kn","Узлы (kn)","0.5144444444444444444444444444444444")
        )),
        UnitCategory("time", "Время", listOf(
            u("ms","Миллисекунды (мс)","0.001"), u("s","Секунды (с)"),
            u("min","Минуты (мин)","60"), u("h","Часы (ч)","3600"), u("day","Дни","86400")
        )),
        UnitCategory("pressure", "Давление", listOf(
            u("pa","Паскали (Па)"), u("kpa","Килопаскали (кПа)","1000"),
            u("mpa","Мегапаскали (МПа)","1000000"), u("bar","Бары","100000"),
            u("atm","Атмосферы (атм)","101325"), u("mmhg","мм рт. ст.","133.322387415")
        )),
        UnitCategory("energy", "Энергия", listOf(
            u("j","Джоули (Дж)"), u("kj","Килоджоули (кДж)","1000"),
            u("cal","Калории (cal)","4.184"), u("kcal","Килокалории (ккал)","4184"),
            u("wh","Ватт-часы (Вт·ч)","3600"), u("kwh","Киловатт-часы (кВт·ч)","3600000")
        )),
        UnitCategory("data", "Данные", listOf(
            u("b","Байты (Б)"), u("kb","Килобайты (кБ)","1000"),
            u("mb","Мегабайты (МБ)","1000000"), u("gb","Гигабайты (ГБ)","1000000000"),
            u("tb","Терабайты (ТБ)","1000000000000"), u("kib","Кибибайты (КиБ)","1024"),
            u("mib","Мебибайты (МиБ)","1048576"), u("gib","Гибибайты (ГиБ)","1073741824")
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

@Composable
internal fun UnitConverterContent() {
    val categories = UnitConverter.categories
    var categoryId by rememberSaveable { mutableStateOf("length") }
    var input by rememberSaveable { mutableStateOf("1") }
    var fromId by rememberSaveable { mutableStateOf("km") }
    var toId by rememberSaveable { mutableStateOf("m") }

    val category = categories.first { it.id == categoryId }
    val from = category.units.firstOrNull { it.id == fromId } ?: category.units.first()
    val to = category.units.firstOrNull { it.id == toId } ?: category.units.getOrElse(1) { category.units.first() }
    val result = remember(input, categoryId, from.id, to.id) {
        if (input.isBlank()) null else runCatching { UnitConverter.convert(input, categoryId, from.id, to.id) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Конвертер величин", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            categories.forEach { item ->
                FilterChip(
                    selected = item.id == categoryId,
                    onClick = {
                        categoryId = item.id
                        fromId = item.units.first().id
                        toId = item.units.getOrElse(1) { item.units.first() }.id
                    },
                    label = { Text(item.title, maxLines = 1) }
                )
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Значение") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            UnitSelector("Из", from, category.units, Modifier.weight(1f)) { fromId = it.id }
            IconButton(onClick = { val previous = from.id; fromId = to.id; toId = previous }) {
                Icon(Icons.Default.SwapVert, contentDescription = "Поменять единицы местами")
            }
            UnitSelector("В", to, category.units, Modifier.weight(1f)) { toId = it.id }
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("РЕЗУЛЬТАТ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (result == null) {
                    Text("Введите значение", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val value = result.getOrNull()
                    if (value != null) {
                        Text(value, fontSize = 29.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${from.label} → ${to.label}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(result.exceptionOrNull()?.message ?: "Не удалось выполнить конвертацию", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            }
        }
        Text("Пересчёт выполняется автоматически. Интернет не требуется.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun UnitSelector(
    label: String,
    selected: UnitDefinition,
    units: List<UnitDefinition>,
    modifier: Modifier = Modifier,
    onSelect: (UnitDefinition) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text(selected.label.substringBefore(" ("), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                units.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.label) },
                        onClick = { onSelect(item); expanded = false }
                    )
                }
            }
        }
    }
}
