package com.example.enlacesmentales.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.enlacesmentales.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.SportsEsports, contentDescription = "Juegos")
            },
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) },
            label = { Text("Juegos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3ABEFF), // azul claro
                indicatorColor = Color(0xFFE0F7FA)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Filled.BarChart, contentDescription = "Progreso")
            },
            selected = currentRoute == Screen.Progreso.route,
            onClick = { navController.navigate(Screen.Progreso.route) },
            label = { Text("Progreso") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3ABEFF), // azul claro
                indicatorColor = Color(0xFFE0F7FA)
            )
        )
    }
}