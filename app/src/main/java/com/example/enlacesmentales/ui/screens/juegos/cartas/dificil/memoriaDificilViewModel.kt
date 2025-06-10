package com.example.enlacesmentales.ui.screens.juegos.cartas.dificil

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Carta(
    val id: Int,
    val valor: String,
    val estaBocaArriba: Boolean = false,
    val esPareja: Boolean = false
)

@HiltViewModel
class MemoriaDificilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val progresoRepository: ProgresoRepository
) : ViewModel() {


    private var lastSavedTime: Long? = null

    private val dificultad: String = savedStateHandle["dificultad"] ?: "dificil"
    private var resultadoGuardado = false

    private val emojis = listOf("🐶", "🐱", "🦊", "🐰", "🐼", "🐸","🐲","🐒")

    private val _cartas = MutableStateFlow<List<Carta>>(emptyList())
    val cartas: StateFlow<List<Carta>> = _cartas

    private val _tiempo = MutableStateFlow(0)
    val tiempo: StateFlow<Int> = _tiempo

    private val _completado = MutableStateFlow(false)
    val completado: StateFlow<Boolean> = _completado

    private var indicePrimeraCarta: Int? = null
    private var bloqueado = false

    init {
        iniciarJuego()
    }

    private fun iniciarJuego() {
        val pares = (emojis + emojis).shuffled()
            .mapIndexed { index, emoji -> Carta(id = index, valor = emoji) }
        _cartas.value = pares
        iniciarTemporizador()
    }

    private fun iniciarTemporizador() {
        viewModelScope.launch {
            while (!_completado.value) {
                delay(1000)
                _tiempo.update { it + 1 }
            }
            if (!resultadoGuardado) {
                guardarResultadoFinal()
                resultadoGuardado = true
            }
        }
    }

    fun alSeleccionarCarta(indice: Int) {
        if (bloqueado) return
        val cartaActual = _cartas.value[indice]
        if (cartaActual.estaBocaArriba || cartaActual.esPareja) return

        val nuevasCartas = _cartas.value.toMutableList()
        nuevasCartas[indice] = cartaActual.copy(estaBocaArriba = true)
        _cartas.value = nuevasCartas

        if (indicePrimeraCarta == null) {
            indicePrimeraCarta = indice
        } else {
            val i1 = indicePrimeraCarta!!
            val i2 = indice
            val carta1 = _cartas.value[i1]
            val carta2 = _cartas.value[i2]

            bloqueado = true
            viewModelScope.launch {
                delay(800)

                val cartasModificadas = _cartas.value.toMutableList()
                if (carta1.valor == carta2.valor) {
                    cartasModificadas[i1] = carta1.copy(esPareja = true)
                    cartasModificadas[i2] = carta2.copy(esPareja = true)
                } else {
                    cartasModificadas[i1] = carta1.copy(estaBocaArriba = false)
                    cartasModificadas[i2] = carta2.copy(estaBocaArriba = false)
                }
                _cartas.value = cartasModificadas

                indicePrimeraCarta = null
                bloqueado = false

                if (_cartas.value.all { it.esPareja }) {
                    _completado.value = true
                }
            }
        }
    }

    private fun guardarResultadoFinal() {
        viewModelScope.launch {
            var timestamp = System.currentTimeMillis()

            // Evita timestamps duplicados si el usuario juega varias veces muy rápido
            if (lastSavedTime == timestamp) {
                timestamp += 1
            }
            lastSavedTime = timestamp

            val result = GameResult(
                gameName = "Memoria",
                tiempoEnSegundos = _tiempo.value,
                dificultad = dificultad,
                timeStamp = timestamp
            )
            progresoRepository.guardarResultadoJuego(result)
        }
    }

}
