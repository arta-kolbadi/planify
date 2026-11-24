package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppStrings

enum class MascotMood {
    IDLE,
    STUDYING,
    SLEEPING,
    CELEBRATING,
    THINKING,
    HAPPY,
    SAD;

    fun label(strings: AppStrings): String = when (this) {
        IDLE -> strings.mascotIdle
        STUDYING -> strings.mascotStudying
        SLEEPING -> strings.mascotSleeping
        CELEBRATING -> strings.mascotCelebrating
        THINKING -> strings.mascotThinking
        HAPPY -> strings.mascotHappy
        SAD -> strings.mascotSad
    }
}

data class MascotShopItem(
    val id: String,
    val category: String, // OUTFIT, ACCESSORY, ROOM
    val nameEn: String,
    val nameFa: String,
    val costCoins: Int
)

val MascotCatalog = listOf(
    MascotShopItem("classic_blue", "OUTFIT", "Classic Blue", "آبی کلاسیک", 0),
    MascotShopItem("scholar_hoodie", "OUTFIT", "Scholar Teal", "هودی دانشجو", 40),
    MascotShopItem("starlight_cape", "OUTFIT", "Gold Star Cape", "شنل ستاره", 80),
    MascotShopItem("mint_jacket", "OUTFIT", "Mint Focus Jacket", "ژاکت نعنایی", 60),
    MascotShopItem("none", "ACCESSORY", "No Accessory", "بدون اکسسوری", 0),
    MascotShopItem("glasses", "ACCESSORY", "Study Glasses", "عینک مطالعه", 30),
    MascotShopItem("headphones", "ACCESSORY", "Focus Headphones", "هدفون تمرکز", 55),
    MascotShopItem("crown", "ACCESSORY", "Master Crown", "تاج پیشرفت", 120),
    MascotShopItem("minimal_desk", "ROOM", "Minimal Desk", "میز مینیمال", 0),
    MascotShopItem("sunny_window", "ROOM", "Sunny Window", "پنجره آفتابی", 45),
    MascotShopItem("night_library", "ROOM", "Cozy Night Desk", "میز شبانه", 70),
    MascotShopItem("garden_patio", "ROOM", "Garden Study", "مطالعه در باغ", 90)
)

