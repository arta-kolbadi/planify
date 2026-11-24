package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class PlanifyThemeType(val id: String) {
    LIGHT("light"),
    DARK("dark"),
    AMOLED("amoled"),
    OCEAN_BLUE("ocean_blue"),
    COZY_GREEN("cozy_green"),
    SAKURA_PINK("sakura_pink"),
    SUNSET_ORANGE("sunset_orange"),
    CUSTOM("custom");

    companion object {
        fun fromId(id: String?): PlanifyThemeType =
            entries.firstOrNull { it.id == id } ?: LIGHT
    }
}

@Immutable
data class PlanifyExtraColors(
    val border: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val elevatedSurface: Color,
    val isDark: Boolean
)

data class PlanifyPalette(
    val colorScheme: ColorScheme,
    val extra: PlanifyExtraColors,
    val previewSwatch: List<Color>
)

// Primary Light Mode Tokens (Master Prompt Spec — zero purple)
val PlanifyBlue = Color(0xFF168BFF)
val PlanifyCyan = Color(0xFF20D4E8)
val LightBg = Color(0xFFF5F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF17243B)
val LightTextSecondary = Color(0xFF68758A)
val LightBorder = Color(0xFFE3EAF2)
val SemanticSuccess = Color(0xFF20B879)
val SemanticWarning = Color(0xFFF5A623)
val SemanticDanger = Color(0xFFEF5350)

