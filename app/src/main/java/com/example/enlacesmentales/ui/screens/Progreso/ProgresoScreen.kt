package com.example.enlacesmentales.ui.screens.Progreso

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.ui.components.BottomNavigationBar
import com.example.enlacesmentales.ui.components.TopBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*

import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

@Composable
fun ProgresoScreen(navController: NavController) {
    val viewModel: ProgresoViewModel = hiltViewModel()
    val juegos by viewModel.juegosDisponibles.collectAsState()
    val resultadosFacil by viewModel.resultadosFacil.collectAsState()
    val resultadosDificil by viewModel.resultadosDificil.collectAsState()
    val context = LocalContext.current

    var juegoSeleccionado by remember { mutableStateOf<String?>(null) }
    var mostrarFacil by remember { mutableStateOf(true) }
    var mostrarDificil by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { navController.navigate("ajustes_usuario") },
                onSettingsClick = { /* Acción ajustes */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, currentRoute = "progreso")
        }
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

            Box(modifier = Modifier.padding(16.dp)) {
                TextButton(onClick = { expanded = true }) {
                    Text(juegoSeleccionado ?: "Selecciona un juego")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    juegos.forEach { juego ->
                        DropdownMenuItem(
                            text = { Text(juego) },
                            onClick = {
                                juegoSeleccionado = juego
                                expanded = false
                                viewModel.cargarProgresoDeJuego(juego)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = mostrarFacil, onCheckedChange = { mostrarFacil = it })
                Text("Fácil", modifier = Modifier.padding(end = 16.dp))
                Switch(checked = mostrarDificil, onCheckedChange = { mostrarDificil = it })
                Text("Difícil")
            }

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

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "EXPLICACIÓN DEL AVANCE QUE TIENE EL ALUMNO",
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
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

    if (mostrarFacil && datosFacil.isNotEmpty()) {
        val dataSetFacil = LineDataSet(datosFacil, "Fácil").apply {
            color = Color.GREEN
            setDrawCircles(true)
            setDrawValues(false)
        }
        lineDataSets.add(dataSetFacil)
    }

    if (mostrarDificil && datosDificil.isNotEmpty()) {
        val dataSetDificil = LineDataSet(datosDificil, "Difícil").apply {
            color = Color.RED
            setDrawCircles(true)
            setDrawValues(false)
        }
        lineDataSets.add(dataSetDificil)
    }

    chart.data = LineData(lineDataSets)
    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    chart.axisRight.isEnabled = false
    chart.description.isEnabled = false
    chart.setBackgroundColor(Color.WHITE)
    chart.invalidate()

    return chart
}
