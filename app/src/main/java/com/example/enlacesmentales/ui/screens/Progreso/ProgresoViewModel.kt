package com.example.enlacesmentales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProgresoViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _explicacionFacil = MutableStateFlow("")
    val explicacionFacil: StateFlow<String> = _explicacionFacil

    private val _explicacionDificil = MutableStateFlow("")
    val explicacionDificil: StateFlow<String> = _explicacionDificil

    private val _resultadosFacil = MutableStateFlow<List<Entry>>(emptyList())
    val resultadosFacil: StateFlow<List<Entry>> = _resultadosFacil

    private val _resultadosDificil = MutableStateFlow<List<Entry>>(emptyList())
    val resultadosDificil: StateFlow<List<Entry>> = _resultadosDificil

    private val _juegosDisponibles = MutableStateFlow<List<String>>(emptyList())
    val juegosDisponibles: StateFlow<List<String>> = _juegosDisponibles

    private var juegoActual: String? = null
    private var firstTimestamp: Long = 0L

    init {
        cargarJuegosDisponibles()
        actualizarProgresoPeriodicamente()
    }

    private fun cargarJuegosDisponibles() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).collection("progreso")
            .get()
            .addOnSuccessListener { snapshot ->
                val disponibles = mutableSetOf<String>()
                snapshot.documents.forEach { doc ->
                    doc.reference.collection("registros")
                        .get()
                        .addOnSuccessListener { registros ->
                            registros.forEach { reg ->
                                reg.getString("gameName")?.let { disponibles.add(it) }
                            }
                            _juegosDisponibles.value = disponibles.toList().distinct()
                        }
                }
            }
    }

    fun seleccionarJuego(nombre: String) {
        juegoActual = nombre
        cargarProgresoDeJuego(nombre)
    }

    private fun cargarProgresoDeJuego(nombreJuego: String) {
        val uid = auth.currentUser?.uid ?: return

        val facil = mutableListOf<Entry>()
        val dificil = mutableListOf<Entry>()

        db.collection("usuarios").document(uid)
            .collection("progreso").document(nombreJuego)
            .collection("registros")
            .get()
            .addOnSuccessListener { documents ->
                val sortedDocs = documents.sortedBy { it.getLong("timeStamp") ?: 0L }

                val firstTimestamp = sortedDocs.firstOrNull()?.getLong("timeStamp") ?: 0L

                sortedDocs.forEachIndexed { index, doc ->
                    val dificultad = doc.getString("dificultad") ?: "facil"
                    val tiempo = doc.getLong("tiempoEnSegundos")?.toFloat() ?: return@forEachIndexed
                    val fechaBase = sortedDocs.firstOrNull()?.getLong("timeStamp") ?: 0L
                    val fechaAjustada =
                        fechaBase + index * 120_000  // fuerza separación regular entre puntos
                    val entry = Entry(fechaAjustada.toFloat(), tiempo)


                    if (dificultad == "dificil") {
                        dificil.add(entry)
                    } else {
                        facil.add(entry)
                    }

                    Log.d(
                        "DEBUG_REGISTRO",
                        "[$index] dificultad: $dificultad, tiempo: $tiempo, fecha: $fechaAjustada"
                    )
                }

                _resultadosFacil.value = facil
                _resultadosDificil.value = dificil
                _explicacionFacil.value = generarTextoExplicativo(facil)
                _explicacionDificil.value = generarTextoExplicativo(dificil)
                Log.d("DEBUG_ENTRIES", "Facil: ${facil.size}, Dificil: ${dificil.size}")
            }
            .addOnFailureListener {
                Log.e("DEBUG_PROGRESO", "Error al cargar registros de $nombreJuego", it)
            }
    }


    private fun actualizarProgresoPeriodicamente() {
        viewModelScope.launch {
            while (true) {
                juegoActual?.let {
                    cargarProgresoDeJuego(it)
                }
                delay(15000)
            }
        }
    }

    private fun generarTextoExplicativo(entries: List<Entry>): String {
        if (entries.size < 2) {
            return "No se ha jugado a este juego o solo hay un intento registrado. Se necesita más información para evaluar el progreso."
        }

        val sorted = entries.sortedBy { it.x }
        val anterior = sorted[sorted.size - 2].y
        val actual = sorted.last().y
        val diferencia = actual - anterior

        return when {
            diferencia < 0 -> "Se observan mejoras en el desempeño del alumno respecto al intento anterior."
            diferencia in 1f..8f -> "El alumno pareció perder algo de concentración en este intento."
            diferencia in 9f..14f -> "El alumno mostró signos de distracción durante la actividad."
            diferencia >= 15f -> "El alumno está teniendo dificultades para completar la tarea."
            else -> "No hubo cambios significativos en el rendimiento."
        }
    }

}

