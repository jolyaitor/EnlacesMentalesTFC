package com.example.enlacesmentales.ui.screens.juegos.semanticos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CamposSemanticosViewModel : ViewModel() {

    // Lista de categorías visibles
    val categorias = MutableStateFlow(listOf("Animales", "Objetos"))

    // Palabras originales con sus categorías
    val palabrasOriginales = listOf(
        "Gato" to "Animales",
        "Perro" to "Animales",
        "León" to "Animales",
        "Mesa" to "Objetos",
        "Silla" to "Objetos",
        "Elefante" to "Animales",
        "Vaso" to "Objetos",
        "Tigre" to "Animales",
        "Cuchara" to "Objetos",
        "Caballo" to "Animales"
    )

    // Palabras aún no clasificadas
    private val _palabras = MutableStateFlow(palabrasOriginales.map { it.first })
    val palabras: StateFlow<List<String>> = _palabras

    // Palabras mal colocadas para animación de vibración
    private val _palabrasFallidas = MutableStateFlow(setOf<String>())
    val palabrasFallidas: StateFlow<Set<String>> = _palabrasFallidas

    // Palabras clasificadas por categoría
    private val _matchedWords = MutableStateFlow(
        categorias.value.associateWith { mutableListOf<String>() }
    )
    val matchedWords: StateFlow<Map<String, List<String>>> = _matchedWords

    // Temporizador en segundos
    private val _tiempo = MutableStateFlow(0)
    val tiempo: StateFlow<Int> = _tiempo

    // Control del tiempo
    private var contadorJob: Job? = null

    // Estado de finalización
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
    }

    fun reset() {
        _palabras.value = palabrasOriginales.map { it.first }
        _palabrasFallidas.value = emptySet()
        _matchedWords.value = categorias.value.associateWith { mutableListOf() }
        _tiempo.value = 0
        iniciarContador()
    }

    private fun iniciarContador() {
        contadorJob?.cancel()
        contadorJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _tiempo.update { it + 1 }

                // Detener el contador si se completa
                if (_matchedWords.value.values.sumOf { it.size } == palabrasOriginales.size) {
                    break
                }
            }
        }
    }
}
