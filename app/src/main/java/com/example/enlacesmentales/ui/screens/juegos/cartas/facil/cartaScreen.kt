package com.example.enlacesmentales.ui.screens.juegos.cartas.facil

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

@Composable
fun MemoriaScreen(
    navController: NavController,
    viewModel: MemoriaViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val totalParejas = cards.size / 2
    val parejasEncontradas = cards.count { it.isMatched } / 2


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Botón salir arriba derecha
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Título y tiempo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Juego de memoria", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tiempo jugado: ${elapsedTime}s", fontSize = 18.sp, color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cartas 3x2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        if (index < cards.size) {
                            val card = cards[index]
                            Card(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(8.dp)
                                    .clickable { viewModel.onCardClicked(index) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (card.isMatched || card.isFaceUp) Color.White else Color(
                                        0xFFEDE7F6
                                    )
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (card.isFaceUp || card.isMatched) card.content else "?",
                                        fontSize = 32.sp,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // NUEVO: Mostrar parejas encontradas
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Parejas encontradas: $parejasEncontradas / $totalParejas",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isCompleted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡Felicidades! Has completado el juego 🎉",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50), // Verde
                fontSize = 18.sp
            )
        }
    }
}

