package com.example.enlacesmentales.ui.screens.Progreso

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.ui.components.BottomNavigationBar
import com.example.enlacesmentales.ui.components.TopBar
import com.example.enlacesmentales.ui.components.AccountOptionsDialog
import com.example.enlacesmentales.ui.components.DeleteAccountConfirmDialog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import androidx.compose.ui.viewinterop.AndroidView
import com.example.enlacesmentales.ui.screens.Home.SettingsNavigation
import com.example.enlacesmentales.ui.screens.Home.SettingsViewModel
import com.example.enlacesmentales.utils.DateAxisValueFormatter
import com.example.enlacesmentales.viewmodel.ProgresoViewModel
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ProgresoScreen(
    navController: NavController,
    viewModel: ProgresoViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val juegos by viewModel.juegosDisponibles.collectAsState()
    val resultadosFacil by viewModel.resultadosFacil.collectAsState()
    val resultadosDificil by viewModel.resultadosDificil.collectAsState()
    val explicacionFacil by viewModel.explicacionFacil.collectAsState()
    val explicacionDificil by viewModel.explicacionDificil.collectAsState()
    val context = LocalContext.current

    var juegoSeleccionado by remember { mutableStateOf<String?>(null) }
    var mostrarFacil by remember { mutableStateOf(true) }
    var mostrarDificil by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    val showOptionsDialog by settingsViewModel.showOptionsDialog.collectAsState()
    val showDeleteConfirmDialog by settingsViewModel.showDeleteConfirmDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { navController.navigate("ajustes_usuario") },
                onSettingsClick = { settingsViewModel.onOptionsClicked() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, currentRoute = "progreso")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("PROGRESO", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = juegoSeleccionado ?: "Selecciona un juego")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    juegos.forEach { juego ->
                        DropdownMenuItem(
                            text = { Text(juego) },
                            onClick = {
                                juegoSeleccionado = juego
                                expanded = false
                                viewModel.seleccionarJuego(juego)
                            }
                        )
                    }
                }
            }

            if (juegos.isEmpty()) {
                Text(
                    "No hay juegos disponibles",
                    modifier = Modifier.padding(16.dp),
                    color = ComposeColor.Gray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Checkbox(checked = mostrarFacil, onCheckedChange = { mostrarFacil = it })
                Text("Fácil", color = ComposeColor.Green)

                Spacer(modifier = Modifier.width(16.dp))

                Checkbox(checked = mostrarDificil, onCheckedChange = { mostrarDificil = it })
                Text("Difícil", color = ComposeColor.Red)
            }

            if ((resultadosFacil.isNotEmpty() && mostrarFacil) ||
                (resultadosDificil.isNotEmpty() && mostrarDificil)
            ) {
                key(mostrarFacil to mostrarDificil to (resultadosFacil.hashCode() + resultadosDificil.hashCode())) {
                    AndroidView(
                        factory = {
                            createLineChart(
                                context,
                                mostrarFacil,
                                mostrarDificil,
                                resultadosFacil,
                                resultadosDificil
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp)
                    )
                }

            } else {
                Text(
                    "No hay datos para mostrar",
                    color = ComposeColor.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                "EXPLICACIÓN DEL AVANCE QUE TIENE EL ALUMNO",
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (mostrarFacil && explicacionFacil.isNotBlank()) {
                Text(
                    text = "Modo Fácil: $explicacionFacil",
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 14.sp
                )
            }

            if (mostrarDificil && explicacionDificil.isNotBlank()) {
                Text(
                    text = "Modo Difícil: $explicacionDificil",
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 14.sp
                )
            }

            Text(
                text = if (juegos.isEmpty()) "No se encontraron juegos"
                else "Juegos: ${juegos.joinToString()}",
                fontSize = 12.sp,
                color = ComposeColor.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (showOptionsDialog) {
            AccountOptionsDialog(
                onDismiss = { settingsViewModel.onOptionsDialogDismiss() },
                onLogout = { settingsViewModel.onLogoutSelected() },
                onDeleteAccount = { settingsViewModel.onDeleteAccountSelected() }
            )
        }

        if (showDeleteConfirmDialog) {
            DeleteAccountConfirmDialog(
                onDismiss = { settingsViewModel.onDeleteConfirmDialogDismiss() },
                onConfirm = { settingsViewModel.deleteAccount() }
            )
        }

        LaunchedEffect(Unit) {
            settingsViewModel.snackbarMessage.collectLatest { mensaje ->
                snackbarHostState.showSnackbar(mensaje)
            }
        }

        LaunchedEffect(Unit) {
            settingsViewModel.navigationEvents.collectLatest { evento ->
                if (evento is SettingsNavigation.ToLogin) {
                    navController.navigate("login") {
                        popUpTo("progreso") { inclusive = true }
                    }
                }
            }
        }
    }
}

private fun createLineChart(
    context: Context,
    mostrarFacil: Boolean,
    mostrarDificil: Boolean,
    datosFacil: List<Entry>,
    datosDificil: List<Entry>
): LineChart {
    val chart = LineChart(context)
    val lineDataSets = mutableListOf<ILineDataSet>()

    // Dataset FÁCIL
    if (mostrarFacil && datosFacil.isNotEmpty()) {
        val dataSetFacil = LineDataSet(datosFacil, "Fácil").apply {
            color = Color.GREEN
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
        }
        lineDataSets.add(dataSetFacil)
    }

    // Dataset DIFÍCIL
    if (mostrarDificil && datosDificil.isNotEmpty()) {
        val dataSetDificil = LineDataSet(datosDificil, "Difícil").apply {
            color = Color.RED
            setDrawCircles(true)
            setDrawValues(true)
            lineWidth = 2f
        }
        lineDataSets.add(dataSetDificil)
    }

    chart.data = LineData(lineDataSets)

    // Eje X configurado correctamente
    chart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = DateAxisValueFormatter() // "HH:mm:ss"
        granularity = 60_000f                      // 1 minuto entre etiquetas
        isGranularityEnabled = true
        setAvoidFirstLastClipping(true)
        labelRotationAngle = -45f
        setLabelCount(4, false)                    // Dejar que decida automáticamente
        textSize = 10f
    }

    val allX = (datosFacil + datosDificil).map { it.x }
    if (allX.isNotEmpty()) {
        val minX = allX.minOrNull() ?: 0f
        val maxX = allX.maxOrNull() ?: 0f
        val padding = 60_000f // 1 minuto de espacio extra

        chart.xAxis.axisMinimum = minX - padding
        chart.xAxis.axisMaximum = maxX + padding
    }

    // Eje Y
    chart.axisLeft.apply {
        axisMinimum = 0f
        spaceTop = 10f
    }
    chart.axisRight.isEnabled = false

    // Otros ajustes
    chart.setExtraOffsets(10f, 10f, 20f, 30f)
    chart.description.isEnabled = false
    chart.setTouchEnabled(true)
    chart.setPinchZoom(true)
    chart.setBackgroundColor(Color.WHITE)
    chart.legend.isEnabled = true
    chart.data.notifyDataChanged()
    chart.notifyDataSetChanged()
    chart.invalidate()

    return chart
}

