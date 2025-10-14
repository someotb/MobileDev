package com.example.mycal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.sp
import com.example.mycal.activities.LocationActivity
import com.example.mycal.ui.theme.MycalTheme
import com.example.mycal.ui.theme.Rose
import com.example.mycal.ui.theme.Russian_Violete

class MainPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MycalTheme {
                MainPageScreen()
            }
        }
    }
}

@Composable
fun MainPageScreen() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Russian_Violete
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                    )
                },
                colors = ButtonDefaults.buttonColors(Rose),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Go to Calculator", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, MusicPlayer::class.java)
                    )
                },
                colors = ButtonDefaults.buttonColors(Rose),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Go to Music Player", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, LocationActivity::class.java)
                    )
                },
                colors = ButtonDefaults.buttonColors(Rose),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Go to Map", color = Color.White, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, ClientServer::class.java)
                    )
                },
                colors = ButtonDefaults.buttonColors(Rose),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text("Go to Server-Client", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
