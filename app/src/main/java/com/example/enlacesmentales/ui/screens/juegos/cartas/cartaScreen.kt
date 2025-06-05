// MemoriaScreen.kt
package com.example.enlacesmentales.ui.screens.juegos.cartas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.ui.components.TopBar
import com.example.enlacesmentales.ui.components.BottomNavigationBar

@Composable
fun MemoriaScreen(navController: NavController) {
    val viewModel: MemoriaViewModel = hiltViewModel()
    val cartas by viewModel.cartas.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                onUserClick = { /* Acción perfil */ },
                onSettingsClick = { /* Acción ajustes */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, currentRoute = "memoria")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Juego de Memoria (Fácil)",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartas) { carta ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { viewModel.seleccionarCarta(carta) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (carta.descubierta) {
                            Image(
                                painter = painterResource(id = carta.imagen),
                                contentDescription = "Carta ${carta.id}",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
