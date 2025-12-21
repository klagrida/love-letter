@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.loveletter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.loveletter.game.GameRoom
import com.loveletter.game.RoomStatus
import com.loveletter.ui.components.*
import com.loveletter.ui.theme.LoveRed

sealed class LobbyEvent {
    data class CreateRoom(val name: String, val maxPlayers: Int, val isPrivate: Boolean, val password: String?) : LobbyEvent()
    data class JoinRoom(val roomId: String, val password: String?) : LobbyEvent()
    object Refresh : LobbyEvent()
    object Back : LobbyEvent()
}

@Composable
fun LobbyScreen(
    rooms: List<GameRoom>,
    isLoading: Boolean,
    onEvent: (LobbyEvent) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<GameRoom?>(null) }
    var passwordInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GameHeader(
            title = "Game Lobby",
            subtitle = "${rooms.size} rooms available",
            onBack = { onEvent(LobbyEvent.Back) }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (rooms.isEmpty() && !isLoading) {
                EmptyState(
                    title = "No Games Available",
                    message = "Be the first to create a game room!",
                    emoji = "🎮",
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    LoveLetterButton(
                        text = "Create Room",
                        onClick = { showCreateDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rooms) { room ->
                        RoomCard(
                            room = room,
                            onJoin = {
                                if (room.isPrivate) {
                                    selectedRoom = room
                                } else {
                                    onEvent(LobbyEvent.JoinRoom(room.id, null))
                                }
                            }
                        )
                    }
                }
            }

            // Refresh button
            FloatingActionButton(
                onClick = { onEvent(LobbyEvent.Refresh) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text("🔄")
            }

            LoadingOverlay(isLoading = isLoading)
        }

        // Bottom bar with create button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoveLetterButton(
                    text = "Create New Room",
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Create Room Dialog
    if (showCreateDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, maxPlayers, isPrivate, password ->
                showCreateDialog = false
                onEvent(LobbyEvent.CreateRoom(name, maxPlayers, isPrivate, password))
            }
        )
    }

    // Password Dialog
    selectedRoom?.let { room ->
        PasswordDialog(
            roomName = room.name,
            password = passwordInput,
            onPasswordChange = { passwordInput = it },
            onDismiss = {
                selectedRoom = null
                passwordInput = ""
            },
            onJoin = {
                onEvent(LobbyEvent.JoinRoom(room.id, passwordInput))
                selectedRoom = null
                passwordInput = ""
            }
        )
    }
}

@Composable
fun RoomCard(
    room: GameRoom,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (room.isPrivate) {
                        Text("🔒")
                    }
                }
                Text(
                    text = "Host: ${room.hostName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${room.currentPlayers}/${room.maxPlayers} players",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onJoin,
                enabled = room.currentPlayers < room.maxPlayers && room.status == RoomStatus.WAITING,
                colors = ButtonDefaults.buttonColors(containerColor = LoveRed)
            ) {
                Text("Join")
            }
        }
    }
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, Boolean, String?) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf(4) }
    var isPrivate by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Max Players:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..4).forEach { num ->
                        FilterChip(
                            selected = maxPlayers == num,
                            onClick = { maxPlayers = num },
                            label = { Text("$num") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Private Room")
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                }

                if (isPrivate) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        roomName.ifEmpty { "Game Room" },
                        maxPlayers,
                        isPrivate,
                        if (isPrivate) password else null
                    )
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PasswordDialog(
    roomName: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Room: $roomName")
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onJoin) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
