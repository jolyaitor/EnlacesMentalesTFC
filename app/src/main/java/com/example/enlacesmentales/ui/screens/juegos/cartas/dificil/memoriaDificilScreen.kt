package com.example.enlacesmentales.ui.screens.juegos.cartas.dificil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.ui.components.GameFinishedDialog


@Composable
fun MemoriaDificilScreen(
    navController: NavController,
    viewModel: MemoriaDificilViewModel = hiltViewModel()
) {
    val cartas by viewModel.cartas.collectAsState()
    val tiempo by viewModel.tiempo.collectAsState()
    val completado by viewModel.completado.collectAsState()
    var showGameFinishedDialog by remember { mutableStateOf(false) }

    val totalParejas = cartas.size / 2
    val parejasEncontradas = cartas.count { it.esPareja } / 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botón de salir
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(125.dp))

        Text(
            text = "Juego de Memoria - Difícil",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Tiempo: ${tiempo}s",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(85.dp))

        // 4x3 Grid de cartas
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (fila in 0 until 4) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (col in 0 until 4) {
                        val index = fila * 4 + col
                        if (index < cartas.size) {
                            val carta = cartas[index]
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Color(0xFFEFE7F6))
                                    .clickable { viewModel.alSeleccionarCarta(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (carta.estaBocaArriba || carta.esPareja) {
                                    // Mostrar directamente el emoji
                                    Text(
                                        text = carta.valor,
                                        fontSize = 24.sp
                                    )
                                } else {
                                    Text("?", fontSize = 24.sp, color = Color.Red)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Parejas encontradas: $parejasEncontradas / $totalParejas",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))


        if (completado && !showGameFinishedDialog) {
            showGameFinishedDialog = true
        }

        if (showGameFinishedDialog) {
            GameFinishedDialog(
                timeTaken = "1 minuto y 30 segundos",
                onDismiss = {
                    showGameFinishedDialog = false
                    navController.popBackStack()
                }
            )
        }

    }
}
