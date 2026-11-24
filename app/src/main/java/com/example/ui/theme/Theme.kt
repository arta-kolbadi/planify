package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.CalendarSystem
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalCalendarSystem
import com.example.core.i18n.LocalStrings
import com.example.core.i18n.stringsFor

val LocalPlanifyColors = staticCompositionLocalOf {
    buildPalette(PlanifyThemeType.LIGHT).extra
}

val PlanifyShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun PlanifyTheme(
    themeType: PlanifyThemeType = PlanifyThemeType.LIGHT,
    language: AppLanguage = AppLanguage.EN,
    calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    fontScale: Float = 1.0f,
    customPrimaryHex: Long = 0xFF168BFF,
    customSecondaryHex: Long = 0xFF20D4E8,
    customDarkBase: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = buildPalette(
        themeType = themeType,
        customPrimaryHex = customPrimaryHex,
        customSecondaryHex = customSecondaryHex,
        customDarkBase = customDarkBase
    )
    val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = buildPlanifyTypography(language, fontScale)
    val strings = stringsFor(language)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !palette.extra.isDark
                    isAppearanceLightNavigationBars = !palette.extra.isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalPlanifyColors provides palette.extra,
        LocalLayoutDirection provides layoutDirection,
        LocalStrings provides strings,
        LocalAppLanguage provides language,
        LocalCalendarSystem provides calendarSystem
    ) {
        MaterialTheme(
            colorScheme = palette.colorScheme,
            typography = typography,
            shapes = PlanifyShapes,
            content = content
        )
    }
}
