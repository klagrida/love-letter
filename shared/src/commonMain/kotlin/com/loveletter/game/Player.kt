package com.loveletter.game

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    var hand: Card? = null,
    var isProtected: Boolean = false,
    var isEliminated: Boolean = false,
    val discardedCards: MutableList<Card> = mutableListOf(),
    var tokensOfAffection: Int = 0,
    var isConnected: Boolean = true
) {
    val isActive: Boolean get() = !isEliminated && isConnected

    fun discardValue(): Int = discardedCards.sumOf { it.value }

    fun resetForNewRound() {
        hand = null
        isProtected = false
        isEliminated = false
        discardedCards.clear()
    }

    fun canBeTargeted(byPlayer: Player): Boolean {
        return !isEliminated && !isProtected && id != byPlayer.id
    }
}
