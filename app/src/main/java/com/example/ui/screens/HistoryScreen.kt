package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.dao.WorkoutLogWithDetails
import com.example.ui.components.MuscleChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    uiState: WorkoutUiState,
    onDeleteLog: (Long) -> Unit
) {
    val logs = uiState.allLogs
    val unit = uiState.userProfile.unit

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Workout History",
                        tint = ApexEmerald
                    )
                }

                Column {
                    Text(
                        text = "WORKOUT HISTORY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = ApexTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Review previous workouts, sets & weight progress",
                        fontSize = 12.sp,
                        color = ApexTextMuted
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No completed workouts logged yet.",
                        fontSize = 14.sp,
                        color = ApexTextMuted
                    )
                }
            }
        } else {
            items(logs, key = { it.log.id }) { logItem ->
                HistoryCard(
                    logWithDetails = logItem,
                    unit = unit,
                    onDelete = { onDeleteLog(logItem.log.id) }
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    logWithDetails: WorkoutLogWithDetails,
    unit: String,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    val log = logWithDetails.log
    val formattedDate = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val totalLogVolume = remember(logWithDetails) {
        logWithDetails.exercisesWithSets.sumOf { ex ->
            ex.sets.filter { it.isCompleted }.sumOf { it.weightKg * it.reps }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ApexCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Log Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ApexEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexEmerald
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Log",
                            tint = ApexCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Expand",
                            tint = ApexTextMuted
                        )
                    }
                }
            }

            Column {
                Text(
                    text = log.dayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ApexTextPrimary
                )
                Text(
                    text = "${log.programTitle} • ${totalLogVolume.toInt()} $unit Total Volume",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ApexTextMuted
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = ApexCardBorder, thickness = 1.dp)

                    logWithDetails.exercisesWithSets.forEach { exWithSets ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ApexInput)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = exWithSets.exercise.exerciseName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ApexTextPrimary
                                )

                                MuscleChip(muscle = exWithSets.exercise.targetMuscle)
                            }

                            exWithSets.sets.forEach { setItem ->
                                Text(
                                    text = "Set ${setItem.setNumber}:  ${setItem.weightKg} $unit × ${setItem.reps} reps",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ApexTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
