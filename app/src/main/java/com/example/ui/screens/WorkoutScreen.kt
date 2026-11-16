package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.dao.DayWithExercises
import com.example.data.db.dao.ProgramWithDays
import com.example.ui.components.MuscleChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState

@Composable
fun WorkoutScreen(
    uiState: WorkoutUiState,
    onCreateProgramClick: () -> Unit,
    onSelectDayToEdit: (DayWithExercises) -> Unit,
    onStartWorkout: (DayWithExercises, String) -> Unit,
    onSwitchActiveProgram: (Long) -> Unit,
    onDeleteProgram: (Long) -> Unit
) {
    val activeProg = uiState.activeProgram
    var showProgramSelectorModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Program Header & Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = ApexEmerald
                                )
                            }

                            Column {
                                Text(
                                    text = "PROGRAM TITLE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ApexTextMuted,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = activeProg?.program?.title ?: "No Program Selected",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ApexTextPrimary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { showProgramSelectorModal = true },
                                modifier = Modifier
                                    .background(ApexInput, RoundedCornerShape(10.dp))
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Program",
                                    tint = ApexEmerald
                                )
                            }
                        }
                    }

                    // Create Program FAB / Button
                    Button(
                        onClick = onCreateProgramClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("create_workout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CREATE WORKOUT PROGRAM",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Days in Program
        if (activeProg != null && activeProg.daysWithExercises.isNotEmpty()) {
            items(activeProg.daysWithExercises, key = { it.day.id }) { dayWithEx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ApexCard),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dayWithEx.day.dayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ApexTextPrimary
                                )
                                Text(
                                    text = "${dayWithEx.day.dayOfWeek} • ${dayWithEx.exercises.size} Exercises",
                                    fontSize = 12.sp,
                                    color = ApexTextMuted
                                )
                            }

                            TextButton(
                                onClick = { onSelectDayToEdit(dayWithEx) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ApexEmerald)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Day", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Exercises", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Exercises summary
                        if (dayWithEx.exercises.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dayWithEx.exercises.forEach { ex ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ApexInput)
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ex.exerciseName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ApexTextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )

                                            MuscleChip(muscle = ex.targetMuscle)
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${ex.defaultSets} Sets × ${ex.repsRange}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ApexEmerald
                                            )
                                            if (ex.rir.isNotBlank() && ex.rir != "N/A") {
                                                Text(
                                                    text = "RIR: ${ex.rir}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ApexGold
                                                )
                                            }
                                            if (ex.restSeconds > 0) {
                                                Text(
                                                    text = "Rest: ${ex.restSeconds}s",
                                                    fontSize = 11.sp,
                                                    color = ApexTextMuted
                                                )
                                            }
                                        }

                                        if (ex.notes.isNotBlank()) {
                                            Text(
                                                text = "💡 ${ex.notes}",
                                                fontSize = 11.sp,
                                                color = ApexTextMuted,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No exercises added to this day yet. Tap 'Edit Exercises' to add your custom workout routines.",
                                fontSize = 12.sp,
                                color = ApexTextMuted,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        // Start Training Button
                        Button(
                            onClick = { onStartWorkout(dayWithEx, activeProg.program.title) },
                            enabled = dayWithEx.exercises.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_day_workout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexEmerald,
                                contentColor = ApexBackground,
                                disabledContainerColor = ApexInput,
                                disabledContentColor = ApexTextMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START ${dayWithEx.day.dayOfWeek.uppercase()} WORKOUT", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        } else if (activeProg != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workout days created for this program.",
                        fontSize = 14.sp,
                        color = ApexTextMuted
                    )
                }
            }
        }
    }

    // Program Selector Modal
    if (showProgramSelectorModal) {
        AlertDialog(
            onDismissRequest = { showProgramSelectorModal = false },
            title = {
                Text("Select Active Program", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.allPrograms.forEach { prog ->
                        val isSelected = prog.program.id == activeProg?.program?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ApexEmerald.copy(alpha = 0.15f) else ApexInput)
                                .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    onSwitchActiveProgram(prog.program.id)
                                    showProgramSelectorModal = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = prog.program.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ApexEmerald else ApexTextPrimary
                                )
                                Text(
                                    text = "${prog.daysWithExercises.size} Workout Days",
                                    fontSize = 12.sp,
                                    color = ApexTextMuted
                                )
                            }

                            if (uiState.allPrograms.size > 1) {
                                IconButton(onClick = { onDeleteProgram(prog.program.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ApexCoral)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProgramSelectorModal = false }) {
                    Text("Close", color = ApexEmerald)
                }
            },
            containerColor = ApexCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
