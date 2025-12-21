package com.loveletter.network

import com.loveletter.game.GameAction
import com.loveletter.game.GameState
import com.loveletter.game.Player
import com.loveletter.game.GameRoom
import com.loveletter.game.RoomStatus
import com.loveletter.network.models.GameActionDto
import com.loveletter.network.models.GameDto
import com.loveletter.network.models.PlayerDto
import com.loveletter.network.models.RealtimeMessage
import com.loveletter.network.models.RoomDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameRepository {
    private val client = SupabaseClientProvider.client
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _gameUpdates = MutableSharedFlow<GameState>()
    val gameUpdates: Flow<GameState> = _gameUpdates.asSharedFlow()

    private val _roomUpdates = MutableSharedFlow<List<GameRoom>>()
    val roomUpdates: Flow<List<GameRoom>> = _roomUpdates.asSharedFlow()

    // Room Operations
    suspend fun createRoom(
        name: String,
        hostId: String,
        hostName: String,
        maxPlayers: Int = 4,
        isPrivate: Boolean = false,
        password: String? = null
    ): Result<GameRoom> = runCatching {
        val roomDto = RoomDto(
            name = name,
            hostId = hostId,
            hostName = hostName,
            maxPlayers = maxPlayers,
            isPrivate = isPrivate,
            password = password
        )

        val result = client.from(SupabaseConfig.TABLE_ROOMS)
            .insert(roomDto) {
                select()
            }
            .decodeSingle<RoomDto>()

        result.toGameRoom()
    }

    suspend fun getRooms(): Result<List<GameRoom>> = runCatching {
        client.from(SupabaseConfig.TABLE_ROOMS)
            .select {
                filter {
                    eq("status", "WAITING")
                    eq("is_private", false)
                }
            }
            .decodeList<RoomDto>()
            .map { it.toGameRoom() }
    }

    suspend fun getRoom(roomId: String): Result<GameRoom> = runCatching {
        client.from(SupabaseConfig.TABLE_ROOMS)
            .select {
                filter {
                    eq("id", roomId)
                }
            }
            .decodeSingle<RoomDto>()
            .toGameRoom()
    }

    suspend fun joinRoom(roomId: String): Result<GameRoom> = runCatching {
        val room = getRoom(roomId).getOrThrow()

        if (room.currentPlayers >= room.maxPlayers) {
            throw Exception("Room is full")
        }

        client.from(SupabaseConfig.TABLE_ROOMS)
            .update({
                set("current_players", room.currentPlayers + 1)
            }) {
                filter {
                    eq("id", roomId)
                }
                select()
            }
            .decodeSingle<RoomDto>()
            .toGameRoom()
    }

    suspend fun leaveRoom(roomId: String, playerId: String, isHost: Boolean): Result<Unit> = runCatching {
        if (isHost) {
            // Delete the room if host leaves
            client.from(SupabaseConfig.TABLE_ROOMS)
                .delete {
                    filter {
                        eq("id", roomId)
                    }
                }
        } else {
            val room = getRoom(roomId).getOrThrow()
            client.from(SupabaseConfig.TABLE_ROOMS)
                .update({
                    set("current_players", (room.currentPlayers - 1).coerceAtLeast(1))
                }) {
                    filter {
                        eq("id", roomId)
                    }
                }
        }
    }

    suspend fun updateRoomStatus(roomId: String, status: RoomStatus): Result<Unit> = runCatching {
        client.from(SupabaseConfig.TABLE_ROOMS)
            .update({
                set("status", status.name)
            }) {
                filter {
                    eq("id", roomId)
                }
            }
    }

    // Game Operations
    suspend fun createGame(roomId: String, initialState: GameState): Result<String> = runCatching {
        val gameDto = GameDto(
            roomId = roomId,
            state = json.encodeToString(initialState)
        )

        val result = client.from(SupabaseConfig.TABLE_GAMES)
            .insert(gameDto) {
                select()
            }
            .decodeSingle<GameDto>()

        result.id ?: throw Exception("Failed to create game")
    }

    suspend fun getGame(gameId: String): Result<GameState> = runCatching {
        val gameDto = client.from(SupabaseConfig.TABLE_GAMES)
            .select {
                filter {
                    eq("id", gameId)
                }
            }
            .decodeSingle<GameDto>()

        json.decodeFromString<GameState>(gameDto.state)
    }

    suspend fun updateGameState(gameId: String, state: GameState): Result<Unit> = runCatching {
        client.from(SupabaseConfig.TABLE_GAMES)
            .update({
                set("state", json.encodeToString(state))
            }) {
                filter {
                    eq("id", gameId)
                }
            }
    }

    suspend fun recordAction(
        gameId: String,
        playerId: String,
        action: GameAction
    ): Result<Unit> = runCatching {
        val actionDto = GameActionDto(
            gameId = gameId,
            playerId = playerId,
            actionType = action::class.simpleName ?: "Unknown",
            actionData = json.encodeToString(action)
        )

        client.from(SupabaseConfig.TABLE_GAME_ACTIONS)
            .insert(actionDto)
    }

    // Realtime Subscriptions
    suspend fun subscribeToRoom(roomId: String, onUpdate: (GameRoom) -> Unit) {
        val channel = client.realtime.channel("room:$roomId")

        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = SupabaseConfig.TABLE_ROOMS
            filter = "id=eq.$roomId"
        }.collect { change ->
            val roomDto = change.decodeRecord<RoomDto>()
            onUpdate(roomDto.toGameRoom())
        }

        channel.subscribe()
    }

    suspend fun subscribeToGame(gameId: String, onUpdate: (GameState) -> Unit) {
        val channel = client.realtime.channel("game:$gameId")

        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = SupabaseConfig.TABLE_GAMES
            filter = "id=eq.$gameId"
        }.collect { change ->
            val gameDto = change.decodeRecord<GameDto>()
            val state = json.decodeFromString<GameState>(gameDto.state)
            onUpdate(state)
            _gameUpdates.emit(state)
        }

        channel.subscribe()
    }

    suspend fun subscribeToLobby(onUpdate: (List<GameRoom>) -> Unit) {
        val channel = client.realtime.channel(SupabaseConfig.CHANNEL_LOBBY)

        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = SupabaseConfig.TABLE_ROOMS
        }.collect {
            val rooms = getRooms().getOrDefault(emptyList())
            onUpdate(rooms)
            _roomUpdates.emit(rooms)
        }

        channel.subscribe()
    }

    suspend fun unsubscribe(channelName: String) {
        client.realtime.removeChannel(channelName)
    }

    private fun RoomDto.toGameRoom(): GameRoom = GameRoom(
        id = id ?: "",
        name = name,
        hostId = hostId,
        hostName = hostName,
        maxPlayers = maxPlayers,
        currentPlayers = currentPlayers,
        isPrivate = isPrivate,
        password = password,
        status = RoomStatus.valueOf(status),
        createdAt = 0 // Parse if needed
    )
}
