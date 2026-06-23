package com.contratop.ipatata

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contratop.ipatata.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.io.File

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever

enum class ScreenState {
    MAIN_MENU,
    MUSIC_LIST,
    SETTINGS,
    THEMES,
    CROSSFADE,
    GAMES,
    SNAKE,
    NOW_PLAYING,
    NO_MUSIC
}

data class ThemeColors(
    val color: Color,
    val wheelColor: Color,
    val centerButtonColor: Color,
    val controlTextColor: Color
)

enum class IPodTheme(
    val color: Color,
    val wheelColor: Color,
    val centerButtonColor: Color,
    val controlTextColor: Color
) {
    GREY(CasingGrey, Color(0xFFEFEFEF), Color(0xFFDCDCDC), Color.Gray),
    BLACK(CasingBlack, Color(0xFF222222), Color(0xFF111111), Color.White),
    BLUE(CasingBlue, Color(0xFFEFEFEF), Color(0xFFDCDCDC), Color.Gray),
    POKE(CasingPoke, Color(0xFFFFF0F5), CasingPoke, CasingPoke),
    HOPPS(CasingHopps, Color(0xFF111111), Color(0xFFD32F2F), Color.White),
    CONTRATOP(CasingContratop, Color(0xFF004D33), Color(0xFFFFFFFF), Color.White),
    DYNAMIC(Color.DarkGray, Color.Black, Color.DarkGray, Color.White)
}

data class Song(
    val title: String, 
    val file: File,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val coverArt: Bitmap? = null
)

class IPodViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ipatata_prefs", android.content.Context.MODE_PRIVATE)

    var currentScreen by mutableStateOf(ScreenState.MAIN_MENU)
        private set
        
    var batteryLevel by mutableStateOf(100)
    var isBatteryCharging by mutableStateOf(false)
    var currentTime by mutableStateOf("")

    var showVolumeBar by mutableStateOf(false)
        private set
    var currentVolumePercentage by mutableStateOf(0f)
        private set
    private var hideVolumeJob: kotlinx.coroutines.Job? = null

    var currentTheme by mutableStateOf(IPodTheme.valueOf(prefs.getString("theme", "GREY") ?: "GREY"))
        private set
        
    var dynamicThemeColors by mutableStateOf<ThemeColors?>(null)
        private set
        
    val activeThemeColors: ThemeColors
        get() {
            if (currentTheme == IPodTheme.DYNAMIC) {
                return dynamicThemeColors ?: ThemeColors(Color.DarkGray, Color.Black, Color.DarkGray, Color.White)
            }
            return ThemeColors(currentTheme.color, currentTheme.wheelColor, currentTheme.centerButtonColor, currentTheme.controlTextColor)
        }

    private fun setTheme(theme: IPodTheme) {
        currentTheme = theme
        prefs.edit().putString("theme", theme.name).apply()
    }

    // Main Menu items
    val mainMenuItems = listOf("Música", "Escuchando ahora", "Juegos", "Ajustes")
    var mainMenuSelection by mutableStateOf(0)
        private set
        
    val settingsItems = listOf("Escanear biblioteca", "Temas", "Crossfade")
    var settingsSelection by mutableStateOf(0)
        private set
        
    val themeItems = listOf("Tema Gris", "Tema Negro", "Tema Azul", "Tema Poke", "Tema Hopps", "Tema Contratop", "Music Dynamics")
    var themeSelection by mutableStateOf(0)
        private set
        
    val crossfadeItems = listOf("Desactivado", "3 segundos", "5 segundos", "7 segundos", "10 segundos")
    var crossfadeSelection by mutableStateOf(0)
        private set
    var crossfadeSeconds by mutableStateOf(prefs.getInt("crossfade", 0))
        private set
    var isCrossfading by mutableStateOf(false)
        private set
        
    val gamesItems = listOf("Snake")
    var gamesSelection by mutableStateOf(0)
        private set
        
    // Music
    var musicList by mutableStateOf<List<Song>>(emptyList())
        private set
    var musicSelection by mutableStateOf(0)
        private set
    var currentSong by mutableStateOf<Song?>(null)
        private set
        
    var currentPositionMs by mutableStateOf(0L)
    var isPlaying by mutableStateOf(false)

    var onPlaySong: ((Song) -> Unit)? = null
    var onTogglePlayPause: (() -> Unit)? = null
    var onSeekTo: ((Long) -> Unit)? = null
    var onFastForward: (() -> Unit)? = null
    var onRewind: (() -> Unit)? = null

    init {
        scanMusic()
    }
    
    fun playNext() {
        if (musicList.isNotEmpty()) {
            musicSelection = (musicSelection + 1) % musicList.size
            val song = musicList[musicSelection]
            currentSong = song
            onPlaySong?.invoke(song)
            loadSongDetails(song)
        }
    }
    
    fun playPrev() {
        if (currentPositionMs > 3000) {
            onSeekTo?.invoke(0L)
            return
        }
        if (musicList.isNotEmpty()) {
            musicSelection = if (musicSelection - 1 < 0) musicList.size - 1 else musicSelection - 1
            val song = musicList[musicSelection]
            currentSong = song
            onPlaySong?.invoke(song)
            loadSongDetails(song)
        }
    }
    
    fun loadSongDetails(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(song.file.absolutePath)
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Artista desconocido"
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Álbum desconocido"
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                val artBytes = retriever.embeddedPicture
                val bitmap = if (artBytes != null) BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size) else null
                
                var newDynamicTheme: ThemeColors? = null
                if (bitmap != null) {
                    val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
                    val dominantInt = palette.getDominantColor(android.graphics.Color.DKGRAY)
                    val dominant = Color(dominantInt)
                    
                    // Slightly darker version of the primary color for the wheel
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(dominantInt, hsv)
                    hsv[2] = (hsv[2] * 0.5f).coerceAtMost(1f) // Darken by 50%
                    val darkerInt = android.graphics.Color.HSVToColor(hsv)
                    val wheelColor = Color(darkerInt)
                    
                    val center = Color(palette.getLightVibrantColor(android.graphics.Color.LTGRAY))
                    val text = Color(palette.getDominantSwatch()?.bodyTextColor ?: android.graphics.Color.WHITE)
                    
                    newDynamicTheme = ThemeColors(dominant, wheelColor, center, text)
                }
                
                val updatedSong = song.copy(artist = artist, album = album, durationMs = duration, coverArt = bitmap)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    currentSong = updatedSong
                    dynamicThemeColors = newDynamicTheme
                }
            } catch (e: Exception) {
            } finally {
                retriever.release()
            }
        }
    }
    
    // Snake Game State
    var snakeBody by mutableStateOf(listOf(Pair(10, 10), Pair(10, 11), Pair(10, 12)))
        private set
    var snakeDirection by mutableStateOf(0) // 0: Up, 1: Right, 2: Down, 3: Left
        private set
    var snakeFood by mutableStateOf(Pair(5, 5))
        private set
    var isSnakeGameOver by mutableStateOf(false)
        private set
    var snakeScore by mutableStateOf(0)
        private set
    private var snakeJob: kotlinx.coroutines.Job? = null

    fun startSnakeGame() {
        snakeBody = listOf(Pair(10, 10), Pair(10, 11), Pair(10, 12))
        snakeDirection = 0
        snakeScore = 0
        isSnakeGameOver = false
        spawnFood()
        
        snakeJob?.cancel()
        snakeJob = viewModelScope.launch {
            while (isActive && !isSnakeGameOver) {
                kotlinx.coroutines.delay(150)
                moveSnake()
            }
        }
    }

    private fun spawnFood() {
        var newFood: Pair<Int, Int>
        do {
            newFood = Pair((0..19).random(), (0..19).random())
        } while (snakeBody.contains(newFood))
        snakeFood = newFood
    }

    private fun moveSnake() {
        val head = snakeBody.first()
        val nextHead = when (snakeDirection) {
            0 -> Pair(head.first, head.second - 1)
            1 -> Pair(head.first + 1, head.second)
            2 -> Pair(head.first, head.second + 1)
            3 -> Pair(head.first - 1, head.second)
            else -> head
        }

        if (nextHead.first !in 0..19 || nextHead.second !in 0..19 || snakeBody.contains(nextHead)) {
            isSnakeGameOver = true
            return
        }

        val newBody = snakeBody.toMutableList()
        newBody.add(0, nextHead)
        
        if (nextHead == snakeFood) {
            snakeScore += 10
            spawnFood()
        } else {
            newBody.removeLast()
        }
        
        snakeBody = newBody
    }

    fun updateSnakeDirection(newDirection: Int) {
        // Prevent 180 degree turns
        if (snakeDirection == 0 && newDirection == 2) return
        if (snakeDirection == 2 && newDirection == 0) return
        if (snakeDirection == 1 && newDirection == 3) return
        if (snakeDirection == 3 && newDirection == 1) return
        
        snakeDirection = newDirection
    }

    fun recoverPlayingSong(song: Song) {
        if (currentSong == null || currentSong?.file != song.file) {
            currentSong = song
            loadSongDetails(song)
        }
    }

    fun scanMusic(showFeedback: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val patataDir = File(downloadsDir, "patatatube")
            
            val newMusicList = if (patataDir.exists() && patataDir.isDirectory) {
                val mp3Files = patataDir.listFiles { file ->
                    file.isFile && file.name.endsWith(".mp3", ignoreCase = true)
                } ?: emptyArray()
                
                mp3Files.map { Song(it.nameWithoutExtension, it) }
            } else {
                emptyList()
            }
            
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                musicList = newMusicList
                if (showFeedback) {
                    android.widget.Toast.makeText(
                        getApplication<android.app.Application>(),
                        "Biblioteca escaneada. ${newMusicList.size} canciones encontradas.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun navigateBack() {
        when (currentScreen) {
            ScreenState.THEMES -> currentScreen = ScreenState.SETTINGS
            ScreenState.CROSSFADE -> currentScreen = ScreenState.SETTINGS
            ScreenState.GAMES -> currentScreen = ScreenState.MAIN_MENU
            ScreenState.SNAKE -> {
                snakeJob?.cancel()
                currentScreen = ScreenState.GAMES
            }
            ScreenState.SETTINGS, ScreenState.MUSIC_LIST, ScreenState.NOW_PLAYING, ScreenState.NO_MUSIC -> currentScreen = ScreenState.MAIN_MENU
            else -> {}
        }
    }

    fun selectCurrentItem() {
        when (currentScreen) {
            ScreenState.MAIN_MENU -> {
                when (mainMenuSelection) {
                    0 -> {
                        scanMusic()
                        currentScreen = if (musicList.isEmpty()) ScreenState.NO_MUSIC else ScreenState.MUSIC_LIST
                    }
                    1 -> {
                        if (currentSong != null) {
                            currentScreen = ScreenState.NOW_PLAYING
                        }
                    }
                    2 -> currentScreen = ScreenState.GAMES
                    3 -> currentScreen = ScreenState.SETTINGS
                }
            }
            ScreenState.SETTINGS -> {
                when (settingsSelection) {
                    0 -> {
                        scanMusic(showFeedback = true)
                    }
                    1 -> currentScreen = ScreenState.THEMES
                    2 -> currentScreen = ScreenState.CROSSFADE
                }
            }
            ScreenState.THEMES -> {
                when (themeSelection) {
                    0 -> setTheme(IPodTheme.GREY)
                    1 -> setTheme(IPodTheme.BLACK)
                    2 -> setTheme(IPodTheme.BLUE)
                    3 -> setTheme(IPodTheme.POKE)
                    4 -> setTheme(IPodTheme.HOPPS)
                    5 -> setTheme(IPodTheme.CONTRATOP)
                    6 -> setTheme(IPodTheme.DYNAMIC)
                }
            }
            ScreenState.CROSSFADE -> {
                when (crossfadeSelection) {
                    0 -> setCrossfade(0)
                    1 -> setCrossfade(3)
                    2 -> setCrossfade(5)
                    3 -> setCrossfade(7)
                    4 -> setCrossfade(10)
                }
                currentScreen = ScreenState.SETTINGS
            }
            ScreenState.GAMES -> {
                when (gamesSelection) {
                    0 -> {
                        currentScreen = ScreenState.SNAKE
                        startSnakeGame()
                    }
                }
            }
            ScreenState.SNAKE -> {
                if (isSnakeGameOver) {
                    startSnakeGame()
                }
            }
            ScreenState.MUSIC_LIST -> {
                if (musicList.isNotEmpty()) {
                    val song = musicList[musicSelection]
                    currentSong = song
                    currentScreen = ScreenState.NOW_PLAYING
                    onPlaySong?.invoke(song)
                    loadSongDetails(song)
                }
            }
            else -> {}
        }
    }

    fun scrollMenu(isClockwise: Boolean) {
        when (currentScreen) {
            ScreenState.MAIN_MENU -> mainMenuSelection = updateSelection(mainMenuSelection, mainMenuItems.size, isClockwise)
            ScreenState.MUSIC_LIST -> if (musicList.isNotEmpty()) musicSelection = updateSelection(musicSelection, musicList.size, isClockwise)
            ScreenState.SETTINGS -> settingsSelection = updateSelection(settingsSelection, settingsItems.size, isClockwise)
            ScreenState.THEMES -> themeSelection = updateSelection(themeSelection, themeItems.size, isClockwise)
            ScreenState.CROSSFADE -> crossfadeSelection = updateSelection(crossfadeSelection, crossfadeItems.size, isClockwise)
            ScreenState.GAMES -> gamesSelection = updateSelection(gamesSelection, gamesItems.size, isClockwise)
            else -> {}
        }
    }

    private fun updateSelection(current: Int, size: Int, clockwise: Boolean): Int {
        val direction = if (clockwise) 1 else -1
        return (current + direction).coerceIn(0, size - 1)
    }
    
    private fun setCrossfade(seconds: Int) {
        crossfadeSeconds = seconds
        prefs.edit().putInt("crossfade", seconds).apply()
    }

    fun adjustVolume(increase: Boolean) {
        val audioManager = getApplication<android.app.Application>().getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        
        val newVolume = if (increase) {
            (currentVolume + 1).coerceAtMost(maxVolume)
        } else {
            (currentVolume - 1).coerceAtLeast(0)
        }
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
        
        currentVolumePercentage = newVolume.toFloat() / maxVolume.toFloat()
        showVolumeBar = true
        
        hideVolumeJob?.cancel()
        hideVolumeJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            showVolumeBar = false
        }
    }

    fun executeCrossfade(controller: androidx.media3.common.Player, context: android.content.Context) {
        if (isCrossfading) return
        isCrossfading = true
        
        val currentMediaItem = controller.currentMediaItem
        if (currentMediaItem == null) {
            isCrossfading = false
            return
        }
        val currentPos = controller.currentPosition
        
        val fadeOutPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(context).build()
        fadeOutPlayer.setMediaItem(currentMediaItem)
        fadeOutPlayer.seekTo(currentPos)
        fadeOutPlayer.volume = 1.0f
        fadeOutPlayer.prepare()
        fadeOutPlayer.play()
        
        controller.volume = 0.0f
        playNext()
        
        viewModelScope.launch {
            val steps = 20
            val delayPerStep = (crossfadeSeconds * 1000L) / steps
            for (i in 0..steps) {
                if (!isActive) break
                val fraction = i.toFloat() / steps.toFloat()
                fadeOutPlayer.volume = 1.0f - fraction
                controller.volume = fraction
                kotlinx.coroutines.delay(delayPerStep)
            }
            controller.volume = 1.0f
            fadeOutPlayer.release()
            isCrossfading = false
        }
    }
}
