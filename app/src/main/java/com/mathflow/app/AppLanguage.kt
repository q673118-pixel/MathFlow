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
    "icon_hint" to listOf("Выберите вариант. Изменения вступят в силу после нажатия «Применить».", "Choose an option. Changes take effect after tapping Apply.", "Нұсқаны таңдаңыз. Өзгерістер «Қолдану» батырмасын басқаннан кейін күшіне енеді.")
)

@Composable
internal fun tx(key: String): String {
    val language = LocalLanguage.current
    val index = when (language) { "en" -> 1; "kk" -> 2; else -> 0 }
    return uiText[key]?.getOrNull(index) ?: uiText[key]?.firstOrNull() ?: key
}
