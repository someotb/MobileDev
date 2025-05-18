package com.example.mycal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mycal.ui.theme.MycalTheme
import com.example.mycal.ui.theme.Rose
import com.example.mycal.ui.theme.Russian_Violete

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MycalTheme {
                MusicPlayerScreen()
            }
        }
    }
}

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
        launcher.launch(perm)
    }

    var audioList by remember { mutableStateOf(emptyList<AudioFile>()) }
    var currentIndex by remember { mutableStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            audioList = fetchAudioFiles(context)
        }
    }

    fun playSong(audioFile: AudioFile) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFile.data)
            prepare()
            start()
            setOnCompletionListener {
                isPlaying = false
                release()
                mediaPlayer = null
            }
        }
        isPlaying = true
    }

    fun playNext() {
        if (currentIndex < audioList.lastIndex) {
            currentIndex++
            playSong(audioList[currentIndex])
        }
    }

    fun playPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            playSong(audioList[currentIndex])
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                mediaPlayer?.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Russian_Violete) {
        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Разрешение не получено", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { playPrevious() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous", tint = Rose)
                        }
                        IconButton(
                            onClick = {
                                mediaPlayer?.let { player ->
                                    if (player.isPlaying) {
                                        player.pause()
                                        isPlaying = false
                                    } else {
                                        player.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Rose, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { playNext() }) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = Rose)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(audioList) { file ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentIndex = audioList.indexOf(file)
                                        playSong(file)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = file.name,
                                    color = if (audioList.indexOf(file) == currentIndex) Rose else Color.White
                                )
                            }
                        }
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
}
