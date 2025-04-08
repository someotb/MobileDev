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
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mycal.ui.theme.Rose

data class AudioFile(val id: Long, val name: String, val data: String)

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
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val name = it.getString(nameColumn)
            val data = it.getString(dataColumn)
            audioList.add(AudioFile(id, name, data))
        }
    }
    return audioList
}

@Composable
fun RequestAudioPermission(onPermissionResult: (Boolean) -> Unit) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_AUDIO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(permission)
    }
}

class MusicPlayer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerScreen()
        }
    }
}

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var audioList by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    RequestAudioPermission { granted ->
        hasPermission = granted
    }

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
            currentIndex = if (currentIndex < audioList.size - 1) currentIndex + 1 else 0
            playSong(audioList[currentIndex])
        }
    }

    fun playPrevious() {
        if (audioList.isNotEmpty()) {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else audioList.size - 1
            playSong(audioList[currentIndex])
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Нет разрешения на доступ к аудиофайлам", color = Color.White, fontSize = 20.sp)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Music Player", color = Color.White, fontSize = 24.sp)

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Button(
                        onClick = { playPrevious() },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .padding(6.dp)
                            .height(56.dp)
                            .weight(1f)
                    ) {
                        Text("Prev", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            mediaPlayer?.let { player ->
                                if (player.isPlaying) {
                                    player.pause()
                                } else {
                                    player.start()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .padding(6.dp)
                            .height(56.dp)
                            .weight(1f)
                    ) {
                        Text("Play/Pause", color = Color.White, fontSize = 9.sp)
                    }
                    Button(
                        onClick = { playNext() },
                        colors = ButtonDefaults.buttonColors(Rose),
                        modifier = Modifier
                            .padding(6.dp)
                            .height(56.dp)
                            .weight(1f)
                    ) {
                        Text("Next", color = Color.White, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(Rose),
                    modifier = Modifier
                        .padding(8.dp)
                        .height(56.dp)
                ) {
                    Text("Go to calculator", color = Color.White, fontSize = 15.sp)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    items(audioList) { audioFile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentIndex = audioList.indexOf(audioFile)
                                    playSong(audioFile)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = audioFile.name,
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
