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

data class Personaje(val nombre: String, val posicion: Offset)

@HiltViewModel
class EncuentraPersonajeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progresoRepository: ProgresoRepository
) : ViewModel() {

    private val dificultad: String = savedStateHandle["dificultad"] ?: "facil"
    private var resultadoGuardado = false

    private val _objetivos = listOf(
        Personaje("Manu", Offset(0.026953313f, 0.3583053f)),
        Personaje("Milo", Offset(0.33041024f, 0.3496263f)),
        Personaje("Azul", Offset(0.70419943f, 0.1804919f)),
        Personaje("Luli", Offset(0.35257387f, 0.15159722f)),
        Personaje("Franco", Offset(0.74754167f, 0.7021837f))
    )
    val objetivos: List<Personaje> = _objetivos

    private val _encontrados = MutableStateFlow<List<Personaje>>(emptyList())
    val encontrados: StateFlow<List<Personaje>> = _encontrados

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
                    if (!resultadoGuardado) {
                        guardarResultadoFinal()
                        resultadoGuardado = true
                    }
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
        val yaEncontrado =
            _encontrados.value.any { it.posicion.distanceTo(normalizedOffset) < 0.03f }
        if (yaEncontrado) return

        val encontrado = _objetivos.find { it.posicion.distanceTo(normalizedOffset) < 0.03f }
        if (encontrado != null) {
            _encontrados.update { it + encontrado }

            if (estanTodosEncontrados() && !resultadoGuardado) {
                guardarResultadoFinal()
                resultadoGuardado = true
            }
        }
    }

    fun estanTodosEncontrados(): Boolean {
        return _encontrados.value.size == _objetivos.size
    }

    private fun guardarResultadoFinal() {
        viewModelScope.launch {
            val result = GameResult(
                gameName = "EncuentraObjeto_$dificultad",
                tiempoEnSegundos = _tiempoTranscurrido.value,
                dificultad = dificultad,
                timeStamp = System.currentTimeMillis()
            )
            progresoRepository.guardarResultadoJuego(result)
        }
    }
}

// Función auxiliar para calcular distancia
private fun Offset.distanceTo(other: Offset): Float {
    return sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
}
