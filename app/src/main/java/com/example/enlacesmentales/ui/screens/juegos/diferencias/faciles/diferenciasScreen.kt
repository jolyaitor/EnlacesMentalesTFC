package com.example.enlacesmentales.ui.screens.juegos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.R
import com.example.enlacesmentales.ui.components.GameFinishedDialog

@Composable
fun EncuentraLasDiferenciasScreen(
    navController: NavController,
    viewModel: EncuentraDiferenciasViewModel = hiltViewModel()
) {
    val image1 = painterResource(id = R.drawable.diferencia1)
    val image2 = painterResource(id = R.drawable.diferencias2)

    val foundDifferences by viewModel.foundDifferences.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()
    val isCompleted = foundDifferences.size == viewModel.differences.size
    val remaining = viewModel.differences.size - foundDifferences.size

    // ─────── ESTADO PARA MOSTRAR EL DIÁLOGO ───────
    var showGameFinishedDialog by remember { mutableStateOf(false) }

    // Mostrar el diálogo solo una vez al completar el juego
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            showGameFinishedDialog = true
        }
    }

    // ─────── UI PRINCIPAL ───────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Botón de salir
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(90.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Encuentra las diferencias",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tiempo jugado: ${elapsedTime}s",
                color = Color.DarkGray,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(100.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(image1, image2).forEach { painter ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .pointerInput(isCompleted) {
                            if (!isCompleted) {
                                detectTapGestures { tap ->
                                    viewModel.registerTap(tap)
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned {
                                viewModel.setImageSize(IntSize(it.size.width, it.size.height))
                            }
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        foundDifferences.forEach {
                            drawCircle(
                                color = Color.Red,
                                radius = 25f,
                                center = Offset(it.x * size.width, it.y * size.height),
                                style = Stroke(width = 5f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Diferencias restantes: $remaining",
                color = Color.DarkGray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isCompleted) {
                Text("¡Felicidades! Encontraste todas 🎉", fontWeight = FontWeight.Bold)
            }
        }
    }

    // ─────── DIÁLOGO FINAL ───────
    if (showGameFinishedDialog) {
        GameFinishedDialog(
            timeTaken = "${elapsedTime}s",
            onDismiss = {
                showGameFinishedDialog = false
                navController.popBackStack()
            }
        )
    }
}
