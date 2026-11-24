package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.core.i18n.LocalStrings
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun PlanifyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = modifier
            .liquidGlassContainer(shape = shape, elevation = 4.dp, heroAccent = false)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = shape,
        color = Color.Transparent,
        border = liquidGlassBorder(heroAccent = false)
    ) {
        content()
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    PlanifyCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PlanifyMarkdownView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val lines = markdown.lines()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.trimStart().startsWith("```")) {
                val langHint = line.trimStart().removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                val codeText = codeLines.joinToString("\n")
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = langHint.ifBlank { "code" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(codeText)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = strings.copy,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Text(
                                text = codeText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else if (line.startsWith("### ")) {
                Text(
                    text = formatInlineMarkdown(line.removePrefix("### ")),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            } else if (line.startsWith("## ")) {
                Text(
                    text = formatInlineMarkdown(line.removePrefix("## ")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else if (line.startsWith("# ")) {
                Text(
                    text = formatInlineMarkdown(line.removePrefix("# ")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            } else if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
                val bulletBody = line.trimStart().drop(2)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatInlineMarkdown(bulletBody),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (line.isNotBlank()) {
                Text(
                    text = formatInlineMarkdown(line),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }
            i++
        }
    }
}

private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
    var idx = 0
    while (idx < text.length) {
        when {
            text.startsWith("**", idx) -> {
                val end = text.indexOf("**", idx + 2)
                if (end > idx) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(idx + 2, end))
                    }
                    idx = end + 2
                } else {
                    append(text[idx])
                    idx++
                }
            }
            text[idx] == '`' -> {
                val end = text.indexOf('`', idx + 1)
                if (end > idx) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)) {
                        append(text.substring(idx + 1, end))
                    }
                    idx = end + 1
                } else {
                    append(text[idx])
                    idx++
                }
            }
            else -> {
                append(text[idx])
                idx++
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    values: List<Pair<String, Float>>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = (values.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            if (values.isEmpty()) return@Canvas
            val slotW = size.width / values.size
            val barW = (slotW * 0.52f).coerceAtMost(36.dp.toPx())
            values.forEachIndexed { idx, (_, value) ->
                val ratio = (value / maxVal).coerceIn(0f, 1f)
                val barH = size.height * ratio
                val left = idx * slotW + (slotW - barW) / 2f
                // Background track
                drawRoundRect(
                    color = barColor.copy(alpha = 0.14f),
                    topLeft = Offset(left, 0f),
                    size = Size(barW, size.height),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                if (barH > 0f) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, size.height - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            values.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SimpleLineTrendChart(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        if (points.size < 2) return@Canvas
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val path = Path()
        points.forEachIndexed { idx, v ->
            val x = idx * stepX
            val y = size.height - ((v / maxVal).coerceIn(0f, 1f) * (size.height - 16f)) - 8f
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = lineColor, radius = 5.5f, center = Offset(x, y))
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
    }
}

@Composable
fun CategoryDistributionBar(
    slices: List<Triple<String, Float, Color>>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            slices.forEach { (_, value, color) ->
                val weight = (value / total).coerceAtLeast(0.02f)
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(16.dp)
                        .background(color)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            slices.take(4).forEach { (name, value, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$name (${value.toInt()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
