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

    socket.connect("tcp://10.0.2.2:2222")
    socket.send("Hello Server!")

    val reply = socket.recvStr()
    println("Received reply from server: $reply")

    socket.close()
    context.close()
}
