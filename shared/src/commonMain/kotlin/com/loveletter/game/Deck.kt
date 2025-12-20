package com.loveletter.game

class Deck {
    private val cards: MutableList<Card> = mutableListOf()

    val size: Int get() = cards.size
    val isEmpty: Boolean get() = cards.isEmpty()
    val isNotEmpty: Boolean get() = cards.isNotEmpty()

    fun initialize(): Deck {
        cards.clear()
        CardType.entries.forEach { cardType ->
            repeat(cardType.count) { index ->
                cards.add(Card(cardType, "${cardType.name}_$index"))
            }
        }
        return this
    }

    fun shuffle(): Deck {
        cards.shuffle()
        return this
    }

    fun draw(): Card? {
        return if (cards.isNotEmpty()) cards.removeAt(0) else null
    }

    fun peek(): Card? = cards.firstOrNull()

    fun burnOne(): Card? = draw()

    fun burnForTwoPlayer(): List<Card> {
        val burned = mutableListOf<Card>()
        repeat(3) {
            draw()?.let { burned.add(it) }
        }
        return burned
    }

    fun toList(): List<Card> = cards.toList()

    fun toMutableList(): MutableList<Card> = cards.toMutableList()

    companion object {
        fun createShuffled(): Deck = Deck().initialize().shuffle()

        fun fromList(cards: List<Card>): Deck {
            val deck = Deck()
            deck.cards.addAll(cards)
            return deck
        }
    }
}
