package com.example.enlacesmentales.ui.screens.juegos.semanticos

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.ui.components.GameFinishedDialog
import kotlinx.coroutines.launch

data class CategoriaBoxBounds(val categoria: String, val topLeft: Offset, val size: IntSize)

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CamposSemanticosScreen(
    navController: NavController,
    viewModel: CamposSemanticosViewModel = hiltViewModel()
) {
    val categorias by viewModel.categorias.collectAsState()
    val palabras by viewModel.palabras.collectAsState()
    val matched by viewModel.matchedWords.collectAsState()
    val fallos by viewModel.palabrasFallidas.collectAsState()
    val tiempo by viewModel.tiempo.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    val boxBounds = remember { mutableStateListOf<CategoriaBoxBounds>() }
    val wordPositions = remember { mutableStateMapOf<String, Offset>() }
    var draggingWord by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val animatableOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Diálogo al completar
    var showGameFinishedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            showGameFinishedDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Clasifica las palabras en su campo semántico",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            Text("Tiempo jugado: ${tiempo}s", color = Color.DarkGray, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            categorias.forEach { categoria ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .wrapContentHeight()
                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp))
                        .onGloballyPositioned {
                            val position = it.positionInRoot()
                            val size = it.size
                            val newBounds = CategoriaBoxBounds(categoria, position, size)
                            boxBounds.removeAll { it.categoria == categoria }
                            boxBounds.add(newBounds)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(categoria, fontWeight = FontWeight.Bold)
                        matched[categoria]?.forEach { word ->
                            Text(word, color = Color.Green)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            val palabrasClasificadas = matched.values.flatten().toSet()

            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                palabras.forEach { word ->
                    if (word != draggingWord) {
                        DraggableWord(
                            word = word,
                            dragOffset = Offset.Zero,
                            isShaking = fallos.contains(word),
                            onDragStart = {
                                draggingWord = word
                                scope.launch { animatableOffset.snapTo(Offset.Zero) }
                            },
                            onDrag = { delta ->
                                scope.launch { animatableOffset.snapTo(animatableOffset.value + delta) }
                            },
                            onDragEnd = {
                                val position = wordPositions[word] ?: Offset.Zero
                                val wordCenter =
                                    position + animatableOffset.value + Offset(40f, 20f)

                                val matchedBox = boxBounds.find { bounds ->
                                    val left = bounds.topLeft.x
                                    val top = bounds.topLeft.y
                                    val right = left + bounds.size.width
                                    val bottom = top + bounds.size.height
                                    wordCenter.x in left..right && wordCenter.y in top..bottom
                                }

                                if (matchedBox != null) {
                                    viewModel.onDrop(word, matchedBox.categoria)
                                } else {
                                    viewModel.onDrop(word, "")
                                    scope.launch {
                                        animatableOffset.animateTo(Offset.Zero, tween(300))
                                    }
                                }
                                draggingWord = null
                            },
                            onPositionMeasured = { pos -> wordPositions[word] = pos }
                        )
                    }
                }
            }

            draggingWord?.let { word ->
                DraggableWord(
                    word = word,
                    dragOffset = animatableOffset.value,
                    isShaking = fallos.contains(word),
                    onDragStart = {},
                    onDrag = { delta ->
                        scope.launch { animatableOffset.snapTo(animatableOffset.value + delta) }
                    },
                    onDragEnd = {
                        val position = wordPositions[word] ?: Offset.Zero
                        val wordCenter = position + animatableOffset.value + Offset(40f, 20f)

                        val matchedBox = boxBounds.find { bounds ->
                            val left = bounds.topLeft.x
                            val top = bounds.topLeft.y
                            val right = left + bounds.size.width
                            val bottom = top + bounds.size.height
                            wordCenter.x in left..right && wordCenter.y in top..bottom
                        }

                        if (matchedBox != null) {
                            viewModel.onDrop(word, matchedBox.categoria)
                        } else {
                            viewModel.onDrop(word, "")
                            scope.launch {
                                animatableOffset.animateTo(Offset.Zero, tween(300))
                            }
                        }
                        draggingWord = null
                    },
                    onPositionMeasured = { pos -> wordPositions[word] = pos }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (isCompleted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡Todo está bien organizado! 🎉",
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

        }
    }

    // Mostrar GameFinishedDialog
    if (showGameFinishedDialog) {
        GameFinishedDialog(
            timeTaken = "${tiempo}s",
            onDismiss = {
                showGameFinishedDialog = false
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun DraggableWord(
    word: String,
    dragOffset: Offset,
    isShaking: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onPositionMeasured: (Offset) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse),
        label = "shakeAnim"
    )

    val offsetX = if (isShaking) shakeOffset else 0f

    Box(
        modifier = Modifier
            .onGloballyPositioned { onPositionMeasured(it.positionInRoot()) }
            .offset { IntOffset((dragOffset.x + offsetX).toInt(), dragOffset.y.toInt()) }
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = word, fontSize = 18.sp, color = if (isShaking) Color.Red else Color.Black)
    }
}
