package com.contratop.ipatata

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.contratop.ipatata.ui.theme.*

@Composable
fun IPodScreenDisplay(viewModel: IPodViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ScreenBackground)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ScreenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBackground)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = viewModel.currentTime,
                    color = ScreenText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                
                Text(
                    text = when (viewModel.currentScreen) {
                        ScreenState.MAIN_MENU -> "iPatata"
                        ScreenState.MUSIC_LIST -> "Música"
                        ScreenState.SETTINGS -> "Ajustes"
                        ScreenState.THEMES -> "Temas"
                        ScreenState.CROSSFADE -> "Crossfade"
                        ScreenState.GAMES -> "Juegos"
                        ScreenState.SNAKE -> "Snake"
                        ScreenState.NOW_PLAYING -> "Reproduciendo"
                        ScreenState.NO_MUSIC -> "Error"
                    },
                    color = ScreenText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.isBatteryCharging) {
                        Text("⚡", fontSize = 10.sp, color = ScreenText)
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = "${viewModel.batteryLevel}%",
                        color = ScreenText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Battery Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 8.dp)
                                .border(1.dp, ScreenText, RoundedCornerShape(1.dp))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(viewModel.batteryLevel / 100f)
                                    .background(ScreenText)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 2.dp, height = 4.dp)
                                .background(ScreenText, RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
                        )
                    }
                }
            }
            
            androidx.compose.material3.Divider(
                color = ScreenText,
                thickness = 1.dp
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ScreenBackground)
            ) {
                when (viewModel.currentScreen) {
                    ScreenState.MAIN_MENU -> MenuList(
                        items = viewModel.mainMenuItems,
                        selectedIndex = viewModel.mainMenuSelection
                    )
                    ScreenState.SETTINGS -> MenuList(
                        items = viewModel.settingsItems,
                        selectedIndex = viewModel.settingsSelection
                    )
                    ScreenState.THEMES -> MenuList(
                        items = viewModel.themeItems,
                        selectedIndex = viewModel.themeSelection
                    )
                    ScreenState.CROSSFADE -> MenuList(
                        items = viewModel.crossfadeItems,
                        selectedIndex = viewModel.crossfadeSelection
                    )
                    ScreenState.GAMES -> MenuList(
                        items = viewModel.gamesItems,
                        selectedIndex = viewModel.gamesSelection
                    )
                    ScreenState.SNAKE -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Puntuación: ${viewModel.snakeScore}", color = ScreenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(ScreenHighlight)
                                    .padding(2.dp)
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val cellWidth = size.width / 20f
                                    val cellHeight = size.height / 20f
                                    
                                    // Draw food
                                    drawRect(
                                        color = Color.Red,
                                        topLeft = androidx.compose.ui.geometry.Offset(viewModel.snakeFood.first * cellWidth, viewModel.snakeFood.second * cellHeight),
                                        size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight)
                                    )
                                    
                                    // Draw snake
                                    viewModel.snakeBody.forEach { segment ->
                                        drawRect(
                                            color = ScreenBackground,
                                            topLeft = androidx.compose.ui.geometry.Offset(segment.first * cellWidth, segment.second * cellHeight),
                                            size = androidx.compose.ui.geometry.Size(cellWidth - 1f, cellHeight - 1f)
                                        )
                                    }
                                }
                                
                                if (viewModel.isSnakeGameOver) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                        Text("GAME OVER\nPulsa A para continuar", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                    ScreenState.MUSIC_LIST -> {
                        MenuList(
                            items = viewModel.musicList.map { it.title },
                            selectedIndex = viewModel.musicSelection
                        )
                    }
                    ScreenState.NOW_PLAYING -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        ) {
                            // Top part: Cover art and text
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover Art
                                val bitmap = viewModel.currentSong?.coverArt
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Cover Art",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.size(100.dp).background(Color.DarkGray)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(100.dp).background(Color.Gray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("♫", fontSize = 48.sp, color = Color.White)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Text Info
                                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewModel.currentSong?.title ?: "Desconocido",
                                        color = ScreenText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = viewModel.currentSong?.artist ?: "Artista",
                                        color = ScreenText,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = viewModel.currentSong?.album ?: "Álbum",
                                        color = ScreenText,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                
                                // Visualizer
                                if (viewModel.isPlaying) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                                    Row(
                                        modifier = Modifier.height(24.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        repeat(4) { index ->
                                            val heightAnim = infiniteTransition.animateFloat(
                                                initialValue = 4f,
                                                targetValue = 24f,
                                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                    animation = androidx.compose.animation.core.tween(
                                                        durationMillis = 300 + (index * 50),
                                                        easing = androidx.compose.animation.core.LinearEasing
                                                    ),
                                                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                                                    initialStartOffset = androidx.compose.animation.core.StartOffset(index * 150)
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(heightAnim.value.dp)
                                                    .background(ScreenText)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Bottom part: Progress bar or Volume bar
                            if (viewModel.showVolumeBar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔉", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.weight(1f).height(12.dp).background(Color.LightGray)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxHeight().fillMaxWidth(viewModel.currentVolumePercentage).background(ScreenText)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("🔊", fontSize = 12.sp)
                                }
                            } else {
                                val currentPos = viewModel.currentPositionMs
                                val duration = viewModel.currentSong?.durationMs ?: 0L
                                val progress = if (duration > 0) currentPos.toFloat() / duration.toFloat() else 0f
                                
                                fun formatTime(ms: Long): String {
                                    val totalSecs = ms / 1000
                                    val mins = totalSecs / 60
                                    val secs = totalSecs % 60
                                    return String.format("%d:%02d", mins, secs)
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(formatTime(currentPos), color = ScreenText, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                            .background(Color.LightGray)
                                            .pointerInput(duration) {
                                                if (duration > 0L) {
                                                    detectTapGestures { offset ->
                                                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                        viewModel.onSeekTo?.invoke((fraction * duration).toLong())
                                                    }
                                                }
                                            }
                                            .pointerInput(duration) {
                                                if (duration > 0L) {
                                                    detectDragGestures { change, _ ->
                                                        val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                        viewModel.onSeekTo?.invoke((fraction * duration).toLong())
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0f, 1f)).background(Color(0xFF3B82F6))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val remaining = duration - currentPos
                                    Text("-" + formatTime(remaining.coerceAtLeast(0L)), color = ScreenText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    ScreenState.NO_MUSIC -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "No se encontró música en\nDescargas/Patatatube.",
                                color = ScreenText,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "¡Usa la app Patatatube para descargar nuevas canciones!",
                                color = ScreenText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuList(items: List<String>, selectedIndex: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) ScreenHighlight else ScreenBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item,
                    color = if (isSelected) ScreenHighlightText else ScreenText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }
    }
}
