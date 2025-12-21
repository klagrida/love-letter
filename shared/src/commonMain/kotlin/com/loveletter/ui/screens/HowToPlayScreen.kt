package com.loveletter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loveletter.game.CardType
import com.loveletter.ui.components.GameHeader
import com.loveletter.ui.theme.LoveRed

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GameHeader(
            title = "How to Play",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introduction
            RulesCard(
                title = "The Story",
                emoji = "💌",
                content = """
                    All the young men (and many of the old) seek to woo the princess of Tempest. Unfortunately, she has shut herself away in the palace, and all you have to communicate with her is through letters.

                    Your goal is to get your love letter into Princess Annette's hands while keeping other players' letters away.
                """.trimIndent()
            )

            // Objective
            RulesCard(
                title = "Objective",
                emoji = "🎯",
                content = """
                    Win tokens of affection by having the highest-value card at the end of each round, or by being the last player remaining.

                    Tokens needed to win:
                    • 2 players: 7 tokens
                    • 3 players: 5 tokens
                    • 4 players: 4 tokens
                """.trimIndent()
            )

            // Gameplay
            RulesCard(
                title = "Gameplay",
                emoji = "🎮",
                content = """
                    1. Each player starts with one card in hand.
                    2. On your turn, draw one card from the deck.
                    3. Play one of your two cards and apply its effect.
                    4. The round ends when the deck runs out or only one player remains.
                    5. The player with the highest card value wins the round.
                """.trimIndent()
            )

            // Cards Section
            Text(
                text = "The Cards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LoveRed
            )

            CardType.entries.forEach { cardType ->
                CardRuleItem(cardType = cardType)
            }

            // Special Rules
            RulesCard(
                title = "Special Rules",
                emoji = "⚠️",
                content = """
                    • Countess Rule: If you have the Countess with the King or Prince, you MUST play the Countess.

                    • Protection: Handmaid protection lasts until the start of your next turn.

                    • No Valid Targets: If all other players are protected or eliminated, you can still play targeting cards with no effect.

                    • 2-Player Variant: Three cards are placed face-up and removed from the game at the start of each round.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RulesCard(
    title: String,
    emoji: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CardRuleItem(cardType: CardType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card value circle
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "${cardType.value}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cardType.getEmoji())
                    Text(
                        text = cardType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "(×${cardType.count})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = cardType.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
