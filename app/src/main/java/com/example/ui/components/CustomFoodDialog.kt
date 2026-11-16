package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.data.db.entities.FoodItem
import com.example.ui.theme.*

@Composable
fun CustomFoodDialog(
    onDismiss: () -> Unit,
    onSaveCustomFood: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var prepState by remember { mutableStateOf("Cooked") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var customServingUnit by remember { mutableStateOf("") }
    var customServingGramsText by remember { mutableStateOf("") }

    val prepOptions = listOf("Raw", "Boiled", "Cooked", "Baked", "Grilled", "Fried")

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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CREATE CUSTOM FOOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexCyan, letterSpacing = 0.8.sp)
                        Text("Manual Nutrition Entry", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                // Food Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FOOD NAME *", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().testTag("custom_food_name_input"),
                        placeholder = { Text("e.g. Homemade Protein Pancakes", fontSize = 13.sp, color = ApexTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexInput,
                            unfocusedContainerColor = ApexInput,
                            focusedBorderColor = ApexCyan,
                            unfocusedBorderColor = ApexCardBorder,
                            focusedTextColor = ApexTextPrimary,
                            unfocusedTextColor = ApexTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Brand & Prep State
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("BRAND / SOURCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            placeholder = { Text("e.g. Custom", fontSize = 12.sp, color = ApexTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("PREPARATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            color = ApexInput,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prepState, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                            }
                        }
                    }
                }

                // Preparation option pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    prepOptions.take(4).forEach { option ->
                        val isSelected = option == prepState
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ApexCyan else ApexInput)
                                .clickable { prepState = option }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(option, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) ApexBackground else ApexTextSecondary)
                        }
                    }
                }

                // Calories, Protein, Carbs, Fat (Per 100g)
                Text("NUTRITION PER 100 GRAMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexCyan)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("KCAL *", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CARBS (G)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = carbsText,
                            onValueChange = { carbsText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("PROTEIN (G)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = proteinText,
                            onValueChange = { proteinText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("FAT (G)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = fatText,
                            onValueChange = { fatText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Optional custom serving
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SERVING NAME (OPTIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = customServingUnit,
                            onValueChange = { customServingUnit = it },
                            placeholder = { Text("e.g. 1 scoop", fontSize = 11.sp, color = ApexTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("GRAM WEIGHT (G)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                        OutlinedTextField(
                            value = customServingGramsText,
                            onValueChange = { customServingGramsText = it },
                            placeholder = { Text("e.g. 30", fontSize = 11.sp, color = ApexTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ApexInput,
                                unfocusedContainerColor = ApexInput,
                                focusedBorderColor = ApexCyan,
                                unfocusedBorderColor = ApexCardBorder,
                                focusedTextColor = ApexTextPrimary,
                                unfocusedTextColor = ApexTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit button
                Button(
                    onClick = {
                        if (name.isBlank() || caloriesText.toDoubleOrNull() == null) return@Button

                        val cal = caloriesText.toDoubleOrNull() ?: 0.0
                        val prot = proteinText.toDoubleOrNull() ?: 0.0
                        val carbs = carbsText.toDoubleOrNull() ?: 0.0
                        val fat = fatText.toDoubleOrNull() ?: 0.0

                        val servingJson = if (customServingUnit.isNotBlank() && customServingGramsText.toDoubleOrNull() != null) {
                            val g = customServingGramsText.toDouble()
                            """[{"unit":"100 g","grams":100.0},{"unit":"$customServingUnit (${g.toInt()}g)","grams":$g}]"""
                        } else {
                            """[{"unit":"100 g","grams":100.0}]"""
                        }

                        val customItem = FoodItem(
                            name = name.trim(),
                            brand = if (brand.isNotBlank()) brand.trim() else "Custom Food",
                            source = "Custom Food",
                            sourceId = "CUSTOM-${System.currentTimeMillis()}",
                            preparationState = prepState,
                            referenceAmount = 100.0,
                            referenceUnit = "g",
                            caloriesPer100g = cal,
                            proteinPer100g = prot,
                            carbsPer100g = carbs,
                            fatPer100g = fat,
                            servingOptionsJson = servingJson,
                            isCustom = true
                        )

                        onSaveCustomFood(customItem)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_custom_food_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground),
                    shape = RoundedCornerShape(14.dp),
                    enabled = name.isNotBlank() && caloriesText.toDoubleOrNull() != null
                ) {
                    Text("SAVE CUSTOM FOOD", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
