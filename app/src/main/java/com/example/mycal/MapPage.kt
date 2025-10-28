package com.example.mycal.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mycal.MainPage
import com.example.mycal.ui.theme.MycalTheme
import com.example.mycal.ui.theme.Rose
import com.example.mycal.ui.theme.Russian_Violete
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class LocationActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MycalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Russian_Violete) {
                    LocationScreen()
                }
            }
        }
    }

    @Composable
    fun LocationScreen() {
        val context = LocalContext.current

        var latText by remember { mutableStateOf("—") }
        var lonText by remember { mutableStateOf("—") }
        var hasPermission by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
            if (!granted) {
                Toast.makeText(
                    context,
                    "Без разрешения получение локации невозможно",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        LaunchedEffect(hasPermission) {
            if (hasPermission) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (!enabled) {
                    Toast.makeText(
                        context,
                        "Включите геолокацию в настройках, пожалуйста",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        val client = LocationServices.getFusedLocationProviderClient(context)
                        client.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    latText = location.latitude.toString()
                                    lonText = location.longitude.toString()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Не удалось получить локацию",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Ошибка при получении локации",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Разрешение на геолокацию не получено",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Текущие координаты",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))
                Text("Широта: $latText", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text("Долгота: $lonText", style = MaterialTheme.typography.bodyLarge, color = Color.White)

                Button(
                    onClick = {
                        saveCoordsToDownloads(context, latText, lonText)
                    },
                    colors = ButtonDefaults.buttonColors(Rose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 10.dp)
                ) {
                    Text("Save coords to Downloads", color = Color.White)
                }
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, MainPage::class.java))
                },
                colors = ButtonDefaults.buttonColors(Rose),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Back to Main", color = Color.White)
            }
        }
    }

    private fun saveCoordsToDownloads(context: Context, lat: String, lon: String) {
        val json = JSONObject().apply {
            put("latitude", lat)
            put("longitude", lon)
            put("timestamp", System.currentTimeMillis())
        }
        val filename = "coords_${System.currentTimeMillis()}.json"
        val bytes = json.toString(2).toByteArray()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            )
            if (uri != null) {
                context.contentResolver.openOutputStream(uri).use { it?.write(bytes) }
                Toast.makeText(
                    context,
                    "Координаты сохранены в Downloads/$filename",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, "Не удалось создать файл в Downloads", Toast.LENGTH_SHORT).show()
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            try {
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { it.write(bytes) }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("application/json"),
                    null
                )
                Toast.makeText(
                    context,
                    "Координаты сохранены в Downloads/${file.name}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
