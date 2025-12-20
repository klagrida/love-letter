package com.loveletter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveletter.game.Player
import com.loveletter.ui.theme.LoveRed
import com.loveletter.ui.theme.RoyalGold

@Composable
fun PlayerView(
    player: Player,
    modifier: Modifier = Modifier,
    isCurrentTurn: Boolean = false,
    isLocalPlayer: Boolean = false,
    showCard: Boolean = false,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null
) {
    val backgroundColor = when {
        isSelected -> RoyalGold.copy(alpha = 0.2f)
        isCurrentTurn -> MaterialTheme.colorScheme.primaryContainer
        player.isEliminated -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isSelected -> RoyalGold
        isCurrentTurn -> MaterialTheme.colorScheme.primary
        isLocalPlayer -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelectable && !player.isEliminated && !player.isProtected) {
                    Modifier.clickable { onSelect?.invoke() }
                } else {
                    Modifier
                }
            )
            .border(
                width = if (isCurrentTurn || isLocalPlayer || isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        tonalElevation = if (isCurrentTurn) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Player avatar
                PlayerAvatar(
                    name = player.name,
                    isEliminated = player.isEliminated,
                    isProtected = player.isProtected,
                    isHost = player.isHost
                )

                // Player info
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                            textDecoration = if (player.isEliminated) TextDecoration.LineThrough else null,
                            color = if (player.isEliminated) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isLocalPlayer) {
                            Text(
                                text = "(You)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (player.isHost) {
                            Text(
                                text = "👑",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Status indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (player.isProtected) {
                            StatusBadge(text = "Protected", emoji = "🛡️", color = Color(0xFF2196F3))
                        }
                        if (player.isEliminated) {
                            StatusBadge(text = "Eliminated", emoji = "💀", color = LoveRed)
                        }
                    }
                }
            }

            // Right side: tokens and card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tokens of affection
                TokensDisplay(count = player.tokensOfAffection)

                // Card indicator
                if (showCard && player.hand != null) {
                    player.hand?.let { card ->
                        MiniCardIndicator(card = card)
                    }
                } else if (!player.isEliminated && player.hand != null) {
                    // Show card back
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🃏", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    isEliminated: Boolean = false,
    isProtected: Boolean = false,
    isHost: Boolean = false,
    size: Int = 40
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                color = if (isEliminated) {
                    Color.Gray
                } else {
                    getAvatarColor(name)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4).sp
        )

        // Protected indicator
        AnimatedVisibility(
            visible = isProtected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2196F3).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = (size * 0.5).sp)
            }
        }
    }
}

@Composable
fun TokensDisplay(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = if (count > 0) RoyalGold.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count.coerceAtMost(7)) {
            Text(text = "❤️", fontSize = 12.sp)
        }
        if (count == 0) {
            Text(
                text = "0 ❤️",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 10.sp)
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFFEF4444), // Red
        Color(0xFFF97316), // Orange
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
    )
    val hash = name.hashCode()
    return colors[kotlin.math.abs(hash) % colors.size]
}

@Composable
fun PlayersList(
    players: List<Player>,
    modifier: Modifier = Modifier,
    currentPlayerId: String? = null,
    localPlayerId: String? = null,
    showCards: Boolean = false,
    selectablePlayerIds: List<String> = emptyList(),
    selectedPlayerId: String? = null,
    onPlayerSelect: ((Player) -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        players.forEach { player ->
            PlayerView(
                player = player,
                isCurrentTurn = player.id == currentPlayerId,
                isLocalPlayer = player.id == localPlayerId,
                showCard = showCards && player.id == localPlayerId,
                isSelectable = player.id in selectablePlayerIds,
                isSelected = player.id == selectedPlayerId,
                onSelect = { onPlayerSelect?.invoke(player) }
            )
        }
    }
}
