package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.entities.FoodItem
import com.example.data.db.entities.FoodLog
import com.example.data.db.entities.RecipeEntity
import com.example.data.repository.MultiLayerSearchResult
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FoodSearchDialog(
    searchResults: List<FoodItem>,
    favoriteFoods: List<FoodItem>,
    recentFoods: List<FoodItem>,
    myFoods: List<FoodItem>,
    multiLayerResult: MultiLayerSearchResult? = null,
    isSearching: Boolean,
    userCountry: String = "Egypt",
    onSearchQuery: (String) -> Unit,
    onGenerateAiEstimate: (String) -> Unit,
    onScanLabel: (String) -> Unit,
    onSaveRecipe: (RecipeEntity) -> Unit,
    onToggleFavorite: (FoodItem) -> Unit,
    onConfirmAddLog: (FoodLog, FoodItem?) -> Unit,
    onSaveCustomFood: (FoodItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("Big Mac") }
    var activeTab by remember { mutableStateOf(0) } // 0: Search, 1: My Foods, 2: Favorites, 3: Recents
    var selectedFoodForPortion by remember { mutableStateOf<FoodItem?>(null) }
    var selectedFoodForDetails by remember { mutableStateOf<FoodItem?>(null) }
    var showCustomFoodModal by remember { mutableStateOf(false) }
    var showRecipeBuilderModal by remember { mutableStateOf(false) }
    var showLabelScannerModal by remember { mutableStateOf(false) }
    var aiEstimateItem by remember { mutableStateOf<FoodItem?>(null) }
    var overrideItem by remember { mutableStateOf<FoodItem?>(null) }

    // Quick filter chips
    val quickChips = listOf("McDonald's", "KFC", "Papa John's", "Pizza Hut", "Burger King", "Hardee's", "Subway", "Starbucks", "Egyptian Koshari", "Boiled Potato")

    // Trigger search on open
    LaunchedEffect(Unit) {
        if (searchQuery.isNotBlank()) {
            onSearchQuery(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            shape = RoundedCornerShape(24.dp),
            color = ApexBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ApexEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = ApexEmerald)
                        }
                        Column {
                            Text("FOOD SEARCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexEmerald, letterSpacing = 0.8.sp)
                            Text("ApexFit Nutrition Database", fontSize = 16.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                // Clean Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_search_input"),
                    placeholder = { Text("Search food...", fontSize = 13.sp, color = ApexTextMuted) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = ApexEmerald) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearchQuery("")
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = ApexTextMuted)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchQuery(searchQuery) }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexInput,
                        unfocusedContainerColor = ApexInput,
                        focusedBorderColor = ApexEmerald,
                        unfocusedBorderColor = ApexCardBorder,
                        focusedTextColor = ApexTextPrimary,
                        unfocusedTextColor = ApexTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Quick Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickChips) { chip ->
                        Surface(
                            modifier = Modifier.clickable {
                                searchQuery = chip
                                onSearchQuery(chip)
                            },
                            color = if (searchQuery.contains(chip, ignoreCase = true)) ApexEmerald.copy(alpha = 0.2f) else ApexInput,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (searchQuery.contains(chip, ignoreCase = true)) ApexEmerald else ApexCardBorder)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (searchQuery.contains(chip, ignoreCase = true)) ApexEmerald else ApexTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Secondary Action Toolbar (Scanner, Recipe, Custom Food)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { showLabelScannerModal = true },
                        modifier = Modifier.weight(1f).height(34.dp).testTag("open_label_scanner_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexCyan)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("SCAN LABEL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showRecipeBuilderModal = true },
                        modifier = Modifier.weight(1f).height(34.dp).testTag("open_recipe_builder_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexEmerald)
                    ) {
                        Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("CREATE RECIPE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCustomFoodModal = true },
                        modifier = Modifier.weight(1f).height(34.dp).testTag("create_custom_food_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexCyan, contentColor = ApexBackground)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("CUSTOM FOOD", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Section Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("SEARCH", "MY FOODS (${myFoods.size})", "FAVORITES (${favoriteFoods.size})", "RECENTS (${recentFoods.size})")
                    tabs.forEachIndexed { idx, title ->
                        val isSelected = activeTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ApexEmerald else ApexInput)
                                .border(1.dp, if (isSelected) ApexEmerald else ApexCardBorder, RoundedCornerShape(10.dp))
                                .clickable { activeTab = idx }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) ApexBackground else ApexTextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = ApexCardBorder, thickness = 1.dp)

                // Results Content
                val displayList = when (activeTab) {
                    0 -> searchResults
                    1 -> myFoods
                    2 -> favoriteFoods
                    else -> recentFoods
                }

                if (isSearching && activeTab == 0) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = ApexEmerald, modifier = Modifier.size(36.dp))
                            Text("Searching nutrition database...", fontSize = 12.sp, color = ApexTextMuted)
                        }
                    }
                } else if (activeTab == 0 && searchResults.isEmpty() && !isSearching) {
                    // REQUIREMENT 11: MISSING FOOD - NO DEAD END
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ApexCard),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexGold),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = searchQuery.ifBlank { "Food" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ApexTextPrimary
                                )

                                Text(
                                    text = "No verified nutrition data found.",
                                    fontSize = 13.sp,
                                    color = ApexTextMuted,
                                    fontWeight = FontWeight.Medium
                                )

                                Button(
                                    onClick = { onGenerateAiEstimate(searchQuery) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ApexGold, contentColor = ApexBackground),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("generate_ai_estimate_button")
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GENERATE ESTIMATE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                } else if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No items found in this section.", fontSize = 13.sp, color = ApexTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayList) { item ->
                            UnifiedFoodResultCard(
                                foodItem = item,
                                onAddFood = { selectedFoodForPortion = item },
                                onFoodDetails = { selectedFoodForDetails = item },
                                onEditValues = { overrideItem = item },
                                onSaveToMyFoods = { onSaveCustomFood(item.copy(isCustom = true, confidenceStatus = "CUSTOM")) },
                                onToggleFavorite = { onToggleFavorite(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal for Food Details / Source Info (Requirement 7)
    selectedFoodForDetails?.let { item ->
        FoodDetailsDialog(
            foodItem = item,
            onDismiss = { selectedFoodForDetails = null },
            onReportCorrect = {
                selectedFoodForDetails = null
                overrideItem = item
            }
        )
    }

    // Modal for AI Estimate / Edit Values
    aiEstimateItem?.let { item ->
        AiEstimateDialog(
            foodItem = item,
            onDismiss = { aiEstimateItem = null },
            onEditValues = { est ->
                aiEstimateItem = null
                overrideItem = est
            },
            onAddToDiary = { est ->
                aiEstimateItem = null
                selectedFoodForPortion = est
            }
        )
    }

    // Manual Nutrition Override Modal
    overrideItem?.let { item ->
        EditNutritionOverrideDialog(
            foodItem = item,
            onDismiss = { overrideItem = null },
            onSaveOverride = { updated ->
                overrideItem = null
                selectedFoodForPortion = updated
            }
        )
    }

    // Modal when user clicks ADD to select quantity & meal
    selectedFoodForPortion?.let { item ->
        FoodQuantityDialog(
            foodItem = item,
            onDismiss = { selectedFoodForPortion = null },
            onConfirmAdd = { log ->
                onConfirmAddLog(log, item)
                selectedFoodForPortion = null
                onDismiss()
            }
        )
    }

    // Recipe Builder Modal
    if (showRecipeBuilderModal) {
        RecipeBuilderDialog(
            onDismiss = { showRecipeBuilderModal = false },
            onSaveRecipe = { recipe ->
                onSaveRecipe(recipe)
                showRecipeBuilderModal = false
            }
        )
    }

    // Label Scanner Modal
    if (showLabelScannerModal) {
        LabelScannerDialog(
            onDismiss = { showLabelScannerModal = false },
            onConfirmScannedFood = { text ->
                onScanLabel(text)
                showLabelScannerModal = false
            }
        )
    }

    // Custom Food Creator Modal
    if (showCustomFoodModal) {
        CustomFoodDialog(
            onDismiss = { showCustomFoodModal = false },
            onSaveCustomFood = { customItem ->
                onSaveCustomFood(customItem)
                showCustomFoodModal = false
                selectedFoodForPortion = customItem
            }
        )
    }
}

@Composable
fun UnifiedFoodResultCard(
    foodItem: FoodItem,
    onAddFood: () -> Unit,
    onFoodDetails: () -> Unit,
    onEditValues: () -> Unit,
    onSaveToMyFoods: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val countryFlag = when (foodItem.country.lowercase()) {
        "egypt" -> "🇪🇬"
        "usa", "us" -> "🇺🇸"
        "uk" -> "🇬🇧"
        "uae" -> "🇦🇪"
        "saudi arabia" -> "🇸🇦"
        else -> "🌐"
    }

    val restaurantOrBrandHeader = when {
        foodItem.restaurant.isNotBlank() -> "${foodItem.restaurant} $countryFlag"
        foodItem.brand.isNotBlank() -> "${foodItem.brand} $countryFlag"
        else -> "${foodItem.country} $countryFlag"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ApexCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (foodItem.confidenceStatus) {
                "AI ESTIMATE" -> ApexGold
                "CUSTOM" -> ApexCyan
                else -> ApexCardBorder
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Restaurant/Brand + Country Flag, Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = restaurantOrBrandHeader,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexEmerald
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (foodItem.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (foodItem.isFavorite) ApexGold else ApexTextMuted
                    )
                }
            }

            // Food Name
            Text(
                text = foodItem.name.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ApexTextPrimary
            )

            // Calories & Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${foodItem.caloriesPer100g.roundToInt()} kcal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ApexTextPrimary
                )

                Text("•", color = ApexTextMuted)
                Text("${foodItem.proteinPer100g} g Protein", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                Text("•", color = ApexTextMuted)
                Text("${foodItem.carbsPer100g} g Carbs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexTextSecondary)
                Text("•", color = ApexTextMuted)
                Text("${foodItem.fatPer100g} g Fat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexGold)
            }

            // Verification Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (foodItem.confidenceStatus) {
                    "VERIFIED" -> {
                        Surface(
                            color = ApexEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🟢", fontSize = 10.sp)
                                Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ApexEmerald)
                            }
                        }
                    }
                    "AI ESTIMATE" -> {
                        Surface(
                            color = ApexGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🟠", fontSize = 10.sp)
                                Text("AI ESTIMATE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ApexGold)
                            }
                        }
                    }
                    "CUSTOM" -> {
                        Surface(
                            color = ApexCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔵", fontSize = 10.sp)
                                Text("CUSTOM", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ApexCyan)
                            }
                        }
                    }
                }
            }

            // Notice for AI estimate
            if (foodItem.confidenceStatus == "AI ESTIMATE") {
                Text(
                    text = "Recipe-dependent. Values may vary depending on ingredients and preparation.",
                    fontSize = 11.sp,
                    color = ApexTextMuted,
                    fontWeight = FontWeight.Normal
                )
            }

            HorizontalDivider(color = ApexCardBorder, thickness = 0.5.dp)

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onFoodDetails,
                    modifier = Modifier.weight(1f).height(34.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("Food Details", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexTextSecondary)
                }

                if (foodItem.confidenceStatus == "AI ESTIMATE" || foodItem.confidenceStatus == "CUSTOM") {
                    OutlinedButton(
                        onClick = onEditValues,
                        modifier = Modifier.weight(1f).height(34.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ApexGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexGold)
                    ) {
                        Text("EDIT VALUES", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (foodItem.confidenceStatus == "AI ESTIMATE") {
                    OutlinedButton(
                        onClick = onSaveToMyFoods,
                        modifier = Modifier.weight(1f).height(34.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ApexCyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexCyan)
                    ) {
                        Text("SAVE MY FOODS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAddFood,
                    modifier = Modifier.weight(1f).height(34.dp).testTag("add_food_card_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("ADD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun FoodDetailsDialog(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onReportCorrect: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexEmerald),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
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
                        Text("FOOD DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexEmerald)
                        Text(foodItem.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = ApexTextPrimary)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder)

                // Key Information Table
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Food:", foodItem.name)
                    DetailRow("Country:", foodItem.country)
                    DetailRow("Restaurant / Brand:", foodItem.restaurant.ifBlank { foodItem.brand.ifBlank { "N/A" } })
                    DetailRow("Serving:", "${foodItem.referenceAmount.roundToInt()} ${foodItem.referenceUnit}")
                    DetailRow("Calories:", "${foodItem.caloriesPer100g.roundToInt()} kcal")
                    DetailRow("Protein:", "${foodItem.proteinPer100g} g")
                    DetailRow("Carbs:", "${foodItem.carbsPer100g} g")
                    DetailRow("Fat:", "${foodItem.fatPer100g} g")
                    DetailRow("Status:", foodItem.confidenceStatus)

                    if (foodItem.confidenceStatus == "AI ESTIMATE" && foodItem.aiReason.isNotBlank()) {
                        DetailRow("Reason:", foodItem.aiReason)
                    }

                    DetailRow("Source:", foodItem.source)
                    DetailRow("Last Updated:", foodItem.lastUpdated)
                }

                HorizontalDivider(color = ApexCardBorder)

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReportCorrect,
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ApexGold),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexGold)
                    ) {
                        Text("Report / Correct Nutrition", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CLOSE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = ApexTextMuted, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 12.sp, color = ApexTextPrimary, fontWeight = FontWeight.Bold)
    }
}
