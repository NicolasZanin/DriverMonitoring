package com.drivermonitoring.drivermonitoringsmartwatch.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.drivermonitoring.drivermonitoringsmartwatch.R
import com.drivermonitoring.drivermonitoringsmartwatch.presentation.theme.DriverMonitoringTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
        scheduleNotification()
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
}

@Composable
fun WearApp(greetingName: String) {
    DriverMonitoringTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.hello_world, greetingName),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espace entre le texte et le bouton
        Button(onClick = { /* Action du bouton */ }) {
            Text(text = stringResource(R.string.driver_monitoring_hello))
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}