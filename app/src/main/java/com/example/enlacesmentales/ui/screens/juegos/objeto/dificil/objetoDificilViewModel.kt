package com.example.enlacesmentales.ui.screens.juegos.encuentrapersonaje

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.sqrt

data class ObjetoDificil(val nombre: String, val posicion: Offset)

@HiltViewModel
class EncuentraPersonajeDificilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progresoRepository: ProgresoRepository
) : ViewModel() {

    private val _objetivos = listOf(
        ObjetoDificil("Manu", Offset(0.7215367f, 0.4912067f)),
        ObjetoDificil("Milo", Offset(0.7514159f, 0.23549409f)),
        ObjetoDificil("Azul", Offset(0.29091024f, 0.6631988f)),
        ObjetoDificil("Luli", Offset(0.5809051f, 0.3597518f)),
        ObjetoDificil("Franco", Offset(0.11557401f, 0.7152728f)),
        ObjetoDificil("Corona", Offset(0.9132572f, 0.40166494f)),
        ObjetoDificil("Garrote", Offset(0.6223102f, 0.4955462f)),
        ObjetoDificil("Trompeta", Offset(0.380516f, 0.71093327f)),
        ObjetoDificil("Hacha", Offset(0.061626926f, 0.5230649f)),
        ObjetoDificil("Cofre", Offset(0.5173053f, 0.31064144f)),
        ObjetoDificil("Escudo", Offset(0.78608954f, 0.4522571f)),
        ObjetoDificil("Botella", Offset(0.073184796f, 0.4840095f))
    )

    private val dificultad: String = savedStateHandle["dificultad"] ?: "facil"
    private var lastSavedTimestamp: Long? = null

    val objetivos: List<ObjetoDificil> = _objetivos

    private val _encontrados = MutableStateFlow<List<ObjetoDificil>>(emptyList())
    val encontrados: StateFlow<List<ObjetoDificil>> = _encontrados

    private val _tiempoTranscurrido = MutableStateFlow(0)
    val tiempoTranscurrido: StateFlow<Int> = _tiempoTranscurrido

    private val _temporizadorActivo = MutableStateFlow(true)
    val temporizadorActivo: StateFlow<Boolean> = _temporizadorActivo

    private var temporizadorJob: Job? = null

    init {
        iniciarTemporizador()
    }

    private fun iniciarTemporizador() {
        temporizadorJob?.cancel()
        temporizadorJob = viewModelScope.launch {
            while (_temporizadorActivo.value) {
                delay(1000)
                _tiempoTranscurrido.update { it + 1 }

                if (estanTodosEncontrados()) {
                    detenerTemporizador()
                    guardarResultadoFinal()
                }
            }
        }
    }

    private fun detenerTemporizador() {
        _temporizadorActivo.value = false
        temporizadorJob?.cancel()
        temporizadorJob = null
    }

    fun verificarToque(normalizedOffset: Offset) {
        val yaEncontrado = _encontrados.value.any {
            it.posicion.distanceTo(normalizedOffset) < 0.03f
        }
        if (yaEncontrado) return

        val encontrado = _objetivos.find {
            it.posicion.distanceTo(normalizedOffset) < 0.03f
        }
        if (encontrado != null) {
            _encontrados.update { it + encontrado }
        }
    }

    fun estanTodosEncontrados(): Boolean {
        return _encontrados.value.size == _objetivos.size
    }

    fun guardarResultadoFinal() {
        viewModelScope.launch {
            var timestamp = System.currentTimeMillis()
            if (lastSavedTimestamp == timestamp) {
                timestamp += 1
            }
            lastSavedTimestamp = timestamp

            val result = GameResult(
                gameName = "EncuentraObjeto",
                tiempoEnSegundos = tiempoTranscurrido.value,
                dificultad = dificultad,
                timeStamp = timestamp
            )
            progresoRepository.guardarResultadoJuego(result)
        }
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
}