@Composable
fun MascotCanvas(
    mood: MascotMood,
    equippedOutfit: String,
    equippedAccessory: String,
    equippedRoom: String,
    modifier: Modifier = Modifier,
    size: Dp = 116.dp,
    contentDescriptionText: String = "Plani Mascot",
    onClick: (() -> Unit)? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val infinite = rememberInfiniteTransition(label = "mascot_anim")
    val bob by infinite.animateFloat(
        initialValue = -3f,
        targetValue = if (mood == MascotMood.CELEBRATING) -9f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mood == MascotMood.CELEBRATING) 500 else 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val sparklePulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val outfitColor = when (equippedOutfit) {
        "scholar_hoodie" -> Color(0xFF0D9488)
        "starlight_cape" -> Color(0xFFF59E0B)
        "mint_jacket" -> Color(0xFF20B879)
        else -> primary
    }

    val roomTop = when (equippedRoom) {
        "sunny_window" -> Color(0xFFE0F2FE)
        "night_library" -> Color(0xFF172554)
        "garden_patio" -> Color(0xFFDCFCE7)
        else -> primary.copy(alpha = 0.12f)
    }
    val roomBottom = when (equippedRoom) {
        "sunny_window" -> Color(0xFFFEF3C7)
        "night_library" -> Color(0xFF0F172A)
        "garden_patio" -> Color(0xFFBBF7D0)
        else -> secondary.copy(alpha = 0.14f)
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = contentDescriptionText }
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.52f + bob

        // Room Backdrop Circle/Card
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(roomTop, roomBottom)),
            cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
        )

        // Desk Surface if studying or room theme
        drawRoundRect(
            color = Color(0xFF94A3B8).copy(alpha = 0.25f),
            topLeft = Offset(w * 0.12f, h * 0.78f),
            size = Size(w * 0.76f, h * 0.12f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Body / Hoodie
        drawRoundRect(
            color = outfitColor,
            topLeft = Offset(cx - w * 0.2f, cy + h * 0.08f),
            size = Size(w * 0.4f, h * 0.26f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        // Head
        val headRadius = w * 0.23f
        val headCenter = Offset(cx, cy - h * 0.06f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFE6F2FF)),
                center = headCenter - Offset(headRadius * 0.2f, headRadius * 0.25f),
                radius = headRadius * 1.4f
            ),
            radius = headRadius,
            center = headCenter
        )
        drawCircle(
            color = outfitColor.copy(alpha = 0.35f),
            radius = headRadius,
            center = headCenter,
            style = Stroke(width = 3f)
        )

        // Cute antenna / orbital leaf
        drawLine(
            color = secondary,
            start = Offset(cx, headCenter.y - headRadius),
            end = Offset(cx, headCenter.y - headRadius - h * 0.07f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFFF5A623).copy(alpha = sparklePulse),
            radius = w * 0.036f,
            center = Offset(cx, headCenter.y - headRadius - h * 0.08f)
        )

        // Eyes & Mouth by mood
        val eyeY = headCenter.y - headRadius * 0.08f
        val eyeOffsetX = headRadius * 0.38f
        val eyeColor = Color(0xFF17243B)

        when (mood) {
            MascotMood.SLEEPING -> {
                drawLine(eyeColor, Offset(cx - eyeOffsetX - 6f, eyeY), Offset(cx - eyeOffsetX + 6f, eyeY), 3.5f, StrokeCap.Round)
                drawLine(eyeColor, Offset(cx + eyeOffsetX - 6f, eyeY), Offset(cx + eyeOffsetX + 6f, eyeY), 3.5f, StrokeCap.Round)
            }
            MascotMood.HAPPY, MascotMood.CELEBRATING -> {
                drawArc(
                    color = eyeColor,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeOffsetX - 6f, eyeY - 4f),
                    size = Size(12f, 10f),
                    style = Stroke(3.5f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = eyeColor,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeOffsetX - 6f, eyeY - 4f),
                    size = Size(12f, 10f),
                    style = Stroke(3.5f, cap = StrokeCap.Round)
                )
            }
            else -> {
                drawCircle(eyeColor, radius = w * 0.028f, center = Offset(cx - eyeOffsetX, eyeY))
                drawCircle(eyeColor, radius = w * 0.028f, center = Offset(cx + eyeOffsetX, eyeY))
                drawCircle(Color.White, radius = w * 0.009f, center = Offset(cx - eyeOffsetX - 1.5f, eyeY - 1.5f))
                drawCircle(Color.White, radius = w * 0.009f, center = Offset(cx + eyeOffsetX - 1.5f, eyeY - 1.5f))
            }
        }

        // Mouth
        val mouthY = headCenter.y + headRadius * 0.25f
        when (mood) {
            MascotMood.SAD -> {
                drawArc(
                    color = eyeColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - 8f, mouthY),
                    size = Size(16f, 10f),
                    style = Stroke(3f, cap = StrokeCap.Round)
                )
            }
            MascotMood.THINKING, MascotMood.SLEEPING -> {
                drawCircle(eyeColor, radius = 3.5f, center = Offset(cx, mouthY + 2f))
            }
            else -> {
                drawArc(
                    color = eyeColor,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx - 10f, mouthY - 4f),
                    size = Size(20f, 12f),
                    style = Stroke(3.2f, cap = StrokeCap.Round)
                )
            }
        }

        // Accessories
        when (equippedAccessory) {
            "glasses" -> {
                val r = headRadius * 0.28f
                drawCircle(Color(0xFF1E293B), r, Offset(cx - eyeOffsetX, eyeY), style = Stroke(3f))
                drawCircle(Color(0xFF1E293B), r, Offset(cx + eyeOffsetX, eyeY), style = Stroke(3f))
                drawLine(Color(0xFF1E293B), Offset(cx - eyeOffsetX + r, eyeY), Offset(cx + eyeOffsetX - r, eyeY), 3f)
            }
            "headphones" -> {
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 185f,
                    sweepAngle = 170f,
                    useCenter = false,
                    topLeft = Offset(headCenter.x - headRadius * 1.08f, headCenter.y - headRadius * 1.08f),
                    size = Size(headRadius * 2.16f, headRadius * 2.16f),
                    style = Stroke(6f, cap = StrokeCap.Round)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(headCenter.x - headRadius - 6f, eyeY - 10f),
                    size = Size(10f, 22f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = secondary,
                    topLeft = Offset(headCenter.x + headRadius - 4f, eyeY - 10f),
                    size = Size(10f, 22f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            "crown" -> {
                val crownPath = Path().apply {
                    val baseY = headCenter.y - headRadius + 2f
                    moveTo(cx - 18f, baseY)
                    lineTo(cx - 20f, baseY - 16f)
                    lineTo(cx - 8f, baseY - 7f)
                    lineTo(cx, baseY - 20f)
                    lineTo(cx + 8f, baseY - 7f)
                    lineTo(cx + 20f, baseY - 16f)
                    lineTo(cx + 18f, baseY)
                    close()
                }
                drawPath(crownPath, color = Color(0xFFF59E0B))
            }
        }

        // Studying open book prop
        if (mood == MascotMood.STUDYING) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cx - w * 0.14f, h * 0.71f),
                size = Size(w * 0.28f, h * 0.12f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawLine(
                color = primary,
                start = Offset(cx, h * 0.71f),
                end = Offset(cx, h * 0.83f),
                strokeWidth = 2.5f
            )
        }
    }
}
