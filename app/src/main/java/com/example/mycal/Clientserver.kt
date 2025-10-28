package com.example.mycal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mycal.ui.theme.MycalTheme
import com.example.mycal.ui.theme.Rose
import com.example.mycal.ui.theme.Russian_Violete
import org.zeromq.SocketType
import org.zeromq.ZMQ
import com.google.gson.Gson
//import com.example.mycal.model.DeviceData
import com.example.mycal.model.*
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

private var periodicTimer: Timer? = null

fun getMockDeviceData(): DeviceData {
    val location = LocationData(
        latitude = 55.7558,
        longitude = 37.6173,
        altitude = 200.0,
        timestamp = System.currentTimeMillis(),
        speed = 0.0f,
        accuracy = 5.0f
    )

    val cellIdentity = CellIdentityLte(
        band = 3,
        cellIdentity = 12345,
        earfcn = 6300,
        mcc = 250,
        mnc = 99,
        pci = 100,
        tac = 1234
    )

    val signal = CellSignalStrengthLte(
        asuLevel = 20,
        cqi = 10,
        rsrp = -95,
        rsrq = -10,
        rssi = -70,
        rssnr = 30,
        timingAdvance = 5
    )

    val cellInfo = CellInfoLte(
        cellIdentityLte = cellIdentity,
        cellSignalStrengthLte = signal
    )

    return DeviceData(location, listOf(cellInfo))
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

    val context = LocalContext.current

    Surface (
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
            Column (
                modifier = Modifier
                    .fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { StartClient() }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Send message to Server")
                }

                Button(onClick = { startPeriodicSending() }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Start Periodic Sending")
                }

                Button(onClick = { stopPeriodicSending() }, colors = ButtonDefaults.buttonColors(Rose)) {
                    Text("Stop Periodic Sending")
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
}
fun StartClient() {
    val context = ZMQ.context(1)
    val socket = context.socket(SocketType.REQ)

    val deviceData = getMockDeviceData()

    val gson = Gson()
    val jsonString = gson.toJson(deviceData)

    println("Sending JSON to server:")
    println(jsonString)

    socket.connect("tcp://10.0.2.2:2222")
    socket.send(jsonString)

    val reply = socket.recvStr()
    println("Received reply from server: $reply")

    socket.close()
    context.close()
}

fun startPeriodicSending() {
    if (periodicTimer != null) {
        println("Periodic Timer already running!")
        return
    }

    val context = ZMQ.context(1)
    val socket = context.socket(SocketType.REQ)
    socket.connect("tcp://10.0.2.2:2222")

    val gson = Gson()

    periodicTimer = Timer()
    periodicTimer!!.scheduleAtFixedRate(0, 1000) {
        try {
            val deviceData = getMockDeviceData()
            val jsonString = gson.toJson(deviceData)

            println("Sending JSON at ${System.currentTimeMillis()}")
            socket.send(jsonString)

            val reply = socket.recvStr()
            println("Server replied: $reply")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}

fun stopPeriodicSending() {
    periodicTimer?.cancel()
    periodicTimer = null
    println("Periodic Timer stopped")
}
