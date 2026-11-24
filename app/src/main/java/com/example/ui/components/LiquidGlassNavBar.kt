package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.AppStrings
import com.example.ui.theme.LocalPlanifyColors

data class BottomNavItemSpec(
    val routeId: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun LiquidGlassBottomBar(
    items: List<BottomNavItemSpec>,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val extra = LocalPlanifyColors.current
    val dockShape = RoundedCornerShape(30.dp)
    val selectedIndex = items.indexOfFirst { it.isSelected }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft luminous ambient halo beneath the glass dock on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .height(54.dp)
                    .blur(20.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                cs.primary.copy(alpha = if (extra.isDark) 0.28f else 0.16f),
                                cs.secondary.copy(alpha = if (extra.isDark) 0.26f else 0.15f),
                                cs.primary.copy(alpha = if (extra.isDark) 0.24f else 0.14f)
                            )
                        ),
                        shape = dockShape
                    )
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .liquidGlassContainer(shape = dockShape, elevation = 14.dp, heroAccent = true),
            shape = dockShape,
            color = Color.Transparent,
            border = liquidGlassBorder(heroAccent = true)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                val count = items.size.coerceAtLeast(1)
                val slotWidth = maxWidth / count

                val indicatorOffset by animateDpAsState(
                    targetValue = slotWidth * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "liquid_nav_indicator_offset"
                )

                val indicatorShape = RoundedCornerShape(24.dp)
                // Sliding Liquid Glass Active Capsule
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(slotWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = indicatorShape,
                            ambientColor = cs.primary.copy(alpha = 0.4f),
                            spotColor = cs.primary.copy(alpha = 0.45f)
                        )
                        .clip(indicatorShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    cs.primary.copy(alpha = 0.94f),
                                    cs.secondary.copy(alpha = 0.86f)
                                )
                            ),
                            shape = indicatorShape
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.75f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = indicatorShape
                        )
                )

                // Navigation Tabs Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val iconScale by animateFloatAsState(
                            targetValue = if (item.isSelected) 1.12f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = 0.65f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "liquid_nav_icon_scale"
                        )
                        val contentTint by animateColorAsState(
                            targetValue = if (item.isSelected) cs.onPrimary else cs.onSurfaceVariant,
                            label = "liquid_nav_tint"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .minimumInteractiveComponentSize()
                                .clip(indicatorShape)
                                .clickable(onClick = item.onClick)
                                .testTag(item.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = contentTint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentTint,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassTopBar(
    title: String,
    language: AppLanguage,
    strings: AppStrings,
    onToggleLanguage: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val barShape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .liquidGlassContainer(shape = barShape, elevation = 6.dp, heroAccent = false),
        shape = barShape,
        color = Color.Transparent,
        border = liquidGlassBorder(heroAccent = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = cs.primaryContainer.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.planify_mark),
                            contentDescription = strings.appName,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = cs.primaryContainer.copy(alpha = 0.68f),
                    border = BorderStroke(1.dp, cs.primary.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleLanguage)
                        .testTag("topbar_lang_toggle")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "EN" else "FA",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = cs.primary
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = cs.surfaceContainerHigh.copy(alpha = 0.75f),
                    border = liquidGlassBorder(heroAccent = false),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSettings)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = strings.navSettings,
                            tint = cs.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassMoreNavIcon(): ImageVector = Icons.Rounded.GridView
