package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.MuscleVolumeData
import com.example.ui.theme.*

@Composable
fun VolumeBarChart(
    muscleVolumes: List<MuscleVolumeData>,
    unit: String = "KG",
    modifier: Modifier = Modifier
) {
    if (muscleVolumes.isEmpty()) return

    val maxVolume = (muscleVolumes.maxOfOrNull { maxOf(it.weeklyVolumeKg, it.previousWeekVolumeKg) } ?: 1.0).coerceAtLeast(100.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ApexCard, RoundedCornerShape(20.dp))
            .border(1.5.dp, ApexCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MUSCLE VOLUME DISTRIBUTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexEmerald,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Current Week vs Previous Week",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ApexTextMuted
                )
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(ApexEmerald, RoundedCornerShape(2.dp)))
                    Text("Current", fontSize = 10.sp, color = ApexTextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(ApexCardBorder, RoundedCornerShape(2.dp)))
                    Text("Prev", fontSize = 10.sp, color = ApexTextMuted)
                }
            }
        }

        muscleVolumes.filter { it.weeklySets > 0 || it.previousWeekSets > 0 }.forEach { item ->
            val animatedFractionCurrent by animateFloatAsState(
                targetValue = (item.weeklyVolumeKg / maxVolume).toFloat().coerceIn(0.02f, 1f),
                animationSpec = tween(600),
                label = "barCurrent"
            )

            val animatedFractionPrev by animateFloatAsState(
                targetValue = (item.previousWeekVolumeKg / maxVolume).toFloat().coerceIn(0.02f, 1f),
                animationSpec = tween(600),
                label = "barPrev"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.muscle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexTextPrimary
                        )
                        MuscleChip(muscle = item.muscle)
                    }

                    Text(
                        text = "${item.weeklySets} Sets • ${item.weeklyVolumeKg.toInt()} $unit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexEmerald
                    )
                }

                // Current Week Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(ApexInput)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedFractionCurrent)
                            .clip(RoundedCornerShape(5.dp))
                            .background(getMuscleColor(item.muscle))
                    )
                }

                // Previous Week Bar (Subtle comparison)
                if (item.previousWeekVolumeKg > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ApexInput)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedFractionPrev)
                                .clip(RoundedCornerShape(2.dp))
                                .background(ApexTextMuted.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}
