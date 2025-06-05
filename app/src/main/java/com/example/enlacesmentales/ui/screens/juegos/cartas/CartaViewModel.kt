package com.example.enlacesmentales.ui.screens.juegos.cartas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enlacesmentales.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartaMemoria(
    val id: Int,
    val imagen: Int,
    val descubierta: Boolean = false
)

class MemoriaViewModel : ViewModel() {

    private val _cartas = MutableStateFlow<List<CartaMemoria>>(emptyList())
    val cartas: StateFlow<List<CartaMemoria>> = _cartas

    // Bloquea clicks mientras dura la animación de volteo
    private var canSelect = true

    // Guarda solo el ID de la primera carta en cada par
    private var primeraCartaId: Int? = null

    init {
        iniciarJuego()
    }

    fun iniciarJuego() {
        canSelect = true
        primeraCartaId = null

        // Lista de imágenes a duplicar
        val imagenes = listOf(
            R.drawable.abeja,
            R.drawable.miel,
            R.drawable.abeja2,
            R.drawable.miel2
        )

        // Duplicamos, barajamos y creamos cartas con ID único
        _cartas.value = (imagenes + imagenes)
            .shuffled()
            .mapIndexed { index, img ->
                CartaMemoria(id = index, imagen = img)
            }
    }

    fun seleccionarCarta(carta: CartaMemoria) {
        // 1) Si ya no se puede seleccionar o ya está descubierta, ignoramos
        if (!canSelect || carta.descubierta) return

        // 2) Revelamos la carta clicada
        _cartas.value = _cartas.value.map {
            if (it.id == carta.id) it.copy(descubierta = true) else it
        }

        // 3) Si no hay primera carta guardada, esta pasa a serla
        if (primeraCartaId == null) {
            primeraCartaId = carta.id
            return
        }

        // 4) Ya había otra carta seleccionada: comprobamos match
        val primeraId = primeraCartaId!!
        val primera = _cartas.value.first { it.id == primeraId }
        val segunda = _cartas.value.first { it.id == carta.id }

        if (primera.imagen == segunda.imagen) {
            // **Emparejadas**: limpiamos y, si termina el juego, guardamos progreso
            primeraCartaId = null
            if (_cartas.value.all { it.descubierta }) {
                guardarProgreso()
            }
        } else {
            // **No emparejadas**: bloqueamos selección y tras delay las ocultamos
            canSelect = false
            viewModelScope.launch {
                delay(1_000)
                _cartas.value = _cartas.value.map {
                    if (it.id == primeraId || it.id == carta.id) {
                        it.copy(descubierta = false)
                    } else it
                }
                primeraCartaId = null
                canSelect = true
            }
        }
    }

    private fun guardarProgreso() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(userId)
            .collection("Progreso")
            .add(
                mapOf(
                    "juego" to "Memoria",
                    "dificultad" to "fácil",
                    "valor" to 1.0
                )
            )
    }
}
