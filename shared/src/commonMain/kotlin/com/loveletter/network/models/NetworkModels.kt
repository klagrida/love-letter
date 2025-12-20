package com.loveletter.network.models

import com.loveletter.game.GameAction
import com.loveletter.game.GameState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    val id: String? = null,
    val name: String,
    @SerialName("host_id")
    val hostId: String,
    @SerialName("host_name")
    val hostName: String,
    @SerialName("max_players")
    val maxPlayers: Int = 4,
    @SerialName("current_players")
    val currentPlayers: Int = 1,
    @SerialName("is_private")
    val isPrivate: Boolean = false,
    val password: String? = null,
    val status: String = "WAITING",
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class GameDto(
    val id: String? = null,
    @SerialName("room_id")
    val roomId: String,
    val state: String, // JSON serialized GameState
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class PlayerDto(
    val id: String? = null,
    @SerialName("user_id")
    val oderId: String,
    @SerialName("game_id")
    val gameId: String,
    val name: String,
    @SerialName("is_host")
    val isHost: Boolean = false,
    @SerialName("is_connected")
    val isConnected: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class GameActionDto(
    val id: String? = null,
    @SerialName("game_id")
    val gameId: String,
    @SerialName("player_id")
    val playerId: String,
    @SerialName("action_type")
    val actionType: String,
    @SerialName("action_data")
    val actionData: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
sealed class RealtimeMessage {
    @Serializable
    @SerialName("player_joined")
    data class PlayerJoined(
        val playerId: String,
        val playerName: String
    ) : RealtimeMessage()

    @Serializable
    @SerialName("player_left")
    data class PlayerLeft(
        val playerId: String
    ) : RealtimeMessage()

    @Serializable
    @SerialName("game_started")
    data class GameStarted(
        val gameId: String
    ) : RealtimeMessage()

    @Serializable
    @SerialName("game_state_updated")
    data class GameStateUpdated(
        val state: String // JSON serialized GameState
    ) : RealtimeMessage()

    @Serializable
    @SerialName("action_performed")
    data class ActionPerformed(
        val playerId: String,
        val action: String
    ) : RealtimeMessage()
}
