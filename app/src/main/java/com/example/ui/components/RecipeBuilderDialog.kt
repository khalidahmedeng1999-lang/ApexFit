package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OutdoorGrill
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
import com.example.data.db.entities.RecipeEntity
import com.example.data.db.entities.RecipeIngredient
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun RecipeBuilderDialog(
    onDismiss: () -> Unit,
    onSaveRecipe: (RecipeEntity) -> Unit
) {
    var recipeTitle by remember { mutableStateOf("Egyptian Kofta Recipe") }
    var recipeDescription by remember { mutableStateOf("Homemade minced beef kofta with onions") }
    var servingsInput by remember { mutableStateOf("5") }
    var cookedWeightInput by remember { mutableStateOf("620") }

    // Ingredients list
    val ingredients = remember {
        mutableStateListOf(
            RecipeIngredient("500 g minced beef", 500.0, 1250.0, 92.0, 0.0, 85.0),
            RecipeIngredient("100 g onion", 100.0, 40.0, 1.1, 9.3, 0.1),
            RecipeIngredient("50 g breadcrumbs", 50.0, 195.0, 6.5, 36.0, 2.5),
            RecipeIngredient("10 g vegetable oil", 10.0, 88.0, 0.0, 0.0, 10.0)
        )
    }

    var newIngName by remember { mutableStateOf("") }
    var newIngGrams by remember { mutableStateOf("100") }
    var newIngKcal by remember { mutableStateOf("150") }
    var newIngProtein by remember { mutableStateOf("10") }
    var newIngCarbs by remember { mutableStateOf("15") }
    var newIngFat by remember { mutableStateOf("5") }

    var showAddIngredientRow by remember { mutableStateOf(false) }

    // Totals calculation
    val totalCalories = ingredients.sumOf { it.calories }
    val totalProtein = ingredients.sumOf { it.proteinG }
    val totalCarbs = ingredients.sumOf { it.carbsG }
    val totalFat = ingredients.sumOf { it.fatG }
    val rawTotalWeight = ingredients.sumOf { it.grams }

    val servings = servingsInput.toIntOrNull() ?: 1
    val cookedWeight = cookedWeightInput.toDoubleOrNull() ?: rawTotalWeight

    // Per 100g cooked nutrition calculation (Requirement 61)
    val factor100g = if (cookedWeight > 0) 100.0 / cookedWeight else 1.0
    val caloriesPer100g = totalCalories * factor100g
    val proteinPer100g = totalProtein * factor100g
    val carbsPer100g = totalCarbs * factor100g
    val fatPer100g = totalFat * factor100g

    val caloriesPerServing = if (servings > 0) totalCalories / servings else totalCalories

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).background(ApexEmerald.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null, tint = ApexEmerald)
                        }
                        Column {
                            Text("HOMEMADE RECIPE BUILDER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                            Text("Create & Calculate Recipe", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder)

                // Recipe Title & Description
                OutlinedTextField(
                    value = recipeTitle,
                    onValueChange = { recipeTitle = it },
                    label = { Text("Recipe Title", color = ApexTextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("recipe_title_input"),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = servingsInput,
                        onValueChange = { servingsInput = it },
                        label = { Text("Servings", color = ApexTextMuted, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).testTag("recipe_servings_input"),
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

                    OutlinedTextField(
                        value = cookedWeightInput,
                        onValueChange = { cookedWeightInput = it },
                        label = { Text("Cooked Weight (g)", color = ApexTextMuted, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f).testTag("recipe_cooked_weight_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // Ingredients Header & Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("INGREDIENTS (${ingredients.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)

                    Button(
                        onClick = { showAddIngredientRow = !showAddIngredientRow },
                        colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("ADD INGREDIENT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showAddIngredientRow) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ApexInput),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newIngName,
                                onValueChange = { newIngName = it },
                                placeholder = { Text("Ingredient name (e.g. 500g minced beef)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("new_ing_name"),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = newIngGrams, onValueChange = { newIngGrams = it }, label = { Text("Grams", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = newIngKcal, onValueChange = { newIngKcal = it }, label = { Text("Kcal", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = newIngProtein, onValueChange = { newIngProtein = it }, label = { Text("Protein", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = newIngCarbs, onValueChange = { newIngCarbs = it }, label = { Text("Carbs", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = newIngFat, onValueChange = { newIngFat = it }, label = { Text("Fat", fontSize = 9.sp) }, modifier = Modifier.weight(1f))
                            }
                            Button(
                                onClick = {
                                    if (newIngName.isNotBlank()) {
                                        ingredients.add(
                                            RecipeIngredient(
                                                foodName = newIngName,
                                                grams = newIngGrams.toDoubleOrNull() ?: 100.0,
                                                calories = newIngKcal.toDoubleOrNull() ?: 100.0,
                                                proteinG = newIngProtein.toDoubleOrNull() ?: 5.0,
                                                carbsG = newIngCarbs.toDoubleOrNull() ?: 5.0,
                                                fatG = newIngFat.toDoubleOrNull() ?: 2.0
                                            )
                                        )
                                        newIngName = ""
                                        showAddIngredientRow = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald)
                            ) {
                                Text("CONFIRM INGREDIENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Ingredient List Display
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ingredients) { ing ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ApexInput),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ing.foodName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                                    Text("${ing.grams.toInt()}g • ${ing.calories.toInt()} kcal (${ing.proteinG}g P, ${ing.carbsG}g C, ${ing.fatG}g F)", fontSize = 10.sp, color = ApexTextMuted)
                                }
                                IconButton(onClick = { ingredients.remove(ing) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ApexTextMuted)
                                }
                            }
                        }
                    }
                }

                // Total Recipe Calculations Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ApexInput),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexEmerald.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("RECIPE TOTALS & PER 100G COOKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total: ${totalCalories.roundToInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                                Text("Per Serving ($servings): ${caloriesPerServing.roundToInt()} kcal", fontSize = 11.sp, color = ApexCyan)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Per 100g Cooked: ${caloriesPer100g.roundToInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ApexEmerald)
                                Text("${proteinPer100g.roundToInt()}g P • ${carbsPer100g.roundToInt()}g C • ${fatPer100g.roundToInt()}g F", fontSize = 10.sp, color = ApexGold)
                            }
                        }
                    }
                }

                // Save Recipe Button
                Button(
                    onClick = {
                        val jsonBuilder = StringBuilder("[")
                        ingredients.forEachIndexed { idx, ing ->
                            if (idx > 0) jsonBuilder.append(",")
                            jsonBuilder.append("""{"foodName":"${ing.foodName}","grams":${ing.grams},"calories":${ing.calories},"proteinG":${ing.proteinG},"carbsG":${ing.carbsG},"fatG":${ing.fatG}}""")
                        }
                        jsonBuilder.append("]")

                        val recipe = RecipeEntity(
                            title = recipeTitle.ifBlank { "Homemade Recipe" },
                            description = recipeDescription,
                            totalServings = servings,
                            totalCookedWeightG = cookedWeight,
                            totalCalories = totalCalories,
                            totalProteinG = totalProtein,
                            totalCarbsG = totalCarbs,
                            totalFatG = totalFat,
                            caloriesPer100g = caloriesPer100g,
                            proteinPer100g = proteinPer100g,
                            carbsPer100g = carbsPer100g,
                            fatPer100g = fatPer100g,
                            ingredientsJson = jsonBuilder.toString()
                        )
                        onSaveRecipe(recipe)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_recipe_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SAVE RECIPE TO MY FOODS", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
