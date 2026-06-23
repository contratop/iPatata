package com.contratop.ipatata

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import kotlinx.coroutines.delay
import java.io.File
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.contratop.ipatata.playback.PlaybackService
import com.contratop.ipatata.ui.theme.IPatataTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    
    private val viewModel: IPodViewModel by viewModels()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            
            if (level != -1 && scale != -1) {
                viewModel.batteryLevel = (level * 100) / scale
            }
            viewModel.isBatteryCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }
    }

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var onPlaybackEnded: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestStoragePermissions()
        
        setContent {
            IPatataTheme {
                onPlaybackEnded = { viewModel.playNext() }
                
                // Set up media actions
                viewModel.onPlaySong = { song ->
                    mediaController?.let { controller ->
                        val mediaItem = MediaItem.fromUri(android.net.Uri.fromFile(song.file))
                        controller.setMediaItem(mediaItem)
                        controller.prepare()
                        controller.play()
                    }
                }
                
                viewModel.onTogglePlayPause = {
                    mediaController?.let { controller ->
                        if (controller.isPlaying) {
                            controller.pause()
                        } else {
                            controller.play()
                        }
                    }
                }
                
                viewModel.onSeekTo = { position ->
                    mediaController?.seekTo(position)
                }
                viewModel.onFastForward = {
                    mediaController?.let { controller ->
                        controller.seekTo(controller.currentPosition + 3000)
                    }
                }
                viewModel.onRewind = {
                    mediaController?.let { controller ->
                        controller.seekTo((controller.currentPosition - 3000).coerceAtLeast(0L))
                    }
                }
                
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    while (isActive) {
                        viewModel.currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                        mediaController?.let { controller ->
                            viewModel.isPlaying = controller.isPlaying
                            if (controller.isPlaying) {
                                viewModel.currentPositionMs = controller.currentPosition
                                
                                val crossfadeSecs = viewModel.crossfadeSeconds
                                if (crossfadeSecs > 0) {
                                    val duration = controller.duration
                                    val position = controller.currentPosition
                                    val timeLeft = duration - position
                                    if (duration > 0 && timeLeft > 0 && timeLeft <= crossfadeSecs * 1000L && !viewModel.isCrossfading) {
                                        viewModel.executeCrossfade(controller, this@MainActivity)
                                    }
                                }
                            }
                        }
                        delay(1000)
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IPodApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            val controller = controllerFuture?.get()
            mediaController = controller
            
            val uri = controller?.currentMediaItem?.localConfiguration?.uri
            if (uri != null && uri.path != null) {
                val file = File(uri.path!!)
                val song = Song(file.nameWithoutExtension, file)
                viewModel.recoverPlayingSong(song)
            }
            
            controller?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackEnded?.invoke()
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
    
    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE
            )
        }
        requestPermissionLauncher.launch(permissions)
    }
}
