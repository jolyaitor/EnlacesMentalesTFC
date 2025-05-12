package com.example.enlacesmentales.data.model

data class GameResult(
    val gameName: String,
    val correcto: Int,
    val preguntasTotales: Int,
    val timeStamp: Long = System.currentTimeMillis()
)