fun buildPalette(
    themeType: PlanifyThemeType,
    customPrimaryHex: Long = 0xFF168BFF,
    customSecondaryHex: Long = 0xFF20D4E8,
    customDarkBase: Boolean = false
): PlanifyPalette = when (themeType) {
    PlanifyThemeType.LIGHT -> PlanifyPalette(
        colorScheme = lightColorScheme(
            primary = PlanifyBlue,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCEEFF),
            onPrimaryContainer = Color(0xFF06386D),
            secondary = PlanifyCyan,
            onSecondary = Color(0xFF003239),
            secondaryContainer = Color(0xFFD7F8FC),
            onSecondaryContainer = Color(0xFF004752),
            tertiary = SemanticSuccess,
            onTertiary = Color.White,
            background = LightBg,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = Color(0xFFEDF2F8),
            onSurfaceVariant = LightTextSecondary,
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF9FBFE),
            surfaceContainer = Color(0xFFF0F4FA),
            surfaceContainerHigh = Color(0xFFE8EEF6),
            outline = LightBorder,
            outlineVariant = Color(0xFFD5DFEC),
            error = SemanticDanger,
            onError = Color.White
        ),
        extra = PlanifyExtraColors(
            border = LightBorder,
            success = SemanticSuccess,
            warning = SemanticWarning,
            danger = SemanticDanger,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            elevatedSurface = Color.White,
            isDark = false
        ),
        previewSwatch = listOf(LightBg, PlanifyBlue, PlanifyCyan)
    )

    PlanifyThemeType.DARK -> PlanifyPalette(
        colorScheme = darkColorScheme(
            primary = Color(0xFF389BFF),
            onPrimary = Color(0xFF002447),
            primaryContainer = Color(0xFF0E447A),
            onPrimaryContainer = Color(0xFFD6EAFF),
            secondary = Color(0xFF2EE0F2),
            onSecondary = Color(0xFF00353B),
            secondaryContainer = Color(0xFF0B4E57),
            onSecondaryContainer = Color(0xFFCFF8FC),
            tertiary = Color(0xFF34D399),
            background = Color(0xFF0D1422),
            onBackground = Color(0xFFF1F5FB),
            surface = Color(0xFF151F32),
            onSurface = Color(0xFFF1F5FB),
            surfaceVariant = Color(0xFF1E2B42),
            onSurfaceVariant = Color(0xFF9AA9C1),
            surfaceContainerLowest = Color(0xFF0A101B),
            surfaceContainerLow = Color(0xFF121B2B),
            surfaceContainer = Color(0xFF182438),
            surfaceContainerHigh = Color(0xFF213049),
            outline = Color(0xFF2C3D5A),
            outlineVariant = Color(0xFF23314A),
            error = Color(0xFFF87171),
            onError = Color(0xFF4B0909)
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFF273752),
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            danger = Color(0xFFF87171),
            textPrimary = Color(0xFFF1F5FB),
            textSecondary = Color(0xFF9AA9C1),
            elevatedSurface = Color(0xFF1D2940),
            isDark = true
        ),
        previewSwatch = listOf(Color(0xFF0D1422), Color(0xFF389BFF), Color(0xFF2EE0F2))
    )

    PlanifyThemeType.AMOLED -> PlanifyPalette(
        colorScheme = darkColorScheme(
            primary = Color(0xFF2596FF),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF0B3766),
            onPrimaryContainer = Color(0xFFD6EAFF),
            secondary = Color(0xFF20D4E8),
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF08424A),
            onSecondaryContainer = Color(0xFFCFF8FC),
            tertiary = Color(0xFF20B879),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF0B0E14),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF141923),
            onSurfaceVariant = Color(0xFF94A3B8),
            surfaceContainerLowest = Color(0xFF000000),
            surfaceContainerLow = Color(0xFF090C11),
            surfaceContainer = Color(0xFF10151E),
            surfaceContainerHigh = Color(0xFF181F2C),
            outline = Color(0xFF222B3A),
            outlineVariant = Color(0xFF1A222E),
            error = Color(0xFFEF5350),
            onError = Color.Black
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFF1F2735),
            success = Color(0xFF20B879),
            warning = Color(0xFFF5A623),
            danger = Color(0xFFEF5350),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            elevatedSurface = Color(0xFF131924),
            isDark = true
        ),
        previewSwatch = listOf(Color(0xFF000000), Color(0xFF2596FF), Color(0xFF20D4E8))
    )

    PlanifyThemeType.OCEAN_BLUE -> PlanifyPalette(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0284C7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD0F0FF),
            onPrimaryContainer = Color(0xFF00344F),
            secondary = Color(0xFF0D9488),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF0F3F3A),
            background = Color(0xFFEDF7FC),
            onBackground = Color(0xFF0C2438),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0C2438),
            surfaceVariant = Color(0xFFE0F1FA),
            onSurfaceVariant = Color(0xFF4B6B84),
            surfaceContainer = Color(0xFFE6F4FB),
            outline = Color(0xFFC5E1F0),
            error = SemanticDanger
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFFCBE4F2),
            success = Color(0xFF10B981),
            warning = SemanticWarning,
            danger = SemanticDanger,
            textPrimary = Color(0xFF0C2438),
            textSecondary = Color(0xFF4B6B84),
            elevatedSurface = Color.White,
            isDark = false
        ),
        previewSwatch = listOf(Color(0xFFEDF7FC), Color(0xFF0284C7), Color(0xFF0D9488))
    )

    PlanifyThemeType.COZY_GREEN -> PlanifyPalette(
        colorScheme = lightColorScheme(
            primary = Color(0xFF15803D),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCFCE7),
            onPrimaryContainer = Color(0xFF064E23),
            secondary = Color(0xFF0D9488),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD1FAE5),
            onSecondaryContainer = Color(0xFF064E3B),
            background = Color(0xFFF3F9F4),
            onBackground = Color(0xFF132A1B),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF132A1B),
            surfaceVariant = Color(0xFFE4F1E7),
            onSurfaceVariant = Color(0xFF4E6B57),
            surfaceContainer = Color(0xFFE9F5EC),
            outline = Color(0xFFCBE2D0),
            error = SemanticDanger
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFFD1E6D6),
            success = Color(0xFF16A34A),
            warning = SemanticWarning,
            danger = SemanticDanger,
            textPrimary = Color(0xFF132A1B),
            textSecondary = Color(0xFF4E6B57),
            elevatedSurface = Color.White,
            isDark = false
        ),
        previewSwatch = listOf(Color(0xFFF3F9F4), Color(0xFF15803D), Color(0xFF0D9488))
    )

    PlanifyThemeType.SAKURA_PINK -> PlanifyPalette(
        colorScheme = lightColorScheme(
            primary = Color(0xFFDB2777),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFCE7F3),
            onPrimaryContainer = Color(0xFF6B0F3B),
            secondary = Color(0xFFF43F5E),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFE4E6),
            onSecondaryContainer = Color(0xFF6E0E23),
            background = Color(0xFFFFF5F8),
            onBackground = Color(0xFF331826),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF331826),
            surfaceVariant = Color(0xFFFCE9F0),
            onSurfaceVariant = Color(0xFF7D5368),
            surfaceContainer = Color(0xFFFDF0F5),
            outline = Color(0xFFF5D0DF),
            error = SemanticDanger
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFFF5D0DF),
            success = SemanticSuccess,
            warning = SemanticWarning,
            danger = SemanticDanger,
            textPrimary = Color(0xFF331826),
            textSecondary = Color(0xFF7D5368),
            elevatedSurface = Color.White,
            isDark = false
        ),
        previewSwatch = listOf(Color(0xFFFFF5F8), Color(0xFFDB2777), Color(0xFFF43F5E))
    )

    PlanifyThemeType.SUNSET_ORANGE -> PlanifyPalette(
        colorScheme = lightColorScheme(
            primary = Color(0xFFEA580C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFEDD5),
            onPrimaryContainer = Color(0xFF6C2300),
            secondary = Color(0xFFF59E0B),
            onSecondary = Color(0xFF3D2200),
            secondaryContainer = Color(0xFFFEF3C7),
            onSecondaryContainer = Color(0xFF5A3300),
            background = Color(0xFFFFF8F2),
            onBackground = Color(0xFF331D0F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF331D0F),
            surfaceVariant = Color(0xFFFEEDDF),
            onSurfaceVariant = Color(0xFF7D5D48),
            surfaceContainer = Color(0xFFFFF2E6),
            outline = Color(0xFFF4D9C4),
            error = SemanticDanger
        ),
        extra = PlanifyExtraColors(
            border = Color(0xFFF4D9C4),
            success = SemanticSuccess,
            warning = Color(0xFFD97706),
            danger = SemanticDanger,
            textPrimary = Color(0xFF331D0F),
            textSecondary = Color(0xFF7D5D48),
            elevatedSurface = Color.White,
            isDark = false
        ),
        previewSwatch = listOf(Color(0xFFFFF8F2), Color(0xFFEA580C), Color(0xFFF59E0B))
    )

    PlanifyThemeType.CUSTOM -> {
        val p = Color(customPrimaryHex)
        val s = Color(customSecondaryHex)
        val onP = if (p.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
        val onS = if (s.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
        if (customDarkBase) {
            PlanifyPalette(
                colorScheme = darkColorScheme(
                    primary = p,
                    onPrimary = onP,
                    primaryContainer = p.copy(alpha = 0.24f),
                    onPrimaryContainer = Color.White,
                    secondary = s,
                    onSecondary = onS,
                    secondaryContainer = s.copy(alpha = 0.24f),
                    onSecondaryContainer = Color.White,
                    background = Color(0xFF10151F),
                    onBackground = Color(0xFFF1F5F9),
                    surface = Color(0xFF18202F),
                    onSurface = Color(0xFFF1F5F9),
                    surfaceVariant = Color(0xFF212B3E),
                    onSurfaceVariant = Color(0xFF9FB0C7),
                    surfaceContainer = Color(0xFF1C2536),
                    outline = Color(0xFF2D3A52),
                    error = Color(0xFFF87171)
                ),
                extra = PlanifyExtraColors(
                    border = Color(0xFF2D3A52),
                    success = Color(0xFF34D399),
                    warning = Color(0xFFFBBF24),
                    danger = Color(0xFFF87171),
                    textPrimary = Color(0xFFF1F5F9),
                    textSecondary = Color(0xFF9FB0C7),
                    elevatedSurface = Color(0xFF202A3C),
                    isDark = true
                ),
                previewSwatch = listOf(Color(0xFF10151F), p, s)
            )
        } else {
            PlanifyPalette(
                colorScheme = lightColorScheme(
                    primary = p,
                    onPrimary = onP,
                    primaryContainer = p.copy(alpha = 0.14f),
                    onPrimaryContainer = Color(0xFF102033),
                    secondary = s,
                    onSecondary = onS,
                    secondaryContainer = s.copy(alpha = 0.16f),
                    onSecondaryContainer = Color(0xFF102033),
                    background = Color(0xFFF6F9FC),
                    onBackground = LightTextPrimary,
                    surface = Color.White,
                    onSurface = LightTextPrimary,
                    surfaceVariant = Color(0xFFEDF2F7),
                    onSurfaceVariant = LightTextSecondary,
                    surfaceContainer = Color(0xFFF1F5FA),
                    outline = LightBorder,
                    error = SemanticDanger
                ),
                extra = PlanifyExtraColors(
                    border = LightBorder,
                    success = SemanticSuccess,
                    warning = SemanticWarning,
                    danger = SemanticDanger,
                    textPrimary = LightTextPrimary,
                    textSecondary = LightTextSecondary,
                    elevatedSurface = Color.White,
                    isDark = false
                ),
                previewSwatch = listOf(Color(0xFFF6F9FC), p, s)
            )
        }
    }
}
