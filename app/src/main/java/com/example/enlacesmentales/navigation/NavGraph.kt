package com.example.enlacesmentales.navigation


import ForgotPasswordScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.enlacesmentales.ui.screens.Home.HomeScreen
import com.example.enlacesmentales.ui.screens.Progreso.ProgresoScreen
import com.example.enlacesmentales.ui.screens.ajustes.AjustesUsuarioScreen
import com.example.enlacesmentales.ui.screens.auth.Login.LoginScreen
import com.example.enlacesmentales.ui.screens.auth.Registro.RegisterScreen
import com.example.enlacesmentales.ui.screens.juegos.cartas.MemoriaScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Progreso : Screen("progreso")
    object AjustesUsuario : Screen("ajustes_usuario")
    object MemoriaScreen : Screen("memoria_screen")
    object ForgotPassword : Screen("forgotpassword")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Progreso.route) {
            ProgresoScreen(navController)
        }
        composable(Screen.AjustesUsuario.route) {
            AjustesUsuarioScreen(navController)
        }
        composable(Screen.MemoriaScreen.route) {
            MemoriaScreen(navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

    }
}
