package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MuscleChip
import com.example.ui.components.VolumeBarChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState

@Composable
fun VolumeScreen(
    uiState: WorkoutUiState
) {
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
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Volume Tracking",
                        tint = ApexEmerald
                    )
                }

                Column {
                    Text(
                        text = "VOLUME TRACKING SYSTEM",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = ApexTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Automated hypertrophy set & tonnage calculations",
                        fontSize = 12.sp,
                        color = ApexTextMuted
                    )
                }
            }
        }

        // Formula Callout Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ApexEmerald,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Volume Formula: Volume = Weight × Reps × Sets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexTextPrimary
                    )
                }
            }
        }

        // Weekly Volume Visual Charts
        item {
            VolumeBarChart(
                muscleVolumes = uiState.muscleVolumes,
                unit = unit
            )
        }

        // Muscle Breakdown List Header
        item {
            Text(
                text = "TARGET MUSCLE BREAKDOWN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ApexTextMuted,
                letterSpacing = 0.8.sp
            )
        }

        // Muscle List Cards
        items(uiState.muscleVolumes) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MuscleChip(muscle = item.muscle)

                        Text(
                            text = item.muscle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexTextPrimary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${item.weeklySets} Sets",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexCyan
                        )

                        Text(
                            text = "${item.weeklyVolumeKg.toInt()} $unit Volume",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ApexEmerald
                        )
                    }
                }
            }
        }
    }
}
