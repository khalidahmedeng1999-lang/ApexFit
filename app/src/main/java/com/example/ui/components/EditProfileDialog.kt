package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.entities.UserProfile
import com.example.ui.theme.*

@Composable
fun EditProfileDialog(
    initialProfile: UserProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile.name) }
    var sex by remember { mutableStateOf(initialProfile.sex) }
    var age by remember { mutableStateOf(initialProfile.age.toString()) }
    var heightCm by remember { mutableStateOf(initialProfile.heightCm.toString()) }
    var weightKg by remember { mutableStateOf(initialProfile.weightKg.toString()) }
    var waistCm by remember { mutableStateOf(if (initialProfile.waistCm > 0) initialProfile.waistCm.toString() else "") }
    var neckCm by remember { mutableStateOf(if (initialProfile.neckCm > 0) initialProfile.neckCm.toString() else "") }
    var dailyActivity by remember { mutableStateOf(initialProfile.dailyActivity) }
    var gymDaysPerWeek by remember { mutableIntStateOf(initialProfile.gymDaysPerWeek) }
    var goal by remember { mutableStateOf(initialProfile.goal) }
    var aggressiveness by remember { mutableStateOf(initialProfile.deficitAggressiveness) }

    val sexOptions = listOf("Male", "Female")
    val activityOptions = listOf(
        "Sedentary" to "Little/no activity outside workouts",
        "Lightly Active" to "Light daily movement",
        "Moderately Active" to "Regular daily movement",
        "Very Active" to "High daily activity",
        "Extremely Active" to "Very physically demanding work"
    )
    val gymFrequencyOptions = listOf(2, 3, 4, 5, 6, 7)
    val goalOptions = listOf("Fat Loss", "Maintenance", "Muscle Gain")
    val aggressivenessOptions = listOf("Conservative", "Moderate", "Aggressive")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ApexEmerald
                            )
                        }

                        Column {
                            Text(
                                text = "EDIT USER PROFILE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ApexTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Body stats, activity & nutrition target",
                                fontSize = 11.sp,
                                color = ApexTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ApexTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ATHLETE NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
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

                    // SEX Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SEX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            sexOptions.forEach { option ->
                                val isSelected = option.equals(sex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ApexEmerald else ApexInput)
                                        .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { sex = option }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ApexBackground else ApexTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // AGE, HEIGHT, WEIGHT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Age
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        // Height
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("HEIGHT (CM)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        // Weight
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("WEIGHT (KG)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                    // WAIST & NECK (Body Fat Estimator)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("WAIST (CM)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                            OutlinedTextField(
                                value = waistCm,
                                onValueChange = { waistCm = it },
                                placeholder = { Text("e.g. 82", color = ApexTextMuted, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("NECK (CM)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                            OutlinedTextField(
                                value = neckCm,
                                onValueChange = { neckCm = it },
                                placeholder = { Text("e.g. 38", color = ApexTextMuted, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                    // DAILY ACTIVITY Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("DAILY ACTIVITY LEVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            activityOptions.forEach { (title, subtitle) ->
                                val isSelected = title.equals(dailyActivity, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ApexEmerald.copy(alpha = 0.2f) else ApexInput)
                                        .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { dailyActivity = title }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) ApexEmerald else ApexTextPrimary
                                            )
                                            Text(
                                                text = subtitle,
                                                fontSize = 11.sp,
                                                color = ApexTextMuted
                                            )
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { dailyActivity = title },
                                            colors = RadioButtonDefaults.colors(selectedColor = ApexEmerald, unselectedColor = ApexTextMuted)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // GYM WORKOUT FREQUENCY Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("GYM WORKOUT FREQUENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            gymFrequencyOptions.forEach { days ->
                                val isSelected = days == gymDaysPerWeek
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ApexCyan else ApexInput)
                                        .border(1.dp, if (isSelected) ApexCyan else ApexCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { gymDaysPerWeek = days }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$days d/wk",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ApexBackground else ApexTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // PRIMARY GOAL Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PRIMARY GOAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goalOptions.forEach { option ->
                                val isSelected = option.equals(goal, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ApexEmerald else ApexInput)
                                        .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(12.dp))
                                        .clickable { goal = option }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ApexBackground else ApexTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // DEFICIT / SURPLUS AGGRESSIVENESS (if Fat Loss or Muscle Gain)
                    if (!goal.equals("Maintenance", ignoreCase = true)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("TARGET RATE / AGGRESSIVENESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                aggressivenessOptions.forEach { option ->
                                    val isSelected = option.equals(aggressiveness, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) ApexAmber else ApexInput)
                                            .border(1.dp, if (isSelected) ApexAmber else ApexCardBorder, RoundedCornerShape(12.dp))
                                            .clickable { aggressiveness = option }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ApexBackground else ApexTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        onSaveProfile(
                            initialProfile.copy(
                                name = name.ifBlank { "Apex Athlete" },
                                sex = sex,
                                age = age.toIntOrNull() ?: 25,
                                heightCm = heightCm.toDoubleOrNull() ?: 178.0,
                                weightKg = weightKg.toDoubleOrNull() ?: 75.0,
                                waistCm = waistCm.toDoubleOrNull() ?: 0.0,
                                neckCm = neckCm.toDoubleOrNull() ?: 0.0,
                                dailyActivity = dailyActivity,
                                gymDaysPerWeek = gymDaysPerWeek,
                                goal = goal,
                                deficitAggressiveness = aggressiveness
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE & UPDATE NUTRITION", fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
