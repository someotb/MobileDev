package com.example.mycal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mycal.ui.theme.Rose

class AudioFile(val id: Long, val name: String, val data: String)

fun fetchAudioFiles(context: Context): List<AudioFile> {
    val audioList = mutableListOf<AudioFile>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DATA
    )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val cursor = context.contentResolver.query(uri, projection, selection, null, null)
    cursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        while (it.moveToNext()) {
            audioList += AudioFile(
                it.getLong(idCol),
                it.getString(nameCol),
                it.getString(dataCol)
            )
        }
    }
    return audioList
}

class MusicPlayer : ComponentActivity() {
    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        permissionLauncher.launch(permission)

        setContent {
            MusicPlayerScreen(hasPermission)
        }
    }
}

@Composable
fun MusicPlayerScreen(hasPermission: Boolean) {
    val context = LocalContext.current
    var audioList by remember { mutableStateOf(emptyList<AudioFile>()) }
    var currentIndex by remember { mutableStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            audioList = fetchAudioFiles(context)
        }
    }

    fun playSong(audioFile: AudioFile) {
        mediaPlayer?.release()
        val uri = Uri.parse(audioFile.data)
        mediaPlayer = MediaPlayer.create(context, uri)?.apply {
            start()
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
        }
    }
    fun playNext() {
        if (audioList.isNotEmpty()) {
            currentIndex = (currentIndex + 1).coerceAtMost(audioList.lastIndex).let {
                if (it > audioList.lastIndex) 0 else it
            }
            playSong(audioList[currentIndex])
        }
    }
    fun playPrevious() {
        if (audioList.isNotEmpty()) {
            currentIndex = (currentIndex - 1).coerceAtLeast(0).let {
                if (it < 0) audioList.lastIndex else it
            }
            playSong(audioList[currentIndex])
        }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        if (!hasPermission) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Нет разрешения на доступ к аудиофайлам",
                    color = Color.White, fontSize = 20.sp
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Music Player", color = Color.White, fontSize = 24.sp)
                Row(
                    Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { playPrevious() },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(6.dp)
                    ) { Text("Prev", color = Color.White, fontSize = 12.sp) }

                    Button(
                        onClick = {
                            mediaPlayer?.let {
                                if (it.isPlaying) it.pause() else it.start()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(6.dp)
                    ) { Text("Play/Pause", color = Color.White, fontSize = 9.sp) }

                    Button(
                        onClick = { playNext() },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(6.dp)
                    ) { Text("Next", color = Color.White, fontSize = 12.sp) }
                }

                Button(
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(Rose),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(8.dp)
                ) {
                    Text("Go to calculator", color = Color.White, fontSize = 15.sp)
                }

                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    items(audioList) { audioFile ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentIndex = audioList.indexOf(audioFile)
                                    playSong(audioFile)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                audioFile.name,
                                color = if (audioList.indexOf(audioFile) == currentIndex) Rose else Color.White,
                                fontSize = 16.sp
                            )
                        }
                        Divider(color = Color.Gray)
                    }
                }
            }
        }
    }
}
