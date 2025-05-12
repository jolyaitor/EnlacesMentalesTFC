package com.example.enlacesmentales.data.repository



import com.example.enlacesmentales.data.model.User
import com.example.enlacesmentales.data.remote.FirebaseAuthService
import com.example.enlacesmentales.data.remote.FirebaseFirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService
) {

    suspend fun registerUser(
        email: String,
        password: String,
        nombre: String,
        username: String
    ): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("UID nulo"))

            val user = User(
                uid = uid,
                name = nombre,
                username = username,
                email = email
            )

            firestoreService.saveUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): User? {
        return firestoreService.getUser(uid)
    }

    fun logout() {
        authService.logout()
    }

    fun getCurrentUserId(): String? = authService.getCurrentUserId()

    fun isUserLoggedIn(): Boolean = authService.isUserLoggedIn()
}
