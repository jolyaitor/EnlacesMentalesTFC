package com.example.enlacesmentales.ui.screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.enlacesmentales.data.model.Juego
import com.example.enlacesmentales.ui.components.TopBar
import com.example.enlacesmentales.ui.components.BottomNavigationBar
import com.example.enlacesmentales.ui.components.GameSelectionDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    var juegoSeleccionado by remember { mutableStateOf<Juego?>(null) }


    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { navController.navigate("ajustes_usuario") },
                onSettingsClick = {  }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
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

        // Diálogo de selección de juego
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
    }
}
