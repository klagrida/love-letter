package com.loveletter.ui.screens

import androidx.compose.animation.*
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
import com.loveletter.game.*
import com.loveletter.ui.components.*
import com.loveletter.ui.theme.LoveRed

sealed class GameScreenEvent {
    object DrawCard : GameScreenEvent()
    data class SelectCard(val card: Card) : GameScreenEvent()
    data class PlayCard(val card: Card, val action: GameAction) : GameScreenEvent()
    object CancelAction : GameScreenEvent()
    object NextRound : GameScreenEvent()
    object NewGame : GameScreenEvent()
    object LeaveGame : GameScreenEvent()
}

@Composable
fun GameScreen(
    gameState: GameState,
    localPlayerId: String,
    onEvent: (GameScreenEvent) -> Unit
) {
    val currentPlayer = gameState.currentPlayer
    val localPlayer = gameState.getPlayer(localPlayerId)
    val isMyTurn = currentPlayer?.id == localPlayerId

    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var showActionSelector by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Update message based on game state
    LaunchedEffect(gameState.lastAction) {
        if (gameState.lastAction.isNotEmpty()) {
            message = gameState.lastAction
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        GameHeader(
            title = "Love Letter",
            subtitle = "Round ${gameState.roundNumber}",
            onBack = { onEvent(GameScreenEvent.LeaveGame) }
        )

        // Main content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Winner Banner
            gameState.gameWinner?.let { winnerId ->
                val winner = gameState.getPlayer(winnerId)
                WinnerBanner(
                    winnerName = winner?.name ?: "Unknown",
                    isGameWinner = true,
                    onAction = { onEvent(GameScreenEvent.NewGame) }
                )
            }

            // Round Winner Banner
            if (gameState.gameWinner == null) {
                gameState.roundWinner?.let { winnerId ->
                    val winner = gameState.getPlayer(winnerId)
                    WinnerBanner(
                        winnerName = winner?.name ?: "Unknown",
                        isGameWinner = false,
                        onAction = { onEvent(GameScreenEvent.NextRound) }
                    )
                }
            }

            // Game info bar
            if (gameState.roundWinner == null && gameState.gameWinner == null) {
                GameInfoBar(
                    deckCount = gameState.deckSize,
                    currentPlayerName = currentPlayer?.name ?: "",
                    isMyTurn = isMyTurn,
                    revealedCards = gameState.revealedCards
                )

                // Players
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Players",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        PlayersList(
                            players = gameState.players,
                            currentPlayerId = currentPlayer?.id,
                            localPlayerId = localPlayerId
                        )
                    }
                }

                // Message banner
                AnimatedVisibility(
                    visible = message.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    MessageBanner(
                        message = message,
                        type = when {
                            message.contains("eliminated", ignoreCase = true) -> MessageType.WARNING
                            message.contains("wins", ignoreCase = true) -> MessageType.SUCCESS
                            else -> MessageType.INFO
                        }
                    )
                }

                // Action selector dialog
                AnimatedVisibility(
                    visible = showActionSelector && selectedCard != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    selectedCard?.let { card ->
                        val availableTargets = gameState.players.filter { player ->
                            !player.isEliminated && !player.isProtected && player.id != localPlayerId
                        }

                        ActionSelector(
                            cardType = card.type,
                            availableTargets = availableTargets,
                            canTargetSelf = card.type == CardType.PRINCE,
                            localPlayer = localPlayer,
                            onActionSelected = { action ->
                                onEvent(GameScreenEvent.PlayCard(card, action))
                                selectedCard = null
                                showActionSelector = false
                            },
                            onCancel = {
                                selectedCard = null
                                showActionSelector = false
                            }
                        )
                    }
                }
            }
        }

        // Bottom section: Player's hand and actions
        if (gameState.roundWinner == null && gameState.gameWinner == null && localPlayer != null && !localPlayer.isEliminated) {
            PlayerHandSection(
                hand = localPlayer.hand,
                drawnCard = if (isMyTurn) gameState.drawnCard else null,
                isMyTurn = isMyTurn,
                phase = gameState.phase,
                selectedCard = selectedCard,
                onDrawCard = { onEvent(GameScreenEvent.DrawCard) },
                onSelectCard = { card ->
                    selectedCard = card
                    showActionSelector = true
                }
            )
        }
    }
}

@Composable
fun GameInfoBar(
    deckCount: Int,
    currentPlayerName: String,
    isMyTurn: Boolean,
    revealedCards: List<Card>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMyTurn) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMyTurn) "Your Turn!" else "$currentPlayerName's Turn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isMyTurn) LoveRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DeckCounter(count = deckCount)
            }

            // Revealed cards (2-player variant)
            if (revealedCards.isNotEmpty()) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Out of play:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    revealedCards.forEach { card ->
                        MiniCardIndicator(card = card)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerHandSection(
    hand: Card?,
    drawnCard: Card?,
    isMyTurn: Boolean,
    phase: GamePhase,
    selectedCard: Card?,
    onDrawCard: () -> Unit,
    onSelectCard: (Card) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Hand",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hand card
                hand?.let { card ->
                    GameCardView(
                        card = card,
                        isSelected = selectedCard?.id == card.id,
                        isPlayable = isMyTurn && phase == GamePhase.PLAY_PHASE,
                        size = CardSize.MEDIUM,
                        onClick = {
                            if (isMyTurn && phase == GamePhase.PLAY_PHASE) {
                                onSelectCard(card)
                            }
                        }
                    )
                }

                // Drawn card
                drawnCard?.let { card ->
                    GameCardView(
                        card = card,
                        isSelected = selectedCard?.id == card.id,
                        isPlayable = isMyTurn && phase == GamePhase.PLAY_PHASE,
                        size = CardSize.MEDIUM,
                        onClick = {
                            if (isMyTurn && phase == GamePhase.PLAY_PHASE) {
                                onSelectCard(card)
                            }
                        }
                    )
                }

                // Draw card button
                if (isMyTurn && phase == GamePhase.DRAW_PHASE && drawnCard == null) {
                    CardBackView(size = CardSize.MEDIUM)
                }
            }

            // Draw button
            if (isMyTurn && phase == GamePhase.DRAW_PHASE && drawnCard == null) {
                LoveLetterButton(
                    text = "Draw Card",
                    onClick = onDrawCard,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Instructions
            if (isMyTurn) {
                Text(
                    text = when (phase) {
                        GamePhase.DRAW_PHASE -> "Draw a card to begin your turn"
                        GamePhase.PLAY_PHASE -> "Select a card to play"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
