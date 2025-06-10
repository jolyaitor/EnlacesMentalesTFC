package com.example.enlacesmentales.ui.screens.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

// Estado UI
data class AjustesUiState(
    val name: String = "",
    val currentPassword: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false
)

class AjustesViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = auth.currentUser

    private val _uiState = MutableStateFlow(
        AjustesUiState(
            name = user?.displayName.orEmpty()
        )
    )
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow: SharedFlow<UIEvent> = _eventFlow.asSharedFlow()

    sealed class UIEvent {
        data class ShowError(val message: String) : UIEvent()
        data class ShowSuccess(val message: String) : UIEvent()
        object Logout : UIEvent()
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onCurrentPasswordChange(v: String) = _uiState.update { it.copy(currentPassword = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onRepeatPasswordChange(v: String) = _uiState.update { it.copy(repeatPassword = v) }

    fun clearFields() {
        _uiState.update {
            it.copy(
                name = "",
                currentPassword = "",
                password = "",
                repeatPassword = ""
            )
        }
    }

    fun onSaveClick() {
        val s = uiState.value

        viewModelScope.launch {
            if (s.password.isNotEmpty() && s.password != s.repeatPassword) {
                _eventFlow.emit(UIEvent.ShowError("Las contraseñas nuevas no coinciden"))
                return@launch
            }

            if (s.currentPassword.isBlank()) {
                _eventFlow.emit(UIEvent.ShowError("Introduce la contraseña actual"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val email = user?.email.orEmpty()
                val credential = EmailAuthProvider.getCredential(email, s.currentPassword)
                user?.reauthenticate(credential)?.await()

                if (s.password.isNotBlank()) {
                    user?.updatePassword(s.password)?.await()
                }

                val profileUpdates = userProfileChangeRequest {
                    displayName = s.name
                }
                user?.updateProfile(profileUpdates)?.await()

                user?.uid?.let { uid ->
                    firestore.collection("usuarios")
                        .document(uid)
                        .set(mapOf("username" to s.name), SetOptions.merge())
                        .await()
                }

                _eventFlow.emit(UIEvent.ShowSuccess("Datos actualizados correctamente"))
                clearFields()

            } catch (e: Exception) {
                val message = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                        "La contraseña actual no es correcta."

                    is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ->
                        "Tu sesión ha expirado. Vuelve a iniciar sesión."

                    else -> "Error: ${e.message.orEmpty()}"
                }
                _eventFlow.emit(UIEvent.ShowError(message))
            } finally {
                _uiState.update { it.copy(isLoading = false, currentPassword = "") }
            }
        }
    }

    fun onLogout() = viewModelScope.launch {
        auth.signOut()
        _eventFlow.emit(UIEvent.Logout)
    }
}
