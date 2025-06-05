package com.example.enlacesmentales.ui.screens.auth.RecuperacionContrasena


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estados de la recuperación de contraseña
sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _forgotPasswordState =
        MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _forgotPasswordState.value =
                ForgotPasswordState.Error("El correo no puede estar vacío.")
            return
        }
        _forgotPasswordState.value = ForgotPasswordState.Loading

        // Llamada a Firebase
        viewModelScope.launch {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _forgotPasswordState.value = ForgotPasswordState.Success
                    } else {
                        // Capturamos el mensaje de error que venga de Firebase (si lo hay)
                        val errMsg = task.exception?.localizedMessage
                            ?: "Error al enviar correo de restablecimiento."
                        _forgotPasswordState.value = ForgotPasswordState.Error(errMsg)
                    }
                }
        }
    }
}
