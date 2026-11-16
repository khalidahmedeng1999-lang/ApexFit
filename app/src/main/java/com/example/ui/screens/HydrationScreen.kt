package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.WaterLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HydrationScreen(
    uiState: WorkoutUiState,
    onLogWater: (Int) -> Unit,
    onDeleteWaterLog: (Long) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    val progressFraction = if (uiState.dailyWaterTargetMl > 0)
        (uiState.todayWaterMl.toFloat() / uiState.dailyWaterTargetMl.toFloat()).coerceIn(0f, 1f)
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "waterProgress"
    )

    val progressColor = when {
        progressFraction >= 1f -> ApexEmerald
        progressFraction >= 0.6f -> Color(0xFF4FC3F7)
        else -> Color(0xFF0288D1)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Hydration",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = ApexTextPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                fontSize = 13.sp,
                color = ApexTextMuted
            )
        }

        // Progress ring card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(160.dp),
                            strokeWidth = 12.dp,
                            color = progressColor,
                            trackColor = ApexInput,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = progressColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatMl(uiState.todayWaterMl),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = ApexTextPrimary
                            )
                            Text(
                                text = "of ${formatMl(uiState.dailyWaterTargetMl)}",
                                fontSize = 13.sp,
                                color = ApexTextMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val remaining = (uiState.dailyWaterTargetMl - uiState.todayWaterMl).coerceAtLeast(0)
                    Text(
                        text = if (remaining == 0) "Goal reached! Great work." else "${formatMl(remaining)} remaining",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remaining == 0) ApexEmerald else ApexTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Quick-add buttons
        item {
            Text(
                text = "Quick Add",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ApexTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(150, 250, 500).forEach { ml ->
                    QuickAddButton(
                        label = "${ml}ml",
                        modifier = Modifier.weight(1f),
                        onClick = { onLogWater(ml) }
                    )
                }
                QuickAddButton(
                    label = "Custom",
                    modifier = Modifier.weight(1f),
                    onClick = { showCustomDialog = true }
                )
            }
        }

        // Today's log
        if (uiState.todayWaterLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Today's Log",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(uiState.todayWaterLogs.reversed(), key = { it.id }) { log ->
                WaterLogRow(log = log, onDelete = { onDeleteWaterLog(log.id) })
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onConfirm = { ml ->
                onLogWater(ml)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

@Composable
private fun QuickAddButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ApexInput,
            contentColor = ApexTextPrimary
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WaterLogRow(log: WaterLog, onDelete: () -> Unit) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ApexCard)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${log.amountMl} ml", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
            Text(time, fontSize = 12.sp, color = ApexTextMuted)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ApexTextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CustomAmountDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ApexCard,
        title = { Text("Custom Amount", color = ApexTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) input = it },
                label = { Text("Amount (ml)", color = ApexTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexEmerald,
                    unfocusedBorderColor = ApexInput,
                    focusedTextColor = ApexTextPrimary,
                    unfocusedTextColor = ApexTextPrimary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val ml = input.toIntOrNull() ?: return@TextButton
                if (ml in 1..2000) onConfirm(ml)
            }) { Text("Add", color = ApexEmerald, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ApexTextMuted) }
        }
    )
}

private fun formatMl(ml: Int): String = if (ml >= 1000) "${"%.1f".format(ml / 1000.0)}L" else "${ml}ml"
