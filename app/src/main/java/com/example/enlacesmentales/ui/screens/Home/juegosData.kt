package com.example.enlacesmentales.ui.screens.Home

import com.example.enlacesmentales.R
import com.example.enlacesmentales.data.model.Juego
import com.example.enlacesmentales.navigation.Screen

val juegos = listOf(
    Juego(
        "Memoria",
        "Encuentra las parejas ocultas lo más rápido posible.",
        iconoResId = R.drawable.juego_parejas,
        rutaNavegacion = Screen.MemoriaScreen.route
    ),
    Juego(
        "Diferencias2",
        "Encuentra las diferencias que hay entre las dos imágenes en el menor tiempo posible.",
        iconoResId = R.drawable.encuentra_diferencias,
        "atencion"
    ),
    Juego(
        "Campos semánticos",
        "Arrastra las palabras a los campos semánticos que están asociadas",
        iconoResId = R.drawable.campos_semanticos,
        "secuencias"
    ),
    Juego(
        "Encuentra el objeto",
        "Encuentra los objetos en la imagen en el menor tiempo posible",
        iconoResId = R.drawable.encuentra_objeto,
        "resolucion"
    )
)