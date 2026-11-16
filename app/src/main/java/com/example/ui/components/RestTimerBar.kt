package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun RestTimerBar(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onAddSeconds: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalSeconds <= 0 && remainingSeconds <= 0) return

    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    AnimatedVisibility(
        visible = remainingSeconds > 0 || isRunning,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, ApexEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = ApexCard,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ApexEmerald.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Rest Timer",
                                tint = ApexEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "REST TIMER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexTextMuted,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = timeFormatted,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (remainingSeconds <= 5 && isRunning) ApexCoral else ApexTextPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick +30s
                        OutlinedButton(
                            onClick = { onAddSeconds(30) },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(ApexCardBorder)
                            )
                        ) {
                            Text("+30s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                        }

                        // Play/Pause
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(36.dp)
                                .background(ApexEmerald, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                tint = ApexBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Dismiss
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Rest Timer",
                                tint = ApexTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = ApexEmerald,
                    trackColor = ApexInput
                )
            }
        }
    }
}
