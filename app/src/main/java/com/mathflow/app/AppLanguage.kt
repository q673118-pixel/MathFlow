package com.mathflow.app

internal data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String
)

/**
 * Language catalogue foundation. Only languages with reviewed UI translations
 * should be enabled for selection in production.
 */
internal val AppLanguages = listOf(
    AppLanguage("ru", "Русский", "Russian"),
    AppLanguage("en", "English", "English"),
    AppLanguage("kk", "Қазақша", "Kazakh")
)
