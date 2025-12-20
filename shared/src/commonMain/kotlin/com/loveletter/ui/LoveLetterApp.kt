package com.loveletter.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.loveletter.game.*
import com.loveletter.network.AuthRepository
import com.loveletter.network.GameRepository
import com.loveletter.network.MultiplayerManager
import com.loveletter.ui.screens.*
import com.loveletter.ui.theme.LoveLetterTheme
import kotlinx.coroutines.launch

sealed class Screen {
    object MainMenu : Screen()
    object Lobby : Screen()
    object HowToPlay : Screen()
    data class WaitingRoom(val roomId: String) : Screen()
    data class Game(val isOnline: Boolean = false) : Screen()
}

@Composable
fun LoveLetterApp() {
    val scope = rememberCoroutineScope()

    // Repositories (in real app, use dependency injection)
    val authRepository = remember { AuthRepository() }
    val gameRepository = remember { GameRepository() }
    val multiplayerManager = remember { MultiplayerManager(authRepository, gameRepository) }

    // Navigation state
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    // Game states
    var localGame by remember { mutableStateOf<LoveLetterEngine?>(null) }
    var localGameState by remember { mutableStateOf<GameState?>(null) }
    var localPlayerId by remember { mutableStateOf("local_player") }

    // Online states
    val onlineRoom by multiplayerManager.currentRoom.collectAsState(initial = null)
    val onlinePlayers by multiplayerManager.players.collectAsState(initial = emptyList())
    val onlineGameState by multiplayerManager.gameState.collectAsState(initial = null)

    // Lobby state
    var lobbyRooms by remember { mutableStateOf<List<GameRoom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Player name
    var playerName by remember { mutableStateOf("Player") }

    LoveLetterTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                }
            ) { screen ->
                when (screen) {
                    is Screen.MainMenu -> {
                        MainMenuScreen(
                            onEvent = { event ->
                                when (event) {
                                    is MainMenuEvent.QuickPlay -> {
                                        playerName = event.playerName
                                        scope.launch {
                                            authRepository.signInAnonymously(event.playerName)
                                            isLoading = true
                                            lobbyRooms = multiplayerManager.getAvailableRooms()
                                            isLoading = false
                                            currentScreen = Screen.Lobby
                                        }
                                    }
                                    is MainMenuEvent.CreateRoom -> {
                                        playerName = event.playerName
                                        scope.launch {
                                            authRepository.signInAnonymously(event.playerName)
                                            currentScreen = Screen.Lobby
                                        }
                                    }
                                    is MainMenuEvent.JoinRoom -> {
                                        playerName = event.playerName
                                        scope.launch {
                                            authRepository.signInAnonymously(event.playerName)
                                            isLoading = true
                                            lobbyRooms = multiplayerManager.getAvailableRooms()
                                            isLoading = false
                                            currentScreen = Screen.Lobby
                                        }
                                    }
                                    is MainMenuEvent.LocalGame -> {
                                        // Start local game
                                        val players = event.playerNames.mapIndexed { idx, name ->
                                            Player(
                                                id = "player_$idx",
                                                name = name,
                                                isHost = idx == 0
                                            )
                                        }
                                        localPlayerId = "player_0"
                                        val engine = LoveLetterEngine("local", players)
                                        engine.startGame()
                                        localGame = engine
                                        localGameState = engine.getCurrentState()
                                        currentScreen = Screen.Game(isOnline = false)
                                    }
                                    is MainMenuEvent.HowToPlay -> {
                                        currentScreen = Screen.HowToPlay
                                    }
                                }
                            }
                        )
                    }

                    is Screen.Lobby -> {
                        LobbyScreen(
                            rooms = lobbyRooms,
                            isLoading = isLoading,
                            onEvent = { event ->
                                when (event) {
                                    is LobbyEvent.CreateRoom -> {
                                        scope.launch {
                                            isLoading = true
                                            val result = multiplayerManager.createRoom(
                                                name = event.name,
                                                maxPlayers = event.maxPlayers,
                                                isPrivate = event.isPrivate,
                                                password = event.password
                                            )
                                            isLoading = false
                                            result.onSuccess { room ->
                                                currentScreen = Screen.WaitingRoom(room.id)
                                            }
                                        }
                                    }
                                    is LobbyEvent.JoinRoom -> {
                                        scope.launch {
                                            isLoading = true
                                            val result = multiplayerManager.joinRoom(
                                                roomId = event.roomId,
                                                password = event.password
                                            )
                                            isLoading = false
                                            result.onSuccess { room ->
                                                currentScreen = Screen.WaitingRoom(room.id)
                                            }
                                        }
                                    }
                                    is LobbyEvent.Refresh -> {
                                        scope.launch {
                                            isLoading = true
                                            lobbyRooms = multiplayerManager.getAvailableRooms()
                                            isLoading = false
                                        }
                                    }
                                    is LobbyEvent.Back -> {
                                        currentScreen = Screen.MainMenu
                                    }
                                }
                            }
                        )
                    }

                    is Screen.WaitingRoom -> {
                        val room = onlineRoom
                        if (room != null) {
                            WaitingRoomScreen(
                                roomName = room.name,
                                roomId = room.id,
                                players = onlinePlayers,
                                isHost = room.hostId == multiplayerManager.getMyPlayerId(),
                                localPlayerId = multiplayerManager.getMyPlayerId() ?: "",
                                maxPlayers = room.maxPlayers,
                                onEvent = { event ->
                                    when (event) {
                                        is WaitingRoomEvent.StartGame -> {
                                            scope.launch {
                                                val result = multiplayerManager.startGame()
                                                result.onSuccess {
                                                    currentScreen = Screen.Game(isOnline = true)
                                                }
                                            }
                                        }
                                        is WaitingRoomEvent.LeaveRoom -> {
                                            scope.launch {
                                                multiplayerManager.leaveRoom()
                                                currentScreen = Screen.Lobby
                                            }
                                        }
                                        is WaitingRoomEvent.KickPlayer -> {
                                            // Handle kick (host only)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    is Screen.Game -> {
                        val gameState = if (screen.isOnline) {
                            onlineGameState
                        } else {
                            localGameState
                        }

                        val playerId = if (screen.isOnline) {
                            multiplayerManager.getMyPlayerId() ?: ""
                        } else {
                            localPlayerId
                        }

                        gameState?.let { state ->
                            GameScreen(
                                gameState = state,
                                localPlayerId = playerId,
                                onEvent = { event ->
                                    when (event) {
                                        is GameScreenEvent.DrawCard -> {
                                            if (screen.isOnline) {
                                                scope.launch {
                                                    multiplayerManager.drawCard()
                                                }
                                            } else {
                                                localGame?.drawCard(localPlayerId)
                                                localGameState = localGame?.getCurrentState()
                                            }
                                        }
                                        is GameScreenEvent.PlayCard -> {
                                            if (screen.isOnline) {
                                                scope.launch {
                                                    multiplayerManager.playCard(event.card, event.action)
                                                }
                                            } else {
                                                localGame?.playCard(localPlayerId, event.card, event.action)
                                                localGameState = localGame?.getCurrentState()
                                            }
                                        }
                                        is GameScreenEvent.NextRound -> {
                                            if (screen.isOnline) {
                                                scope.launch {
                                                    multiplayerManager.startNextRound()
                                                }
                                            } else {
                                                localGame?.startNextRound()
                                                localGameState = localGame?.getCurrentState()
                                            }
                                        }
                                        is GameScreenEvent.NewGame -> {
                                            if (screen.isOnline) {
                                                scope.launch {
                                                    multiplayerManager.leaveRoom()
                                                    currentScreen = Screen.Lobby
                                                }
                                            } else {
                                                currentScreen = Screen.MainMenu
                                            }
                                        }
                                        is GameScreenEvent.LeaveGame -> {
                                            if (screen.isOnline) {
                                                scope.launch {
                                                    multiplayerManager.leaveRoom()
                                                    currentScreen = Screen.Lobby
                                                }
                                            } else {
                                                localGame = null
                                                localGameState = null
                                                currentScreen = Screen.MainMenu
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }

                    is Screen.HowToPlay -> {
                        HowToPlayScreen(
                            onBack = { currentScreen = Screen.MainMenu }
                        )
                    }
                }
            }
        }
    }
}
