package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.FoodItem
import com.example.data.db.entities.FoodLog
import com.example.data.db.entities.RecipeEntity
import com.example.data.db.entities.WeightLog
import com.example.data.repository.MultiLayerSearchResult
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.FoodQuantityDialog
import com.example.ui.components.FoodSearchDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkoutUiState
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(
    uiState: WorkoutUiState,
    searchResults: List<FoodItem>,
    favoriteFoods: List<FoodItem>,
    recentFoods: List<FoodItem>,
    myFoods: List<FoodItem>,
    multiLayerResult: MultiLayerSearchResult? = null,
    isSearching: Boolean,
    onSearchQuery: (String) -> Unit,
    onGenerateAiEstimate: (String) -> Unit,
    onScanLabel: (String) -> Unit,
    onSaveRecipe: (RecipeEntity) -> Unit,
    onToggleFavoriteFood: (FoodItem) -> Unit,
    onSaveProfile: (com.example.data.db.entities.UserProfile) -> Unit,
    onLogWeight: (Double) -> Unit,
    onAddFoodLogWithDetails: (FoodLog, FoodItem?) -> Unit,
    onUpdateFoodLogDetails: (FoodLog) -> Unit,
    onReAddFoodLog: (FoodLog) -> Unit,
    onSaveCustomFood: (FoodItem) -> Unit,
    onDeleteFoodLog: (Long) -> Unit
) {
    val profile = uiState.userProfile
    val summary = uiState.nutritionSummary

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showFoodSearchDialog by remember { mutableStateOf(false) }
    var foodToEditPortion by remember { mutableStateOf<FoodLog?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER SECTION ---
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
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Nutrition & TDEE",
                            tint = ApexEmerald
                        )
                    }

                    Column {
                        Text(
                            text = "NUTRITION & DIARY",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "USDA, Multi-Layer Search & AI System",
                            fontSize = 12.sp,
                            color = ApexTextMuted
                        )
                    }
                }

                // EDIT PROFILE BUTTON
                Button(
                    onClick = { showEditProfileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexInput, contentColor = ApexEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexEmerald),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("nutrition_edit_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PROFILE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- FOOD SEARCH LAUNCH BANNER ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFoodSearchDialog = true },
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald)
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
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(ApexEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ApexBackground)
                        }

                        Column {
                            Text("MULTI-LAYER FOOD SEARCH", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                            Text("USDA • Egyptian Local DB • AI Fallback", fontSize = 11.sp, color = ApexTextMuted)
                        }
                    }

                    Surface(
                        color = ApexEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "SEARCH",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ApexEmerald
                        )
                    }
                }
            }
        }

        // --- HERO CALORIE TARGET CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "DAILY CALORIE TARGET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexTextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${summary.targetCalories} kcal/day",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = ApexEmerald
                            )
                        }

                        Surface(
                            color = when {
                                summary.isDeficit -> ApexCoral.copy(alpha = 0.2f)
                                summary.isSurplus -> ApexCyan.copy(alpha = 0.2f)
                                else -> ApexAmber.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = profile.goal.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    summary.isDeficit -> ApexCoral
                                    summary.isSurplus -> ApexCyan
                                    else -> ApexAmber
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                    // Sub-metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MAINTENANCE", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text("${summary.maintenanceCalories} kcal", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ApexTextPrimary)
                        }

                        Column {
                            Text("WEIGHT", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", profile.weightKg)} kg", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ApexCyan)
                        }

                        Column {
                            Text("BODY FAT %", fontSize = 10.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (summary.bodyFatPct != null) "${String.format("%.1f", summary.bodyFatPct)}%" else "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (summary.bodyFatPct != null) ApexEmerald else ApexAmber
                            )
                        }
                    }
                }
            }
        }

        // --- DAILY DIARY & MEAL GROUPS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY FOOD DIARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexEmerald,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Logged meals for today",
                                fontSize = 11.sp,
                                color = ApexTextMuted
                            )
                        }

                        Button(
                            onClick = { showFoodSearchDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_food_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD FOOD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Calorie & Macro Progress Bars
                    val consumedCal = uiState.todayConsumedCalories
                    val targetCal = summary.targetCalories
                    val calRemaining = targetCal - consumedCal
                    val calProgress = (consumedCal.toFloat() / targetCal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Calories: $consumedCal / $targetCal kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                            Text(
                                text = if (calRemaining >= 0) "Remaining: $calRemaining kcal" else "Over: ${-calRemaining} kcal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (calRemaining >= 0) ApexCyan else ApexCoral
                            )
                        }

                        LinearProgressIndicator(
                            progress = { calProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (consumedCal > targetCal) ApexCoral else ApexEmerald,
                            trackColor = ApexInput
                        )
                    }

                    // Macro Breakdown Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroProgressItem(
                            title = "Protein",
                            consumed = uiState.todayConsumedProtein,
                            target = summary.proteinG.toDouble(),
                            color = ApexEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MacroProgressItem(
                            title = "Carbs",
                            consumed = uiState.todayConsumedCarbs,
                            target = summary.carbsG.toDouble(),
                            color = ApexCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MacroProgressItem(
                            title = "Fat",
                            consumed = uiState.todayConsumedFat,
                            target = summary.fatG.toDouble(),
                            color = ApexGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                    // Food Log Items Grouped by Meal
                    if (uiState.todayFoodLogs.isEmpty()) {
                        Text(
                            text = "No foods logged today. Click SEARCH FOOD to log items.",
                            fontSize = 12.sp,
                            color = ApexTextMuted,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        val mealGroups = uiState.todayFoodLogs.groupBy { it.meal }
                        val mealOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Pre-Workout", "Post-Workout", "Other")

                        val sortedMeals = mealGroups.keys.sortedBy { mealOrder.indexOf(it).let { idx -> if (idx >= 0) idx else 99 } }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            sortedMeals.forEach { meal ->
                                val logs = mealGroups[meal] ?: emptyList()
                                val mealCal = logs.sumOf { it.calories }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = meal.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ApexEmerald,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$mealCal kcal",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ApexTextSecondary
                                        )
                                    }

                                    logs.forEach { food ->
                                        FoodLogRowCard(
                                            foodLog = food,
                                            onEditPortion = { foodToEditPortion = food },
                                            onReAdd = { onReAddFoodLog(food) },
                                            onDelete = { onDeleteFoodLog(food.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- BODY COMPOSITION ESTIMATION CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BODY COMPOSITION ESTIMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexEmerald,
                        letterSpacing = 0.8.sp
                    )

                    if (summary.bodyFatPct == null) {
                        Surface(
                            color = ApexAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Add waist and neck measurements in Profile to estimate body fat %.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = ApexAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ESTIMATED BODY FAT", fontSize = 10.sp, color = ApexTextMuted)
                                Text("${String.format("%.1f", summary.bodyFatPct)}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ApexEmerald)
                            }
                            Column {
                                Text("LEAN BODY MASS", fontSize = 10.sp, color = ApexTextMuted)
                                Text("${String.format("%.1f", summary.leanMassKg)} kg", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ApexCyan)
                            }
                            Column {
                                Text("FAT MASS", fontSize = 10.sp, color = ApexTextMuted)
                                Text("${String.format("%.1f", summary.fatMassKg)} kg", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ApexCoral)
                            }
                        }
                    }
                }
            }
        }

        // --- WEEKLY WEIGHT TREND GRAPH ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ApexCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEKLY WEIGHT TREND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexEmerald,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Recorded body weight over time",
                                fontSize = 11.sp,
                                color = ApexTextMuted
                            )
                        }

                        Button(
                            onClick = { showAddWeightDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ApexInput, contentColor = ApexCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ApexCyan),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("log_weight_button")
                        ) {
                            Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LOG WEIGHT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.weightLogs.isEmpty()) {
                        Text("No weight logs recorded yet.", fontSize = 12.sp, color = ApexTextMuted)
                    } else {
                        WeightTrendGraph(
                            weightLogs = uiState.weightLogs,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showFoodSearchDialog) {
        FoodSearchDialog(
            searchResults = searchResults,
            favoriteFoods = favoriteFoods,
            recentFoods = recentFoods,
            myFoods = myFoods,
            multiLayerResult = multiLayerResult,
            isSearching = isSearching,
            onSearchQuery = onSearchQuery,
            onGenerateAiEstimate = onGenerateAiEstimate,
            onScanLabel = onScanLabel,
            onSaveRecipe = onSaveRecipe,
            onToggleFavorite = onToggleFavoriteFood,
            onConfirmAddLog = { log, item ->
                onAddFoodLogWithDetails(log, item)
            },
            onSaveCustomFood = onSaveCustomFood,
            onDismiss = { showFoodSearchDialog = false }
        )
    }

    // Portion Editor for existing FoodLog
    foodToEditPortion?.let { log ->
        val dummyItem = FoodItem(
            id = log.foodId,
            name = log.foodName,
            brand = log.brand,
            source = log.source,
            sourceId = log.sourceId,
            preparationState = "Cooked",
            referenceAmount = 100.0,
            referenceUnit = "g",
            caloriesPer100g = if (log.grams > 0) (log.calories / log.grams) * 100.0 else 100.0,
            proteinPer100g = if (log.grams > 0) (log.proteinG / log.grams) * 100.0 else 0.0,
            carbsPer100g = if (log.grams > 0) (log.carbsG / log.grams) * 100.0 else 0.0,
            fatPer100g = if (log.grams > 0) (log.fatG / log.grams) * 100.0 else 0.0
        )

        FoodQuantityDialog(
            foodItem = dummyItem,
            initialLog = log,
            initialMeal = log.meal,
            onDismiss = { foodToEditPortion = null },
            onConfirmAdd = { updatedLog ->
                onUpdateFoodLogDetails(updatedLog)
                foodToEditPortion = null
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialProfile = profile,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = onSaveProfile
        )
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            onDismiss = { showAddWeightDialog = false },
            onLogWeight = { w ->
                onLogWeight(w)
                showAddWeightDialog = false
            }
        )
    }
}

@Composable
fun FoodLogRowCard(
    foodLog: FoodLog,
    onEditPortion: () -> Unit,
    onReAdd: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ApexInput, RoundedCornerShape(12.dp))
            .clickable { onEditPortion() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = foodLog.foodName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                if (foodLog.quantity > 0) {
                    Text(
                        text = "(${foodLog.quantity.toInt()} ${foodLog.unit})",
                        fontSize = 11.sp,
                        color = ApexCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "P: ${foodLog.proteinG.roundToInt()}g | C: ${foodLog.carbsG.roundToInt()}g | F: ${foodLog.fatG.roundToInt()}g • ${foodLog.source}",
                fontSize = 10.sp,
                color = ApexTextMuted
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "${foodLog.calories} kcal", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ApexEmerald)

            IconButton(
                onClick = onReAdd,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Add Again", tint = ApexCyan, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ApexTextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MacroProgressItem(
    title: String,
    consumed: Double,
    target: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val progress = (consumed / target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ApexInput),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexTextMuted)
                Text("${consumed.roundToInt()}/${target.roundToInt()}g", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = ApexCardBorder
            )
        }
    }
}

@Composable
fun WeightTrendGraph(
    weightLogs: List<WeightLog>,
    modifier: Modifier = Modifier
) {
    if (weightLogs.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Need at least 2 weight logs to draw graph", fontSize = 12.sp, color = ApexTextMuted)
        }
        return
    }

    val weights = weightLogs.map { it.weightKg }
    val minW = weights.minOrNull() ?: 70.0
    val maxW = weights.maxOrNull() ?: 80.0
    val range = (maxW - minW).coerceAtLeast(1.0)

    Canvas(modifier = modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
        val width = size.width
        val height = size.height

        val points = weights.mapIndexed { idx, w ->
            val x = (idx.toFloat() / (weights.size - 1)) * width
            val normalizedY = ((w - minW) / range).toFloat()
            val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()
            Offset(x, y)
        }

        val lineCount = 3
        for (i in 0..lineCount) {
            val y = (height / lineCount) * i
            drawLine(
                color = ApexCardBorder,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = ApexEmerald,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEach { pt ->
            drawCircle(color = ApexEmerald, radius = 5.dp.toPx(), center = pt)
            drawCircle(color = ApexBackground, radius = 2.5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit
) {
    var weightText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("LOG BODY WEIGHT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight in KG (e.g. 78.5)") },
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
                    modifier = Modifier.fillMaxWidth().testTag("weight_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = ApexTextMuted) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = weightText.toDoubleOrNull()
                            if (w != null && w > 0) {
                                onLogWeight(w)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_log_weight_button")
                    ) {
                        Text("LOG WEIGHT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
