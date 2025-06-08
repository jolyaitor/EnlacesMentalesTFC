package com.example.enlacesmentales.ui.screens.juegos

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

class EncuentraDiferenciasViewModel : ViewModel() {

    private val _foundDifferences = MutableStateFlow<List<Offset>>(emptyList())
    val foundDifferences: StateFlow<List<Offset>> = _foundDifferences

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime

    private val _imageSize = MutableStateFlow(IntSize(1, 1))
    val imageSize: StateFlow<IntSize> = _imageSize

    val differences = listOf(
        Offset(0.9579423f, 0.8258832f),
        Offset(0.6770649f, 0.30167183f),
        Offset(0.2745676f, 0.45061296f)
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
