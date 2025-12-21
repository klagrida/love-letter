package com.loveletter.game

import kotlinx.serialization.Serializable

@Serializable
enum class CardType(val value: Int, val count: Int, val displayName: String) {
    GUARD(1, 5, "Guard"),
    PRIEST(2, 2, "Priest"),
    BARON(3, 2, "Baron"),
    HANDMAID(4, 2, "Handmaid"),
    PRINCE(5, 2, "Prince"),
    KING(6, 1, "King"),
    COUNTESS(7, 1, "Countess"),
    PRINCESS(8, 1, "Princess");

    fun getDescription(): String = when (this) {
        GUARD -> "Name a non-Guard card. If the target has that card, they are eliminated."
        PRIEST -> "Look at another player's hand."
        BARON -> "Compare hands with another player. Lower card is eliminated."
        HANDMAID -> "You cannot be targeted until your next turn."
        PRINCE -> "Choose any player (including yourself) to discard their hand and draw a new card."
        KING -> "Trade hands with another player."
        COUNTESS -> "Must be discarded if caught with King or Prince."
        PRINCESS -> "If you discard this card, you are eliminated."
    }

    fun getEmoji(): String = when (this) {
        GUARD -> "🗡️"
        PRIEST -> "🙏"
        BARON -> "⚔️"
        HANDMAID -> "🛡️"
        PRINCE -> "👑"
        KING -> "♔"
        COUNTESS -> "👸"
        PRINCESS -> "💎"
    }

    companion object {
        fun fromValue(value: Int): CardType? = entries.find { it.value == value }
        fun guessableCards(): List<CardType> = entries.filter { it != GUARD }
    }
}
