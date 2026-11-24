package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.LocalAppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.local.AchievementEntity
import com.example.data.local.ChallengeEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.LiquidGlassHeroCard
import com.example.ui.components.MascotCanvas
import com.example.ui.components.MascotCatalog
import com.example.ui.components.MascotMood
import com.example.ui.components.PlanifyCard
import com.example.ui.theme.LocalPlanifyColors

@Composable
fun AchievementsScreen(
    profile: UserProfileEntity?,
    achievements: List<AchievementEntity>,
    challenges: List<ChallengeEntity>,
    onClaimChallenge: (ChallengeEntity) -> Unit,
    onUnlockOrEquipMascot: (itemId: String, category: String, costCoins: Int) -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current

    var tabIndex by remember { mutableIntStateOf(0) } // 0=Badges & Challenges, 1=Mascot Wardrobe

    val xp = profile?.xp ?: 0
    val level = profile?.level ?: 1
    val coins = profile?.coins ?: 0
    val unlockedSet = remember(profile) {
        profile?.unlockedItemsCsv?.split(",")?.filter { it.isNotBlank() }?.toSet()
            ?: setOf("classic_blue", "none", "minimal_desk")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text(strings.achievementsTitle) })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text(strings.mascotWardrobe) })
        }

        if (tabIndex == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LiquidGlassHeroCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${strings.levelLabel} ${JalaliCalendar.formatNumber(level, lang)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${JalaliCalendar.formatNumber(coins, lang)} ${strings.coinsLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extra.warning
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ((xp % 150) / 150f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = strings.achievementsXpRules,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Daily & Weekly Challenges
                item {
                    Text(strings.achievementsDailyChallenges, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(challenges.filter { !it.isWeekly }, key = { it.id }) { ch ->
                    ChallengeCard(ch = ch, onClaim = { onClaimChallenge(ch) })
                }

                item {
                    Text(strings.achievementsWeeklyChallenges, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(challenges.filter { it.isWeekly }, key = { it.id }) { ch ->
                    ChallengeCard(ch = ch, onClaim = { onClaimChallenge(ch) })
                }

                // Badges
                item {
                    Text(strings.achievementsBadges, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(achievements, key = { it.id }) { badge ->
                    PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (badge.isUnlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) extra.warning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == AppLanguage.FA) badge.titleFa else badge.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang == AppLanguage.FA) badge.descFa else badge.descEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (badge.isUnlocked) extra.success.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = if (badge.isUnlocked) strings.achievementsUnlocked else "+${JalaliCalendar.formatNumber(badge.coinReward, lang)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (badge.isUnlocked) extra.success else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 2D Mascot Wardrobe & Room Shop
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LiquidGlassHeroCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MascotCanvas(
                                mood = MascotMood.HAPPY,
                                equippedOutfit = profile?.equippedOutfit ?: "classic_blue",
                                equippedAccessory = profile?.equippedAccessory ?: "none",
                                equippedRoom = profile?.equippedRoom ?: "minimal_desk",
                                size = 140.dp,
                                contentDescriptionText = strings.dashMascotTitle
                            )
                            Text(
                                text = "${JalaliCalendar.formatNumber(coins, lang)} ${strings.coinsLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = extra.warning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                listOf(
                    "OUTFIT" to strings.mascotOutfits,
                    "ACCESSORY" to strings.mascotAccessories,
                    "ROOM" to strings.mascotRooms
                ).forEach { (cat, header) ->
                    item {
                        Text(header, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(MascotCatalog.filter { it.category == cat }, key = { it.id }) { item ->
                        val isUnlocked = unlockedSet.contains(item.id)
                        val isEquipped = profile?.equippedOutfit == item.id ||
                            profile?.equippedAccessory == item.id ||
                            profile?.equippedRoom == item.id
                        PlanifyCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.FA) item.nameFa else item.nameEn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                when {
                                    isEquipped -> {
                                        OutlinedButton(onClick = {}, enabled = false) {
                                            Text(strings.mascotEquipped)
                                        }
                                    }
                                    isUnlocked -> {
                                        Button(onClick = { onUnlockOrEquipMascot(item.id, item.category, item.costCoins) }) {
                                            Text(strings.mascotEquip)
                                        }
                                    }
                                    else -> {
                                        Button(
                                            enabled = coins >= item.costCoins,
                                            onClick = { onUnlockOrEquipMascot(item.id, item.category, item.costCoins) }
                                        ) {
                                            Text("${strings.mascotUnlockFor} ${JalaliCalendar.formatNumber(item.costCoins, lang)} ${strings.coinsLabel}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    ch: ChallengeEntity,
    onClaim: () -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalAppLanguage.current
    val extra = LocalPlanifyColors.current
    val progress = (ch.currentCount.toFloat() / ch.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    val canClaim = !ch.isClaimed && ch.currentCount >= ch.targetCount

    PlanifyCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == AppLanguage.FA) ch.titleFa else ch.titleEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${JalaliCalendar.formatNumber(ch.currentCount, lang)}/${JalaliCalendar.formatNumber(ch.targetCount, lang)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+${JalaliCalendar.formatNumber(ch.xpReward, lang)} XP • +${JalaliCalendar.formatNumber(ch.coinReward, lang)} ${strings.coinsLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (ch.isClaimed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = extra.success)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.achievementsClaimed, style = MaterialTheme.typography.labelMedium, color = extra.success)
                    }
                } else if (canClaim) {
                    Button(onClick = onClaim) {
                        Text(strings.achievementsClaim)
                    }
                }
            }
        }
    }
}
