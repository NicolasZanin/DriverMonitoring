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
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit

class HeartRateService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var powerConsumption: Float = 0f

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "heart_rate_channel"
        const val NOTIFICATION_ID = 1
        private const val PORT = "1880"
        const val IP = "192.168.181.32:$PORT"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.e("HeartRateService", "onStartCommand")
        startForeground(NOTIFICATION_ID, createNotification())

        // Initialisation du SensorManager et du capteur de rythme cardiaque
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)


        if (heartRateSensor != null) {
            sensorManager.registerListener(
                this,
                heartRateSensor,
                1_000_000
            ) // Période de 1 seconde d'échantillonage
            incrementPowerConsumption(heartRateSensor)
            Log.d("Sensor", "Power consumption: $powerConsumption mA")
        } else {
            Log.e("HeartRateService", "Capteur de rythme cardiaque non disponible")
            stopSelf()
        }
        return START_STICKY;
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartRate = event.values[0]
                if (heartRate > 0) {
                    val currentTimestamp = Instant.now().toEpochMilli()
                    CoroutineScope(Dispatchers.Main).launch {
                        val result =
                            fetchUrl("http://$IP/app/cardiaque/$heartRate/$currentTimestamp")
                        println(result)
                        getPowerConsumptionUntilNow()
                    }
                }
                Log.d("HeartRateService", "Rythme cardiaque en arrière-plan : $heartRate")
            }
        }
    }

    private fun createNotification(): Notification {
        val channelName = "Background Service"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
                )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Driver Monitoring")
            .setContentText("Getting your heart-rate in background...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Gérer les changements de précision du capteur
    }

    private fun incrementPowerConsumption(sensor: Sensor?) {
        val sensorPower = sensor?.power
        Log.d("Power consumption", "$sensorPower")
        powerConsumption += sensor?.power ?: 0f
    }

    private fun getPowerConsumptionUntilNow() {
        println("$powerConsumption mA")
    }

    private suspend fun fetchUrl(url: String): String? {
        val DURATION_UNTIL_TIMEOUT = 5L;

        val client = OkHttpClient().newBuilder()
            .connectTimeout(DURATION_UNTIL_TIMEOUT, TimeUnit.SECONDS)    // Timeout pour la connexion
            .readTimeout(DURATION_UNTIL_TIMEOUT, TimeUnit.SECONDS)       // Timeout pour la lecture
            .writeTimeout(DURATION_UNTIL_TIMEOUT, TimeUnit.SECONDS)      // Timeout pour l'écriture
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        return withContext(Dispatchers.IO) { // Switch to IO thread for network call
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string() // Return the response body as a string
                } else {
                    updateSamplingPeriod(10_000_000)
                    null // Handle error response
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null // Handle exception
            }
        }
    }

    private fun updateSamplingPeriod(samplingPeriod: Int) {
        sensorManager.unregisterListener(this) // Désinscrire d'abord
        heartRateSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                samplingPeriod,  // Nouvelle période
                0
            )
        }
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