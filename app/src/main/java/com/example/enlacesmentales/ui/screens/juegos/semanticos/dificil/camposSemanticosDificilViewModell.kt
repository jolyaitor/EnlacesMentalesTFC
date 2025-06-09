package com.example.enlacesmentales.ui.screens.juegos.semanticos.dificil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CamposSemanticosDificilViewModel : ViewModel() {

    private val categoriasList = listOf("Objetos", "Trabajos", "Asignaturas")

    private val _categorias = MutableStateFlow(categoriasList)
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()

    private val palabrasOriginales = listOf(
        // Objetos
        "Silla" to "Objetos",
        "Mesa" to "Objetos",
        "Vaso" to "Objetos",
        "Lápiz" to "Objetos",
        "Portátil" to "Objetos",
        "Teléfono" to "Objetos",

        // Trabajos
        "Médico" to "Trabajos",
        "Profesor" to "Trabajos",
        "Bombero" to "Trabajos",
        "Ingeniero" to "Trabajos",
        "Cocinero" to "Trabajos",
        "Abogado" to "Trabajos",

        // Asignaturas
        "Matemáticas" to "Asignaturas",
        "Historia" to "Asignaturas",
        "Ciencias" to "Asignaturas",
        "Física" to "Asignaturas",
        "Lengua" to "Asignaturas",
        "Inglés" to "Asignaturas"
    )

    private val _palabras = MutableStateFlow(palabrasOriginales.map { it.first }.shuffled())
    val palabras: StateFlow<List<String>> = _palabras.asStateFlow()

    private val _matchedWords = MutableStateFlow(
        categoriasList.associateWith { mutableListOf<String>() }.toMutableMap()
    )
    val matchedWords: StateFlow<Map<String, List<String>>> = _matchedWords.asStateFlow()

    private val _palabrasFallidas = MutableStateFlow(setOf<String>())
    val palabrasFallidas: StateFlow<Set<String>> = _palabrasFallidas.asStateFlow()

    private val _tiempo = MutableStateFlow(0)
    val tiempo: StateFlow<Int> = _tiempo.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    private var timerRunning = true

    init {
        startTimer()
    }

    fun onDrop(word: String, categoria: String) {
        val correcta = palabrasOriginales.find { it.first == word }?.second == categoria
        if (correcta) {
            _matchedWords.update { current ->
                val updated = current.toMutableMap()
                updated[categoria]?.add(word)
                updated
            }
            _palabras.update { it - word }
            _palabrasFallidas.update { it - word }
        } else {
            _palabrasFallidas.update { it + word }
            viewModelScope.launch {
                delay(1000)
                _palabrasFallidas.update { it - word }
            }
        }
        checkCompletion()
    }

    private fun checkCompletion() {
        val total = palabrasOriginales.size
        val classified = _matchedWords.value.values.sumOf { it.size }
        _isCompleted.value = classified == total
        if (_isCompleted.value) stopTimer()
    }

    fun reset() {
        _palabras.value = palabrasOriginales.map { it.first }.shuffled()
        _matchedWords.value =
            categoriasList.associateWith { mutableListOf<String>() }.toMutableMap()
        _palabrasFallidas.value = emptySet()
        _tiempo.value = 0
        _isCompleted.value = false
        timerRunning = true
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (timerRunning) {
                delay(1000)
                _tiempo.update { it + 1 }
            }
        }
    }

    private fun stopTimer() {
        timerRunning = false
    }
}
