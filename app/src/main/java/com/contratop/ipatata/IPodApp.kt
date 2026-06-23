package com.contratop.ipatata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.app.Activity

@Composable
fun IPodApp(viewModel: IPodViewModel) {
    val theme = viewModel.activeThemeColors
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        val color = theme.color
        val isLight = color.luminance() > 0.5f
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            window.navigationBarColor = color.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.color), // Make outer background same as theme just in case
        contentAlignment = Alignment.Center
    ) {
        // iPod Casing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.color)
                .padding(24.dp), // Regular padding, the status bar will take its own space
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen container to hold original layout space
            Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IPodScreenDisplay(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes whatever space remains, keeping ratio dynamic
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (viewModel.currentScreen == ScreenState.SNAKE) {
                    MiniPlayerBubble(
                        viewModel = viewModel, 
                        theme = theme
                    )
                } else {
                    // Empty space to keep the screen size consistent even without the bubble
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            if (viewModel.currentScreen == ScreenState.SNAKE) {
                GameboyControls(theme = theme, viewModel = viewModel, modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(56.dp)) // Move wheel down a bit (was 40)
    
                // Click Wheel
                ClickWheel(
                    theme = theme,
                    onScroll = { isClockwise ->
                        if (viewModel.currentScreen == ScreenState.NOW_PLAYING) {
                            viewModel.adjustVolume(isClockwise)
                        } else {
                            viewModel.scrollMenu(isClockwise)
                        }
                    },
                    onClickCenter = { viewModel.selectCurrentItem() },
                    onClickMenu = { viewModel.navigateBack() },
                    onClickPlayPause = { viewModel.onTogglePlayPause?.invoke() },
                    onClickNext = { viewModel.playNext() },
                    onClickPrev = { viewModel.playPrev() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
