package com.example.enlacesmentales.data.repository

import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.remote.FirebaseFirestoreService
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class ProgresoRepository @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val auth: FirebaseAuth
) {

    suspend fun guardarResultadoJuego(result: GameResult) {
        val userId = auth.currentUser?.uid ?: return
        firestoreService.saveGameResult(userId, result)
    }
}
