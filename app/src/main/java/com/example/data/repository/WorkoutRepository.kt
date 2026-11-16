package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.dao.DayWithExercises
import com.example.data.db.dao.ProgramWithDays
import com.example.data.db.dao.WorkoutDao
import com.example.data.db.dao.WorkoutLogWithDetails
import com.example.data.db.entities.*
import com.example.data.network.AiFoodSearchService
import com.example.data.network.UsdaFoodApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MultiLayerSearchResult(
    val exactResults: List<FoodItem> = emptyList(),
    val similarResult: FoodItem? = null,
    val suggestedQuery: String = "",
    val aiEstimate: FoodItem? = null,
    val searchLayer: Int = 1 // 1 = Local DB, 2 = USDA, 3 = AI Estimate
)

class WorkoutRepository(
    private val database: AppDatabase,
    private val workoutDao: WorkoutDao
) {
    val allPrograms: Flow<List<ProgramWithDays>> = workoutDao.getAllProgramsWithDays()
    val activeProgram: Flow<ProgramWithDays?> = workoutDao.getActiveProgramWithDays()
    val allLogs: Flow<List<WorkoutLogWithDetails>> = workoutDao.getAllWorkoutLogs()
    val userProfile: Flow<UserProfile?> = workoutDao.getUserProfile()
    val allWeightLogs: Flow<List<WeightLog>> = workoutDao.getAllWeightLogs()
    val allFoodLogs: Flow<List<FoodLog>> = workoutDao.getAllFoodLogs()
    val favoriteFoods: Flow<List<FoodItem>> = workoutDao.getFavoriteFoodItems()
    val recentFoods: Flow<List<FoodItem>> = workoutDao.getRecentFoodItems()
    val myFoods: Flow<List<FoodItem>> = workoutDao.getMyFoods()
    val allRecipes: Flow<List<RecipeEntity>> = workoutDao.getAllRecipes()

    fun getFoodLogsForDate(dateStr: String): Flow<List<FoodLog>> = workoutDao.getFoodLogsForDate(dateStr)

    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterLog>> = workoutDao.getWaterLogsForDate(dateStr)
    fun getTotalWaterForDate(dateStr: String): Flow<Int> = workoutDao.getTotalWaterForDate(dateStr)

    suspend fun logWater(amountMl: Int) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        workoutDao.insertWaterLog(WaterLog(dateStr = dateStr, amountMl = amountMl))
    }

    suspend fun deleteWaterLog(id: Long) = workoutDao.deleteWaterLog(id)

    suspend fun getLastWaterLog(): WaterLog? = workoutDao.getLastWaterLog()

    suspend fun seedDefaultDataIfEmpty() {
        if (workoutDao.getProgramCount() == 0) {
            seedApexFitHybridAthleteProgram()
            seedPPLProgram()
            seedArnoldProgram()
        }
        if (workoutDao.getFoodItemCount() == 0) {
            seedInitialFoodsIfNeeded()
        }
    }

    suspend fun seedApexFitHybridAthleteProgram(): Long {
        workoutDao.clearActivePrograms()
        val programId = workoutDao.insertProgram(
            WorkoutProgram(
                title = "APEXFIT HYBRID ATHLETE",
                cycleDays = 14,
                description = "14-Day Hyper Athlete + Hypertrophy Program. Continuous Train/Rest rotation featuring Back, Chest, Legs, Arms/Delts, Core, Power & Zone 2.",
                isActive = true
            )
        )

        // DAY 1 — BACK-FOCUSED FULL BODY
        val d1 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 1", dayName = "Back-Focused Full Body + Core + Farmer Carry", orderIndex = 1))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Neutral Grip Lat Pulldown", targetMuscle = "Lats/Back", defaultSets = 3, repsRange = "6–10", rir = "2", restSeconds = 150, notes = "Focus on driving the elbows down rather than pulling with the hands.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Chest-Supported T-Bar Row", targetMuscle = "Mid Back", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 150, notes = "Keep the chest firmly supported. Avoid excessive shrugging.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Incline Barbell Chest Press", targetMuscle = "Upper Chest", defaultSets = 3, repsRange = "6–10", rir = "2", restSeconds = 150, notes = "Maintain stable arch; lower bar with control.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 3, repsRange = "8–12", rir = "2", restSeconds = 150, notes = "Do not allow the pelvis to roll excessively into posterior tilt at the bottom.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, repsRange = "10–15", rir = "1–2", restSeconds = 100, notes = "Squeeze hamstrings at peak contraction.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 2, repsRange = "12–20", rir = "1–2", restSeconds = 75, notes = "Control the lowering phase.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "McGill Curl-Up", targetMuscle = "Core", defaultSets = 2, repsRange = "6–10 controlled reps", rir = "2", restSeconds = 60, notes = "Controlled reps maintaining neutral lumbar alignment.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Bird Dog", targetMuscle = "Core", defaultSets = 2, repsRange = "6–8 reps/side", rir = "2", restSeconds = 60, notes = "Focus on maintaining a neutral spine rather than moving as far as possible.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d1, exerciseName = "Farmer Carry", targetMuscle = "Full Body", defaultSets = 3, repsRange = "20–30 m", rir = "1–2", restSeconds = 75, notes = "Use a load that allows you to walk tall without lateral trunk leaning.", category = "Hyper Athlete"))

        // DAY 2 — RECOVERY / ZONE 2
        val d2 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 2", dayName = "Recovery / Zone 2", orderIndex = 2))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d2, exerciseName = "Stationary Bike (Zone 2)", targetMuscle = "Cardio", defaultSets = 1, repsRange = "20–30 min", rir = "N/A", restSeconds = 0, notes = "Option A: Target RPE 5–6/10. Able to speak in complete sentences. No hard intervals.", category = "Zone 2"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d2, exerciseName = "Recovery Walk", targetMuscle = "Cardio", defaultSets = 1, repsRange = "25–40 min", rir = "N/A", restSeconds = 0, notes = "Option B: Light active recovery walk.", category = "Zone 2"))

        // DAY 3 — CHEST-FOCUSED FULL BODY + POWER
        val d3 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 3", dayName = "Chest-Focused Full Body + Power", orderIndex = 3))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Incline Barbell Chest Press", targetMuscle = "Upper Chest", defaultSets = 4, repsRange = "5–8", rir = "2", restSeconds = 180, notes = "Primary strength movement for chest.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Machine Pec Deck", targetMuscle = "Chest", defaultSets = 3, repsRange = "10–15", rir = "1", restSeconds = 90, notes = "Control eccentric phase; avoid aggressively stretching shoulder.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Close-Grip Lat Pulldown", targetMuscle = "Lats/Back", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "Smooth pull to collarbone.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 3, repsRange = "8–12", rir = "2", restSeconds = 120, notes = "Keep lower back flat against pad.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, repsRange = "10–15", rir = "1–2", restSeconds = 90, notes = "Strict hamstring contraction.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 2, repsRange = "12–20", rir = "1–2", restSeconds = 75, notes = "Side delts isolation.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Side Plank", targetMuscle = "Core", defaultSets = 2, repsRange = "20–40 sec/side", rir = "1–2", restSeconds = 60, notes = "Hold firm lateral lumbar pillar.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d3, exerciseName = "Explosive Incline Push-Up", targetMuscle = "Chest / Power", defaultSets = 3, repsRange = "3–5 reps", rir = "3", restSeconds = 90, notes = "Bench or Smith-machine bar. Objective is max concentric speed, not muscular fatigue.", category = "Power"))

        // DAY 4 — REST
        val d4 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 4", dayName = "Rest Day", orderIndex = 4, isRestDay = true))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d4, exerciseName = "Rest & CNS Recovery", targetMuscle = "Rest", defaultSets = 1, repsRange = "Full Day", rir = "N/A", restSeconds = 0, notes = "Normal daily activity. Optional light walking.", category = "Rest"))

        // DAY 5 — ARMS + LATERAL/REAR DELTS + CORE
        val d5 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 5", dayName = "Arms + Lateral/Rear Delts + Core", orderIndex = 5))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Bayesian Cable Curl", targetMuscle = "Biceps", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 90, notes = "Stepped forward from cable column for deep bicep stretch.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Zottman Dumbbell Curl", targetMuscle = "Biceps / Forearms", defaultSets = 2, repsRange = "8–12", rir = "1–2", restSeconds = 75, notes = "Supinate on up, pronate on lowering phase.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Rope Hammer Cable Curl", targetMuscle = "Brachialis", defaultSets = 2, repsRange = "10–15", rir = "1", restSeconds = 60, notes = "Keep upper arms pinned.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Wrist / Forearm Dumbbell Curls", targetMuscle = "Forearms", defaultSets = 2, repsRange = "12–20", rir = "1", restSeconds = 60, notes = "Smooth wrist movement.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Overhead Cable Triceps Extension", targetMuscle = "Triceps", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 90, notes = "Keep elbows high and controlled.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Rope Triceps Pushdown", targetMuscle = "Triceps", defaultSets = 2, repsRange = "10–15", rir = "1", restSeconds = 60, notes = "Spread rope handles apart at bottom.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 4, repsRange = "12–20", rir = "1", restSeconds = 75, notes = "Slight forward body angle.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Reverse Pec Deck", targetMuscle = "Rear Delts", defaultSets = 3, repsRange = "12–20", rir = "1", restSeconds = 75, notes = "Lead with rear delts, avoid pinching shoulder blades.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Dead Bug", targetMuscle = "Core", defaultSets = 3, repsRange = "6–10/side", rir = "2", restSeconds = 60, notes = "Keep lower back pressed flat into floor.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d5, exerciseName = "Vacuum", targetMuscle = "Transverse Abdominis", defaultSets = 3, repsRange = "20–30 sec", rir = "N/A", restSeconds = 45, notes = "Exhale fully, pull navel to spine. Rest 30-60s between sets.", category = "Core"))

        // DAY 6 — RECOVERY / ZONE 2
        val d6 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 6", dayName = "Recovery / Zone 2", orderIndex = 6))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d6, exerciseName = "Stationary Bike (Zone 2)", targetMuscle = "Cardio", defaultSets = 1, repsRange = "20–30 min", rir = "N/A", restSeconds = 0, notes = "Keep intensity moderate RPE 5-6/10.", category = "Zone 2"))

        // DAY 7 — LEG-FOCUSED FULL BODY
        val d7 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 7", dayName = "Leg-Focused Full Body + Conditioning", orderIndex = 7))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 4, repsRange = "6–10", rir = "2", restSeconds = 150, notes = "Primary lower body press.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Hip Thrust", targetMuscle = "Glutes", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "Do not finish rep by aggressively extending lumbar spine. Final position from glute contraction.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Leg Extension", targetMuscle = "Quads", defaultSets = 2, repsRange = "10–15", rir = "1", restSeconds = 90, notes = "Treat Hip Thrust and Leg Extension as separate movements.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, repsRange = "10–15", rir = "1", restSeconds = 90, notes = "Hamstrings contraction.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Incline Barbell Chest Press", targetMuscle = "Upper Chest", defaultSets = 2, repsRange = "8–12", rir = "2", restSeconds = 120, notes = "Upper body volume set.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Neutral Grip Lat Pulldown", targetMuscle = "Lats/Back", defaultSets = 2, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "Upper body pulldown volume.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Calf Raise", targetMuscle = "Calves", defaultSets = 4, repsRange = "8–15", rir = "1–2", restSeconds = 60, notes = "Pause 1s at top extension.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Side Plank", targetMuscle = "Core", defaultSets = 2, repsRange = "30–45 sec/side", rir = "1–2", restSeconds = 60, notes = "Side pillar hold.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d7, exerciseName = "Stationary Bike Intervals", targetMuscle = "Conditioning", defaultSets = 8, repsRange = "20s Hard / 80s Easy", rir = "2", restSeconds = 80, notes = "8 rounds: 20s hard (RPE 8/10), 80s easy. Total interval = 13 min 20 sec.", category = "Conditioning"))

        // DAY 8 — REST
        val d8 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 8", dayName = "Rest Day", orderIndex = 8, isRestDay = true))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d8, exerciseName = "Rest & Recovery", targetMuscle = "Rest", defaultSets = 1, repsRange = "Full Day", rir = "N/A", restSeconds = 0, notes = "No structured training. CNS recovery.", category = "Rest"))

        // DAY 9 — BACK-FOCUSED FULL BODY B
        val d9 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 9", dayName = "Back-Focused Full Body B + Core + Carry", orderIndex = 9))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Chest-Supported T-Bar Row", targetMuscle = "Mid Back", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 150, notes = "T-bar row focus.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Close-Grip Lat Pulldown", targetMuscle = "Lats/Back", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "Close grip lat pulldown.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Incline Barbell Chest Press", targetMuscle = "Upper Chest", defaultSets = 3, repsRange = "8–10", rir = "2", restSeconds = 120, notes = "Upper chest incline press.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 3, repsRange = "10–15", rir = "1–2", restSeconds = 120, notes = "Leg press hypertrophy.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, repsRange = "10–15", rir = "1–2", restSeconds = 90, notes = "Hamstring curls.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 2, repsRange = "12–20", rir = "1–2", restSeconds = 60, notes = "Side delts fly.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Pallof Press", targetMuscle = "Core", defaultSets = 2, repsRange = "10–15/side", rir = "1–2", restSeconds = 60, notes = "Hold the end position for approximately 1 second.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d9, exerciseName = "Farmer Carry", targetMuscle = "Full Body", defaultSets = 3, repsRange = "20–30 m", rir = "1–2", restSeconds = 75, notes = "Grip & core carry.", category = "Hyper Athlete"))

        // DAY 10 — RECOVERY / ZONE 2
        val d10 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 10", dayName = "Recovery / Zone 2", orderIndex = 10))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d10, exerciseName = "Stationary Bike or Walking", targetMuscle = "Cardio", defaultSets = 1, repsRange = "20–30 min", rir = "N/A", restSeconds = 0, notes = "Stationary bike or walking zone 2.", category = "Zone 2"))

        // DAY 11 — CHEST-FOCUSED FULL BODY B
        val d11 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 11", dayName = "Chest-Focused Full Body B + Power", orderIndex = 11))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Incline Barbell Chest Press", targetMuscle = "Upper Chest", defaultSets = 4, repsRange = "5–8", rir = "2", restSeconds = 180, notes = "Heavy chest pressing.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Machine Pec Deck", targetMuscle = "Chest", defaultSets = 3, repsRange = "10–15", rir = "1", restSeconds = 90, notes = "Pec deck fly.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Neutral Grip Lat Pulldown", targetMuscle = "Lats/Back", defaultSets = 2, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "Lat pulldown.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Chest-Supported T-Bar Row", targetMuscle = "Mid Back", defaultSets = 2, repsRange = "8–12", rir = "1–2", restSeconds = 120, notes = "T-bar row.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 3, repsRange = "8–12", rir = "2", restSeconds = 120, notes = "Leg press.", category = "Compound"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, repsRange = "10–15", rir = "1–2", restSeconds = 90, notes = "Hamstrings isolation.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 2, repsRange = "12–20", rir = "1", restSeconds = 60, notes = "Lateral raise.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Explosive Incline Push-Up", targetMuscle = "Chest / Power", defaultSets = 3, repsRange = "3–5 reps", rir = "3", restSeconds = 90, notes = "Explosive concentric push.", category = "Power"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d11, exerciseName = "Pallof Press", targetMuscle = "Core", defaultSets = 2, repsRange = "10–15/side", rir = "1–2", restSeconds = 60, notes = "Anti-rotation press.", category = "Core"))

        // DAY 12 — REST
        val d12 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 12", dayName = "Rest Day", orderIndex = 12, isRestDay = true))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d12, exerciseName = "Rest & Recovery", targetMuscle = "Rest", defaultSets = 1, repsRange = "Full Day", rir = "N/A", restSeconds = 0, notes = "Normal activity only.", category = "Rest"))

        // DAY 13 — ARMS + LATERAL/REAR DELTS B
        val d13 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 13", dayName = "Arms + Lateral/Rear Delts B + Core", orderIndex = 13))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Bayesian Cable Curl", targetMuscle = "Biceps", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 90, notes = "Bicep curl.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Rope Hammer Cable Curl", targetMuscle = "Brachialis", defaultSets = 2, repsRange = "10–15", rir = "1", restSeconds = 60, notes = "Hammer curl.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Zottman Dumbbell Curl", targetMuscle = "Biceps / Forearms", defaultSets = 2, repsRange = "8–12", rir = "1–2", restSeconds = 60, notes = "Zottman curl.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Wrist / Forearm Dumbbell Curls", targetMuscle = "Forearms", defaultSets = 2, repsRange = "12–20", rir = "1", restSeconds = 60, notes = "Forearms.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Overhead Cable Triceps Extension", targetMuscle = "Triceps", defaultSets = 3, repsRange = "8–12", rir = "1–2", restSeconds = 90, notes = "Overhead triceps.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Rope Triceps Pushdown", targetMuscle = "Triceps", defaultSets = 2, repsRange = "10–15", rir = "1", restSeconds = 60, notes = "Triceps pushdown.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Cable Lateral Raise", targetMuscle = "Side Delts", defaultSets = 4, repsRange = "12–20", rir = "1", restSeconds = 60, notes = "Side delts.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Reverse Pec Deck", targetMuscle = "Rear Delts", defaultSets = 3, repsRange = "12–20", rir = "1", restSeconds = 60, notes = "Rear delts.", category = "Isolation"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Dead Bug", targetMuscle = "Core", defaultSets = 3, repsRange = "6–10/side", rir = "2", restSeconds = 60, notes = "Dead bug core.", category = "Core"))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d13, exerciseName = "Vacuum", targetMuscle = "Transverse Abdominis", defaultSets = 3, repsRange = "20–30 sec", rir = "N/A", restSeconds = 45, notes = "Vacuum holds.", category = "Core"))

        // DAY 14 — REST
        val d14 = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Day 14", dayName = "Rest Day", orderIndex = 14, isRestDay = true))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = d14, exerciseName = "Rest & Recovery", targetMuscle = "Rest", defaultSets = 1, repsRange = "Full Day", rir = "N/A", restSeconds = 0, notes = "Rest day before cycle repeats at Day 15 (Day 1).", category = "Rest"))

        return programId
    }

    suspend fun createProgramWithDays(
        title: String,
        selectedDays: List<String>,
        cycleDays: Int = 7,
        description: String = ""
    ): Long {
        workoutDao.clearActivePrograms()
        val programId = workoutDao.insertProgram(
            WorkoutProgram(
                title = title,
                cycleDays = cycleDays,
                description = description,
                isActive = true
            )
        )
        selectedDays.forEachIndexed { idx, dayLabel ->
            val isRest = dayLabel.lowercase(Locale.ROOT).contains("rest")
            val cleanWeekDay = if (dayLabel.contains(" - ")) {
                dayLabel.substringAfter(" - ").trim()
            } else {
                dayLabel
            }
            val cleanName = dayLabel
            workoutDao.insertWorkoutDay(
                WorkoutDay(
                    programId = programId,
                    dayOfWeek = cleanWeekDay,
                    dayName = cleanName,
                    orderIndex = idx + 1,
                    isRestDay = isRest
                )
            )
        }
        return programId
    }

    private suspend fun seedPPLProgram() {
        val programId = workoutDao.insertProgram(
            WorkoutProgram(
                title = "Push Pull Legs (PPL)",
                isActive = true
            )
        )

        // Day 1: Push A
        val day1Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Monday", dayName = "Push A - Chest, Shoulders & Triceps", orderIndex = 1))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Barbell Bench Press", targetMuscle = "Chest", defaultSets = 4, restSeconds = 120))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Overhead Barbell Press", targetMuscle = "Shoulders", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Incline Dumbbell Press", targetMuscle = "Chest", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Dumbbell Lateral Raise", targetMuscle = "Shoulders", defaultSets = 4, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Tricep Pushdown (Cable)", targetMuscle = "Triceps", defaultSets = 3, restSeconds = 60))

        // Day 2: Pull A
        val day2Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Tuesday", dayName = "Pull A - Back & Biceps", orderIndex = 2))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Barbell Deadlift", targetMuscle = "Back", defaultSets = 3, restSeconds = 180))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Lat Pulldown (Cable)", targetMuscle = "Back", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Bent Over Barbell Row", targetMuscle = "Back", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Face Pull (Cable)", targetMuscle = "Rear Delts", defaultSets = 4, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Barbell Bicep Curl", targetMuscle = "Biceps", defaultSets = 3, restSeconds = 60))

        // Day 3: Legs A
        val day3Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Wednesday", dayName = "Legs A - Quads, Hamstrings & Calves", orderIndex = 3))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Barbell Back Squat", targetMuscle = "Quads", defaultSets = 4, restSeconds = 180))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Romanian Deadlift", targetMuscle = "Hamstrings", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Leg Press", targetMuscle = "Quads", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Lying Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Standing Calf Raise", targetMuscle = "Calves", defaultSets = 4, restSeconds = 60))

        // Day 4: Push B
        val day4Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Thursday", dayName = "Push B - Hypertrophy", orderIndex = 4))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day4Id, exerciseName = "Incline Barbell Bench Press", targetMuscle = "Chest", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day4Id, exerciseName = "Dumbbell Shoulder Press", targetMuscle = "Shoulders", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day4Id, exerciseName = "Chest Cable Fly", targetMuscle = "Chest", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day4Id, exerciseName = "Cable Lateral Raise", targetMuscle = "Shoulders", defaultSets = 4, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day4Id, exerciseName = "Skullcrusher (EZ Bar)", targetMuscle = "Triceps", defaultSets = 3, restSeconds = 60))

        // Day 5: Pull B
        val day5Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Friday", dayName = "Pull B - Hypertrophy", orderIndex = 5))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day5Id, exerciseName = "Seated Cable Row", targetMuscle = "Back", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day5Id, exerciseName = "Dumbbell Single Arm Row", targetMuscle = "Back", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day5Id, exerciseName = "Straight Arm Cable Pulldown", targetMuscle = "Back", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day5Id, exerciseName = "Incline Dumbbell Curl", targetMuscle = "Biceps", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day5Id, exerciseName = "Hammer Curl", targetMuscle = "Biceps", defaultSets = 3, restSeconds = 60))

        // Day 6: Legs B
        val day6Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Saturday", dayName = "Legs B - Hypertrophy", orderIndex = 6))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day6Id, exerciseName = "Front Squat", targetMuscle = "Quads", defaultSets = 4, restSeconds = 120))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day6Id, exerciseName = "Bulgarian Split Squat", targetMuscle = "Quads", defaultSets = 3, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day6Id, exerciseName = "Leg Extension", targetMuscle = "Quads", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day6Id, exerciseName = "Seated Leg Curl", targetMuscle = "Hamstrings", defaultSets = 3, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day6Id, exerciseName = "Seated Calf Raise", targetMuscle = "Calves", defaultSets = 4, restSeconds = 60))
    }

    private suspend fun seedArnoldProgram() {
        val programId = workoutDao.insertProgram(
            WorkoutProgram(
                title = "Arnold Schwarzenegger Golden Era Split",
                isActive = false
            )
        )

        val day1Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Monday", dayName = "Chest & Back A", orderIndex = 1))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Barbell Bench Press", targetMuscle = "Chest", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Wide Grip Pull-up", targetMuscle = "Back", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Incline Dumbbell Press", targetMuscle = "Chest", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "T-Bar Row", targetMuscle = "Back", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day1Id, exerciseName = "Dumbbell Pullover", targetMuscle = "Chest", defaultSets = 3, restSeconds = 60))

        val day2Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Tuesday", dayName = "Shoulders & Arms A", orderIndex = 2))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Clean and Press", targetMuscle = "Shoulders", defaultSets = 4, restSeconds = 120))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Dumbbell Lateral Raise", targetMuscle = "Shoulders", defaultSets = 4, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Barbell Cheat Curl", targetMuscle = "Biceps", defaultSets = 4, restSeconds = 60))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day2Id, exerciseName = "Close Grip Bench Press", targetMuscle = "Triceps", defaultSets = 4, restSeconds = 60))

        val day3Id = workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = "Wednesday", dayName = "Legs & Lower Back A", orderIndex = 3))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Barbell Back Squat", targetMuscle = "Quads", defaultSets = 5, restSeconds = 150))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Stiff Legged Deadlift", targetMuscle = "Hamstrings", defaultSets = 4, restSeconds = 90))
        workoutDao.insertProgramExercise(ProgramExercise(workoutDayId = day3Id, exerciseName = "Standing Calf Raise", targetMuscle = "Calves", defaultSets = 5, restSeconds = 60))
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        workoutDao.insertUserProfile(profile)
    }

    suspend fun saveWeightLog(weightKg: Double) {
        workoutDao.insertWeightLog(WeightLog(weightKg = weightKg))
    }

    suspend fun deleteWeightLog(id: Long) {
        workoutDao.deleteWeightLog(id)
    }

    suspend fun setActiveProgram(programId: Long) {
        workoutDao.clearActivePrograms()
        workoutDao.setActiveProgram(programId)
    }

    suspend fun deleteProgram(programId: Long) {
        workoutDao.deleteProgram(programId)
    }

    suspend fun addExerciseToDay(
        workoutDayId: Long,
        exerciseName: String,
        targetMuscle: String,
        defaultSets: Int,
        restSeconds: Int,
        repsRange: String = "8-12",
        rir: String = "1-2",
        notes: String = "",
        category: String = "Hypertrophy"
    ) {
        workoutDao.insertProgramExercise(
            ProgramExercise(
                workoutDayId = workoutDayId,
                exerciseName = exerciseName,
                targetMuscle = targetMuscle,
                defaultSets = defaultSets,
                restSeconds = restSeconds,
                repsRange = repsRange,
                rir = rir,
                notes = notes,
                category = category
            )
        )
    }

    suspend fun updateExercise(exercise: ProgramExercise) {
        workoutDao.updateProgramExercise(exercise)
    }

    suspend fun deleteExercise(exerciseId: Long) {
        workoutDao.deleteProgramExercise(exerciseId)
    }

    suspend fun getDayWithExercises(dayId: Long): DayWithExercises? {
        return workoutDao.getDayWithExercisesById(dayId)
    }

    suspend fun saveCompletedWorkout(
        programTitle: String,
        dayName: String,
        dayOfWeek: String,
        durationSeconds: Long,
        loggedExercises: List<Pair<ProgramExercise, List<Pair<Double, Int>>>>
    ) {
        val logId = workoutDao.insertWorkoutLog(
            WorkoutLog(
                programTitle = programTitle,
                dayName = dayName,
                dayOfWeek = dayOfWeek,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds
            )
        )

        loggedExercises.forEachIndexed { exIdx, (ex, sets) ->
            val loggedExId = workoutDao.insertLoggedExercise(
                LoggedExercise(
                    workoutLogId = logId,
                    exerciseName = ex.exerciseName,
                    targetMuscle = ex.targetMuscle,
                    restSeconds = ex.restSeconds,
                    orderIndex = exIdx + 1,
                    repsRange = ex.repsRange,
                    rir = ex.rir,
                    notes = ex.notes
                )
            )

            sets.forEachIndexed { setIdx, (weight, reps) ->
                workoutDao.insertLoggedSet(
                    LoggedSet(
                        loggedExerciseId = loggedExId,
                        setNumber = setIdx + 1,
                        weightKg = weight,
                        reps = reps,
                        isCompleted = true
                    )
                )
            }
        }
    }

    fun getCompletedSetsForCurrentWeek(): Flow<List<com.example.data.db.dao.VolumeSetRawData>> {
        val start = getStartOfWeekTimestamp()
        return workoutDao.getCompletedSetsSince(start)
    }

    fun getCompletedSetsForPreviousWeek(): Flow<List<com.example.data.db.dao.VolumeSetRawData>> {
        val startCurrent = getStartOfWeekTimestamp()
        val startPrev = startCurrent - (7 * 24 * 60 * 60 * 1000L)
        return workoutDao.getCompletedSetsInRange(startPrev, startCurrent)
    }

    private fun getStartOfWeekTimestamp(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // --- MULTI-LAYER FOOD SEARCH SYSTEM ---
    suspend fun performMultiLayerFoodSearch(query: String, userCountry: String = "Egypt"): MultiLayerSearchResult {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return MultiLayerSearchResult()

        // Layer 1: Local DB Search with Smart Ranking
        val localMatches = workoutDao.searchFoodItemsSync("%$cleanQuery%")

        val sortedLocalMatches = localMatches.sortedWith(
            compareByDescending<FoodItem> { it.name.equals(cleanQuery, ignoreCase = true) }
                .thenByDescending { it.country.equals(userCountry, ignoreCase = true) }
                .thenByDescending { it.confidenceStatus == "VERIFIED" }
                .thenByDescending { it.brand.contains(cleanQuery, ignoreCase = true) || it.restaurant.contains(cleanQuery, ignoreCase = true) }
        )

        if (sortedLocalMatches.isNotEmpty()) {
            return MultiLayerSearchResult(
                exactResults = sortedLocalMatches,
                searchLayer = 1
            )
        }

        // Layer 2: USDA API Search
        val usdaMatches = try {
            UsdaFoodApiService.searchFoods(cleanQuery)
        } catch (e: Exception) {
            emptyList()
        }

        if (usdaMatches.isNotEmpty()) {
            workoutDao.insertFoodItems(usdaMatches)
            return MultiLayerSearchResult(
                exactResults = usdaMatches,
                searchLayer = 2
            )
        }

        // Layer 3: No exact verified match -> Return empty so UI offers AI Estimate button
        return MultiLayerSearchResult(
            suggestedQuery = cleanQuery,
            searchLayer = 3
        )
    }

    suspend fun generateAiEstimate(query: String): FoodItem {
        val estimate = AiFoodSearchService.generateAiEstimate(query)
        val id = workoutDao.insertFoodItem(estimate)
        return estimate.copy(id = id)
    }

    suspend fun createProgramWithDays(title: String, dayNames: List<String>): Long {
        val programId = workoutDao.insertProgram(WorkoutProgram(title = title, isActive = false))
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        dayNames.forEachIndexed { idx, name ->
            val dayOfWeek = daysOfWeek.getOrElse(idx % 7) { "Monday" }
            workoutDao.insertWorkoutDay(WorkoutDay(programId = programId, dayOfWeek = dayOfWeek, dayName = name, orderIndex = idx + 1))
        }
        return programId
    }

    suspend fun deleteWorkoutLog(logId: Long) {
        workoutDao.deleteWorkoutLog(logId)
    }

    suspend fun addCustomFood(foodItem: FoodItem) {
        saveCustomFood(foodItem)
    }

    suspend fun addFoodLogWithItem(foodLog: FoodLog, foodItem: FoodItem? = null) {
        addFoodLog(foodLog, foodItem)
    }

    suspend fun saveFoodLog(foodLog: FoodLog) {
        addFoodLog(foodLog)
    }

    suspend fun scanNutritionLabel(labelText: String): FoodItem {
        val scanned = AiFoodSearchService.parseNutritionLabel(labelText)
        val id = workoutDao.insertFoodItem(scanned)
        return scanned.copy(id = id)
    }

    suspend fun addFoodLog(log: FoodLog, foodItem: FoodItem? = null) {
        workoutDao.insertFoodLog(log)
        foodItem?.let {
            val updated = it.copy(isRecent = true, lastUsedTimestamp = System.currentTimeMillis())
            workoutDao.updateFoodItem(updated)
        }
    }

    suspend fun updateFoodLog(log: FoodLog) {
        workoutDao.updateFoodLog(log)
    }

    suspend fun deleteFoodLog(id: Long) {
        workoutDao.deleteFoodLog(id)
    }

    suspend fun toggleFavoriteFood(foodItem: FoodItem) {
        val updated = foodItem.copy(isFavorite = !foodItem.isFavorite)
        workoutDao.updateFoodItem(updated)
    }

    suspend fun saveCustomFood(foodItem: FoodItem) {
        workoutDao.insertFoodItem(foodItem.copy(isCustom = true, confidenceStatus = "CUSTOM"))
    }

    suspend fun saveRecipe(recipe: RecipeEntity) {
        val recipeId = workoutDao.insertRecipe(recipe)

        // Also save as a searchable FoodItem in local DB
        val foodItem = FoodItem(
            name = recipe.title,
            brand = "Custom Recipe",
            source = "Recipe Builder",
            sourceBadge = "[Recipe]",
            confidenceStatus = "CUSTOM",
            isRecipe = true,
            isCustom = true,
            referenceAmount = 100.0,
            referenceUnit = "g",
            caloriesPer100g = recipe.caloriesPer100g,
            proteinPer100g = recipe.proteinPer100g,
            carbsPer100g = recipe.carbsPer100g,
            fatPer100g = recipe.fatPer100g,
            servingOptionsJson = """[{"unit":"1 serving","grams":${if (recipe.totalServings > 0) recipe.totalCookedWeightG / recipe.totalServings else 100.0}},{"unit":"100 g","grams":100.0}]"""
        )
        workoutDao.insertFoodItem(foodItem)
    }

    val staticMuscleList = listOf(
        "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Quads", "Hamstrings", "Calves", "Abs", "Rear Delts"
    )

    companion object {
        val ALL_MUSCLES = listOf(
            "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Quads", "Hamstrings", "Calves", "Abs", "Rear Delts"
        )
    }

    private suspend fun seedInitialFoodsIfNeeded() {
        if (workoutDao.getFoodItemCount() == 0) {
            val initialFoods = listOf(
                // --- MCDONALD'S EGYPT 🇪🇬 & USA 🇺🇸 ---
                FoodItem(
                    name = "Big Mac",
                    brand = "McDonald's",
                    restaurant = "McDonald's",
                    country = "Egypt",
                    category = "Burgers",
                    source = "Official McDonald's Egypt Nutrition Data",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "MCD-EG-01",
                    preparationState = "Cooked",
                    referenceAmount = 230.9,
                    referenceUnit = "sandwich",
                    caloriesPer100g = 232.5,
                    proteinPer100g = 12.05,
                    carbsPer100g = 22.71,
                    fatPer100g = 14.07,
                    servingOptionsJson = """[{"unit":"1 sandwich (230.9g)","grams":230.9},{"unit":"100 g","grams":100.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Big Mac USA",
                    brand = "McDonald's",
                    restaurant = "McDonald's",
                    country = "USA",
                    category = "Burgers",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "MCD-US-01",
                    preparationState = "Cooked",
                    referenceAmount = 219.0,
                    referenceUnit = "sandwich",
                    caloriesPer100g = 269.0,
                    proteinPer100g = 11.4,
                    carbsPer100g = 21.0,
                    fatPer100g = 15.5,
                    servingOptionsJson = """[{"unit":"1 sandwich (219g)","grams":219.0},{"unit":"100 g","grams":100.0}]"""
                ),
                FoodItem(
                    name = "McChicken",
                    brand = "McDonald's",
                    restaurant = "McDonald's",
                    country = "Egypt",
                    category = "Chicken",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "MCD-EG-02",
                    preparationState = "Cooked",
                    referenceAmount = 143.0,
                    referenceUnit = "sandwich",
                    caloriesPer100g = 280.0,
                    proteinPer100g = 10.5,
                    carbsPer100g = 28.0,
                    fatPer100g = 14.0,
                    servingOptionsJson = """[{"unit":"1 sandwich (143g)","grams":143.0},{"unit":"100 g","grams":100.0}]"""
                ),
                FoodItem(
                    name = "McChicken Meal",
                    brand = "McDonald's",
                    restaurant = "McDonald's",
                    country = "Egypt",
                    category = "Meals",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "MCD-EG-03",
                    preparationState = "Cooked",
                    referenceAmount = 1.0,
                    referenceUnit = "meal",
                    caloriesPer100g = 220.0,
                    proteinPer100g = 5.4,
                    carbsPer100g = 31.4,
                    fatPer100g = 9.7,
                    servingOptionsJson = """[{"unit":"1 meal combo","grams":350.0},{"unit":"100 g","grams":100.0}]"""
                ),
                FoodItem(
                    name = "French Fries (Medium)",
                    brand = "McDonald's",
                    restaurant = "McDonald's",
                    country = "Egypt",
                    category = "Sides",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "MCD-EG-04",
                    preparationState = "Fried",
                    referenceAmount = 117.0,
                    referenceUnit = "portion",
                    caloriesPer100g = 290.0,
                    proteinPer100g = 3.4,
                    carbsPer100g = 37.6,
                    fatPer100g = 13.7,
                    servingOptionsJson = """[{"unit":"1 portion (117g)","grams":117.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- KFC EGYPT 🇪🇬 & USA 🇺🇸 ---
                FoodItem(
                    name = "KFC Zinger Sandwich",
                    brand = "KFC",
                    restaurant = "KFC",
                    country = "Egypt",
                    category = "Chicken",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "KFC-EG-01",
                    preparationState = "Fried",
                    referenceAmount = 210.0,
                    referenceUnit = "sandwich",
                    caloriesPer100g = 224.0,
                    proteinPer100g = 13.3,
                    carbsPer100g = 19.5,
                    fatPer100g = 10.0,
                    servingOptionsJson = """[{"unit":"1 sandwich (210g)","grams":210.0},{"unit":"100 g","grams":100.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Rizo Rice with Spicy Sauce",
                    brand = "KFC",
                    restaurant = "KFC",
                    country = "Egypt",
                    category = "Sides",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "KFC-EG-02",
                    preparationState = "Cooked",
                    referenceAmount = 250.0,
                    referenceUnit = "bowl",
                    caloriesPer100g = 152.0,
                    proteinPer100g = 3.2,
                    carbsPer100g = 27.2,
                    fatPer100g = 3.6,
                    servingOptionsJson = """[{"unit":"1 bowl (250g)","grams":250.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- PAPA JOHN'S EGYPT 🇪🇬 ---
                FoodItem(
                    name = "Chicken Ranch Papadias",
                    brand = "Papa John's",
                    restaurant = "Papa John's",
                    country = "Egypt",
                    category = "Papadias",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "PAPA-EG-01",
                    preparationState = "Baked",
                    referenceAmount = 240.0,
                    referenceUnit = "item",
                    caloriesPer100g = 258.0,
                    proteinPer100g = 13.3,
                    carbsPer100g = 22.5,
                    fatPer100g = 11.6,
                    servingOptionsJson = """[{"unit":"1 papadias (240g)","grams":240.0},{"unit":"100 g","grams":100.0}]"""
                ),
                FoodItem(
                    name = "Chicken Ranch Pizza",
                    brand = "Papa John's",
                    restaurant = "Papa John's",
                    country = "Egypt",
                    category = "Pizzas",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "PAPA-EG-02",
                    preparationState = "Baked",
                    referenceAmount = 110.0,
                    referenceUnit = "slice",
                    caloriesPer100g = 263.0,
                    proteinPer100g = 11.8,
                    carbsPer100g = 28.1,
                    fatPer100g = 10.9,
                    servingOptionsJson = """[{"unit":"1 slice (110g)","grams":110.0},{"unit":"1 whole pizza (880g)","grams":880.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- PIZZA HUT EGYPT 🇪🇬 ---
                FoodItem(
                    name = "Super Supreme Stuffed Crust Pizza",
                    brand = "Pizza Hut",
                    restaurant = "Pizza Hut",
                    country = "Egypt",
                    category = "Pizzas",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "PH-EG-01",
                    preparationState = "Baked",
                    referenceAmount = 125.0,
                    referenceUnit = "slice",
                    caloriesPer100g = 272.0,
                    proteinPer100g = 12.0,
                    carbsPer100g = 27.2,
                    fatPer100g = 12.8,
                    servingOptionsJson = """[{"unit":"1 slice (125g)","grams":125.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- BURGER KING EGYPT 🇪🇬 ---
                FoodItem(
                    name = "Whopper Burger",
                    brand = "Burger King",
                    restaurant = "Burger King",
                    country = "Egypt",
                    category = "Burgers",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "BK-EG-01",
                    preparationState = "Grilled",
                    referenceAmount = 290.0,
                    referenceUnit = "sandwich",
                    caloriesPer100g = 227.0,
                    proteinPer100g = 9.6,
                    carbsPer100g = 16.9,
                    fatPer100g = 13.8,
                    servingOptionsJson = """[{"unit":"1 sandwich (290g)","grams":290.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- SUBWAY EGYPT 🇪🇬 ---
                FoodItem(
                    name = "Chicken Teriyaki Sub (6 inch)",
                    brand = "Subway",
                    restaurant = "Subway",
                    country = "Egypt",
                    category = "Subs",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "SUB-EG-01",
                    preparationState = "Baked",
                    referenceAmount = 230.0,
                    referenceUnit = "sub",
                    caloriesPer100g = 160.0,
                    proteinPer100g = 11.3,
                    carbsPer100g = 23.4,
                    fatPer100g = 2.1,
                    servingOptionsJson = """[{"unit":"1 sub 6-inch (230g)","grams":230.0},{"unit":"1 footlong (460g)","grams":460.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- STARBUCKS EGYPT 🇪🇬 ---
                FoodItem(
                    name = "Iced Caramel Macchiato",
                    brand = "Starbucks",
                    restaurant = "Starbucks",
                    country = "Egypt",
                    category = "Drinks",
                    source = "Official restaurant nutrition information",
                    sourceType = "Official",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "SB-EG-01",
                    preparationState = "Cold",
                    referenceAmount = 473.0,
                    referenceUnit = "cup",
                    caloriesPer100g = 52.8,
                    proteinPer100g = 2.1,
                    carbsPer100g = 7.4,
                    fatPer100g = 1.4,
                    servingOptionsJson = """[{"unit":"1 cup Grande (473ml)","grams":473.0},{"unit":"100 g","grams":100.0}]"""
                ),

                // --- EGYPTIAN LOCAL SPECIALTIES 🇪🇬 ---
                FoodItem(
                    name = "Egyptian Koshari",
                    brand = "Local Egyptian Cuisine",
                    restaurant = "Egyptian Cuisine",
                    country = "Egypt",
                    category = "Traditional",
                    source = "Verified Egyptian Database",
                    sourceType = "Database",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "EG-KOSHARI-01",
                    preparationState = "Cooked",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 165.0,
                    proteinPer100g = 5.2,
                    carbsPer100g = 31.0,
                    fatPer100g = 2.8,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 bowl (300g)","grams":300.0},{"unit":"1 large plate (450g)","grams":450.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Egyptian Grilled Kofta",
                    brand = "Local Egyptian Cuisine",
                    restaurant = "Egyptian Cuisine",
                    country = "Egypt",
                    category = "Traditional",
                    source = "Verified Egyptian Database",
                    sourceType = "Database",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "EG-KOFTA-01",
                    preparationState = "Grilled",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 250.0,
                    proteinPer100g = 18.5,
                    carbsPer100g = 3.5,
                    fatPer100g = 17.0,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 portion (200g)","grams":200.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Egyptian Baladi Bread",
                    brand = "Local Egyptian Bakery",
                    restaurant = "Egyptian Bakery",
                    country = "Egypt",
                    category = "Bakery",
                    source = "Verified Egyptian Database",
                    sourceType = "Database",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "EG-BREAD-01",
                    preparationState = "Baked",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 260.0,
                    proteinPer100g = 9.0,
                    carbsPer100g = 52.0,
                    fatPer100g = 1.2,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 loaf (100g)","grams":100.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Foul Medames with Olive Oil",
                    brand = "Local Egyptian Cuisine",
                    restaurant = "Egyptian Cuisine",
                    country = "Egypt",
                    category = "Traditional",
                    source = "Verified Egyptian Database",
                    sourceType = "Database",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "EG-FOUL-01",
                    preparationState = "Boiled",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 110.0,
                    proteinPer100g = 6.8,
                    carbsPer100g = 14.5,
                    fatPer100g = 3.2,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 plate (200g)","grams":200.0}]"""
                ),
                FoodItem(
                    name = "Egyptian Taameya (Falafel)",
                    brand = "Local Egyptian Cuisine",
                    restaurant = "Egyptian Cuisine",
                    country = "Egypt",
                    category = "Traditional",
                    source = "Verified Egyptian Database",
                    sourceType = "Database",
                    sourceBadge = "[Verified]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "EG-TAAMEYA-01",
                    preparationState = "Fried",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 290.0,
                    proteinPer100g = 11.0,
                    carbsPer100g = 28.0,
                    fatPer100g = 15.0,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"3 pieces (150g)","grams":150.0}]"""
                ),

                // --- STAPLE WHOLE FOODS ---
                FoodItem(
                    name = "Boiled Potato",
                    brand = "USDA",
                    country = "Global",
                    category = "Vegetables",
                    source = "USDA FoodData Central",
                    sourceType = "Database",
                    sourceBadge = "[USDA]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "170028",
                    preparationState = "Boiled",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 87.0,
                    proteinPer100g = 1.9,
                    carbsPer100g = 20.1,
                    fatPer100g = 0.1,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 medium potato (173g)","grams":173.0},{"unit":"1 cup (150g)","grams":150.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Cooked Chicken Breast",
                    brand = "USDA",
                    country = "Global",
                    category = "Poultry",
                    source = "USDA FoodData Central",
                    sourceType = "Database",
                    sourceBadge = "[USDA]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "171077",
                    preparationState = "Cooked",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 165.0,
                    proteinPer100g = 31.0,
                    carbsPer100g = 0.0,
                    fatPer100g = 3.6,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 breast (172g)","grams":172.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Cooked White Rice",
                    brand = "USDA",
                    country = "Global",
                    category = "Grains",
                    source = "USDA FoodData Central",
                    sourceType = "Database",
                    sourceBadge = "[USDA]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "168878",
                    preparationState = "Cooked",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 130.0,
                    proteinPer100g = 2.7,
                    carbsPer100g = 28.2,
                    fatPer100g = 0.3,
                    servingOptionsJson = """[{"unit":"100 g","grams":100.0},{"unit":"1 bowl (200g)","grams":200.0}]""",
                    isFavorite = true
                ),
                FoodItem(
                    name = "Boiled Large Egg",
                    brand = "USDA",
                    country = "Global",
                    category = "Eggs",
                    source = "USDA FoodData Central",
                    sourceType = "Database",
                    sourceBadge = "[USDA]",
                    confidenceStatus = "VERIFIED",
                    lastUpdated = "March 2026",
                    sourceId = "171287",
                    preparationState = "Boiled",
                    referenceAmount = 100.0,
                    referenceUnit = "g",
                    caloriesPer100g = 155.0,
                    proteinPer100g = 12.6,
                    carbsPer100g = 1.1,
                    fatPer100g = 10.6,
                    servingOptionsJson = """[{"unit":"1 large egg (50g)","grams":50.0},{"unit":"100 g","grams":100.0}]""",
                    isFavorite = true
                )
            )
            workoutDao.insertFoodItems(initialFoods)
        }
    }
}
