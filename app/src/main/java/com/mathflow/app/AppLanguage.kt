package com.mathflow.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

internal data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String
)

internal val AppLanguages = listOf(
    AppLanguage("ru", "Русский", "Russian"),
    AppLanguage("en", "English", "English"),
    AppLanguage("kk", "Қазақша", "Kazakh")
)

internal val LocalLanguage = staticCompositionLocalOf { "ru" }

private val uiText = mapOf(
    "tools" to listOf("Инструменты", "Tools", "Құралдар"),
    "solver" to listOf("Решатель", "Equation solver", "Теңдеу шешуші"),
    "history" to listOf("История", "History", "Тарих"),
    "settings" to listOf("Настройки", "Settings", "Баптаулар"),
    "app_icons" to listOf("Иконки приложения", "App icons", "Қолданба белгішелері"),
    "app_language" to listOf("Язык приложения", "App language", "Қолданба тілі"),
    "themes" to listOf("Темы оформления", "Themes", "Тақырыптар"),
    "history_empty" to listOf("Здесь появятся результаты основного калькулятора.", "Your calculator results will appear here.", "Мұнда калькулятор нәтижелері көрсетіледі."),
    "clear_history" to listOf("Очистить историю", "Clear history", "Тарихты тазарту"),
    "calculate" to listOf("Вычислить", "Calculate", "Есептеу"),
    "solve" to listOf("Решить", "Solve", "Шешу"),
    "font" to listOf("Шрифт", "Font", "Қаріп"),
    "choose_font" to listOf("Выбрать TTF / OTF", "Choose TTF / OTF", "TTF / OTF таңдау"),
    "reset" to listOf("Сброс", "Reset", "Қалпына келтіру"),
    "text_size" to listOf("Размер текста", "Text size", "Мәтін өлшемі"),
    "language_hint" to listOf("Выберите язык интерфейса. Выбор сохраняется на устройстве.", "Choose the interface language. Your choice is saved on this device.", "Интерфейс тілін таңдаңыз. Таңдау құрылғыда сақталады."),
    "theme_hint" to listOf("Тема не зависит от выбранной иконки приложения.", "The theme is independent of the selected app icon.", "Тақырып қолданба белгішесіне тәуелді емес."),
    "icon_hint" to listOf("Выберите вариант. Изменения вступят в силу после нажатия «Применить».", "Choose an option. Changes take effect after tapping Apply.", "Нұсқаны таңдаңыз. Өзгерістер «Қолдану» батырмасын басқаннан кейін күшіне енеді."),
    "steps" to listOf("Решение", "Steps", "Шешімі"),
    "delete" to listOf("Удалить", "Delete", "Жою"),
    "error" to listOf("Ошибка", "Error", "Қате"),
    "fractions" to listOf("Дроби", "Fractions", "Бөлшектер"),
    "percentages" to listOf("Проценты", "Percentages", "Пайыздар"),
    "geometry" to listOf("Геометрия", "Geometry", "Геометрия"),
    "converter" to listOf("Конвертер", "Converter", "Түрлендіргіш"),
    "percent_of" to listOf("% от числа", "% of a number", "Санның пайызы"),
    "what_percent" to listOf("Сколько %", "What percent", "Неше пайыз"),
    "change_percent" to listOf("± на %", "Change by %", "% өзгерту"),
    "rectangle" to listOf("Прямоугольник", "Rectangle", "Тіктөртбұрыш"),
    "circle" to listOf("Круг", "Circle", "Шеңбер"),
    "triangle" to listOf("Треугольник", "Triangle", "Үшбұрыш"),
    "fraction_one" to listOf("Дробь 1", "Fraction 1", "Бөлшек 1"),
    "fraction_two" to listOf("Дробь 2", "Fraction 2", "Бөлшек 2"),
    "part" to listOf("Часть", "Part", "Бөлік"),
    "whole" to listOf("Целое", "Whole", "Бүтін"),
    "percent" to listOf("Процент", "Percent", "Пайыз"),
    "number" to listOf("Число", "Number", "Сан"),
    "radius" to listOf("Радиус", "Radius", "Радиус"),
    "side_a" to listOf("Сторона a", "Side a", "a қабырғасы"),
    "side_b" to listOf("Сторона b", "Side b", "b қабырғасы"),
    "side_c" to listOf("Сторона c", "Side c", "c қабырғасы"),
    "applied" to listOf("Применяется", "Applied", "Қолданылған"),
    "selected" to listOf("Выбрана", "Selected", "Таңдалған")
)

@Composable
internal fun tx(key: String): String {
    val language = LocalLanguage.current
    val index = when (language) { "en" -> 1; "kk" -> 2; else -> 0 }
    return uiText[key]?.getOrNull(index) ?: uiText[key]?.firstOrNull() ?: key
}

@Composable
internal fun displayText(source: String): String {
    val key = when (source) {
        "Дроби" -> "fractions"
        "Проценты" -> "percentages"
        "Геометрия" -> "geometry"
        "Конвертер" -> "converter"
        "% от числа" -> "percent_of"
        "Сколько %" -> "what_percent"
        "± на %" -> "change_percent"
        "Прямоугольник" -> "rectangle"
        "Круг" -> "circle"
        "Треугольник" -> "triangle"
        "Дробь 1" -> "fraction_one"
        "Дробь 2" -> "fraction_two"
        "Часть" -> "part"
        "Целое" -> "whole"
        "Процент" -> "percent"
        "Число" -> "number"
        "Радиус" -> "radius"
        "Сторона a" -> "side_a"
        "Сторона b" -> "side_b"
        "Сторона c" -> "side_c"
        "Применяется" -> "applied"
        "Выбрана" -> "selected"
        else -> return source
    }
    return tx(key)
}
