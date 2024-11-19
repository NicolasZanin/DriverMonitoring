package com.drivermonitoring.drivermonitoringsmartwatch.presentation

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.drivermonitoring.drivermonitoringsmartwatch.R
import com.drivermonitoring.drivermonitoringsmartwatch.presentation.theme.DriverMonitoringTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var bleServer: BleServer
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
        // bluetooth

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (!bluetoothManager.adapter?.isEnabled!!) {
            // Gérer le cas où le Bluetooth n'est pas activé
            return
        }

        // Vérifier et demander les permissions nécessaires
        while (!hasRequiredPermissions()) {
            requestPermissions();
        }

        // Initialiser et démarrer le serveur BLE
        bleServer = BleServer(this)
        bleServer.startServer()

        // fin bluetooth

        mediaPlayer = MediaPlayer.create(this, R.raw.reveilmilitaire)

        scheduleNotification()

        startPeriodicVibration()
    }

    private fun startPeriodicVibration() {
        object : CountDownTimer(Long.MAX_VALUE, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val vibrationPattern = longArrayOf(100, 500, 50, 300)
                    val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
                    mediaPlayer?.start()
                    vibrator.vibrate(vibrationEffect)
                } else {
                    val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val vibrationPattern = longArrayOf(100, 500, 100, 300)
                    mediaPlayer?.start()
                    vibrator.vibrate(vibrationPattern, -1)
                }
            }

            override fun onFinish() {
                // Redémarre le timer une fois terminé, ou faire d'autres actions si nécessaire.
                start()
            }
        }.start()
    }


    private fun hasRequiredPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Toutes les permissions sont accordées, démarrer le serveur
                bleServer = BleServer(this)
                bleServer.startServer()
            } else {
                // Gérer le cas où les permissions sont refusées
            }
        }
    }


    private fun scheduleNotification() {
        val workRequest = PeriodicWorkRequestBuilder<BreakNotifier>(
            2, TimeUnit.HOURS,  // Répétition toutes les 2 heures
            10, TimeUnit.MINUTES // Flexibilité de 10 minutes
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueue(workRequest)
    }

    override fun onStart() {
        super.onStart()

        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            // Démarre le service uniquement si les permissions sont accordées
            val serviceIntent = Intent(this, HeartRateService::class.java)
            startForegroundService(serviceIntent)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.BODY_SENSORS), 1001)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Arrêter le service lorsque l'activité est détruite, si nécessaire
        val serviceIntent = Intent(this, HeartRateService::class.java)
        stopService(serviceIntent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}

@Composable
fun WearApp(greetingName: String) {
    DriverMonitoringTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE35B02)),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {

    var imageResource by remember { mutableStateOf(R.drawable.play) } // Corrected state variable type

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                imageResource = if (imageResource == R.drawable.play) {
                    R.drawable.pause // Switch to pause image
                } else {
                    R.drawable.play // Switch to play image
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Use the state variable to determine the image
                Image(
                    painter = painterResource(id = imageResource), // Use the state to dynamically load the image
                    contentDescription = "play/pause button",
                    colorFilter = ColorFilter.tint(Color(0xFFE35B02)),
                    modifier = Modifier
                        .size(50.dp) // Set image size
                        .background(Color(0xFFFFFFFF)) // Optional: background color for the button
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Spacer for spacing between button and text
        Text(
            text = "Heart rate collected for safety",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
    }
}