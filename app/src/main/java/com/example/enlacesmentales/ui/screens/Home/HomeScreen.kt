// HomeScreen.kt
package com.example.enlacesmentales.ui.screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.data.model.Juego
import com.example.enlacesmentales.navigation.Screen
import com.example.enlacesmentales.ui.components.BottomNavigationBar
import com.example.enlacesmentales.ui.components.GameSelectionDialog
import com.example.enlacesmentales.ui.components.TopBar
import kotlinx.coroutines.flow.collectLatest
import com.example.enlacesmentales.ui.components.AccountOptionsDialog
import com.example.enlacesmentales.ui.components.DeleteAccountConfirmDialog

import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var juegoSeleccionado by remember { mutableStateOf<Juego?>(null) }

    // 1. Estado para el nuevo diálogo de reautenticación

    // Estados para los diálogos anteriores (opciones, cambiar correo, borrar cuenta)
    val showOptionsDialog by viewModel.showOptionsDialog.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { navController.navigate("ajustes_usuario") },
                onSettingsClick = { viewModel.onOptionsClicked() }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "JUEGOS",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(juegos) { juego ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { juegoSeleccionado = juego },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = juego.iconoResId),
                                contentDescription = juego.titulo,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de selección de juego (igual que antes)
        juegoSeleccionado?.let { juego ->
            GameSelectionDialog(
                gameTitle = juego.titulo,
                gameDescription = juego.descripcion,
                gameIconRes = juego.iconoResId,
                onDismiss = { juegoSeleccionado = null },
                onStartGame = { dificultad ->
                    juegoSeleccionado = null
                    navController.navigate("${juego.rutaNavegacion}?dificultad=$dificultad")
                }
            )
        }

        // 2. Recoger eventos de Snackbar
        LaunchedEffect(Unit) {
            viewModel.snackbarMessage.collectLatest { mensaje ->
                snackbarHostState.showSnackbar(mensaje)
            }
        }

        // 3. Recoger eventos de navegación
        LaunchedEffect(Unit) {
            viewModel.navigationEvents.collectLatest { evento ->
                when (evento) {
                    is SettingsNavigation.ToLogin -> {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                    else -> Unit
                }
            }
        }

        // ──────── DIÁLOGO PRINCIPAL: TRES OPCIONES ────────
        if (showOptionsDialog) {
            AccountOptionsDialog(
                onDismiss = { viewModel.onOptionsDialogDismiss() },
                onLogout = { viewModel.onLogoutSelected() },
                onDeleteAccount = { viewModel.onDeleteAccountSelected() }
            )
        }


        // ──────── DIÁLOGO CONFIRMACIÓN “BORRAR CUENTA” ────────
        if (showDeleteConfirmDialog) {
            DeleteAccountConfirmDialog(
                onDismiss = { viewModel.onDeleteConfirmDialogDismiss() },
                onConfirm = { viewModel.deleteAccount() }
            )
        }
    }
}
