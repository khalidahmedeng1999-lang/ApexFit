package com.example.data.network

import com.example.data.db.entities.FoodItem
import com.example.data.db.entities.PortionServing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object UsdaFoodApiService {

    private const val USDA_API_KEY = "DEMO_KEY"
    private const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/foods/search"

    suspend fun searchFoods(query: String): List<FoodItem> = withContext(Dispatchers.IO) {
        if (query.trim().isBlank()) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val urlString = "$BASE_URL?query=$encodedQuery&api_key=$USDA_API_KEY&pageSize=20"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode != 200) {
                return@withContext emptyList()
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val foodsArray = root.optJSONArray("foods") ?: return@withContext emptyList()

            val resultList = mutableListOf<FoodItem>()

            for (i in 0 until foodsArray.length()) {
                val foodJson = foodsArray.getJSONObject(i)
                val fdcId = foodJson.optLong("fdcId", 0L)
                val description = foodJson.optString("description", "Unknown Food")
                val brandOwner = foodJson.optString("brandOwner", foodJson.optString("brandName", ""))
                val dataType = foodJson.optString("dataType", "")

                // Nutrients parsing (per 100g in FDC)
                var calories = 0.0
                var protein = 0.0
                var carbs = 0.0
                var fat = 0.0

                val nutrientsArray = foodJson.optJSONArray("foodNutrients") ?: JSONArray()
                for (j in 0 until nutrientsArray.length()) {
                    val n = nutrientsArray.getJSONObject(j)
                    val name = n.optString("nutrientName", "").lowercase()
                    val unit = n.optString("unitName", "").lowercase()
                    val value = n.optDouble("value", 0.0)

                    when {
                        (name.contains("energy") && (unit == "kcal" || name.contains("kcal"))) -> calories = value
                        (name.contains("energy") && calories == 0.0) -> calories = value
                        name.contains("protein") -> protein = value
                        name.contains("carbohydrate") -> carbs = value
                        (name.contains("total lipid") || name.contains("fat")) -> fat = value
                    }
                }

                // Parse serving measures if available
                val servingOptions = mutableListOf<PortionServing>()
                servingOptions.add(PortionServing("100 g", 100.0))

                val servingSize = foodJson.optDouble("servingSize", 0.0)
                val servingUnit = foodJson.optString("servingSizeUnit", "g")
                if (servingSize > 0) {
                    servingOptions.add(PortionServing("1 $servingUnit (${servingSize.toInt()}g)", servingSize))
                }

                val measuresArray = foodJson.optJSONArray("foodMeasures")
                if (measuresArray != null) {
                    for (m in 0 until measuresArray.length()) {
                        val measureJson = measuresArray.getJSONObject(m)
                        val modifier = measureJson.optString("modifier", measureJson.optString("dissectionDescription", "serving"))
                        val gramWeight = measureJson.optDouble("gramWeight", 0.0)
                        if (gramWeight > 0 && modifier.isNotBlank()) {
                            servingOptions.add(PortionServing("1 $modifier (${gramWeight.toInt()}g)", gramWeight))
                        }
                    }
                }

                // Detect preparation state from description
                val prepState = when {
                    description.contains("boiled", ignoreCase = true) -> "Boiled"
                    description.contains("baked", ignoreCase = true) -> "Baked"
                    description.contains("raw", ignoreCase = true) -> "Raw"
                    description.contains("cooked", ignoreCase = true) -> "Cooked"
                    description.contains("fried", ignoreCase = true) -> "Fried"
                    description.contains("grilled", ignoreCase = true) -> "Grilled"
                    description.contains("roasted", ignoreCase = true) -> "Roasted"
                    description.contains("canned", ignoreCase = true) -> "Canned"
                    else -> "Cooked"
                }

                // Format name nicely
                val cleanName = cleanFoodName(description)

                // Build JSON string for serving options
                val optionsJsonBuilder = StringBuilder("[")
                servingOptions.distinctBy { it.unit }.forEachIndexed { idx, opt ->
                    if (idx > 0) optionsJsonBuilder.append(",")
                    optionsJsonBuilder.append("{\"unit\":\"${opt.unit}\",\"grams\":${opt.grams}}")
                }
                optionsJsonBuilder.append("]")

                val foodItem = FoodItem(
                    name = cleanName,
                    brand = if (brandOwner.isNotBlank()) brandOwner else "USDA",
                    source = "USDA FoodData Central",
                    sourceId = fdcId.toString(),
                    preparationState = prepState,
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = calories,
                    proteinPer100g = protein,
                    carbsPer100g = carbs,
                    fatPer100g = fat,
                    servingOptionsJson = optionsJsonBuilder.toString(),
                    isCustom = false,
                    isFavorite = false,
                    isRecent = false
                )

                resultList.add(foodItem)
            }

            resultList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun cleanFoodName(description: String): String {
        val parts = description.split(",")
        return if (parts.size >= 2) {
            val main = parts[0].trim().lowercase().capitalize()
            val modifier = parts[1].trim().lowercase()
            "$main ($modifier)"
        } else {
            description.trim().lowercase().capitalize()
        }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
