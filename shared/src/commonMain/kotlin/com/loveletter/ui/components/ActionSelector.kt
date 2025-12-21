package com.loveletter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loveletter.game.CardType
import com.loveletter.game.GameAction
import com.loveletter.game.Player
import com.loveletter.ui.theme.RoyalGold

@Composable
fun ActionSelector(
    cardType: CardType,
    availableTargets: List<Player>,
    modifier: Modifier = Modifier,
    canTargetSelf: Boolean = false,
    localPlayer: Player? = null,
    onActionSelected: (GameAction) -> Unit,
    onCancel: () -> Unit
) {
    var selectedTarget by remember { mutableStateOf<Player?>(null) }
    var selectedGuess by remember { mutableStateOf<CardType?>(null) }

    val allTargets = if (canTargetSelf && localPlayer != null) {
        availableTargets + localPlayer
    } else {
        availableTargets
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playing: ${cardType.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cardType.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(text = cardType.getEmoji(), style = MaterialTheme.typography.headlineMedium)
            }

            Divider()

            when (cardType) {
                CardType.GUARD -> {
                    GuardActionSelector(
                        targets = allTargets,
                        selectedTarget = selectedTarget,
                        selectedGuess = selectedGuess,
                        onTargetSelected = { selectedTarget = it },
                        onGuessSelected = { selectedGuess = it }
                    )
                }
                CardType.PRIEST, CardType.BARON, CardType.KING -> {
                    TargetOnlySelector(
                        targets = allTargets,
                        selectedTarget = selectedTarget,
                        onTargetSelected = { selectedTarget = it },
                        label = when (cardType) {
                            CardType.PRIEST -> "Select a player to see their card:"
                            CardType.BARON -> "Select a player to compare cards with:"
                            CardType.KING -> "Select a player to trade cards with:"
                            else -> "Select target:"
                        }
                    )
                }
                CardType.PRINCE -> {
                    TargetOnlySelector(
                        targets = allTargets,
                        selectedTarget = selectedTarget,
                        onTargetSelected = { selectedTarget = it },
                        label = "Select a player to discard their card (can be yourself):",
                        showSelfOption = canTargetSelf
                    )
                }
                CardType.HANDMAID, CardType.COUNTESS, CardType.PRINCESS -> {
                    // No target needed
                    Text(
                        text = when (cardType) {
                            CardType.HANDMAID -> "You will be protected until your next turn."
                            CardType.COUNTESS -> "The Countess will be discarded."
                            CardType.PRINCESS -> "Warning: Playing the Princess will eliminate you!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cardType == CardType.PRINCESS) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val action = createAction(cardType, selectedTarget, selectedGuess)
                        if (action != null) {
                            onActionSelected(action)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isActionValid(cardType, selectedTarget, selectedGuess, allTargets.isEmpty())
                ) {
                    Text("Play Card")
                }
            }
        }
    }
}

@Composable
private fun GuardActionSelector(
    targets: List<Player>,
    selectedTarget: Player?,
    selectedGuess: CardType?,
    onTargetSelected: (Player) -> Unit,
    onGuessSelected: (CardType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (targets.isEmpty()) {
            Text(
                text = "No valid targets available. Card will be played with no effect.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Select target player:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            targets.forEach { player ->
                TargetOption(
                    player = player,
                    isSelected = selectedTarget?.id == player.id,
                    onClick = { onTargetSelected(player) }
                )
            }

            AnimatedVisibility(
                visible = selectedTarget != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Guess their card:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    CardType.guessableCards().forEach { cardType ->
                        CardGuessOption(
                            cardType = cardType,
                            isSelected = selectedGuess == cardType,
                            onClick = { onGuessSelected(cardType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetOnlySelector(
    targets: List<Player>,
    selectedTarget: Player?,
    onTargetSelected: (Player) -> Unit,
    label: String,
    showSelfOption: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (targets.isEmpty()) {
            Text(
                text = "No valid targets available. Card will be played with no effect.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            targets.forEach { player ->
                TargetOption(
                    player = player,
                    isSelected = selectedTarget?.id == player.id,
                    onClick = { onTargetSelected(player) }
                )
            }
        }
    }
}

@Composable
private fun TargetOption(
    player: Player,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            RoyalGold.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            PlayerAvatar(name = player.name, size = 32)
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun CardGuessOption(
    cardType: CardType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            RoyalGold.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Text(text = cardType.getEmoji())
            Text(
                text = "${cardType.value} - ${cardType.displayName}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun isActionValid(
    cardType: CardType,
    selectedTarget: Player?,
    selectedGuess: CardType?,
    noTargets: Boolean
): Boolean = when (cardType) {
    CardType.GUARD -> noTargets || (selectedTarget != null && selectedGuess != null)
    CardType.PRIEST, CardType.BARON, CardType.KING -> noTargets || selectedTarget != null
    CardType.PRINCE -> noTargets || selectedTarget != null
    CardType.HANDMAID, CardType.COUNTESS, CardType.PRINCESS -> true
}

private fun createAction(
    cardType: CardType,
    selectedTarget: Player?,
    selectedGuess: CardType?
): GameAction? = when (cardType) {
    CardType.GUARD -> {
        if (selectedTarget != null && selectedGuess != null) {
            GameAction.PlayGuard(selectedTarget.id, selectedGuess)
        } else {
            GameAction.NoAction
        }
    }
    CardType.PRIEST -> selectedTarget?.let { GameAction.PlayPriest(it.id) } ?: GameAction.NoAction
    CardType.BARON -> selectedTarget?.let { GameAction.PlayBaron(it.id) } ?: GameAction.NoAction
    CardType.HANDMAID -> GameAction.PlayHandmaid
    CardType.PRINCE -> selectedTarget?.let { GameAction.PlayPrince(it.id) } ?: GameAction.NoAction
    CardType.KING -> selectedTarget?.let { GameAction.PlayKing(it.id) } ?: GameAction.NoAction
    CardType.COUNTESS -> GameAction.PlayCountess
    CardType.PRINCESS -> GameAction.PlayPrincess
}
