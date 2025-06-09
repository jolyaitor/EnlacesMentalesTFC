package com.example.enlacesmentales.ui.screens.Progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProgresoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _juegosDisponibles = MutableStateFlow<List<String>>(emptyList())
    val juegosDisponibles: StateFlow<List<String>> = _juegosDisponibles

    private val _resultadosFacil = MutableStateFlow<List<Entry>>(emptyList())
    val resultadosFacil: StateFlow<List<Entry>> = _resultadosFacil

    private val _resultadosDificil = MutableStateFlow<List<Entry>>(emptyList())
    val resultadosDificil: StateFlow<List<Entry>> = _resultadosDificil

    init {
        cargarListaDeJuegos()
    }

    private fun cargarListaDeJuegos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(userId)
            .collection("Progreso")
            .get()
            .addOnSuccessListener { snapshot ->
                val juegos = snapshot.documents.map { it.id }
                _juegosDisponibles.value = juegos
            }
    }

    fun cargarProgresoDeJuego(juego: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(userId)
            .collection("Progreso").document(juego)
            .collection("Registros")
            .get()
            .addOnSuccessListener { documentos ->
                val facil = mutableListOf<Entry>()
                val dificil = mutableListOf<Entry>()

                documentos.sortedBy { it.getLong("timeStamp") ?: 0L }.forEachIndexed { index, doc ->
                    val tiempo = doc.getLong("tiempoEnSegundos")?.toFloat() ?: return@forEachIndexed
                    val dificultad = doc.getString("dificultad") ?: "facil"

                    if (dificultad == "facil") {
                        facil.add(Entry(index.toFloat(), tiempo))
                    } else {
                        dificil.add(Entry(index.toFloat(), tiempo))
                    }
                }

                _resultadosFacil.value = facil
                _resultadosDificil.value = dificil
            }
            .addOnFailureListener {
                _resultadosFacil.value = emptyList()
                _resultadosDificil.value = emptyList()
            }
    }
}
