package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CreateProgramDialog(
    onDismiss: () -> Unit,
    onCreateProgram: (title: String, selectedDays: List<String>, cycleDays: Int) -> Unit,
    onLoadApexFitHybrid: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSystemMode by remember { mutableStateOf("14") } // "7" for 7-day week, "14" for 14-day cycle

    val standard7Days = remember { listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday") }
    val standard14Days = remember {
        listOf(
            "Week 1 - Saturday",
            "Week 1 - Sunday",
            "Week 1 - Monday",
            "Week 1 - Tuesday",
            "Week 1 - Wednesday",
            "Week 1 - Thursday",
            "Week 1 - Friday",
            "Week 2 - Saturday",
            "Week 2 - Sunday",
            "Week 2 - Monday",
            "Week 2 - Tuesday",
            "Week 2 - Wednesday",
            "Week 2 - Thursday",
            "Week 2 - Friday"
        )
    }

    val selectedDays = remember { mutableStateListOf<String>() }

    LaunchedEffect(selectedSystemMode) {
        selectedDays.clear()
        if (selectedSystemMode == "7") {
            selectedDays.addAll(standard7Days)
        } else {
            selectedDays.addAll(standard14Days)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = null,
        containerColor = ApexCard,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                                .size(40.dp)
                                .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Create Workout",
                                tint = ApexEmerald
                            )
                        }
                        Column {
                            Text(
                                text = "Create Workout System",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexTextPrimary
                            )
                            Text(
                                text = "7-Day Weekly or 14-Day Cycle",
                                fontSize = 11.sp,
                                color = ApexTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                // Featured 14-Day Preset Loader Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ApexEmerald.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ApexEmerald)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ApexEmerald, modifier = Modifier.size(18.dp))
                            Text(
                                text = "APEXFIT HYBRID ATHLETE (14-DAY SYSTEM)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ApexEmerald
                            )
                        }
                        Text(
                            text = "Instant 2-week continuous train/rest program with pre-configured exercises, RIR targets, reps, and lumbar-safe coaching cues.",
                            fontSize = 11.sp,
                            color = ApexTextPrimary
                        )
                        Button(
                            onClick = {
                                onLoadApexFitHybrid()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "LOAD APEXFIT HYBRID ATHLETE (14 DAYS)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // System Type Selector (7-Day vs 14-Day)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SYSTEM ARCHITECTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("14" to "14-Day Continuous Cycle", "7" to "7-Day Weekly Split").forEach { (mode, label) ->
                            val isSelected = selectedSystemMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ApexEmerald else ApexInput)
                                    .clickable { selectedSystemMode = mode }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ApexBackground else ApexTextMuted
                                )
                            }
                        }
                    }
                }

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CUSTOM PROGRAM TITLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextMuted,
                        letterSpacing = 0.8.sp
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                text = if (selectedSystemMode == "14") "e.g., ApexFit Hyper Athlete, 14-Day Recomp" else "e.g., Push Pull Legs, Arnold Split",
                                fontSize = 12.sp,
                                color = ApexTextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Days Selection List
                val displayList = if (selectedSystemMode == "14") standard14Days else standard7Days
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (selectedSystemMode == "14") "14-DAY SYSTEM DAYS" else "SELECT WEEKLY DAYS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextMuted,
                        letterSpacing = 0.8.sp
                    )

                    displayList.forEach { dayLabel ->
                        val isChecked = selectedDays.contains(dayLabel)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isChecked) ApexEmerald.copy(alpha = 0.1f) else ApexInput)
                                .border(
                                    width = 1.dp,
                                    color = if (isChecked) ApexEmerald else ApexCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isChecked) selectedDays.remove(dayLabel) else selectedDays.add(dayLabel)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChecked) ApexTextPrimary else ApexTextMuted
                            )

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChecked) ApexEmerald else ApexCardBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ApexBackground,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Create Action Button
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank {
                            if (selectedSystemMode == "14") "Custom 14-Day System" else "Custom Weekly Split"
                        }
                        if (selectedDays.isNotEmpty()) {
                            onCreateProgram(finalTitle, selectedDays.toList(), selectedSystemMode.toIntOrNull() ?: 7)
                            onDismiss()
                        }
                    },
                    enabled = selectedDays.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_create_program_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApexEmerald,
                        contentColor = ApexBackground,
                        disabledContainerColor = ApexCardBorder,
                        disabledContentColor = ApexTextMuted
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "CREATE ${selectedSystemMode}-DAY SYSTEM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    )
}
