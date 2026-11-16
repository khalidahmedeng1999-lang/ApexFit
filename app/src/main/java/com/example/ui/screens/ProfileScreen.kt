package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.UserProfile
import com.example.ui.components.EditProfileDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState

@Composable
fun ProfileScreen(
    uiState: WorkoutUiState,
    onSaveProfile: (UserProfile) -> Unit
) {
    val currentProfile = uiState.userProfile
    var showEditDialog by remember { mutableStateOf(false) }

    // BMI calculation
    val heightM = currentProfile.heightCm / 100.0
    val weightVal = currentProfile.weightKg
    val bmi = if (heightM > 0) weightVal / (heightM * heightM) else 22.0
    val bmiCategory = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal Weight"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
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
                            .size(42.dp)
                            .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = ApexEmerald
                        )
                    }

                    Column {
                        Text(
                            text = "ATHLETE PROFILE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Personal body stats & fitness metrics",
                            fontSize = 12.sp,
                            color = ApexTextMuted
                        )
                    }
                }

                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_full_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EDIT PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(currentProfile.name.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                            Text("${currentProfile.sex} • ${currentProfile.age} yrs", fontSize = 13.sp, color = ApexTextMuted)
                        }

                        Surface(color = ApexEmerald.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = currentProfile.goal.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ApexEmerald
                            )
                        }
                    }

                    HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("HEIGHT", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text("${currentProfile.heightCm.toInt()} cm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                        }

                        Column {
                            Text("WEIGHT", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text("${currentProfile.weightKg} kg", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                        }

                        Column {
                            Text("BMI", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", bmi)} ($bmiCategory)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                        }
                    }
                }
            }
        }

        // Detailed Body Stats & Activity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("BODY & ACTIVITY DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexEmerald, letterSpacing = 0.8.sp)

                    ProfileDetailRow(label = "Waist Measurement", value = if (currentProfile.waistCm > 0) "${currentProfile.waistCm} cm" else "Not provided")
                    ProfileDetailRow(label = "Neck Measurement", value = if (currentProfile.neckCm > 0) "${currentProfile.neckCm} cm" else "Not provided")
                    ProfileDetailRow(label = "Daily Activity", value = currentProfile.dailyActivity)
                    ProfileDetailRow(label = "Gym Frequency", value = "${currentProfile.gymDaysPerWeek} days / week")
                    ProfileDetailRow(label = "Target Aggressiveness", value = currentProfile.deficitAggressiveness)

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexInput, contentColor = ApexEmerald),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EDIT USER BODY DATA & GOALS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            initialProfile = currentProfile,
            onDismiss = { showEditDialog = false },
            onSaveProfile = onSaveProfile
        )
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = ApexTextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
    }
}
