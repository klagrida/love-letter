package com.loveletter.game

import kotlinx.serialization.Serializable

@Serializable
sealed class GameAction {
    @Serializable
    data class PlayGuard(val targetPlayerId: String, val guessedCard: CardType) : GameAction()

    @Serializable
    data class PlayPriest(val targetPlayerId: String) : GameAction()

    @Serializable
    data class PlayBaron(val targetPlayerId: String) : GameAction()

    @Serializable
    data object PlayHandmaid : GameAction()

    @Serializable
    data class PlayPrince(val targetPlayerId: String) : GameAction()

    @Serializable
    data class PlayKing(val targetPlayerId: String) : GameAction()

    @Serializable
    data object PlayCountess : GameAction()

    @Serializable
    data object PlayPrincess : GameAction()

    @Serializable
    data object NoAction : GameAction()

    fun requiresTarget(): Boolean = when (this) {
        is PlayGuard, is PlayPriest, is PlayBaron, is PlayPrince, is PlayKing -> true
        else -> false
    }
}

@Serializable
data class GameActionResult(
    val success: Boolean,
    val message: String,
    val revealedCard: Card? = null,
    val eliminatedPlayerId: String? = null
)
