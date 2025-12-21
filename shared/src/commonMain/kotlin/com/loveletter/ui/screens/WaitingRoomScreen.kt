package com.loveletter.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveletter.game.Player
import com.loveletter.ui.components.*

sealed class WaitingRoomEvent {
    object StartGame : WaitingRoomEvent()
    object LeaveRoom : WaitingRoomEvent()
    data class KickPlayer(val playerId: String) : WaitingRoomEvent()
}

@Composable
fun WaitingRoomScreen(
    roomName: String,
    roomId: String,
    players: List<Player>,
    isHost: Boolean,
    localPlayerId: String,
    minPlayers: Int = 2,
    maxPlayers: Int = 4,
    onEvent: (WaitingRoomEvent) -> Unit
) {
    val canStart = players.size >= minPlayers

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GameHeader(
            title = roomName,
            subtitle = "Room ID: ${roomId.take(8)}...",
            onBack = { onEvent(WaitingRoomEvent.LeaveRoom) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Players section
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Players",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${players.size}/$maxPlayers",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(players) { player ->
                            WaitingPlayerCard(
                                player = player,
                                isLocalPlayer = player.id == localPlayerId,
                                isHost = isHost,
                                showKick = isHost && player.id != localPlayerId,
                                onKick = { onEvent(WaitingRoomEvent.KickPlayer(player.id)) }
                            )
                        }

                        // Empty slots
                        items(maxPlayers - players.size) { idx ->
                            EmptyPlayerSlot(slotNumber = players.size + idx + 1)
                        }
                    }
                }
            }

            // Waiting message
            AnimatedVisibility(
                visible = !canStart,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Waiting for ${minPlayers - players.size} more player(s)...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Start button (host only)
            if (isHost) {
                LoveLetterButton(
                    text = "Start Game",
                    onClick = { onEvent(WaitingRoomEvent.StartGame) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canStart
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Waiting for host to start the game...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Leave button
            LoveLetterButton(
                text = "Leave Room",
                onClick = { onEvent(WaitingRoomEvent.LeaveRoom) },
                modifier = Modifier.fillMaxWidth(),
                isSecondary = true
            )
        }
    }
}

@Composable
fun WaitingPlayerCard(
    player: Player,
    isLocalPlayer: Boolean,
    isHost: Boolean,
    showKick: Boolean,
    onKick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocalPlayer) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatar(
                    name = player.name,
                    isHost = player.isHost,
                    size = 48
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (player.isHost) {
                            Text("👑", fontSize = 14.sp)
                        }
                        if (isLocalPlayer) {
                            Text(
                                text = "(You)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = if (player.isHost) "Host" else "Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showKick) {
                IconButton(onClick = onKick) {
                    Text("❌")
                }
            }
        }
    }
}

@Composable
fun EmptyPlayerSlot(slotNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Waiting for Player $slotNumber...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
