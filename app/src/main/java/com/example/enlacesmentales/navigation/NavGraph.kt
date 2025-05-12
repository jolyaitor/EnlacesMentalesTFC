package com.example.enlacesmentales.navigation

import com.example.enlacesmentales.presentation.auth.LoginScreen


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.enlacesmentales.ui.screens.auth.Registro.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    // Agrega más pantallas aquí según las necesites
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        // Agrega aquí HomeScreen, juegos, etc.
    }
}
