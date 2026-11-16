package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.dao.DayWithExercises
import com.example.data.db.dao.ProgramWithDays
import com.example.data.db.dao.WorkoutLogWithDetails
import com.example.data.db.entities.FoodItem
import com.example.data.db.entities.FoodLog
import com.example.data.db.entities.ProgramExercise
import com.example.data.db.entities.RecipeEntity
import com.example.data.db.entities.UserProfile
import com.example.data.db.entities.WeightLog
import com.example.data.repository.MultiLayerSearchResult
import com.example.data.repository.WorkoutRepository
import com.example.ui.data.MuscleVolumeData
import com.example.ui.data.NutritionCalculator
import com.example.ui.data.NutritionSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class ActiveSetState(
    val setNumber: Int,
    var weightKg: Double = 0.0,
    var reps: Int = 0,
    var isCompleted: Boolean = false
)

data class ActiveExerciseState(
    val exercise: ProgramExercise,
    val sets: MutableList<ActiveSetState>
)

data class ActiveTrainingSession(
    val isTraining: Boolean = false,
    val programTitle: String = "",
    val dayName: String = "",
    val dayOfWeek: String = "",
    val elapsedSeconds: Int = 0,
    val exercises: List<ActiveExerciseState> = emptyList()
)

data class RestTimerState(
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false
)

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val allPrograms: List<ProgramWithDays> = emptyList(),
    val activeProgram: ProgramWithDays? = null,
    val allLogs: List<WorkoutLogWithDetails> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val muscleVolumes: List<MuscleVolumeData> = emptyList(),
    val totalWeeklySets: Int = 0,
    val totalWeeklyVolumeKg: Double = 0.0,
    val activeSession: ActiveTrainingSession = ActiveTrainingSession(),
    val restTimer: RestTimerState = RestTimerState(),
    val toastMessage: String? = null,
    val weightLogs: List<WeightLog> = emptyList(),
    val todayFoodLogs: List<FoodLog> = emptyList(),
    val nutritionSummary: NutritionSummary = NutritionCalculator.calculateSummary(UserProfile()),
    val currentWeekAvgWeight: Double? = null,
    val prevWeekAvgWeight: Double? = null,
    val weeklyWeightChange: Double? = null,
    val todayConsumedCalories: Int = 0,
    val todayConsumedProtein: Double = 0.0,
    val todayConsumedCarbs: Double = 0.0,
    val todayConsumedFat: Double = 0.0,
    val todayWaterLogs: List<com.example.data.db.entities.WaterLog> = emptyList(),
    val todayWaterMl: Int = 0,
    val dailyWaterTargetMl: Int = 2500
)

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var sessionTimerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        viewModelScope.launch {
            // Seed database on first launch if needed
            repository.seedDefaultDataIfEmpty()
            delay(400) // Brief smooth splash / skeleton display transition
            _uiState.update { it.copy(isLoading = false) }
        }

        // Combine repository flows
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        viewModelScope.launch {
            repository.getWaterLogsForDate(todayStr).collect { logs ->
                val totalMl = logs.sumOf { it.amountMl }
                val profile = _uiState.value.userProfile
                val target = computeWaterTarget(profile)
                _uiState.update { it.copy(todayWaterLogs = logs, todayWaterMl = totalMl, dailyWaterTargetMl = target) }
            }
        }

        combine(
            repository.allPrograms,
            repository.activeProgram,
            repository.allLogs,
            repository.userProfile,
            repository.getCompletedSetsForCurrentWeek(),
            repository.getCompletedSetsForPreviousWeek(),
            repository.allWeightLogs,
            repository.allFoodLogs
        ) { flows: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val programs = flows[0] as List<ProgramWithDays>
            @Suppress("UNCHECKED_CAST")
            val activeProg = flows[1] as ProgramWithDays?
            @Suppress("UNCHECKED_CAST")
            val logs = flows[2] as List<WorkoutLogWithDetails>
            val profile = flows[3] as UserProfile? ?: UserProfile()
            @Suppress("UNCHECKED_CAST")
            val currentWeekSets = flows[4] as List<com.example.data.db.dao.VolumeSetRawData>
            @Suppress("UNCHECKED_CAST")
            val prevWeekSets = flows[5] as List<com.example.data.db.dao.VolumeSetRawData>
            @Suppress("UNCHECKED_CAST")
            val wLogs = flows[6] as List<WeightLog>
            @Suppress("UNCHECKED_CAST")
            val fLogs = flows[7] as List<FoodLog>

            // Calculate Volume per muscle
            val volumes = WorkoutRepository.ALL_MUSCLES.map { muscle ->
                val currentSetsForMuscle = currentWeekSets.filter { it.targetMuscle.equals(muscle, ignoreCase = true) }
                val prevSetsForMuscle = prevWeekSets.filter { it.targetMuscle.equals(muscle, ignoreCase = true) }

                val cSetsCount = currentSetsForMuscle.size
                val cVol = currentSetsForMuscle.sumOf { it.weightKg * it.reps }

                val pSetsCount = prevSetsForMuscle.size
                val pVol = prevSetsForMuscle.sumOf { it.weightKg * it.reps }

                MuscleVolumeData(
                    muscle = muscle,
                    weeklySets = cSetsCount,
                    weeklyVolumeKg = cVol,
                    previousWeekSets = pSetsCount,
                    previousWeekVolumeKg = pVol
                )
            }

            val totalSets = volumes.sumOf { it.weeklySets }
            val totalVol = volumes.sumOf { it.weeklyVolumeKg }

            // Calculate Nutrition summary based on UserProfile
            val nutritionSummary = NutritionCalculator.calculateSummary(profile)

            // Calculate today's food logs & macro totals
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayFoods = fLogs.filter { it.dateStr == todayStr }
            val consumedCal = todayFoods.sumOf { it.calories }
            val consumedProtein = todayFoods.sumOf { it.proteinG }
            val consumedCarbs = todayFoods.sumOf { it.carbsG }
            val consumedFat = todayFoods.sumOf { it.fatG }

            // Calculate Weekly Weight Adaptation averages
            val sortedWeightLogs = wLogs.sortedBy { it.timestamp }
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val currentWeekLogs = sortedWeightLogs.filter { it.timestamp >= now - (7 * dayMs) }
            val prevWeekLogs = sortedWeightLogs.filter { it.timestamp >= now - (14 * dayMs) && it.timestamp < now - (7 * dayMs) }

            val curWeekAvg = if (currentWeekLogs.isNotEmpty()) currentWeekLogs.map { it.weightKg }.average() else null
            val prvWeekAvg = if (prevWeekLogs.isNotEmpty()) prevWeekLogs.map { it.weightKg }.average() else null
            val weightChange = if (curWeekAvg != null && prvWeekAvg != null) curWeekAvg - prvWeekAvg else null

            _uiState.update { state ->
                state.copy(
                    allPrograms = programs,
                    activeProgram = activeProg ?: programs.firstOrNull(),
                    allLogs = logs,
                    userProfile = profile,
                    muscleVolumes = volumes,
                    totalWeeklySets = totalSets,
                    totalWeeklyVolumeKg = totalVol,
                    weightLogs = sortedWeightLogs,
                    todayFoodLogs = todayFoods,
                    nutritionSummary = nutritionSummary,
                    currentWeekAvgWeight = curWeekAvg,
                    prevWeekAvgWeight = prvWeekAvg,
                    weeklyWeightChange = weightChange,
                    todayConsumedCalories = consumedCal,
                    todayConsumedProtein = consumedProtein,
                    todayConsumedCarbs = consumedCarbs,
                    todayConsumedFat = consumedFat
                )
            }
        }.launchIn(viewModelScope)
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // --- PROGRAM MANAGEMENT ---
    fun createProgram(title: String, selectedDays: List<String>, cycleDays: Int = 7, description: String = "") {
        viewModelScope.launch {
            val id = repository.createProgramWithDays(title, selectedDays, cycleDays, description)
            repository.setActiveProgram(id)
            showToast("Created Program: $title ($cycleDays Days)")
        }
    }

    fun loadApexFitHyperAthleteProgram() {
        viewModelScope.launch {
            val id = repository.seedApexFitHybridAthleteProgram()
            showToast("APEXFIT HYBRID ATHLETE (14-Day System) Loaded & Activated!")
        }
    }

    fun setActiveProgram(programId: Long) {
        viewModelScope.launch {
            repository.setActiveProgram(programId)
            showToast("Active Program updated!")
        }
    }

    fun deleteProgram(programId: Long) {
        viewModelScope.launch {
            repository.deleteProgram(programId)
            showToast("Program deleted")
        }
    }

    fun addExerciseToDay(
        dayId: Long,
        exerciseName: String,
        targetMuscle: String,
        defaultSets: Int,
        restSeconds: Int,
        repsRange: String = "8-12",
        rir: String = "1-2",
        notes: String = "",
        category: String = "Hypertrophy"
    ) {
        viewModelScope.launch {
            repository.addExerciseToDay(dayId, exerciseName, targetMuscle, defaultSets, restSeconds, repsRange, rir, notes, category)
            showToast("Added $exerciseName")
        }
    }

    fun updateExercise(exercise: ProgramExercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
            showToast("Updated ${exercise.exerciseName}")
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
            showToast("Exercise removed")
        }
    }

    fun deleteLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(logId)
            showToast("Log entry deleted")
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
            showToast("Profile saved")
        }
    }

    // --- WORKOUT LOGGER / SESSION ---
    fun startTraining(dayWithExercises: DayWithExercises, programTitle: String) {
        val activeExercises = dayWithExercises.exercises.map { ex ->
            val initialSets = (1..ex.defaultSets).map { setNum ->
                ActiveSetState(setNumber = setNum, weightKg = 0.0, reps = 0, isCompleted = false)
            }.toMutableList()
            ActiveExerciseState(exercise = ex, sets = initialSets)
        }

        _uiState.update {
            it.copy(
                activeSession = ActiveTrainingSession(
                    isTraining = true,
                    programTitle = programTitle,
                    dayName = dayWithExercises.day.dayName,
                    dayOfWeek = dayWithExercises.day.dayOfWeek,
                    elapsedSeconds = 0,
                    exercises = activeExercises
                )
            )
        }

        // Start workout timer
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update {
                    val currentSession = it.activeSession
                    if (currentSession.isTraining) {
                        it.copy(activeSession = currentSession.copy(elapsedSeconds = currentSession.elapsedSeconds + 1))
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun addSetToActiveExercise(exerciseIndex: Int) {
        _uiState.update { state ->
            val session = state.activeSession
            if (exerciseIndex in session.exercises.indices) {
                val exState = session.exercises[exerciseIndex]
                val nextSetNum = exState.sets.size + 1
                val lastSet = exState.sets.lastOrNull()
                val defaultWeight = lastSet?.weightKg ?: 0.0
                val defaultReps = lastSet?.reps ?: 0

                exState.sets.add(ActiveSetState(setNumber = nextSetNum, weightKg = defaultWeight, reps = defaultReps, isCompleted = false))
            }
            state.copy(activeSession = session.copy(exercises = ArrayList(session.exercises)))
        }
    }

    fun removeSetFromActiveExercise(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val session = state.activeSession
            if (exerciseIndex in session.exercises.indices) {
                val exState = session.exercises[exerciseIndex]
                if (setIndex in exState.sets.indices) {
                    exState.sets.removeAt(setIndex)
                }
            }
            state.copy(activeSession = session.copy(exercises = ArrayList(session.exercises)))
        }
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: Double) {
        _uiState.update { state ->
            val session = state.activeSession
            if (exerciseIndex in session.exercises.indices) {
                val sets = session.exercises[exerciseIndex].sets
                if (setIndex in sets.indices) {
                    sets[setIndex] = sets[setIndex].copy(weightKg = weight)
                }
            }
            state.copy(activeSession = session.copy(exercises = ArrayList(session.exercises)))
        }
    }

    fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: Int) {
        _uiState.update { state ->
            val session = state.activeSession
            if (exerciseIndex in session.exercises.indices) {
                val sets = session.exercises[exerciseIndex].sets
                if (setIndex in sets.indices) {
                    sets[setIndex] = sets[setIndex].copy(reps = reps)
                }
            }
            state.copy(activeSession = session.copy(exercises = ArrayList(session.exercises)))
        }
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        var restToStart = 0
        _uiState.update { state ->
            val session = state.activeSession
            if (exerciseIndex in session.exercises.indices) {
                val exState = session.exercises[exerciseIndex]
                val sets = exState.sets
                if (setIndex in sets.indices) {
                    val newCompleted = !sets[setIndex].isCompleted
                    sets[setIndex] = sets[setIndex].copy(isCompleted = newCompleted)
                    if (newCompleted) {
                        restToStart = exState.exercise.restSeconds
                    }
                }
            }
            state.copy(activeSession = session.copy(exercises = ArrayList(session.exercises)))
        }

        if (restToStart > 0) {
            startRestTimer(restToStart)
        }
    }

    fun finishWorkout() {
        val session = _uiState.value.activeSession
        if (!session.isTraining) return

        viewModelScope.launch {
            val loggedExercises = session.exercises.map { exState ->
                val completedSets = exState.sets.filter { it.isCompleted || (it.weightKg > 0 && it.reps > 0) }
                    .map { Pair(it.weightKg, it.reps) }
                Pair(exState.exercise, completedSets)
            }.filter { it.second.isNotEmpty() }

            if (loggedExercises.isNotEmpty()) {
                repository.saveCompletedWorkout(
                    programTitle = session.programTitle,
                    dayName = session.dayName,
                    dayOfWeek = session.dayOfWeek,
                    durationSeconds = session.elapsedSeconds.toLong(),
                    loggedExercises = loggedExercises
                )
                showToast("Workout Completed & Saved!")
            } else {
                showToast("Workout ended (No sets logged).")
            }

            cancelWorkout()
        }
    }

    fun cancelWorkout() {
        sessionTimerJob?.cancel()
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                activeSession = ActiveTrainingSession(),
                restTimer = RestTimerState()
            )
        }
    }

    // --- REST TIMER ---
    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                restTimer = RestTimerState(
                    remainingSeconds = seconds,
                    totalSeconds = seconds,
                    isRunning = true
                )
            )
        }

        restTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                var currentSecs = 0
                _uiState.update { state ->
                    val timer = state.restTimer
                    if (timer.isRunning && timer.remainingSeconds > 0) {
                        currentSecs = timer.remainingSeconds - 1
                        state.copy(
                            restTimer = timer.copy(
                                remainingSeconds = currentSecs,
                                isRunning = currentSecs > 0
                            )
                        )
                    } else {
                        state
                    }
                }
                if (currentSecs <= 0) break
            }
        }
    }

    fun toggleRestTimer() {
        _uiState.update { state ->
            val timer = state.restTimer
            state.copy(restTimer = timer.copy(isRunning = !timer.isRunning))
        }
    }

    fun addRestSeconds(delta: Int) {
        _uiState.update { state ->
            val timer = state.restTimer
            val newSecs = (timer.remainingSeconds + delta).coerceAtLeast(0)
            state.copy(restTimer = timer.copy(remainingSeconds = newSecs, totalSeconds = maxOf(timer.totalSeconds, newSecs)))
        }
    }

    fun dismissRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(restTimer = RestTimerState()) }
    }

    // --- WEIGHT LOGGING ---
    fun logWeight(weightKg: Double) {
        viewModelScope.launch {
            repository.saveWeightLog(weightKg)
            val updatedProfile = _uiState.value.userProfile.copy(weightKg = weightKg)
            repository.saveUserProfile(updatedProfile)
            showToast("Logged body weight: $weightKg kg")
        }
    }

    fun deleteWeightLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWeightLog(id)
            showToast("Weight log deleted")
        }
    }

    // --- FOOD SEARCH & DIARY MANAGEMENT ---
    val favoriteFoods: StateFlow<List<FoodItem>> = repository.favoriteFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentFoods: StateFlow<List<FoodItem>> = repository.recentFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myFoods: StateFlow<List<FoodItem>> = repository.myFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<RecipeEntity>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()

    private val _multiLayerResult = MutableStateFlow<MultiLayerSearchResult?>(null)
    val multiLayerResult: StateFlow<MultiLayerSearchResult?> = _multiLayerResult.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun searchFoods(query: String) {
        if (query.trim().isBlank()) {
            _searchResults.value = emptyList()
            _multiLayerResult.value = null
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val multiResult = repository.performMultiLayerFoodSearch(query)
            _multiLayerResult.value = multiResult
            _searchResults.value = multiResult.exactResults
            _isSearching.value = false
        }
    }

    fun generateAiEstimate(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val estimate = repository.generateAiEstimate(query)
            _searchResults.value = listOf(estimate)
            _multiLayerResult.value = MultiLayerSearchResult(exactResults = listOf(estimate), aiEstimate = estimate, searchLayer = 4)
            _isSearching.value = false
            showToast("Generated AI Estimate for $query")
        }
    }

    fun scanNutritionLabel(labelText: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val scannedFood = repository.scanNutritionLabel(labelText)
            _searchResults.value = listOf(scannedFood)
            _isSearching.value = false
            showToast("Scanned food: ${scannedFood.name}")
        }
    }

    fun saveRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.saveRecipe(recipe)
            showToast("Saved recipe: ${recipe.title}")
        }
    }

    fun addCustomFood(foodItem: FoodItem) {
        viewModelScope.launch {
            repository.addCustomFood(foodItem)
            showToast("Custom food created: ${foodItem.name}")
        }
    }

    fun toggleFavoriteFood(foodItem: FoodItem) {
        viewModelScope.launch {
            repository.toggleFavoriteFood(foodItem)
            val action = if (!foodItem.isFavorite) "Added to" else "Removed from"
            showToast("$action favorites: ${foodItem.name}")
        }
    }

    fun addFoodToLogWithDetails(foodLog: FoodLog, foodItem: FoodItem? = null) {
        viewModelScope.launch {
            repository.addFoodLogWithItem(foodLog, foodItem)
            showToast("Added ${foodLog.foodName} (${foodLog.calories} kcal) to ${foodLog.meal}")
        }
    }

    fun updateFoodLogDetails(foodLog: FoodLog) {
        viewModelScope.launch {
            repository.updateFoodLog(foodLog)
            showToast("Updated ${foodLog.foodName}")
        }
    }

    fun reAddFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val newEntry = foodLog.copy(
                id = 0,
                dateStr = todayStr,
                timestamp = System.currentTimeMillis()
            )
            repository.addFoodLogWithItem(newEntry, null)
            showToast("Re-added ${foodLog.foodName} (${foodLog.quantity.toInt()} ${foodLog.unit})")
        }
    }

    // --- FOOD LOGGING ---
    fun addFoodLog(
        foodName: String,
        calories: Int,
        proteinG: Double = 0.0,
        carbsG: Double = 0.0,
        fatG: Double = 0.0
    ) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.saveFoodLog(
                FoodLog(
                    dateStr = todayStr,
                    foodName = foodName,
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatG = fatG
                )
            )
            showToast("Added $foodName ($calories kcal)")
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
            showToast("Food log removed")
        }
    }

    // --- HYDRATION ---
    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
            showToast("+${amountMl}ml logged")
            // Recalculate today's total
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.getWaterLogsForDate(todayStr).collect { logs ->
                _uiState.update {
                    it.copy(
                        todayWaterLogs = logs,
                        todayWaterMl = logs.sumOf { l -> l.amountMl }
                    )
                }
                return@collect
            }
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWaterLog(id)
        }
    }

    private fun computeWaterTarget(profile: UserProfile): Int {
        val base = profile.weightKg * 35.0
        val multiplier = when (profile.dailyActivity) {
            "Sedentary" -> 1.0
            "Lightly Active" -> 1.1
            "Moderately Active" -> 1.2
            "Very Active" -> 1.4
            "Extremely Active" -> 1.6
            else -> 1.2
        }
        return ((base * multiplier) / 50).toInt() * 50  // round to nearest 50ml
    }
}
