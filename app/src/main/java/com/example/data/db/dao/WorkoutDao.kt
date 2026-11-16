package com.example.data.db.dao

import androidx.room.*
import com.example.data.db.entities.*
import kotlinx.coroutines.flow.Flow

data class DayWithExercises(
    @Embedded val day: WorkoutDay,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutDayId"
    )
    val exercises: List<ProgramExercise>
)

data class ProgramWithDays(
    @Embedded val program: WorkoutProgram,
    @Relation(
        parentColumn = "id",
        entityColumn = "programId",
        entity = WorkoutDay::class
    )
    val daysWithExercises: List<DayWithExercises>
)

data class LoggedExerciseWithSets(
    @Embedded val exercise: LoggedExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "loggedExerciseId"
    )
    val sets: List<LoggedSet>
)

data class WorkoutLogWithDetails(
    @Embedded val log: WorkoutLog,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutLogId",
        entity = LoggedExercise::class
    )
    val exercisesWithSets: List<LoggedExerciseWithSets>
)

data class VolumeSetRawData(
    val targetMuscle: String,
    val weightKg: Double,
    val reps: Int,
    val timestamp: Long
)

@Dao
interface WorkoutDao {

    // --- Programs ---
    @Transaction
    @Query("SELECT * FROM workout_programs ORDER BY createdAt DESC")
    fun getAllProgramsWithDays(): Flow<List<ProgramWithDays>>

    @Transaction
    @Query("SELECT * FROM workout_programs WHERE isActive = 1 LIMIT 1")
    fun getActiveProgramWithDays(): Flow<ProgramWithDays?>

    @Query("SELECT * FROM workout_programs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProgramSync(): WorkoutProgram?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: WorkoutProgram): Long

    @Update
    suspend fun updateProgram(program: WorkoutProgram)

    @Query("UPDATE workout_programs SET isActive = 0")
    suspend fun clearActivePrograms()

    @Query("UPDATE workout_programs SET isActive = 1 WHERE id = :programId")
    suspend fun setActiveProgram(programId: Long)

    @Query("SELECT COUNT(*) FROM workout_programs")
    suspend fun getProgramCount(): Int

    @Query("DELETE FROM workout_programs WHERE id = :programId")
    suspend fun deleteProgram(programId: Long)

    // --- Days ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutDay(day: WorkoutDay): Long

    @Transaction
    @Query("SELECT * FROM workout_days WHERE id = :dayId")
    suspend fun getDayWithExercisesById(dayId: Long): DayWithExercises?

    @Query("DELETE FROM workout_days WHERE id = :dayId")
    suspend fun deleteWorkoutDay(dayId: Long)

    // --- Exercises ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramExercise(exercise: ProgramExercise): Long

    @Update
    suspend fun updateProgramExercise(exercise: ProgramExercise)

    @Query("DELETE FROM program_exercises WHERE id = :exerciseId")
    suspend fun deleteProgramExercise(exerciseId: Long)

    // --- Workout Logs & Volume ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedExercise(exercise: LoggedExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedSet(set: LoggedSet): Long

    @Transaction
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogWithDetails>>

    @Query("DELETE FROM workout_logs WHERE id = :logId")
    suspend fun deleteWorkoutLog(logId: Long)

    @Query("""
        SELECT le.targetMuscle AS targetMuscle, ls.weightKg AS weightKg, ls.reps AS reps, wl.timestamp AS timestamp
        FROM logged_sets ls
        INNER JOIN logged_exercises le ON ls.loggedExerciseId = le.id
        INNER JOIN workout_logs wl ON le.workoutLogId = wl.id
        WHERE ls.isCompleted = 1 AND wl.timestamp >= :startTime
    """)
    fun getCompletedSetsSince(startTime: Long): Flow<List<VolumeSetRawData>>

    @Query("""
        SELECT le.targetMuscle AS targetMuscle, ls.weightKg AS weightKg, ls.reps AS reps, wl.timestamp AS timestamp
        FROM logged_sets ls
        INNER JOIN logged_exercises le ON ls.loggedExerciseId = le.id
        INNER JOIN workout_logs wl ON le.workoutLogId = wl.id
        WHERE ls.isCompleted = 1 AND wl.timestamp >= :startTime AND wl.timestamp < :endTime
    """)
    fun getCompletedSetsInRange(startTime: Long, endTime: Long): Flow<List<VolumeSetRawData>>

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // --- Weight Logs ---
    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog): Long

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLog(id: Long)

    // --- Food Logs ---
    @Query("SELECT * FROM food_logs WHERE dateStr = :dateStr ORDER BY timestamp ASC")
    fun getFoodLogsForDate(dateStr: String): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog): Long

    @Update
    suspend fun updateFoodLog(foodLog: FoodLog)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLog(id: Long)

    // --- Food Items (Database & Search) ---
    @Query("SELECT * FROM food_items ORDER BY isFavorite DESC, lastUsedTimestamp DESC, name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isFavorite = 1 ORDER BY lastUsedTimestamp DESC, name ASC")
    fun getFavoriteFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isRecent = 1 ORDER BY lastUsedTimestamp DESC")
    fun getRecentFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isCustom = 1 OR isRecipe = 1 OR confidenceStatus = 'ESTIMATED' ORDER BY lastUsedTimestamp DESC, name ASC")
    fun getMyFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR restaurant LIKE '%' || :query || '%' OR country LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR preparationState LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchFoodItemsSync(query: String): List<FoodItem>

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodItemCount(): Int

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodItemById(id: Long): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<FoodItem>)

    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFoodItem(id: Long)

    // --- Recipes ---
    @Query("SELECT * FROM recipes ORDER BY timestamp DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    // --- Water Logs ---
    @Query("SELECT * FROM water_logs WHERE dateStr = :dateStr ORDER BY timestamp ASC")
    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterLog>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE dateStr = :dateStr")
    fun getTotalWaterForDate(dateStr: String): Flow<Int>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastWaterLog(): WaterLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog): Long

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLog(id: Long)
}
