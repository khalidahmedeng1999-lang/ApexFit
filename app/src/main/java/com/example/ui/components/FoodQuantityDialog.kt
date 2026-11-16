package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import com.example.data.db.entities.FoodLog
import com.example.data.db.entities.PortionServing
import com.example.ui.theme.*
import org.json.JSONArray
import kotlin.math.roundToInt

@Composable
fun FoodQuantityDialog(
    foodItem: FoodItem,
    initialLog: FoodLog? = null,
    initialMeal: String = "Lunch",
    onDismiss: () -> Unit,
    onConfirmAdd: (FoodLog) -> Unit
) {
    // Parse serving options from JSON or fallback
    val parsedServings = remember(foodItem) {
        val list = mutableListOf<PortionServing>()
        list.add(PortionServing("grams", 1.0))
        try {
            val jsonArray = JSONArray(foodItem.servingOptionsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val unitName = obj.optString("unit", "")
                val g = obj.optDouble("grams", 100.0)
                if (unitName.isNotBlank() && !unitName.equals("grams", ignoreCase = true) && !unitName.equals("100 g", ignoreCase = true)) {
                    list.add(PortionServing(unitName, g))
                }
            }
        } catch (e: Exception) {
            // fallback
            list.add(PortionServing("serving", 100.0))
        }
        list
    }

    var selectedMeal by remember { mutableStateOf(initialLog?.meal ?: initialMeal) }
    var selectedServing by remember { mutableStateOf(parsedServings.firstOrNull { it.unit == initialLog?.unit } ?: parsedServings.first()) }
    var quantityInput by remember { mutableStateOf((initialLog?.quantity ?: if (selectedServing.unit == "grams") 100.0 else 1.0).toString()) }

    val currentQty = quantityInput.toDoubleOrNull() ?: 100.0

    // Dynamic calculations
    val totalGrams = if (selectedServing.unit == "grams") currentQty else currentQty * selectedServing.grams
    val multiplier = totalGrams / 100.0

    val calculatedCalories = (foodItem.caloriesPer100g * multiplier).roundToInt()
    val calculatedProtein = (foodItem.proteinPer100g * multiplier * 10).roundToInt() / 10.0
    val calculatedCarbs = (foodItem.carbsPer100g * multiplier * 10).roundToInt() / 10.0
    val calculatedFat = (foodItem.fatPer100g * multiplier * 10).roundToInt() / 10.0

    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Pre-Workout", "Post-Workout", "Other")
    val quickGrams = listOf(100.0, 150.0, 200.0, 250.0, 300.0, 500.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (initialLog != null) "EDIT FOOD PORTION" else "ADD FOOD TO DIARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexEmerald,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = foodItem.name.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                // Food Specs Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = ApexEmerald.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text(foodItem.preparationState, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                    }
                    if (foodItem.brand.isNotBlank()) {
                        Surface(color = ApexCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text(foodItem.brand, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                        }
                    }
                    Surface(color = ApexBackground, shape = RoundedCornerShape(8.dp)) {
                        Text(foodItem.source, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                // Reference per 100g display
                Text(
                    text = "Nutrition per 100 g: ${foodItem.caloriesPer100g.roundToInt()} kcal • ${foodItem.carbsPer100g}g C • ${foodItem.proteinPer100g}g P • ${foodItem.fatPer100g}g F",
                    fontSize = 11.sp,
                    color = ApexTextMuted,
                    fontWeight = FontWeight.Medium
                )

                // Meal Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SELECT MEAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        meals.take(4).forEach { meal ->
                            val isSelected = meal == selectedMeal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ApexEmerald else ApexInput)
                                    .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedMeal = meal }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(meal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) ApexBackground else ApexTextPrimary)
                            }
                        }
                    }
                }

                // Portion Unit Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SERVING UNIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        parsedServings.forEach { serving ->
                            val isSelected = serving == selectedServing
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ApexCyan else ApexInput)
                                    .border(1.dp, if (isSelected) ApexCyan else ApexCardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedServing = serving
                                        if (serving.unit == "grams" && (quantityInput == "1.0" || quantityInput == "1")) {
                                            quantityInput = "100"
                                        } else if (serving.unit != "grams" && quantityInput == "100") {
                                            quantityInput = "1"
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = serving.unit,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ApexBackground else ApexTextPrimary
                                )
                            }
                        }
                    }
                }

                // Quantity Selector [-] [value] [+]
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("QUANTITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val step = if (selectedServing.unit == "grams") 10.0 else 0.5
                                val newVal = (currentQty - step).coerceAtLeast(step)
                                quantityInput = if (selectedServing.unit == "grams") newVal.roundToInt().toString() else String.format("%.1f", newVal)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(ApexInput, RoundedCornerShape(12.dp))
                                .border(1.dp, ApexCardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = ApexEmerald)
                        }

                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            modifier = Modifier.weight(1f).testTag("food_quantity_input"),
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

                        IconButton(
                            onClick = {
                                val step = if (selectedServing.unit == "grams") 10.0 else 0.5
                                val newVal = currentQty + step
                                quantityInput = if (selectedServing.unit == "grams") newVal.roundToInt().toString() else String.format("%.1f", newVal)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(ApexInput, RoundedCornerShape(12.dp))
                                .border(1.dp, ApexCardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = ApexEmerald)
                        }
                    }

                    // Quick Chips for Grams
                    if (selectedServing.unit == "grams") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickGrams.forEach { g ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (currentQty == g) ApexEmerald.copy(alpha = 0.2f) else ApexInput)
                                        .border(1.dp, if (currentQty == g) ApexEmerald else ApexCardBorder, RoundedCornerShape(8.dp))
                                        .clickable { quantityInput = g.toInt().toString() }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${g.toInt()}g", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentQty == g) ApexEmerald else ApexTextSecondary)
                                }
                            }
                        }
                    }
                }

                // Dynamic Nutrition Calculation Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ApexInput),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexEmerald.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CALCULATED NUTRITION (${totalGrams.toInt()} g)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexEmerald,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("$calculatedCalories kcal", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ApexEmerald)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CARBS", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${calculatedCarbs}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PROTEIN", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${calculatedProtein}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FAT", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${calculatedFat}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexGold)
                                }
                            }
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        val log = FoodLog(
                            id = initialLog?.id ?: 0,
                            dateStr = initialLog?.dateStr ?: todayStr,
                            meal = selectedMeal,
                            foodName = foodItem.name,
                            brand = foodItem.brand,
                            quantity = currentQty,
                            unit = selectedServing.unit,
                            grams = totalGrams,
                            calories = calculatedCalories,
                            proteinG = calculatedProtein,
                            carbsG = calculatedCarbs,
                            fatG = calculatedFat,
                            source = foodItem.source,
                            sourceId = foodItem.sourceId,
                            foodId = foodItem.id
                        )
                        onConfirmAdd(log)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_food_to_diary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialLog != null) "SAVE EDITED PORTION" else "ADD TO DIARY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
