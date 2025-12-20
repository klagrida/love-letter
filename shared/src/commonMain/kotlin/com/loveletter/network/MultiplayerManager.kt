package com.loveletter.network

import com.loveletter.game.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MultiplayerEvent {
    data class RoomCreated(val room: GameRoom) : MultiplayerEvent()
    data class RoomJoined(val room: GameRoom) : MultiplayerEvent()
    data class PlayerJoined(val player: Player) : MultiplayerEvent()
    data class PlayerLeft(val playerId: String) : MultiplayerEvent()
    data class GameStarted(val gameId: String) : MultiplayerEvent()
    data class GameStateUpdated(val state: GameState) : MultiplayerEvent()
    data class Error(val message: String) : MultiplayerEvent()
}

class MultiplayerManager(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _events = MutableSharedFlow<MultiplayerEvent>()
    val events: Flow<MultiplayerEvent> = _events.asSharedFlow()

    private val _currentRoom = MutableStateFlow<GameRoom?>(null)
    val currentRoom: StateFlow<GameRoom?> = _currentRoom.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private var currentGameId: String? = null
    private var localEngine: LoveLetterEngine? = null

    // Lobby Operations
    suspend fun getAvailableRooms(): List<GameRoom> {
        return gameRepository.getRooms().getOrDefault(emptyList())
    }

    suspend fun createRoom(
        name: String,
        maxPlayers: Int = 4,
        isPrivate: Boolean = false,
        password: String? = null
    ): Result<GameRoom> {
        val user = authRepository.getCurrentUserSync()
            ?: return Result.failure(Exception("Not logged in"))

        return gameRepository.createRoom(
            name = name,
            hostId = user.id,
            hostName = user.displayName,
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            password = password
        ).onSuccess { room ->
            _currentRoom.value = room
            _players.value = listOf(
                Player(
                    id = user.id,
                    name = user.displayName,
                    isHost = true
                )
            )
            subscribeToRoom(room.id)
            _events.emit(MultiplayerEvent.RoomCreated(room))
        }.onFailure { e ->
            _events.emit(MultiplayerEvent.Error(e.message ?: "Failed to create room"))
        }
    }

    suspend fun joinRoom(roomId: String, password: String? = null): Result<GameRoom> {
        val user = authRepository.getCurrentUserSync()
            ?: return Result.failure(Exception("Not logged in"))

        val room = gameRepository.getRoom(roomId).getOrNull()
            ?: return Result.failure(Exception("Room not found"))

        if (room.isPrivate && room.password != password) {
            return Result.failure(Exception("Invalid password"))
        }

        return gameRepository.joinRoom(roomId).onSuccess { updatedRoom ->
            _currentRoom.value = updatedRoom

            val newPlayer = Player(
                id = user.id,
                name = user.displayName,
                isHost = false
            )
            _players.value = _players.value + newPlayer

            subscribeToRoom(roomId)
            _events.emit(MultiplayerEvent.RoomJoined(updatedRoom))
            _events.emit(MultiplayerEvent.PlayerJoined(newPlayer))
        }.onFailure { e ->
            _events.emit(MultiplayerEvent.Error(e.message ?: "Failed to join room"))
        }
    }

    suspend fun leaveRoom() {
        val room = _currentRoom.value ?: return
        val user = authRepository.getCurrentUserSync() ?: return

        gameRepository.leaveRoom(room.id, user.id, room.hostId == user.id)
        gameRepository.unsubscribe("room:${room.id}")

        _currentRoom.value = null
        _players.value = emptyList()
        _gameState.value = null
        currentGameId = null
        localEngine = null
    }

    private fun subscribeToRoom(roomId: String) {
        scope.launch {
            gameRepository.subscribeToRoom(roomId) { room ->
                _currentRoom.value = room
            }
        }
    }

    // Game Operations
    suspend fun startGame(): Result<String> {
        val room = _currentRoom.value
            ?: return Result.failure(Exception("Not in a room"))

        val user = authRepository.getCurrentUserSync()
            ?: return Result.failure(Exception("Not logged in"))

        if (room.hostId != user.id) {
            return Result.failure(Exception("Only the host can start the game"))
        }

        if (_players.value.size < 2) {
            return Result.failure(Exception("Need at least 2 players"))
        }

        // Create game engine with current players
        localEngine = LoveLetterEngine(room.id, _players.value)
        localEngine?.startGame()

        val initialState = localEngine?.getCurrentState()
            ?: return Result.failure(Exception("Failed to initialize game"))

        return gameRepository.createGame(room.id, initialState).onSuccess { gameId ->
            currentGameId = gameId
            _gameState.value = initialState

            gameRepository.updateRoomStatus(room.id, RoomStatus.IN_GAME)
            subscribeToGame(gameId)

            _events.emit(MultiplayerEvent.GameStarted(gameId))
        }.onFailure { e ->
            _events.emit(MultiplayerEvent.Error(e.message ?: "Failed to start game"))
        }
    }

    private fun subscribeToGame(gameId: String) {
        scope.launch {
            gameRepository.subscribeToGame(gameId) { state ->
                _gameState.value = state
                localEngine?.updateState(state)
                scope.launch {
                    _events.emit(MultiplayerEvent.GameStateUpdated(state))
                }
            }
        }
    }

    suspend fun drawCard(): Boolean {
        val user = authRepository.getCurrentUserSync() ?: return false
        val engine = localEngine ?: return false

        val success = engine.drawCard(user.id)
        if (success) {
            syncGameState()
        }
        return success
    }

    suspend fun playCard(card: Card, action: GameAction): GameActionResult {
        val user = authRepository.getCurrentUserSync()
            ?: return GameActionResult(false, "Not logged in")

        val engine = localEngine
            ?: return GameActionResult(false, "Game not initialized")

        val result = engine.playCard(user.id, card, action)

        if (result.success) {
            val gameId = currentGameId
            if (gameId != null) {
                gameRepository.recordAction(gameId, user.id, action)
            }
            syncGameState()
        }

        return result
    }

    suspend fun startNextRound() {
        localEngine?.startNextRound()
        syncGameState()
    }

    private suspend fun syncGameState() {
        val gameId = currentGameId ?: return
        val state = localEngine?.getCurrentState() ?: return

        gameRepository.updateGameState(gameId, state)
        _gameState.value = state
    }

    fun getAvailableTargets(excludeSelf: Boolean = true): List<Player> {
        val user = authRepository.getCurrentUserSync() ?: return emptyList()
        return localEngine?.getAvailableTargets(user.id, excludeSelf) ?: emptyList()
    }

    fun hasValidTargets(excludeSelf: Boolean = true): Boolean {
        val user = authRepository.getCurrentUserSync() ?: return false
        return localEngine?.hasValidTargets(user.id, excludeSelf) ?: false
    }

    fun canPlayCard(card: Card): Boolean {
        val user = authRepository.getCurrentUserSync() ?: return false
        return localEngine?.canPlayCard(user.id, card) ?: false
    }

    fun mustPlayCountess(): Boolean {
        val user = authRepository.getCurrentUserSync() ?: return false
        return localEngine?.mustPlayCountess(user.id) ?: false
    }

    fun isMyTurn(): Boolean {
        val user = authRepository.getCurrentUserSync() ?: return false
        val state = _gameState.value ?: return false
        return state.isPlayerTurn(user.id)
    }

    fun getMyPlayerId(): String? = authRepository.getCurrentUserSync()?.id
}
