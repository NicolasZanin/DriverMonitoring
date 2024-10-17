package com.drivermonitoring.drivermonitoringsmartwatch.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.drivermonitoring.drivermonitoringsmartwatch.R

class HeartRateService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "heart_rate_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("HeartRateService", "onCreate")
        // Initialisation du SensorManager et du capteur de rythme cardiaque
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.e("HeartRateService", "ça fonctionne ici")
        } else {
            Log.e("HeartRateService", "Capteur de rythme cardiaque non disponible")
            stopSelf()
        }

        // Lancer le service en avant-plan avec une notification permanente
        startForeground(NOTIFICATION_ID, createNotification())
    }

    // Création de la notification
    private fun createNotification(): Notification {
        // Création du canal de notification pour Android 8.0 et supérieur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Heart Rate Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Monitoring Heart Rate")
            .setContentText("Collecting heart rate data in background")
            //.setSmallIcon(R.drawable.ic_heart)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartRate = event.values[0]
                Log.d("HeartRateService", "Rythme cardiaque en arrière-plan : $heartRate")
                // Ici, tu pourrais envoyer ces données à une base de données ou les traiter.
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Gérer les changements de précision du capteur
    }

    override fun onDestroy() {
        super.onDestroy()
        // Désenregistrer le capteur pour économiser la batterie quand le service est arrêté
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
