package com.loveletter.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoveLetterEngine(
    private val gameId: String,
    initialPlayers: List<Player>
) {
    private val _state = MutableStateFlow(
        GameState(
            gameId = gameId,
            players = initialPlayers,
            requiredTokensToWin = GameState.calculateRequiredTokens(initialPlayers.size),
            phase = GamePhase.WAITING_FOR_PLAYERS
        )
    )
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun getCurrentState(): GameState = _state.value

    fun startGame() {
        if (_state.value.players.size < 2) {
            return
        }
        startNewRound()
    }

    fun startNewRound() {
        val currentState = _state.value

        // Reset players for new round
        val resetPlayers = currentState.players.map { player ->
            player.copy().apply { resetForNewRound() }
        }

        // Create and shuffle deck
        val deck = Deck.createShuffled()

        // Burn one card face down
        val burnedCard = deck.draw()

        // For 2 players, reveal 3 additional cards
        val revealedCards = if (resetPlayers.size == 2) {
            deck.burnForTwoPlayer()
        } else {
            emptyList()
        }

        // Deal one card to each player
        resetPlayers.forEach { player ->
            player.hand = deck.draw()
        }

        _state.value = currentState.copy(
            players = resetPlayers,
            deck = deck.toMutableList(),
            drawnCard = null,
            burnedCard = burnedCard,
            revealedCards = revealedCards,
            phase = GamePhase.DRAW_PHASE,
            currentPlayerIndex = 0,
            roundWinner = null,
            lastAction = "Round ${currentState.roundNumber} started!"
        )
    }

    fun drawCard(playerId: String): Boolean {
        val currentState = _state.value
        val currentPlayer = currentState.currentPlayer ?: return false

        if (currentPlayer.id != playerId) return false
        if (currentState.phase != GamePhase.DRAW_PHASE) return false
        if (currentState.deck.isEmpty()) {
            endRound()
            return false
        }

        // Clear protection from previous turn
        currentPlayer.isProtected = false

        val drawnCard = currentState.deck.removeAt(0)

        _state.value = currentState.copy(
            drawnCard = drawnCard,
            phase = GamePhase.PLAY_PHASE,
            players = currentState.players.map {
                if (it.id == playerId) it.copy(isProtected = false) else it
            }
        )
        return true
    }

    fun canPlayCard(playerId: String, card: Card): Boolean {
        val currentState = _state.value
        val player = currentState.getPlayer(playerId) ?: return false
        val hand = player.hand ?: return false
        val drawn = currentState.drawnCard ?: return false

        // Check if Countess must be discarded
        if (hand.type == CardType.COUNTESS || drawn.type == CardType.COUNTESS) {
            val otherCard = if (hand.type == CardType.COUNTESS) drawn else hand
            if (otherCard.type == CardType.KING || otherCard.type == CardType.PRINCE) {
                return card.type == CardType.COUNTESS
            }
        }

        return card.id == hand.id || card.id == drawn.id
    }

    fun mustPlayCountess(playerId: String): Boolean {
        val currentState = _state.value
        val player = currentState.getPlayer(playerId) ?: return false
        val hand = player.hand ?: return false
        val drawn = currentState.drawnCard ?: return false

        val hasCountess = hand.type == CardType.COUNTESS || drawn.type == CardType.COUNTESS
        val hasKingOrPrince = hand.type == CardType.KING || hand.type == CardType.PRINCE ||
                drawn.type == CardType.KING || drawn.type == CardType.PRINCE

        return hasCountess && hasKingOrPrince
    }

    fun playCard(playerId: String, card: Card, action: GameAction): GameActionResult {
        val currentState = _state.value
        val player = currentState.getPlayer(playerId)
            ?: return GameActionResult(false, "Player not found")

        if (currentState.currentPlayer?.id != playerId) {
            return GameActionResult(false, "Not your turn")
        }

        if (currentState.phase != GamePhase.PLAY_PHASE) {
            return GameActionResult(false, "Cannot play card now")
        }

        if (!canPlayCard(playerId, card)) {
            return GameActionResult(false, "Cannot play this card")
        }

        val hand = player.hand ?: return GameActionResult(false, "No card in hand")
        val drawn = currentState.drawnCard ?: return GameActionResult(false, "No drawn card")

        // Update player's hand
        val updatedPlayers = currentState.players.map { p ->
            if (p.id == playerId) {
                p.copy().apply {
                    this.hand = if (card.id == hand.id) drawn else hand
                    this.discardedCards.add(card)
                }
            } else {
                p.copy()
            }
        }.toMutableList()

        // Execute card effect
        val result = executeCardEffect(card, action, playerId, updatedPlayers)

        // Check if player discarded Princess
        if (card.type == CardType.PRINCESS) {
            val playerIndex = updatedPlayers.indexOfFirst { it.id == playerId }
            if (playerIndex >= 0) {
                updatedPlayers[playerIndex] = updatedPlayers[playerIndex].copy(isEliminated = true)
            }
        }

        _state.value = currentState.copy(
            players = updatedPlayers,
            drawnCard = null,
            lastAction = "${player.name} played ${card.displayName}",
            lastActionResult = result
        )

        // Move to next player or end round
        moveToNextPlayer()

        return result
    }

    private fun executeCardEffect(
        card: Card,
        action: GameAction,
        playerId: String,
        players: MutableList<Player>
    ): GameActionResult {
        val currentPlayer = players.find { it.id == playerId }
            ?: return GameActionResult(false, "Player not found")

        return when (card.type) {
            CardType.GUARD -> executeGuard(action, players)
            CardType.PRIEST -> executePriest(action, players)
            CardType.BARON -> executeBaron(action, currentPlayer, players)
            CardType.HANDMAID -> executeHandmaid(currentPlayer, players)
            CardType.PRINCE -> executePrince(action, players, _state.value)
            CardType.KING -> executeKing(action, currentPlayer, players)
            CardType.COUNTESS -> GameActionResult(true, "Countess discarded")
            CardType.PRINCESS -> GameActionResult(true, "${currentPlayer.name} is eliminated!")
        }
    }

    private fun executeGuard(action: GameAction, players: MutableList<Player>): GameActionResult {
        if (action !is GameAction.PlayGuard) {
            // Check if there are no valid targets
            val hasTargets = players.any { !it.isEliminated && !it.isProtected }
            return if (!hasTargets) {
                GameActionResult(true, "No valid targets for Guard")
            } else {
                GameActionResult(false, "Guard requires a target and guess")
            }
        }

        val target = players.find { it.id == action.targetPlayerId }
            ?: return GameActionResult(false, "Target not found")

        if (target.isEliminated || target.isProtected) {
            return GameActionResult(false, "Invalid target")
        }

        return if (target.hand?.type == action.guessedCard) {
            val targetIndex = players.indexOf(target)
            players[targetIndex] = target.copy(isEliminated = true)
            GameActionResult(
                true,
                "${target.name} is eliminated! They had ${action.guessedCard.displayName}",
                eliminatedPlayerId = target.id
            )
        } else {
            GameActionResult(true, "${target.name} does not have ${action.guessedCard.displayName}")
        }
    }

    private fun executePriest(action: GameAction, players: MutableList<Player>): GameActionResult {
        if (action !is GameAction.PlayPriest) {
            val hasTargets = players.any { !it.isEliminated && !it.isProtected }
            return if (!hasTargets) {
                GameActionResult(true, "No valid targets for Priest")
            } else {
                GameActionResult(false, "Priest requires a target")
            }
        }

        val target = players.find { it.id == action.targetPlayerId }
            ?: return GameActionResult(false, "Target not found")

        if (target.isEliminated || target.isProtected) {
            return GameActionResult(false, "Invalid target")
        }

        return GameActionResult(
            true,
            "${target.name} has ${target.hand?.displayName}",
            revealedCard = target.hand
        )
    }

    private fun executeBaron(
        action: GameAction,
        currentPlayer: Player,
        players: MutableList<Player>
    ): GameActionResult {
        if (action !is GameAction.PlayBaron) {
            val hasTargets = players.any { !it.isEliminated && !it.isProtected && it.id != currentPlayer.id }
            return if (!hasTargets) {
                GameActionResult(true, "No valid targets for Baron")
            } else {
                GameActionResult(false, "Baron requires a target")
            }
        }

        val target = players.find { it.id == action.targetPlayerId }
            ?: return GameActionResult(false, "Target not found")

        if (target.isEliminated || target.isProtected) {
            return GameActionResult(false, "Invalid target")
        }

        val playerValue = currentPlayer.hand?.value ?: 0
        val targetValue = target.hand?.value ?: 0

        return when {
            playerValue > targetValue -> {
                val targetIndex = players.indexOf(target)
                players[targetIndex] = target.copy(isEliminated = true)
                GameActionResult(
                    true,
                    "${target.name} is eliminated! (${target.hand?.displayName} vs ${currentPlayer.hand?.displayName})",
                    eliminatedPlayerId = target.id
                )
            }
            targetValue > playerValue -> {
                val currentIndex = players.indexOf(currentPlayer)
                players[currentIndex] = currentPlayer.copy(isEliminated = true)
                GameActionResult(
                    true,
                    "${currentPlayer.name} is eliminated! (${currentPlayer.hand?.displayName} vs ${target.hand?.displayName})",
                    eliminatedPlayerId = currentPlayer.id
                )
            }
            else -> GameActionResult(true, "Tie! No one is eliminated (both had ${playerValue})")
        }
    }

    private fun executeHandmaid(currentPlayer: Player, players: MutableList<Player>): GameActionResult {
        val currentIndex = players.indexOf(currentPlayer)
        players[currentIndex] = currentPlayer.copy(isProtected = true)
        return GameActionResult(true, "${currentPlayer.name} is protected until their next turn")
    }

    private fun executePrince(
        action: GameAction,
        players: MutableList<Player>,
        state: GameState
    ): GameActionResult {
        if (action !is GameAction.PlayPrince) {
            return GameActionResult(false, "Prince requires a target")
        }

        val target = players.find { it.id == action.targetPlayerId }
            ?: return GameActionResult(false, "Target not found")

        if (target.isEliminated) {
            return GameActionResult(false, "Invalid target")
        }

        // Can target self, but cannot target protected players (unless self)
        if (target.isProtected && target.id != state.currentPlayer?.id) {
            return GameActionResult(false, "Target is protected")
        }

        val discardedCard = target.hand
        val targetIndex = players.indexOf(target)

        if (discardedCard?.type == CardType.PRINCESS) {
            players[targetIndex] = target.copy(
                isEliminated = true,
                discardedCards = (target.discardedCards + discardedCard).toMutableList()
            )
            return GameActionResult(
                true,
                "${target.name} discarded the Princess and is eliminated!",
                eliminatedPlayerId = target.id
            )
        }

        // Draw new card
        val newCard = if (state.deck.isNotEmpty()) {
            state.deck.removeAt(0)
        } else {
            state.burnedCard
        }

        players[targetIndex] = target.copy(
            hand = newCard,
            discardedCards = (target.discardedCards + discardedCard).filterNotNull().toMutableList()
        )

        return GameActionResult(
            true,
            "${target.name} discarded ${discardedCard?.displayName} and drew a new card"
        )
    }

    private fun executeKing(
        action: GameAction,
        currentPlayer: Player,
        players: MutableList<Player>
    ): GameActionResult {
        if (action !is GameAction.PlayKing) {
            val hasTargets = players.any { !it.isEliminated && !it.isProtected && it.id != currentPlayer.id }
            return if (!hasTargets) {
                GameActionResult(true, "No valid targets for King")
            } else {
                GameActionResult(false, "King requires a target")
            }
        }

        val target = players.find { it.id == action.targetPlayerId }
            ?: return GameActionResult(false, "Target not found")

        if (target.isEliminated || target.isProtected) {
            return GameActionResult(false, "Invalid target")
        }

        // Swap hands
        val currentIndex = players.indexOf(currentPlayer)
        val targetIndex = players.indexOf(target)

        val currentHand = currentPlayer.hand
        val targetHand = target.hand

        players[currentIndex] = currentPlayer.copy(hand = targetHand)
        players[targetIndex] = target.copy(hand = currentHand)

        return GameActionResult(true, "${currentPlayer.name} traded cards with ${target.name}")
    }

    private fun moveToNextPlayer() {
        val currentState = _state.value
        val activePlayers = currentState.players.filter { !it.isEliminated }

        // Check if round should end
        if (activePlayers.size <= 1) {
            endRound()
            return
        }

        if (currentState.deck.isEmpty()) {
            endRound()
            return
        }

        // Find next active player
        var nextIndex = (currentState.currentPlayerIndex + 1) % currentState.players.size
        while (currentState.players[nextIndex].isEliminated) {
            nextIndex = (nextIndex + 1) % currentState.players.size
        }

        _state.value = currentState.copy(
            currentPlayerIndex = nextIndex,
            phase = GamePhase.DRAW_PHASE
        )
    }

    private fun endRound() {
        val currentState = _state.value
        val activePlayers = currentState.players.filter { !it.isEliminated }

        val winner = if (activePlayers.size == 1) {
            activePlayers.first()
        } else {
            // Highest card wins, ties broken by discard pile value
            activePlayers.maxByOrNull { player ->
                val cardValue = player.hand?.value ?: 0
                val discardValue = player.discardValue()
                cardValue * 1000 + discardValue
            }
        }

        val updatedPlayers = currentState.players.map { player ->
            if (player.id == winner?.id) {
                player.copy(tokensOfAffection = player.tokensOfAffection + 1)
            } else {
                player
            }
        }

        val gameWinnerId = updatedPlayers.find {
            it.tokensOfAffection >= currentState.requiredTokensToWin
        }?.id

        _state.value = currentState.copy(
            players = updatedPlayers,
            phase = if (gameWinnerId != null) GamePhase.GAME_END else GamePhase.ROUND_END,
            roundWinner = winner?.id,
            gameWinner = gameWinnerId,
            lastAction = "${winner?.name} wins the round!"
        )
    }

    fun startNextRound() {
        val currentState = _state.value
        if (currentState.gameWinner == null) {
            _state.value = currentState.copy(roundNumber = currentState.roundNumber + 1)
            startNewRound()
        }
    }

    fun getAvailableTargets(playerId: String, excludeSelf: Boolean = true): List<Player> {
        val currentState = _state.value
        val currentPlayer = currentState.getPlayer(playerId) ?: return emptyList()

        return currentState.players.filter { player ->
            !player.isEliminated &&
                    !player.isProtected &&
                    (!excludeSelf || player.id != currentPlayer.id)
        }
    }

    fun hasValidTargets(playerId: String, excludeSelf: Boolean = true): Boolean {
        return getAvailableTargets(playerId, excludeSelf).isNotEmpty()
    }

    fun addPlayer(player: Player): Boolean {
        val currentState = _state.value
        if (currentState.phase != GamePhase.WAITING_FOR_PLAYERS) return false
        if (currentState.players.size >= 4) return false
        if (currentState.players.any { it.id == player.id }) return false

        _state.value = currentState.copy(
            players = currentState.players + player,
            requiredTokensToWin = GameState.calculateRequiredTokens(currentState.players.size + 1)
        )
        return true
    }

    fun removePlayer(playerId: String): Boolean {
        val currentState = _state.value
        if (currentState.phase != GamePhase.WAITING_FOR_PLAYERS) return false

        _state.value = currentState.copy(
            players = currentState.players.filter { it.id != playerId },
            requiredTokensToWin = GameState.calculateRequiredTokens(currentState.players.size - 1)
        )
        return true
    }

    fun updateState(newState: GameState) {
        _state.value = newState
    }
}
