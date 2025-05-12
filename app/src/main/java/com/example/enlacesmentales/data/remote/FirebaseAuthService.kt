package com.example.enlacesmentales.data.remote


import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun logout() = auth.signOut()
}
