package com.example.enlacesmentales.ui.screens.Home

import com.example.enlacesmentales.data.model.Juego
import com.example.enlacesmentales.navigation.Screen
import com.example.enlacesmentales.R

val juegos = listOf(
    Juego(
        "Memoria",
        "Encuentra las parejas ocultas lo más rápido posible.",
        iconoResId = R.drawable.juego_parejas,
        rutaNavegacion = Screen.MemoriaScreen.route
    ),
    Juego(
        "Diferencias",
        "Encuentra las diferencias que hay entre las dos imágenes en el menor tiempo posible.",
        iconoResId = R.drawable.encuentra_diferencias,
        rutaNavegacion = Screen.Diferencias.route
    ),
    Juego(
        "Campos semánticos",
        "Arrastra las palabras a los campos semánticos que están asociadas",
        iconoResId = R.drawable.campos_semanticos,
        rutaNavegacion = Screen.CamposSemanticos.route
    ),
    Juego(
        "Encuentra el objeto",
        "Encuentra los objetos en la imagen en el menor tiempo posible",
        iconoResId = R.drawable.encuentra_objeto,
        "resolucion"
    )
)