package com.loveletter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveletter.game.Card as GameCard
import com.loveletter.game.CardType
import com.loveletter.ui.theme.CardBackground
import com.loveletter.ui.theme.LoveRed
import com.loveletter.ui.theme.RoyalGold
import com.loveletter.ui.theme.VelvetPurple

@Composable
fun GameCardView(
    card: GameCard,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    isRevealed: Boolean = true,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring()
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) RoyalGold else Color.Transparent
    )

    Card(
        modifier = modifier
            .size(width = size.width, height = size.height)
            .scale(scale)
            .then(
                if (onClick != null && isPlayable) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isRevealed) CardBackground else VelvetPurple
        )
    ) {
        if (isRevealed) {
            RevealedCardContent(card = card, size = size, isPlayable = isPlayable)
        } else {
            HiddenCardContent(size = size)
        }
    }
}

@Composable
private fun RevealedCardContent(
    card: GameCard,
    size: CardSize,
    isPlayable: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardBackground,
                        CardBackground.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(size.padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card value at top
            Text(
                text = "${card.value}",
                fontSize = size.valueFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = getCardColor(card.type)
            )

            // Card emoji/icon
            Text(
                text = card.emoji,
                fontSize = size.emojiSize.sp
            )

            // Card name
            Text(
                text = card.displayName,
                fontSize = size.nameFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Card description (only on larger cards)
            if (size == CardSize.LARGE) {
                Text(
                    text = card.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }

        // Decorative corner value
        Text(
            text = "${card.value}",
            fontSize = (size.valueFontSize * 0.6).sp,
            fontWeight = FontWeight.Bold,
            color = getCardColor(card.type).copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        )

        // Disabled overlay
        if (!isPlayable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun HiddenCardContent(size: CardSize) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VelvetPurple,
                        VelvetPurple.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative pattern
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💌",
                fontSize = size.emojiSize.sp
            )
            Text(
                text = "Love Letter",
                fontSize = (size.nameFontSize * 0.8).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

private fun getCardColor(type: CardType): Color = when (type) {
    CardType.GUARD -> Color(0xFF5D4037)
    CardType.PRIEST -> Color(0xFF1565C0)
    CardType.BARON -> Color(0xFF2E7D32)
    CardType.HANDMAID -> Color(0xFF00838F)
    CardType.PRINCE -> Color(0xFF6A1B9A)
    CardType.KING -> Color(0xFFC5A100)
    CardType.COUNTESS -> Color(0xFFAD1457)
    CardType.PRINCESS -> LoveRed
}

enum class CardSize(
    val width: Dp,
    val height: Dp,
    val padding: Dp,
    val valueFontSize: Int,
    val emojiSize: Int,
    val nameFontSize: Int
) {
    SMALL(60.dp, 84.dp, 4.dp, 16, 20, 8),
    MEDIUM(90.dp, 126.dp, 8.dp, 24, 28, 11),
    LARGE(120.dp, 168.dp, 12.dp, 32, 36, 14)
}

@Composable
fun CardBackView(
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.MEDIUM
) {
    Card(
        modifier = modifier.size(width = size.width, height = size.height),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VelvetPurple)
    ) {
        HiddenCardContent(size = size)
    }
}

@Composable
fun MiniCardIndicator(
    card: GameCard,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = getCardColor(card.type).copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = card.emoji,
            fontSize = 12.sp
        )
        Text(
            text = "${card.value}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = getCardColor(card.type)
        )
    }
}
