package com.example.core.i18n

import androidx.compose.runtime.staticCompositionLocalOf

val LocalStrings = staticCompositionLocalOf<AppStrings> { EnStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }
val LocalCalendarSystem = staticCompositionLocalOf { CalendarSystem.GREGORIAN }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.FA -> FaStrings
    AppLanguage.EN -> EnStrings
}
