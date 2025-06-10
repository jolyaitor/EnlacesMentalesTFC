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
                    val fecha = doc.getLong("timeStamp") ?: return@forEachIndexed
                    Log.d("FECHA_REGISTRO", "timestamp: $fecha → ${Date(fecha)}")
                    val fechaAjustada =
                        fecha + index * 60_000  // Aumenta 1 min por punto // Simula separación si los timestamps son iguales
                    val entry = Entry(fechaAjustada.toFloat(), tiempo)


                    if (dificultad == "dificil") {
                        dificil.add(entry)
                    } else {
                        facil.add(entry)
                    }

                    Log.d(
                        "DEBUG_REGISTRO",
                        "[$index] dificultad: $dificultad, tiempo: $tiempo, fecha: $fecha"
                    )
                }

                _resultadosFacil.value = facil
                _resultadosDificil.value = dificil
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
}

