package com.example.enlacesmentales.ui.screens.juegos.encuentrapersonaje

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.R
import com.example.enlacesmentales.ui.components.GameFinishedDialog
import androidx.compose.ui.draw.drawBehind

@Composable
fun EncuentraPersonajeScreen(
    navController: NavController,
    viewModel: EncuentraPersonajeViewModel = hiltViewModel()
) {
    val encontrados by viewModel.encontrados.collectAsState()
    val objetivos = viewModel.objetivos
    val tiempoTranscurrido by viewModel.tiempoTranscurrido.collectAsState()
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val juegoCompletado = viewModel.estanTodosEncontrados()
    var showGameFinishedDialog by remember { mutableStateOf(false) }

    // Mostrar el diálogo cuando el juego se completa
    LaunchedEffect(juegoCompletado) {
        if (juegoCompletado) {
            showGameFinishedDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
    ) {
        // Botón de salir
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Salir")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Encuentra los personajes ocultos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tiempo transcurrido: ${tiempoTranscurrido}s",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .background(Color.LightGray)
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            if (imageSize.width > 0 && imageSize.height > 0) {
                                val normalizedX = tapOffset.x / imageSize.width
                                val normalizedY = tapOffset.y / imageSize.height
                                val normalizedOffset = Offset(normalizedX, normalizedY)

                                Log.d("ToqueDetectado", "X: $normalizedX, Y: $normalizedY")

                                viewModel.verificarToque(normalizedOffset)
                            }
                        }
                    }
            ) {
                val image: Painter = painterResource(id = R.drawable.objeto_facil)

                Image(
                    painter = image,
                    contentDescription = "Imagen del juego",
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size -> imageSize = size }
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    encontrados.forEach { personaje ->
                        drawCircle(
                            color = Color.Red,
                            radius = 40f,
                            center = Offset(
                                personaje.posicion.x * size.width,
                                personaje.posicion.y * size.height
                            ),
                            style = Stroke(width = 5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text("Toca a los siguientes personajes:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                objetivos.forEach { personaje ->
                    val encontrado = encontrados.any { it.nombre == personaje.nombre }

                    Text(
                        text = personaje.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (encontrado) Color.Gray else Color.Black,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color.Transparent)
                            .then(
                                if (encontrado) {
                                    Modifier.drawBehind {
                                        val y = size.height / 2
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 4f
                                        )
                                    }
                                } else Modifier
                            )
                    )
                }
            }

            if (juegoCompletado) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "¡Todos encontrados! 🎉",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }

    // Mostrar el diálogo final
    if (showGameFinishedDialog) {
        GameFinishedDialog(
            timeTaken = "${tiempoTranscurrido}s",
            onDismiss = {
                showGameFinishedDialog = false
                navController.popBackStack()
            }
        )
    }
}
