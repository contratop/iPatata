package com.contratop.ipatata

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.contratop.ipatata.ThemeColors

@Composable
fun MiniPlayerBubble(
    viewModel: IPodViewModel,
    theme: ThemeColors,
    modifier: Modifier = Modifier
) {
    val songTitle = viewModel.currentSong?.title ?: "Nada reproduciendo"
    
    Row(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .height(60.dp)
            .clip(CircleShape)
            .background(theme.wheelColor.copy(alpha = 0.9f))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "🎵",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = songTitle,
            color = theme.controlTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⏮",
                fontSize = 18.sp,
                color = theme.controlTextColor,
                modifier = Modifier.clickable { viewModel.playPrev() }
            )
            Text(
                text = "⏯",
                fontSize = 20.sp,
                color = theme.controlTextColor,
                modifier = Modifier.clickable { viewModel.onTogglePlayPause?.invoke() }
            )
            Text(
                text = "⏭",
                fontSize = 18.sp,
                color = theme.controlTextColor,
                modifier = Modifier.clickable { viewModel.playNext() }
            )
        }
    }
}
