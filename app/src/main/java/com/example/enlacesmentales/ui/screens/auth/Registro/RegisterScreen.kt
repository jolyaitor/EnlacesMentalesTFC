package com.example.enlacesmentales.ui.screens.auth.Registro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.enlacesmentales.navigation.Screen
import kotlinx.coroutines.launch

fun isPasswordValid(password: String): Boolean {
    val passwordRegex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{6,}\$")
    return password.matches(passwordRegex)
}

@Composable
fun RegisterScreen(navController: NavController) {
    var nombre by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var passwordMismatch by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val viewModel: RegisterViewModel = hiltViewModel()
    val state by viewModel.registerState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state is RegisterState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar((state as RegisterState.Error).message)
            }
        }
        if (state is RegisterState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    val isFormValid = nombre.isNotBlank() &&
            username.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            repeatPassword.isNotBlank() &&
            password == repeatPassword &&
            isPasswordValid(password)

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Registro", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordMismatch = repeatPassword.isNotEmpty() && it != repeatPassword
                    passwordError = !isPasswordValid(it)
                },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError
            )
            if (passwordError) {
                Text(
                    text = "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula, un número y un símbolo",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = repeatPassword,
                onValueChange = {
                    repeatPassword = it
                    passwordMismatch = password != it
                },
                label = { Text("Repetir contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordMismatch
            )
            if (passwordMismatch) {
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.registerUser(
                        email = email,
                        password = password,
                        nombre = nombre,
                        username = username
                    )
                },
                enabled = isFormValid
            ) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                navController.navigate(Screen.Login.route)
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
