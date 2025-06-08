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
    val email: String = "",
    val currentPassword: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false
)

// ViewModel
class AjustesViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = auth.currentUser


    private val _uiState = MutableStateFlow(
        AjustesUiState(
            name = user?.displayName.orEmpty(),
            email = user?.email.orEmpty()
        )
    )
    val uiState: StateFlow<AjustesUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow: SharedFlow<UIEvent> = _eventFlow.asSharedFlow()

    // Eventos UI
    sealed class UIEvent {
        data class ShowError(val message: String) : UIEvent()
        data class ShowSuccess(val message: String) : UIEvent()
        object Logout : UIEvent()
    }

    // Actualización de campos
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onCurrentPasswordChange(v: String) = _uiState.update { it.copy(currentPassword = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onRepeatPasswordChange(v: String) = _uiState.update { it.copy(repeatPassword = v) }
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

            if (!isValidEmail(s.email)) {
                _eventFlow.emit(UIEvent.ShowError("El correo no tiene un formato válido"))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val email = user?.email.orEmpty()

                // Reautenticación
                val cred = EmailAuthProvider.getCredential(email, s.currentPassword)
                user?.reauthenticate(cred)?.await()

                // ACTUALIZAR CORREO
                if (s.email != email) {
                    val emailUpdateSuccess = try {
                        val result = auth.fetchSignInMethodsForEmail(s.email).await()
                        val signInMethods = result.signInMethods

                        if (!signInMethods.isNullOrEmpty()) {
                            _eventFlow.emit(UIEvent.ShowError("Ese correo ya está en uso por otra cuenta."))
                            false
                        } else {
                            user?.verifyBeforeUpdateEmail(s.email)?.await()
                            _eventFlow.emit(UIEvent.ShowSuccess("Te hemos enviado un email a '${s.email}'. Verifica ese correo para poder continuar."))
                            true
                        }
                    } catch (e: Exception) {
                        Log.e("AjustesViewModel", "Error al actualizar email", e)
                        val message = when (e) {
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                "El correo tiene un formato inválido."

                            else -> "No se pudo enviar el correo de verificación: ${e.message.orEmpty()}"
                        }
                        _eventFlow.emit(UIEvent.ShowError(message))
                        false
                    }

                    if (emailUpdateSuccess) {
                        auth.signOut()
                        _eventFlow.emit(UIEvent.Logout)
                    }

                    return@launch
                }


                // ACTUALIZAR CONTRASEÑA
                if (s.password.isNotBlank()) {
                    user?.updatePassword(s.password)?.await()
                }

                // ACTUALIZAR NOMBRE
                val profileUpdates = userProfileChangeRequest {
                    displayName = s.name
                }
                user?.updateProfile(profileUpdates)?.await()

                // ACTUALIZAR EN FIRESTORE
                user?.uid?.let { uid ->
                    firestore.collection("usuarios")
                        .document(uid)
                        .set(mapOf("username" to s.name), SetOptions.merge())
                        .await()
                }

                _eventFlow.emit(UIEvent.ShowSuccess("Datos actualizados correctamente"))

            } catch (e: Exception) {
                Log.e("AjustesViewModel", "Error durante actualización", e)

                val message = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                        "La contraseña actual no es correcta."

                    is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ->
                        "Tu sesión es antigua. Vuelve a iniciar sesión e inténtalo de nuevo."

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
