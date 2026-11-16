package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MuscleChip
import com.example.ui.components.RestTimerBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveExerciseState
import com.example.ui.viewmodel.ActiveTrainingSession
import com.example.ui.viewmodel.RestTimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLoggerScreen(
    session: ActiveTrainingSession,
    restTimer: RestTimerState,
    unit: String = "KG",
    onAddSet: (exerciseIndex: Int) -> Unit,
    onRemoveSet: (exerciseIndex: Int, setIndex: Int) -> Unit,
    onUpdateWeight: (exerciseIndex: Int, setIndex: Int, weight: Double) -> Unit,
    onUpdateReps: (exerciseIndex: Int, setIndex: Int, reps: Int) -> Unit,
    onToggleSetCompleted: (exerciseIndex: Int, setIndex: Int) -> Unit,
    onToggleRestTimer: () -> Unit,
    onAddRestSeconds: (Int) -> Unit,
    onDismissRestTimer: () -> Unit,
    onFinishWorkout: () -> Unit,
    onCancelWorkout: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val elapsedMinutes = session.elapsedSeconds / 60
    val elapsedSecondsRem = session.elapsedSeconds % 60
    val formattedDuration = String.format("%02d:%02d", elapsedMinutes, elapsedSecondsRem)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session.dayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexTextPrimary
                        )
                        Text(
                            text = "${session.programTitle} • $formattedDuration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ApexEmerald
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Workout", tint = ApexCoral)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ApexBackground)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ApexCard,
                tonalElevation = 12.dp
            ) {
                Column {
                    // Rest Timer Overlay if active
                    RestTimerBar(
                        remainingSeconds = restTimer.remainingSeconds,
                        totalSeconds = restTimer.totalSeconds,
                        isRunning = restTimer.isRunning,
                        onTogglePlayPause = onToggleRestTimer,
                        onAddSeconds = onAddRestSeconds,
                        onDismiss = onDismissRestTimer
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onFinishWorkout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("finish_workout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "FINISH & SAVE WORKOUT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = ApexBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(session.exercises) { exIdx, exState ->
                ExerciseLoggerCard(
                    exerciseState = exState,
                    unit = unit,
                    onAddSet = { onAddSet(exIdx) },
                    onRemoveSet = { setIdx -> onRemoveSet(exIdx, setIdx) },
                    onUpdateWeight = { setIdx, w -> onUpdateWeight(exIdx, setIdx, w) },
                    onUpdateReps = { setIdx, r -> onUpdateReps(exIdx, setIdx, r) },
                    onToggleSetCompleted = { setIdx -> onToggleSetCompleted(exIdx, setIdx) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Discard Workout?", fontWeight = FontWeight.Bold, color = ApexTextPrimary) },
            text = { Text("Are you sure you want to discard this live workout session? Logged data will be lost.", color = ApexTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelWorkout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexCoral, contentColor = ApexTextPrimary)
                ) {
                    Text("Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Continue Training", color = ApexEmerald)
                }
            },
            containerColor = ApexCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ExerciseLoggerCard(
    exerciseState: ActiveExerciseState,
    unit: String,
    onAddSet: () -> Unit,
    onRemoveSet: (setIndex: Int) -> Unit,
    onUpdateWeight: (setIndex: Int, weight: Double) -> Unit,
    onUpdateReps: (setIndex: Int, reps: Int) -> Unit,
    onToggleSetCompleted: (setIndex: Int) -> Unit
) {
    val ex = exerciseState.exercise

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
            // Exercise Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = ex.exerciseName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MuscleChip(muscle = ex.targetMuscle)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = ApexTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Rest: ${ex.restSeconds}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ApexTextMuted
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (ex.repsRange.isNotBlank()) {
                            Text(
                                text = "Target Reps: ${ex.repsRange}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexEmerald
                            )
                        }
                        if (ex.rir.isNotBlank() && ex.rir != "N/A") {
                            Text(
                                text = "RIR: ${ex.rir}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexGold
                            )
                        }
                    }

                    if (ex.notes.isNotBlank()) {
                        Text(
                            text = "💡 ${ex.notes}",
                            fontSize = 11.sp,
                            color = ApexTextMuted,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Divider(color = ApexCardBorder, thickness = 1.dp)

            // Sets Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "SET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, modifier = Modifier.weight(0.8f))
                Text(text = "WEIGHT ($unit)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, modifier = Modifier.weight(1.5f))
                Text(text = "REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, modifier = Modifier.weight(1.2f))
                Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, modifier = Modifier.weight(0.8f))
            }

            // Sets List
            exerciseState.sets.forEachIndexed { setIdx, setItem ->
                var weightInput by remember(setItem.weightKg) {
                    mutableStateOf(if (setItem.weightKg > 0) setItem.weightKg.toString() else "")
                }
                var repsInput by remember(setItem.reps) {
                    mutableStateOf(if (setItem.reps > 0) setItem.reps.toString() else "")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (setItem.isCompleted) ApexEmerald.copy(alpha = 0.08f) else ApexInput)
                        .border(1.dp, if (setItem.isCompleted) ApexEmerald.copy(alpha = 0.4f) else ApexCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Set Number
                    Row(
                        modifier = Modifier.weight(0.8f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${setIdx + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (setItem.isCompleted) ApexEmerald else ApexTextPrimary
                        )

                        if (exerciseState.sets.size > 1) {
                            IconButton(
                                onClick = { onRemoveSet(setIdx) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Set",
                                    tint = ApexCoral.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Weight Input
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { input ->
                            weightInput = input
                            val parsed = input.toDoubleOrNull() ?: 0.0
                            onUpdateWeight(setIdx, parsed)
                        },
                        placeholder = { Text("0", color = ApexTextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(end = 6.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexBackground,
                            unfocusedContainerColor = ApexBackground,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Reps Input
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { input ->
                            repsInput = input
                            val parsed = input.toIntOrNull() ?: 0
                            onUpdateReps(setIdx, parsed)
                        },
                        placeholder = { Text("0", color = ApexTextMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(end = 6.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexBackground,
                            unfocusedContainerColor = ApexBackground,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Checkbox Button
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (setItem.isCompleted) ApexEmerald else ApexCardBorder)
                                .clickable { onToggleSetCompleted(setIdx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete Set",
                                tint = if (setItem.isCompleted) ApexBackground else ApexTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // DYNAMIC SET CREATION "+" BUTTON
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("add_set_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexEmerald),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ApexEmerald.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Set", tint = ApexEmerald)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Add Set ${exerciseState.sets.size + 1}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexEmerald
                )
            }
        }
    }
}
