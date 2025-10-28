package com.example.mycal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mycal.model.*
import com.example.mycal.ui.theme.MycalTheme
import com.example.mycal.ui.theme.Rose
import com.example.mycal.ui.theme.Russian_Violete
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import org.zeromq.SocketType
import org.zeromq.ZMQ
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import android.telephony.TelephonyManager
import android.telephony.CellInfoLte
import android.telephony.CellSignalStrengthLte
import android.telephony.CellIdentityLte

private var periodicTimer: Timer? = null

fun getRealDeviceData(context: Context, callback: (DeviceData?) -> Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        callback(null)
        return
    }

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val locData = LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                timestamp = location.time,
                speed = location.speed,
                accuracy = location.accuracy
            )

            // Получаем LTE-ячейки
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val cellInfoList = mutableListOf<com.example.mycal.model.CellInfoLte>()

            telephonyManager.allCellInfo?.forEach { cell ->
                if (cell is android.telephony.CellInfoLte && cell.isRegistered) {
                    val identity = cell.cellIdentity
                    val signalStrength = cell.cellSignalStrength

                    val cellIdentityModel = CellIdentityLte(
                        band = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) identity.bands?.firstOrNull() ?: -1 else -1,
                        cellIdentity = identity.ci,
                        earfcn = identity.earfcn,
                        mcc = identity.mcc,
                        mnc = identity.mnc,
                        pci = identity.pci,
                        tac = identity.tac
                    )

                    val cellSignalModel = com.example.mycal.model.CellSignalStrengthLte(
                        asuLevel = signalStrength.asuLevel,
                        cqi = signalStrength.cqi,
                        rsrp = signalStrength.rsrp,
                        rsrq = signalStrength.rsrq,
                        rssi = signalStrength.rssi,
                        rssnr = signalStrength.rssnr,
                        timingAdvance = signalStrength.timingAdvance
                    )

                    cellInfoList.add(com.example.mycal.model.CellInfoLte(cellIdentityModel, cellSignalModel))
                }
            }

            val deviceData = DeviceData(locData, cellInfoList)
            callback(deviceData)
        } else {
            callback(null)
        }
    }.addOnFailureListener {
        callback(null)
    }
}

class ClientServer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MycalTheme {
                Clientserver()
            }
        }
    }
}

@Composable
fun Clientserver() {
    val androidContext = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Russian_Violete
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { startClient(androidContext) }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Send message to Server")
                }

                Button(onClick = { startPeriodicSending(androidContext) }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Start Periodic Sending")
                }

                Button(onClick = { stopPeriodicSending() }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Stop Periodic Sending")
                }
            }

            Button(
                onClick = {
                    androidContext.startActivity(Intent(androidContext, MainPage::class.java))
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
}

fun startClient(androidContext: Context) {
    val zmqContext = ZMQ.context(1)

    getRealDeviceData(androidContext) { deviceData ->
        if (deviceData != null) {
            val gson = Gson()
            val jsonString = gson.toJson(deviceData)

            println("Sending JSON to server:")
            println(jsonString)

            val socket = zmqContext.socket(SocketType.REQ)
            socket.connect("tcp://10.0.2.2:2222")
            socket.send(jsonString)
            val reply = socket.recvStr()
            println("Received reply from server: $reply")
            socket.close()
            zmqContext.close()
        } else {
            println("Failed to get device data")
        }
    }
}

private var periodicSocket: ZMQ.Socket? = null

fun startPeriodicSending(androidContext: Context) {
    if (periodicTimer != null) return

    val zmqContext = ZMQ.context(1)
    periodicSocket = zmqContext.socket(SocketType.REQ)
    periodicSocket?.connect("tcp://10.0.2.2:2222")

    val gson = Gson()

    periodicTimer = Timer()
    periodicTimer!!.scheduleAtFixedRate(0, 1000) {
        getRealDeviceData(androidContext) { deviceData ->
            if (deviceData != null) {
                val jsonString = gson.toJson(deviceData)
                println("Sending JSON at ${System.currentTimeMillis()}")
                periodicSocket?.send(jsonString)
                val reply = periodicSocket?.recvStr()
                println("Server replied: $reply")
            }
        }
    }
}

fun stopPeriodicSending() {
    periodicTimer?.cancel()
    periodicTimer = null
    periodicSocket?.close()
    periodicSocket = null
    println("Periodic Timer stopped")
}
