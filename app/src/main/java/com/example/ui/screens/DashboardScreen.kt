package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.dao.DayWithExercises
import com.example.ui.components.MuscleChip
import com.example.ui.components.SkeletonDashboardScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState
import java.util.Calendar

@Composable
fun DashboardScreen(
    uiState: WorkoutUiState,
    onStartWorkout: (DayWithExercises, String) -> Unit,
    onCreateProgramClick: () -> Unit,
    onNavigateToVolume: () -> Unit
) {
    if (uiState.isLoading) {
        SkeletonDashboardScreen()
        return
    }

    val activeProg = uiState.activeProgram
    val profile = uiState.userProfile
    val unit = profile.unit

    // Today's day name (e.g. "Sunday", "Monday"...)
    val todayName = remember {
        val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val cal = Calendar.getInstance()
        days[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }

    val todayScheduledDay = activeProg?.daysWithExercises?.firstOrNull {
        it.day.dayOfWeek.equals(todayName, ignoreCase = true)
    } ?: activeProg?.daysWithExercises?.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Branding
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.apexfit_logo),
                        contentDescription = "APEXFIT Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column {
                        Text(
                            text = "APEXFIT EG",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexTextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Welcome back, ${profile.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ApexTextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, ApexEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = profile.goal.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ApexEmerald
                    )
                }
            }
        }

        // Stats Summary Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weekly Volume Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ApexCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("WEEKLY VOLUME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, letterSpacing = 0.6.sp)
                        Text(
                            text = "${uiState.totalWeeklyVolumeKg.toInt()} $unit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexEmerald
                        )
                        Text("Weight × Reps × Sets", fontSize = 9.sp, color = ApexTextMuted)
                    }
                }

                // Weekly Sets Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ApexCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("TOTAL SETS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, letterSpacing = 0.6.sp)
                        Text(
                            text = "${uiState.totalWeeklySets}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexCyan
                        )
                        Text("Completed Sets", fontSize = 9.sp, color = ApexTextMuted)
                    }
                }

                // Program Count Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ApexCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("PROGRAMS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted, letterSpacing = 0.6.sp)
                        Text(
                            text = "${uiState.allPrograms.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexAmber
                        )
                        Text("Custom Splits", fontSize = 9.sp, color = ApexTextMuted)
                    }
                }
            }
        }

        // Today's Scheduled Workout Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E2836),
                                Color(0xFF131720)
                            )
                        )
                    )
                    .border(1.5.dp, ApexEmerald.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ApexEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ACTIVE PROGRAM: ${activeProg?.program?.title ?: "No Active Program"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ApexEmerald,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = todayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexTextMuted
                        )
                    }

                    if (todayScheduledDay != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = todayScheduledDay.day.dayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = ApexTextPrimary
                            )

                            Text(
                                text = "${todayScheduledDay.exercises.size} Exercises Planned",
                                fontSize = 13.sp,
                                color = ApexTextMuted
                            )
                        }

                        // Target muscles preview
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            todayScheduledDay.exercises.map { it.targetMuscle }.distinct().take(4).forEach { muscle ->
                                MuscleChip(muscle = muscle)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                onStartWorkout(todayScheduledDay, activeProg?.program?.title ?: "Custom Program")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_today_workout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START TRAINING", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No Workout Scheduled",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexTextPrimary
                            )
                            Text(
                                text = "Create or select a workout program to build your training schedule.",
                                fontSize = 13.sp,
                                color = ApexTextMuted
                            )
                            Button(
                                onClick = onCreateProgramClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Workout Program", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Volume Highlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToVolume() },
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(ApexCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Volume Analytics",
                                tint = ApexCyan
                            )
                        }

                        Column {
                            Text(
                                text = "Volume Analytics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexTextPrimary
                            )
                            Text(
                                text = "Track sets & volume per target muscle group",
                                fontSize = 12.sp,
                                color = ApexTextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to Volume",
                        tint = ApexTextMuted
                    )
                }
            }
        }
    }
}
