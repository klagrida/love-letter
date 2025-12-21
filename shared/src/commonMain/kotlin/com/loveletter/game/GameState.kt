package com.loveletter.game

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    WAITING_FOR_PLAYERS,
    STARTING,
    DRAW_PHASE,
    PLAY_PHASE,
    ROUND_END,
    GAME_END
}

@Serializable
data class GameState(
    val gameId: String,
    val players: List<Player>,
    val deck: MutableList<Card> = mutableListOf(),
    val currentPlayerIndex: Int = 0,
    val drawnCard: Card? = null,
    val burnedCard: Card? = null,
    val revealedCards: List<Card> = emptyList(),
    val phase: GamePhase = GamePhase.WAITING_FOR_PLAYERS,
    val roundNumber: Int = 1,
    val roundWinner: String? = null,
    val gameWinner: String? = null,
    val lastAction: String = "",
    val lastActionResult: GameActionResult? = null,
    val requiredTokensToWin: Int = 4,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)

    val activePlayers: List<Player>
        get() = players.filter { it.isActive }

    val isGameOver: Boolean
        get() = phase == GamePhase.GAME_END || gameWinner != null

    val isRoundOver: Boolean
        get() = phase == GamePhase.ROUND_END || roundWinner != null

    val deckSize: Int
        get() = deck.size

    fun getPlayer(playerId: String): Player? = players.find { it.id == playerId }

    fun getPlayerIndex(playerId: String): Int = players.indexOfFirst { it.id == playerId }

    fun isPlayerTurn(playerId: String): Boolean =
        currentPlayer?.id == playerId && phase == GamePhase.PLAY_PHASE

    companion object {
        fun calculateRequiredTokens(playerCount: Int): Int = when (playerCount) {
            2 -> 7
            3 -> 5
            4 -> 4
            else -> 4
        }
    }
}

@Serializable
data class GameRoom(
    val id: String,
    val name: String,
    val hostId: String,
    val hostName: String,
    val maxPlayers: Int = 4,
    val currentPlayers: Int = 1,
    val isPrivate: Boolean = false,
    val password: String? = null,
    val status: RoomStatus = RoomStatus.WAITING,
    val createdAt: Long = 0
)

@Serializable
enum class RoomStatus {
    WAITING,
    IN_GAME,
    FINISHED
}
