@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.loveletter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveletter.ui.components.LoveLetterButton
import com.loveletter.ui.components.LoveLetterTextField
import com.loveletter.ui.theme.LoveRed
import com.loveletter.ui.theme.LovePink

sealed class MainMenuEvent {
    data class QuickPlay(val playerName: String) : MainMenuEvent()
    data class CreateRoom(val playerName: String) : MainMenuEvent()
    data class JoinRoom(val playerName: String) : MainMenuEvent()
    data class LocalGame(val playerNames: List<String>) : MainMenuEvent()
    object HowToPlay : MainMenuEvent()
}

@Composable
fun MainMenuScreen(
    onEvent: (MainMenuEvent) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var showLocalGameSetup by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LovePink, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Text(
                text = "💌",
                fontSize = 72.sp
            )

            Text(
                text = "Love Letter",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = LoveRed
            )

            Text(
                text = "A game of risk, deduction, and luck",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Player name input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LoveLetterTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = "Your Name"
                    )

                    // Quick Play
                    LoveLetterButton(
                        text = "Quick Play",
                        onClick = { onEvent(MainMenuEvent.QuickPlay(playerName.ifEmpty { "Player" })) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Create Room
                        LoveLetterButton(
                            text = "Create Room",
                            onClick = { onEvent(MainMenuEvent.CreateRoom(playerName.ifEmpty { "Player" })) },
                            modifier = Modifier.weight(1f),
                            isSecondary = true
                        )

                        // Join Room
                        LoveLetterButton(
                            text = "Join Room",
                            onClick = { onEvent(MainMenuEvent.JoinRoom(playerName.ifEmpty { "Player" })) },
                            modifier = Modifier.weight(1f),
                            isSecondary = true
                        )
                    }

                    // Local Game
                    OutlinedButton(
                        onClick = { showLocalGameSetup = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Local Multiplayer (Pass & Play)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How to Play
            TextButton(
                onClick = { onEvent(MainMenuEvent.HowToPlay) }
            ) {
                Text(
                    text = "📖 How to Play",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Local Game Setup Dialog
        if (showLocalGameSetup) {
            LocalGameSetupDialog(
                onDismiss = { showLocalGameSetup = false },
                onStart = { names ->
                    showLocalGameSetup = false
                    onEvent(MainMenuEvent.LocalGame(names))
                }
            )
        }
    }
}

@Composable
fun LocalGameSetupDialog(
    onDismiss: () -> Unit,
    onStart: (List<String>) -> Unit
) {
    var numPlayers by remember { mutableStateOf(2) }
    val playerNames = remember { mutableStateListOf("Player 1", "Player 2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Local Game Setup",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Number of Players:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (2..4).forEach { num ->
                        FilterChip(
                            selected = numPlayers == num,
                            onClick = {
                                numPlayers = num
                                while (playerNames.size < num) {
                                    playerNames.add("Player ${playerNames.size + 1}")
                                }
                                while (playerNames.size > num) {
                                    playerNames.removeAt(playerNames.size - 1)
                                }
                            },
                            label = { Text("$num") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                playerNames.forEachIndexed { idx, name ->
                    OutlinedTextField(
                        value = name,
                        onValueChange = { playerNames[idx] = it },
                        label = { Text("Player ${idx + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(playerNames.toList()) }
            ) {
                Text("Start Game")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
