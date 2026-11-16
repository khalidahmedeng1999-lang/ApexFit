package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_programs")
data class WorkoutProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cycleDays: Int = 7,         // e.g. 7 or 14 for 2-week systems
    val description: String = "",   // e.g. "14-Day Hyper Athlete + Hypertrophy Program"
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workout_days",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutProgram::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId")]
)
data class WorkoutDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayOfWeek: String, // e.g., "Day 1", "Day 2", "Saturday", "Monday"
    val dayName: String,   // e.g., "Back-Focused Full Body + Core + Farmer Carry"
    val orderIndex: Int = 0,
    val isRestDay: Boolean = false
)

@Entity(
    tableName = "program_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDay::class,
            parentColumns = ["id"],
            childColumns = ["workoutDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutDayId")]
)
data class ProgramExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutDayId: Long,
    val exerciseName: String,
    val targetMuscle: String, // e.g. "Lats", "Mid Back", "Chest", "Side Delts", "Quads", "Hamstrings", "Core"
    val defaultSets: Int = 3,
    val restSeconds: Int = 120,
    val orderIndex: Int = 0,
    val repsRange: String = "8-12", // e.g. "6-10", "3 x 20-30 m", "20-30 min"
    val rir: String = "1-2",        // e.g. "2", "1-2", "0"
    val notes: String = "",         // Coaching cues & technical instructions
    val category: String = "Hypertrophy" // "Compound", "Isolation", "Core", "Power", "Conditioning", "Zone 2"
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programTitle: String,
    val dayName: String,
    val dayOfWeek: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0
)

@Entity(
    tableName = "logged_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutLog::class,
            parentColumns = ["id"],
            childColumns = ["workoutLogId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutLogId")]
)
data class LoggedExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutLogId: Long,
    val exerciseName: String,
    val targetMuscle: String,
    val restSeconds: Int = 120,
    val orderIndex: Int = 0,
    val repsRange: String = "",
    val rir: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "logged_sets",
    foreignKeys = [
        ForeignKey(
            entity = LoggedExercise::class,
            parentColumns = ["id"],
            childColumns = ["loggedExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("loggedExerciseId")]
)
data class LoggedSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loggedExerciseId: Long,
    val setNumber: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val isCompleted: Boolean = true
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Apex Athlete",
    val sex: String = "Male",               // "Male", "Female"
    val age: Int = 25,
    val heightCm: Double = 178.0,
    val weightKg: Double = 75.0,
    val waistCm: Double = 82.0,             // waist measurement in cm
    val neckCm: Double = 38.0,              // neck measurement in cm
    val dailyActivity: String = "Moderately Active", // "Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active"
    val gymDaysPerWeek: Int = 4,            // 2, 3, 4, 5, 6, 7
    val goal: String = "Muscle Gain",       // "Fat Loss", "Maintenance", "Muscle Gain"
    val deficitAggressiveness: String = "Moderate", // "Conservative", "Moderate", "Aggressive"
    val unit: String = "KG",                // "KG", "LBS"
    val country: String = "Egypt"           // "Egypt", "USA", "UK", "UAE", "Saudi Arabia", etc.
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double
)

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                       // e.g. "Big Mac"
    val brand: String = "",                 // Manufacturer or brand e.g. "McDonald's"
    val restaurant: String = "",            // e.g. "McDonald's", "KFC", "Papa John's"
    val country: String = "Egypt",          // e.g. "Egypt", "USA", "Global"
    val category: String = "",              // e.g. "Burgers", "Chicken", "Pizzas", "Meals", "Sides", "Desserts", "Drinks"
    val source: String = "Official restaurant nutrition information",
    val sourceType: String = "Official",    // "Official", "Database", "AI", "User"
    val sourceBadge: String = "[Verified]", // Badge display string
    val confidenceStatus: String = "VERIFIED", // "VERIFIED", "AI ESTIMATE", "CUSTOM"
    val aiReason: String = "",              // AI explanation e.g. "No verified nutrition information was available."
    val lastUpdated: String = "March 2026",
    val sourceId: String = "",             // Unique source identifier
    val preparationState: String = "Cooked",// "Raw", "Boiled", "Cooked", "Baked", "Grilled", "Fried"
    val referenceAmount: Double = 100.0,
    val referenceUnit: String = "g",        // "g", "sandwich", "piece", "portion", "slice", "cup", "meal"
    val caloriesPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val servingOptionsJson: String = "[]",  // JSON list of PortionServing
    val isCustom: Boolean = false,
    val isRecipe: Boolean = false,
    val aiWarning: String = "",
    val isFavorite: Boolean = false,
    val isRecent: Boolean = false,
    val lastUsedTimestamp: Long = 0
)

data class PortionServing(
    val unit: String,   // e.g., "grams", "medium", "cup", "portion", "serving", "tablespoon", "teaspoon", "piece", "slice", "bowl"
    val grams: Double   // equivalent weight in grams
)

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,                    // e.g. "2026-08-11"
    val meal: String = "Lunch",             // "Breakfast", "Lunch", "Dinner", "Snack", "Pre-Workout", "Post-Workout", "Other"
    val foodName: String,
    val brand: String = "",
    val quantity: Double = 100.0,
    val unit: String = "grams",
    val grams: Double = 100.0,
    val calories: Int,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val source: String = "USDA FoodData Central",
    val sourceId: String = "",
    val foodId: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val totalServings: Int = 1,
    val totalCookedWeightG: Double = 0.0,
    val totalCalories: Double = 0.0,
    val totalProteinG: Double = 0.0,
    val totalCarbsG: Double = 0.0,
    val totalFatG: Double = 0.0,
    val caloriesPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val ingredientsJson: String = "[]",    // JSON list of RecipeIngredient
    val timestamp: Long = System.currentTimeMillis()
)

data class RecipeIngredient(
    val foodName: String,
    val grams: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String,          // "2026-11-16"
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
