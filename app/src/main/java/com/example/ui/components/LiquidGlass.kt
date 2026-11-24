package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalPlanifyColors

/**
 * Full-screen subtle atmospheric backdrop with soft radial color orbs so translucent
 * Liquid Glass surfaces visibly refract depth and color across every theme.
 */
@Composable
fun PlanifyAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val bg = MaterialTheme.colorScheme.background
    val isDark = LocalPlanifyColors.current.isDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = if (isDark) 0.22f else 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.16f, h * 0.12f),
                    radius = w * 0.78f
                ),
                radius = w * 0.78f,
                center = Offset(w * 0.16f, h * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondary.copy(alpha = if (isDark) 0.18f else 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.38f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.88f, h * 0.38f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = if (isDark) 0.16f else 0.11f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.45f, h * 0.92f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.45f, h * 0.92f)
            )
        }
        content()
    }
}

/**
 * Applies a multi-layered refractive Liquid Glass container effect:
 * translucent frost body + specular top-edge sheen + tinted ambient shadow.
 */
fun Modifier.liquidGlassContainer(
    shape: Shape,
    elevation: Dp = 8.dp,
    heroAccent: Boolean = false
): Modifier = composed {
    val extra = LocalPlanifyColors.current
    val cs = MaterialTheme.colorScheme

    val topGlass = when {
        extra.isDark && heroAccent -> cs.primary.copy(alpha = 0.22f)
        extra.isDark -> Color.White.copy(alpha = 0.11f)
        heroAccent -> Color.White.copy(alpha = 0.88f)
        else -> Color.White.copy(alpha = 0.84f)
    }
    val midGlass = if (extra.isDark) {
        cs.surface.copy(alpha = 0.76f)
    } else {
        cs.surface.copy(alpha = 0.72f)
    }
    val bottomGlass = when {
        extra.isDark && heroAccent -> cs.secondary.copy(alpha = 0.14f)
        extra.isDark -> cs.surfaceContainerHigh.copy(alpha = 0.68f)
        heroAccent -> cs.primaryContainer.copy(alpha = 0.42f)
        else -> cs.surfaceContainerLow.copy(alpha = 0.68f)
    }

    val glassBrush = Brush.linearGradient(
        colors = listOf(topGlass, midGlass, bottomGlass)
    )

    val sheenHighlight = if (extra.isDark) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.95f)
    val shadowTint = if (extra.isDark) Color.Black else cs.primary.copy(alpha = 0.24f)

    this
        .shadow(
            elevation = if (extra.isDark) 0.dp else elevation,
            shape = shape,
            ambientColor = shadowTint,
            spotColor = shadowTint
        )
        .clip(shape)
        .background(brush = glassBrush, shape = shape)
        .drawBehind {
            // Subtle inner specular caustic sheen across upper edge
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        sheenHighlight,
                        Color.Transparent
                    )
                ),
                start = Offset(size.width * 0.08f, 1.5.dp.toPx()),
                end = Offset(size.width * 0.92f, 1.5.dp.toPx()),
                strokeWidth = 1.6.dp.toPx()
            )
        }
}

@Composable
fun liquidGlassBorder(heroAccent: Boolean = false): BorderStroke {
    val extra = LocalPlanifyColors.current
    val cs = MaterialTheme.colorScheme
    val startColor = if (extra.isDark) {
        Color.White.copy(alpha = if (heroAccent) 0.34f else 0.22f)
    } else {
        Color.White.copy(alpha = 0.94f)
    }
    val endColor = if (heroAccent) {
        cs.primary.copy(alpha = if (extra.isDark) 0.38f else 0.28f)
    } else {
        extra.border.copy(alpha = if (extra.isDark) 0.70f else 0.75f)
    }
    return BorderStroke(
        width = if (heroAccent) 1.25.dp else 1.dp,
        brush = Brush.linearGradient(colors = listOf(startColor, endColor))
    )
}

@Composable
fun LiquidGlassHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val extra = LocalPlanifyColors.current

    Box(modifier = modifier) {
        // Optional ambient glow halo behind hero card on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(22.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                cs.primary.copy(alpha = if (extra.isDark) 0.16f else 0.10f),
                                cs.secondary.copy(alpha = if (extra.isDark) 0.14f else 0.09f)
                            )
                        ),
                        shape = shape
                    )
            )
        }

        Surface(
            modifier = Modifier
                .liquidGlassContainer(shape = shape, elevation = 10.dp, heroAccent = true)
                .let { if (onClick != null) it.clickable { onClick() } else it },
            shape = shape,
            color = Color.Transparent,
            border = liquidGlassBorder(heroAccent = true)
        ) {
            Box(content = content)
        }
    }
}
