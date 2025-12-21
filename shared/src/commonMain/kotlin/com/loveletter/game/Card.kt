package com.loveletter.game

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val type: CardType,
    val id: String = "${type.name}_${kotlin.random.Random.nextInt(100000)}"
) {
    val value: Int get() = type.value
    val displayName: String get() = type.displayName
    val description: String get() = type.getDescription()
    val emoji: String get() = type.getEmoji()

    override fun toString(): String = "$emoji $displayName ($value)"
}
