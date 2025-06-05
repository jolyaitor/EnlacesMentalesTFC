package com.example.enlacesmentales.ui.screens.ajustes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val workManager = WorkManager.getInstance(context)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val user = auth.currentUser

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        // Lee el valor inicial desde Firestore
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid)
                .addSnapshotListener { snap, _ ->
                    _notificationsEnabled.value = snap?.getBoolean("notificationsEnabled") ?: true
                }
        }
    }

    /** Llama a este método desde tu UI (switch) */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch {
            // Persiste en Firestore
            user?.uid?.let { uid ->
                firestore.collection("users")
                    .document(uid)
                    .update("notificationsEnabled", enabled)
            }
            _notificationsEnabled.value = enabled

            if (enabled) {
                createNotificationChannel()
                // NOTA: los PeriodicWorkRequests <15 min se redondean a 15 min.
                // Aquí usas 3 horas:
                val periodic = PeriodicWorkRequestBuilder<NotificationWorker>(3, TimeUnit.HOURS)
                    .build()
                workManager.enqueueUniquePeriodicWork(
                    "reminder_notifications",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodic
                )
            } else {
                workManager.cancelUniqueWork("reminder_notifications")
            }
        }
    }

    /** Para pruebas rápidas: dispara 1 notificación en ~10 segundos */
    fun testNotificationNow() {
        val testRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(testRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "reminder_channel",
            "Recordatorio en la app",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Canal para recordatorios cada 3 horas"
        }
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannel(channel)
    }
}

/** Worker que dispara la notificación */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val ctx = applicationContext
        val notification = NotificationCompat.Builder(ctx, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Vuelve a la app!")
            .setContentText("Te hemos echado de menos, únete otra vez 🙂")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(1001, notification)
        return Result.success()
    }
}
