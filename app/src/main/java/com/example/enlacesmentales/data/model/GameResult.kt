package com.example.enlacesmentales.data.model

data class GameResult(
    val gameName: String,
    val dificultad: String,
    val tiempoEnSegundos: Int,
    val timeStamp: Long = System.currentTimeMillis()
)