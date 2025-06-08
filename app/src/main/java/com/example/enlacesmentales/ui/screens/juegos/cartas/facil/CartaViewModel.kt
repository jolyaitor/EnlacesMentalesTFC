package com.example.enlacesmentales.ui.screens.juegos.cartas.facil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: Int,
    val content: String,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)

class MemoriaViewModel : ViewModel() {

    private val _cards = MutableStateFlow(generateCards())
    val cards: StateFlow<List<MemoryCard>> = _cards

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime

    private var flippedCardIndex: Int? = null

    init {
        viewModelScope.launch {
            while (!_isCompleted.value) {
                delay(1000L)
                _elapsedTime.update { it + 1 }
            }
        }
    }

    fun onCardClicked(index: Int) {
        val currentCards = _cards.value
        val clickedCard = currentCards[index]

        if (clickedCard.isFaceUp || clickedCard.isMatched) return

        val updatedCards = currentCards.toMutableList()
        updatedCards[index] = clickedCard.copy(isFaceUp = true)
        _cards.value = updatedCards

        if (flippedCardIndex == null) {
            flippedCardIndex = index
        } else {
            val firstIndex = flippedCardIndex!!
            val firstCard = currentCards[firstIndex]
            val secondCard = clickedCard

            if (firstCard.content == secondCard.content) {
                updatedCards[firstIndex] = firstCard.copy(isMatched = true)
                updatedCards[index] = secondCard.copy(isMatched = true)
                _cards.value = updatedCards
                flippedCardIndex = null
            } else {
                viewModelScope.launch {
                    delay(800L)
                    updatedCards[firstIndex] = firstCard.copy(isFaceUp = false)
                    updatedCards[index] = secondCard.copy(isFaceUp = false)
                    _cards.value = updatedCards
                    flippedCardIndex = null
                }
            }
        }
        if (updatedCards.all { it.isMatched }) {
            _isCompleted.value = true
        }
    }

    companion object {
        private fun generateCards(): List<MemoryCard> {
            val contents = listOf("🍕","🍌","🌶️")
            val paired = (contents + contents).shuffled()
            return paired.mapIndexed { index, content -> MemoryCard(id = index, content = content) }
        }
    }
}
