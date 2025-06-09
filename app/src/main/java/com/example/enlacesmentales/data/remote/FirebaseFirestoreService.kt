package com.example.enlacesmentales.data.remote


import com.example.enlacesmentales.data.model.GameResult
import com.example.enlacesmentales.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("usuarios")

    suspend fun saveUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return usersCollection.document(uid).get().await().toObject(User::class.java)
    }

    suspend fun saveGameResult(userId: String, result: GameResult) {
        firestore.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .document(result.gameName)
            .collection("registros")
            .add(result)
            .await()
    }
}
