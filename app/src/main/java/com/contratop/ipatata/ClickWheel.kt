package com.contratop.ipatata

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun ClickWheel(
    theme: ThemeColors,
    modifier: Modifier = Modifier,
    onScroll: (Boolean) -> Unit, // true = clockwise, false = counter-clockwise
    onClickCenter: () -> Unit,
    onClickMenu: () -> Unit,
    onClickPlayPause: () -> Unit,
    onClickNext: () -> Unit,
    onClickPrev: () -> Unit
) {
    var centerPoint by remember { mutableStateOf(Offset.Zero) }
    var lastAngle by remember { mutableStateOf<Double?>(null) }
    val haptic = LocalHapticFeedback.current
    
    // Sensitivity threshold for scrolling (in degrees)
    val angleThreshold = 25.0

    Box(
        modifier = modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(theme.wheelColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val radius = size.width / 2f
                        val center = Offset(radius, radius)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                        
                        // Center button click (approx 30% of radius)
                        if (distance < radius * 0.35f) {
                            onClickCenter()
                            return@detectTapGestures
                        }
                        
                        // Determine which quadrant was clicked
                        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        // Angle: 0 is Right, 90 is Bottom, 180 is Left, -90 is Top
                        when {
                            angle > -135 && angle <= -45 -> onClickMenu() // Top
                            angle > -45 && angle <= 45 -> onClickNext() // Right
                            angle > 45 && angle <= 135 -> onClickPlayPause() // Bottom
                            else -> onClickPrev() // Left
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val radius = size.width / 2f
                        centerPoint = Offset(radius, radius)
                        val dx = offset.x - centerPoint.x
                        val dy = offset.y - centerPoint.y
                        lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val dx = change.position.x - centerPoint.x
                        val dy = change.position.y - centerPoint.y
                        val currentAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        
                        lastAngle?.let { last ->
                            var diff = currentAngle - last
                            // Normalize diff
                            if (diff > 180) diff -= 360
                            if (diff < -180) diff += 360
                            
                            if (Math.abs(diff) > angleThreshold) {
                                onScroll(diff > 0) // Clockwise if diff > 0
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastAngle = currentAngle
                            }
                        }
                    },
                    onDragEnd = {
                        lastAngle = null
                    }
                )
            }
    ) {
        // Labels
        Text("MENU", modifier = Modifier.align(Alignment.TopCenter).padding(16.dp), color = theme.controlTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("▶||", modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), color = theme.controlTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(">>|", modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp), color = theme.controlTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("|<<", modifier = Modifier.align(Alignment.CenterStart).padding(16.dp), color = theme.controlTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        // Center Button
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(theme.centerButtonColor)
        )
    }
}
