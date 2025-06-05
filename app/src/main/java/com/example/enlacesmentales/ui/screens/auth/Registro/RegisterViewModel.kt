package com.example.enlacesmentales.ui.screens.auth.Registro


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun registerUser(email: String, password: String, nombre: String, username: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        val userData = hashMapOf(
                            "nombre" to nombre,
                            "username" to username,
                            "email" to email
                        )

                        firestore.collection("usuarios").document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                _registerState.value = RegisterState.Success
                            }
                            .addOnFailureListener {
                                _registerState.value =
                                    RegisterState.Error(it.message ?: "Error al guardar usuario")
                            }

                    } else {
                        _registerState.value =
                            RegisterState.Error(task.exception?.message ?: "Error al registrar")
                    }
                }
        }
    }
}
