package com.example.enlacesmentales.ui.screens.juegos.diferencias.dificil


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EncuentraDiferenciasDificilViewModel : ViewModel() {

    private val _foundDifferences = MutableStateFlow<List<Offset>>(emptyList())
    val foundDifferences: StateFlow<List<Offset>> = _foundDifferences

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime

    private val _imageSize = MutableStateFlow(IntSize(1, 1))
    val imageSize: StateFlow<IntSize> = _imageSize

    val differences = listOf(
        Offset(0.8804049f, 0.2534579f),
        Offset(0.31650862f, 0.05016092f),
        Offset(0.3333006f, 0.6560596f),
        Offset(0.09008124f, 0.43802205f),
        Offset(0.18653981f, 0.4212854f),
        Offset(0.8804049f, 0.66435117f),
        Offset(0.55545527f, 0.4358724f),
        Offset(0.80496186f, 0.64131904f),
        Offset(0.47163054f, 0.66435117f),
        Offset(0.64147055f, 0.68753684f)
    )


    val isCompleted: Boolean
        get() = _foundDifferences.value.size == differences.size

    init {
        viewModelScope.launch {
            while (!isCompleted) {
                delay(1000L)
                _elapsedTime.update { it + 1 }
            }
        }
    }

    fun setImageSize(size: IntSize) {
        _imageSize.value = size
    }

    fun registerTap(tap: Offset) {
        if (isCompleted) return

        val relTap = Offset(
            tap.x / _imageSize.value.width,
            tap.y / _imageSize.value.height
        )
        Log.d("TapCoords", "x: ${relTap.x}, y: ${relTap.y}")
        val radius = 0.05f

        differences.forEach { diff ->
            if (!_foundDifferences.value.contains(diff)) {
                val dx = diff.x - relTap.x
                val dy = diff.y - relTap.y
                val dist = kotlin.math.hypot(dx, dy)
                if (dist < radius) {
                    _foundDifferences.update { it + diff }
                    return
                }
            }
        }
    }
}
