package com.example.enlacesmentales.ui.screens.juegos.semanticos.dificil

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CamposSemanticosDificilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progresoRepository: ProgresoRepository
) : ViewModel() {

    private val dificultad: String = savedStateHandle["dificultad"] ?: "facil"
    private var resultadoGuardado = false
    private var lastSavedTimestamp: Long? = null

    private val categoriasList = listOf("Objetos", "Trabajos", "Asignaturas")

    private val _categorias = MutableStateFlow(categoriasList)
    val categorias: StateFlow<List<String>> = _categorias.asStateFlow()

    private val palabrasOriginales = listOf(
        // Objetos
        "Silla" to "Objetos", "Mesa" to "Objetos", "Vaso" to "Objetos",
        "Lápiz" to "Objetos", "Portátil" to "Objetos", "Teléfono" to "Objetos",
        // Trabajos
        "Médico" to "Trabajos", "Profesor" to "Trabajos", "Bombero" to "Trabajos",
        "Ingeniero" to "Trabajos", "Cocinero" to "Trabajos", "Abogado" to "Trabajos",
        // Asignaturas
        "Matemáticas" to "Asignaturas", "Historia" to "Asignaturas", "Ciencias" to "Asignaturas",
        "Física" to "Asignaturas", "Lengua" to "Asignaturas", "Inglés" to "Asignaturas"
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
        if (_isCompleted.value) {
            stopTimer()
            if (!resultadoGuardado) {
                guardarResultadoFinal()
                resultadoGuardado = true
            }
        }
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

    private fun guardarResultadoFinal() {
        viewModelScope.launch {
            var timestamp = System.currentTimeMillis()
            if (lastSavedTimestamp == timestamp) {
                timestamp += 1
            }
            lastSavedTimestamp = timestamp

            val result = GameResult(
                gameName = "CamposSemanticos",
                tiempoEnSegundos = _tiempo.value,
                dificultad = dificultad,
                timeStamp = timestamp
            )
            progresoRepository.guardarResultadoJuego(result)
        }
    }
}
