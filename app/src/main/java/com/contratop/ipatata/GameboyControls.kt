package com.contratop.ipatata

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contratop.ipatata.ThemeColors

@Composable
fun GameboyControls(
    theme: ThemeColors,
    viewModel: IPodViewModel,
    modifier: Modifier = Modifier
) {
    val dpadColor = theme.wheelColor
    val buttonColor = theme.centerButtonColor
    val textColor = theme.controlTextColor

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val rawScaleX = maxWidth.value / 360f
        val rawScaleY = maxHeight.value / 280f
        val scale = minOf(1f, minOf(rawScaleX, rawScaleY))

        val dpadBoxSize = (156 * scale).dp
        val dpadW = (52 * scale).dp
        val dpadH = (156 * scale).dp
        val btnSize = (60 * scale).dp
        val btnBorder = (3 * scale).dp
        val actionSpace = (36 * scale).dp
        val padB = (50 * scale).dp
        val padA = (10 * scale).dp
        val btnText = (24 * scale).sp
        val lblText = (12 * scale).sp
        val selW = (48 * scale).dp
        val selH = (14 * scale).dp
        val selText = (10 * scale).sp
        
        // MAIN CONTROLS GROUP (Centered)
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: D-Pad & A/B Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // D-PAD
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(dpadBoxSize)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull()
                                    if (change != null && change.pressed) {
                                        val x = change.position.x
                                        val y = change.position.y
                                        val w = size.width
                                        
                                        val isTop = y < x && y < w - x
                                        val isBottom = y > x && y > w - x
                                        val isLeft = x < y && x < w - y
                                        val isRight = x > y && x > w - y
                                        
                                        if (isTop) viewModel.updateSnakeDirection(0)
                                        else if (isRight) viewModel.updateSnakeDirection(1)
                                        else if (isBottom) viewModel.updateSnakeDirection(2)
                                        else if (isLeft) viewModel.updateSnakeDirection(3)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Vertical bar
                    Box(
                        modifier = Modifier
                            .width(dpadW)
                            .height(dpadH)
                            .clip(RoundedCornerShape(6.dp))
                            .background(dpadColor)
                    )
                    // Horizontal bar
                    Box(
                        modifier = Modifier
                            .width(dpadH)
                            .height(dpadW)
                            .clip(RoundedCornerShape(6.dp))
                            .background(dpadColor)
                    )
                }
                
                // Action Buttons
                Column(
                    modifier = Modifier.height(140.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(actionSpace),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Button B (Reserved) - LOWER
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = padB)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(btnSize)
                                    .clip(CircleShape)
                                    .background(buttonColor)
                                    .border(btnBorder, dpadColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("B", color = textColor, fontWeight = FontWeight.Bold, fontSize = btnText)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(" ", color = dpadColor, fontSize = lblText, fontWeight = FontWeight.Bold)
                        }
                        
                        // Button A - HIGHER
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = padA)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(btnSize)
                                    .clip(CircleShape)
                                    .background(buttonColor)
                                    .border(btnBorder, dpadColor, CircleShape)
                                    .clickable { viewModel.selectCurrentItem() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", color = textColor, fontWeight = FontWeight.Bold, fontSize = btnText)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ACCION", color = dpadColor, fontSize = lblText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Row 2: Select and Start Buttons
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Select
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .rotate(-20f)
                            .size(width = selW, height = selH)
                            .clip(RoundedCornerShape(7.dp))
                            .background(dpadColor.copy(alpha = 0.8f))
                            .clickable { viewModel.navigateBack() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SELECT", color = dpadColor, fontSize = selText, fontWeight = FontWeight.Bold)
                }
                // Start
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .rotate(-20f)
                            .size(width = selW, height = selH)
                            .clip(RoundedCornerShape(7.dp))
                            .background(dpadColor.copy(alpha = 0.8f))
                            .clickable { viewModel.startSnakeGame() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("START", color = dpadColor, fontSize = selText, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // SPEAKER GRILL (Pinned to bottom right)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 4.dp)
                .rotate(-20f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPlaying = viewModel.isPlaying
            val infiniteTransition = rememberInfiniteTransition()
            
            val activeColors = listOf(
                androidx.compose.ui.graphics.Color(0xFFFF3B30), // Red
                androidx.compose.ui.graphics.Color(0xFFFF9500), // Orange
                androidx.compose.ui.graphics.Color(0xFFFFCC00), // Yellow
                androidx.compose.ui.graphics.Color(0xFF4CD964), // Green
                androidx.compose.ui.graphics.Color(0xFF5AC8FA), // Light Blue
                androidx.compose.ui.graphics.Color(0xFF007AFF), // Blue
                androidx.compose.ui.graphics.Color(0xFF5856D6)  // Purple
            )

            repeat(6) { index ->
                val animatedHeight = infiniteTransition.animateFloat(
                    initialValue = 20f,
                    targetValue = 80f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 300 + (index * 70), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 150)
                    )
                )
                
                val activeHeight = if (isPlaying) animatedHeight.value else 80f
                val barColor = if (isPlaying) activeColors[index % activeColors.size] else dpadColor.copy(alpha = 0.4f)
                
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(activeHeight.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }
        }
    }
}
