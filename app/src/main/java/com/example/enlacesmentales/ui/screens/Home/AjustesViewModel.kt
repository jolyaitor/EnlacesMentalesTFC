package com.example.enlacesmentales.ui.screens.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsNavigation {
    object ToLogin : SettingsNavigation()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _showOptionsDialog = MutableStateFlow(false)
    val showOptionsDialog: StateFlow<Boolean> = _showOptionsDialog

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage: Flow<String> = _snackbarMessage.receiveAsFlow()

    private val _navigationEvents = Channel<SettingsNavigation>(Channel.BUFFERED)
    val navigationEvents: Flow<SettingsNavigation> = _navigationEvents.receiveAsFlow()

    fun onOptionsClicked() {
        _showOptionsDialog.value = true
    }

    fun onOptionsDialogDismiss() {
        _showOptionsDialog.value = false
    }

    fun onDeleteAccountSelected() {
        _showOptionsDialog.value = false
        _showDeleteConfirmDialog.value = true
    }

    fun onDeleteConfirmDialogDismiss() {
        _showDeleteConfirmDialog.value = false
    }

    fun deleteAccount() {
        _showDeleteConfirmDialog.value = false
        viewModelScope.launch {
            auth.currentUser?.let { user ->
                user.delete().addOnCompleteListener { task ->
                    viewModelScope.launch {
                        if (task.isSuccessful) {
                            _snackbarMessage.send("Cuenta eliminada completamente.")
                            _navigationEvents.send(SettingsNavigation.ToLogin)
                        } else {
                            val errorMsg = task.exception?.localizedMessage ?: "Error al eliminar cuenta."
                            _snackbarMessage.send(errorMsg)
                        }
                    }
                }
            } ?: run {
                _snackbarMessage.send("No hay usuario logueado.")
            }
        }
    }

    fun onLogoutSelected() {
        auth.signOut()
        viewModelScope.launch {
            _navigationEvents.send(SettingsNavigation.ToLogin)
        }
    }
}
