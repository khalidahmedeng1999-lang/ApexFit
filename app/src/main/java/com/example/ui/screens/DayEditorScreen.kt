package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.db.entities.ProgramExercise
import com.example.data.repository.WorkoutRepository
import com.example.ui.components.MuscleChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorScreen(
    dayWithExercises: DayWithExercises,
    onBack: () -> Unit,
    onAddExercise: (name: String, muscle: String, sets: Int, restSec: Int, repsRange: String, rir: String, notes: String, category: String) -> Unit,
    onUpdateExercise: (ProgramExercise) -> Unit,
    onDeleteExercise: (Long) -> Unit
) {
    var showAddModal by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<ProgramExercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = dayWithExercises.day.dayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexTextPrimary
                        )
                        Text(
                            text = "${dayWithExercises.exercises.size} Exercises / Activities",
                            fontSize = 12.sp,
                            color = ApexTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ApexTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddModal = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(ApexEmerald, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Exercise",
                            tint = ApexBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ApexBackground)
            )
        },
        containerColor = ApexBackground
    ) { paddingValues ->
        if (dayWithExercises.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No Exercises Added",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextPrimary
                    )
                    Text(
                        text = "Tap '+' to add exercises, cardio, or core drills with custom notes, reps, and RIR.",
                        fontSize = 13.sp,
                        color = ApexTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { showAddModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Exercise / Activity", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dayWithExercises.exercises, key = { it.id }) { ex ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ApexCard),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ex.exerciseName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ApexTextPrimary,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if (ex.category.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ex.category,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ApexEmerald
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MuscleChip(muscle = ex.targetMuscle)

                                    Text(
                                        text = "${ex.defaultSets} sets • ${ex.repsRange}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ApexEmerald
                                    )

                                    if (ex.rir.isNotBlank() && ex.rir != "N/A") {
                                        Text(
                                            text = "RIR: ${ex.rir}",
                                            fontSize = 12.sp,
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
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { editingExercise = ex }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = ApexEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteExercise(ex.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ApexCoral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddModal || editingExercise != null) {
        ExerciseEditModal(
            existingExercise = editingExercise,
            onDismiss = {
                showAddModal = false
                editingExercise = null
            },
            onSave = { name, muscle, sets, rest, reps, rir, notes, category ->
                if (editingExercise != null) {
                    onUpdateExercise(
                        editingExercise!!.copy(
                            exerciseName = name,
                            targetMuscle = muscle,
                            defaultSets = sets,
                            restSeconds = rest,
                            repsRange = reps,
                            rir = rir,
                            notes = notes,
                            category = category
                        )
                    )
                } else {
                    onAddExercise(name, muscle, sets, rest, reps, rir, notes, category)
                }
                showAddModal = false
                editingExercise = null
            }
        )
    }
}

@Composable
fun ExerciseEditModal(
    existingExercise: ProgramExercise?,
    onDismiss: () -> Unit,
    onSave: (name: String, muscle: String, sets: Int, restSec: Int, repsRange: String, rir: String, notes: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf(existingExercise?.exerciseName ?: "") }
    var selectedMuscle by remember { mutableStateOf(existingExercise?.targetMuscle ?: "Chest") }
    var category by remember { mutableStateOf(existingExercise?.category ?: "Compound") }
    var repsRange by remember { mutableStateOf(existingExercise?.repsRange ?: "8–12") }
    var rir by remember { mutableStateOf(existingExercise?.rir ?: "1–2") }
    var notes by remember { mutableStateOf(existingExercise?.notes ?: "") }
    var sets by remember { mutableStateOf(existingExercise?.defaultSets ?: 3) }
    var restSec by remember { mutableStateOf(existingExercise?.restSeconds ?: 120) }

    val muscleOptions = WorkoutRepository.ALL_MUSCLES
    val categoryOptions = listOf("Compound", "Isolation", "Core", "Power", "Conditioning", "Zone 2", "Rest")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingExercise == null) "Add Exercise / Activity" else "Edit Exercise / Activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ApexTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Exercise Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("EXERCISE / ACTIVITY NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Incline Bench, Stationary Bike, Farmer Carry", fontSize = 12.sp, color = ApexTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Category Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoryOptions) { cat ->
                            val isSelected = cat.equals(category, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ApexEmerald else ApexInput)
                                    .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ApexBackground else ApexTextPrimary
                                )
                            }
                        }
                    }
                }

                // Target Muscle Picker Tabs
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TARGET MUSCLE / BODY PART", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(muscleOptions) { muscle ->
                            val isSelected = muscle.equals(selectedMuscle, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ApexEmerald else ApexInput)
                                    .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedMuscle = muscle }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = muscle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ApexBackground else ApexTextPrimary
                                )
                            }
                        }
                    }
                }

                // Sets, Reps & RIR Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sets
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("SETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = sets.toString(),
                            onValueChange = { sets = it.toIntOrNull() ?: 1 },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexEmerald,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Reps / Duration
                    Column(
                        modifier = Modifier.weight(1.5f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("REPS / DURATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = repsRange,
                            onValueChange = { repsRange = it },
                            placeholder = { Text("e.g. 8-12 or 20 min", fontSize = 11.sp, color = ApexTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexEmerald,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // RIR
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("RIR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = rir,
                            onValueChange = { rir = it },
                            placeholder = { Text("e.g. 1-2", fontSize = 11.sp, color = ApexTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexEmerald,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Rest & Notes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("REST SECONDS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    OutlinedTextField(
                        value = restSec.toString(),
                        onValueChange = { restSec = it.toIntOrNull() ?: 60 },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Coaching Cues & Notes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("COACHING NOTES / CUES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Keep spine neutral, drive elbows down...", fontSize = 12.sp, color = ApexTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, selectedMuscle, sets, restSec, repsRange, rir, notes, category)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE EXERCISE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ApexTextMuted)
            }
        },
        containerColor = ApexCard,
        shape = RoundedCornerShape(20.dp)
    )
}
