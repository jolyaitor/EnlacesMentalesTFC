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

    private val _progresoEntries = MutableStateFlow<List<Entry>>(emptyList())
    val progresoEntries: StateFlow<List<Entry>> = _progresoEntries

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        obtenerProgreso()
    }

    private fun obtenerProgreso() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            db.collection("Usuarios").document(userId)
                .collection("Progreso")
                .get()
                .addOnSuccessListener { documents ->
                    val entries = documents.mapIndexed { index, doc ->
                        val value = doc.getDouble("valor") ?: 0.0
                        Entry(index.toFloat(), value.toFloat())
                    }
                    _progresoEntries.value = entries
                }
                .addOnFailureListener {
                    _progresoEntries.value = emptyList()
                }
        }
    }
}
