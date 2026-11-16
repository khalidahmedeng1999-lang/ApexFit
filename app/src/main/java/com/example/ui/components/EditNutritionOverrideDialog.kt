package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.FoodItem
import com.example.ui.theme.*

@Composable
fun EditNutritionOverrideDialog(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onSaveOverride: (FoodItem) -> Unit
) {
    var nameInput by remember { mutableStateOf(foodItem.name) }
    var caloriesInput by remember { mutableStateOf(foodItem.caloriesPer100g.toInt().toString()) }
    var proteinInput by remember { mutableStateOf(foodItem.proteinPer100g.toString()) }
    var carbsInput by remember { mutableStateOf(foodItem.carbsPer100g.toString()) }
    var fatInput by remember { mutableStateOf(foodItem.fatPer100g.toString()) }
    var referenceAmountInput by remember { mutableStateOf(foodItem.referenceAmount.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ApexCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = ApexCyan)
                        }
                        Column {
                            Text("MANUAL NUTRITION OVERRIDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                            Text("Edit Values Directly", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder)

                // Food Name Field
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Food Name", color = ApexTextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("override_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexInput,
                        unfocusedContainerColor = ApexInput,
                        focusedBorderColor = ApexCyan,
                        unfocusedBorderColor = ApexCardBorder,
                        focusedTextColor = ApexTextPrimary,
                        unfocusedTextColor = ApexTextPrimary
                    )
                )

                // Reference Quantity (e.g. 100g)
                OutlinedTextField(
                    value = referenceAmountInput,
                    onValueChange = { referenceAmountInput = it },
                    label = { Text("Reference Quantity (g)", color = ApexTextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("override_ref_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexInput,
                        unfocusedContainerColor = ApexInput,
                        focusedBorderColor = ApexCyan,
                        unfocusedBorderColor = ApexCardBorder,
                        focusedTextColor = ApexTextPrimary,
                        unfocusedTextColor = ApexTextPrimary
                    )
                )

                // Calories Field
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it },
                    label = { Text("Calories (kcal)", color = ApexTextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("override_calories_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexInput,
                        unfocusedContainerColor = ApexInput,
                        focusedBorderColor = ApexEmerald,
                        unfocusedBorderColor = ApexCardBorder,
                        focusedTextColor = ApexTextPrimary,
                        unfocusedTextColor = ApexTextPrimary
                    )
                )

                // Macros Row: Protein, Carbs, Fat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proteinInput,
                        onValueChange = { proteinInput = it },
                        label = { Text("Protein (g)", color = ApexTextMuted, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).testTag("override_protein_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexCyan,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = carbsInput,
                        onValueChange = { carbsInput = it },
                        label = { Text("Carbs (g)", color = ApexTextMuted, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).testTag("override_carbs_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexEmerald,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = fatInput,
                        onValueChange = { fatInput = it },
                        label = { Text("Fat (g)", color = ApexTextMuted, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).testTag("override_fat_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexGold,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        )
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        val refGrams = referenceAmountInput.toDoubleOrNull() ?: 100.0
                        val rawCal = caloriesInput.toDoubleOrNull() ?: 0.0
                        val rawP = proteinInput.toDoubleOrNull() ?: 0.0
                        val rawC = carbsInput.toDoubleOrNull() ?: 0.0
                        val rawF = fatInput.toDoubleOrNull() ?: 0.0

                        val mult = 100.0 / refGrams.coerceAtLeast(1.0)

                        val updated = foodItem.copy(
                            name = nameInput.ifBlank { foodItem.name },
                            referenceAmount = 100.0,
                            caloriesPer100g = rawCal * mult,
                            proteinPer100g = rawP * mult,
                            carbsPer100g = rawC * mult,
                            fatPer100g = rawF * mult,
                            confidenceStatus = "CUSTOM",
                            sourceBadge = "[Custom]"
                        )
                        onSaveOverride(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_nutrition_override_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAVE OVERRIDE VALUES", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
