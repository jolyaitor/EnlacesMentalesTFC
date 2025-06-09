package com.example.enlacesmentales.ui.screens.juegos.semanticos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CamposSemanticosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progresoRepository: ProgresoRepository
) : ViewModel() {

    private val dificultad: String = savedStateHandle["dificultad"] ?: "facil"
    private var resultadoGuardado = false

    val categorias = MutableStateFlow(listOf("Animales", "Objetos"))

    val palabrasOriginales = listOf(
        "Gato" to "Animales", "Perro" to "Animales", "León" to "Animales",
        "Mesa" to "Objetos", "Silla" to "Objetos", "Elefante" to "Animales",
        "Vaso" to "Objetos", "Tigre" to "Animales", "Cuchara" to "Objetos",
        "Caballo" to "Animales"
    )

    private val _palabras = MutableStateFlow(palabrasOriginales.map { it.first })
    val palabras: StateFlow<List<String>> = _palabras

    private val _palabrasFallidas = MutableStateFlow(setOf<String>())
    val palabrasFallidas: StateFlow<Set<String>> = _palabrasFallidas

    private val _matchedWords = MutableStateFlow(
        categorias.value.associateWith { mutableListOf<String>() }
    )
    val matchedWords: StateFlow<Map<String, List<String>>> = _matchedWords

    private val _tiempo = MutableStateFlow(0)
    val tiempo: StateFlow<Int> = _tiempo

    private var contadorJob: Job? = null

    val isCompleted: StateFlow<Boolean> = _matchedWords
        .map { it.values.sumOf { lista -> lista.size } == palabrasOriginales.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        reset()
    }

    fun onDrop(palabra: String, categoria: String) {
        val correcta = palabrasOriginales.find { it.first == palabra }?.second == categoria

        if (correcta) {
            _matchedWords.update { current ->
                val updated = current.toMutableMap()
                val list = updated[categoria]?.toMutableList() ?: mutableListOf()

                if (!list.contains(palabra)) {
                    list.add(palabra)
                    updated[categoria] = list
                    _palabras.update { it - palabra }
                    _palabrasFallidas.update { it - palabra }
                }
                updated
            }
        } else {
            _palabrasFallidas.update { it + palabra }
            viewModelScope.launch {
                delay(1000)
                _palabrasFallidas.update { it - palabra }
            }
        }

        // Verificar si se completó el juego
        checkCompletion()
    }

    private fun checkCompletion() {
        val total = palabrasOriginales.size
        val clasificadas = _matchedWords.value.values.sumOf { it.size }
        if (clasificadas == total && !resultadoGuardado) {
            resultadoGuardado = true
            guardarResultadoFinal()
        }
    }

    fun reset() {
        _palabras.value = palabrasOriginales.map { it.first }
        _palabrasFallidas.value = emptySet()
        _matchedWords.value = categorias.value.associateWith { mutableListOf() }
        _tiempo.value = 0
        resultadoGuardado = false
        iniciarContador()
    }

    private fun iniciarContador() {
        contadorJob?.cancel()
        contadorJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _tiempo.update { it + 1 }

                if (_matchedWords.value.values.sumOf { it.size } == palabrasOriginales.size) break
            }
        }
    }

    private fun guardarResultadoFinal() {
        viewModelScope.launch {
            val result = GameResult(
                gameName = "CamposSemanticos_$dificultad",
                tiempoEnSegundos = _tiempo.value,
                dificultad = dificultad,
                timeStamp = System.currentTimeMillis()
            )
            progresoRepository.guardarResultadoJuego(result)
        }
    }
}
