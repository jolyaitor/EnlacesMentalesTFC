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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.LineChart
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ProgresoScreen(navController: NavController) {
    val viewModel: ProgresoViewModel = hiltViewModel()
    val entries by viewModel.progresoEntries.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { navController.navigate("ajustes_usuario") },
                onSettingsClick = { /* Acción para ajustes */ }
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
                .background(color = MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("PROGRESO", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { createLineChart(context, entries) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "EXPLICACIÓN DEL AVANCE QUE TIENE EL ALUMNO",
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun createLineChart(context: Context, entries: List<Entry>): LineChart {
    val chart = LineChart(context)

    val dataSet = LineDataSet(entries, "Progreso")
    dataSet.color = Color.BLUE
    dataSet.valueTextColor = Color.BLACK
    dataSet.setDrawCircles(true)
    dataSet.setDrawValues(false)

    val lineData = LineData(dataSet)
    chart.data = lineData

    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    chart.axisRight.isEnabled = false
    chart.description.isEnabled = false
    chart.setBackgroundColor(android.graphics.Color.WHITE)

    chart.invalidate()
    return chart
}
