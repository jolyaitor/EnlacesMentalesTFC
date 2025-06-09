package com.example.enlacesmentales.ui.screens.auth.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    // Activar para desarrollo sin conexión
    private val modoLocal = true

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            if (modoLocal) {
                // Simulación de inicio de sesión local
                if (email.isNotBlank() && password.isNotBlank()) {
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Credenciales inválidas en modo local")
                }
            } else {
                // Inicio de sesión real con Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _loginState.value = LoginState.Success
                        } else {
                            _loginState.value =
                                LoginState.Error(
                                    task.exception?.message ?: "Error al iniciar sesión"
                                )
                        }
                    }
            }
        }
    }
}
