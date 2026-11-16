package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.db.dao.DayWithExercises
import com.example.data.repository.WorkoutRepository
import com.example.ui.navigation.BottomNavBar
import com.example.ui.navigation.BottomTab
import com.example.ui.screens.*
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexFitTheme
import com.example.ui.viewmodel.ViewModelFactory
import com.example.ui.viewmodel.WorkoutViewModel
import com.example.worker.HydrationReminderWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val repository = WorkoutRepository(db, db.workoutDao())
        val factory = ViewModelFactory(repository)
        HydrationReminderWorker.schedulePeriodic(this)

        setContent {
            ApexFitTheme {
                val viewModel: WorkoutViewModel = viewModel(factory = factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val favoriteFoods by viewModel.favoriteFoods.collectAsStateWithLifecycle()
                val recentFoods by viewModel.recentFoods.collectAsStateWithLifecycle()
                val myFoods by viewModel.myFoods.collectAsStateWithLifecycle()
                val multiLayerResult by viewModel.multiLayerResult.collectAsStateWithLifecycle()
                val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(BottomTab.Dashboard) }
                var editingDay by remember { mutableStateOf<DayWithExercises?>(null) }
                var showCreateProgramDialog by remember { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }

                // Toast Handler
                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                // If training session is active, display live logger
                if (uiState.activeSession.isTraining) {
                    WorkoutLoggerScreen(
                        session = uiState.activeSession,
                        restTimer = uiState.restTimer,
                        unit = uiState.userProfile.unit,
                        onAddSet = { exIdx -> viewModel.addSetToActiveExercise(exIdx) },
                        onRemoveSet = { exIdx, setIdx -> viewModel.removeSetFromActiveExercise(exIdx, setIdx) },
                        onUpdateWeight = { exIdx, setIdx, w -> viewModel.updateSetWeight(exIdx, setIdx, w) },
                        onUpdateReps = { exIdx, setIdx, r -> viewModel.updateSetReps(exIdx, setIdx, r) },
                        onToggleSetCompleted = { exIdx, setIdx -> viewModel.toggleSetCompleted(exIdx, setIdx) },
                        onToggleRestTimer = { viewModel.toggleRestTimer() },
                        onAddRestSeconds = { sec -> viewModel.addRestSeconds(sec) },
                        onDismissRestTimer = { viewModel.dismissRestTimer() },
                        onFinishWorkout = {
                            viewModel.finishWorkout()
                            HydrationReminderWorker.schedulePostWorkoutReminder(this@MainActivity)
                        },
                        onCancelWorkout = { viewModel.cancelWorkout() }
                    )
                } else if (editingDay != null) {
                    // Custom Day Exercise Editor
                    DayEditorScreen(
                        dayWithExercises = editingDay!!,
                        onBack = { editingDay = null },
                        onAddExercise = { name, muscle, sets, restSec, reps, rir, notes, category ->
                            viewModel.addExerciseToDay(editingDay!!.day.id, name, muscle, sets, restSec, reps, rir, notes, category)
                            editingDay = editingDay!!.copy(
                                exercises = editingDay!!.exercises + com.example.data.db.entities.ProgramExercise(
                                    workoutDayId = editingDay!!.day.id,
                                    exerciseName = name,
                                    targetMuscle = muscle,
                                    defaultSets = sets,
                                    restSeconds = restSec,
                                    repsRange = reps,
                                    rir = rir,
                                    notes = notes,
                                    category = category
                                )
                            )
                        },
                        onUpdateExercise = { ex ->
                            viewModel.updateExercise(ex)
                            editingDay = editingDay!!.copy(
                                exercises = editingDay!!.exercises.map { if (it.id == ex.id) ex else it }
                            )
                        },
                        onDeleteExercise = { exId ->
                            viewModel.deleteExercise(exId)
                            editingDay = editingDay!!.copy(
                                exercises = editingDay!!.exercises.filter { it.id != exId }
                            )
                        }
                    )
                } else {
                    // Primary Navigation Scaffold
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(
                                selectedTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = ApexBackground
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "tabTransition") { tab ->
                                when (tab) {
                                    BottomTab.Dashboard -> DashboardScreen(
                                        uiState = uiState,
                                        onStartWorkout = { dayWithEx, progTitle ->
                                            viewModel.startTraining(dayWithEx, progTitle)
                                        },
                                        onCreateProgramClick = { showCreateProgramDialog = true },
                                        onNavigateToVolume = { currentTab = BottomTab.Volume }
                                    )

                                    BottomTab.Workout -> WorkoutScreen(
                                        uiState = uiState,
                                        onCreateProgramClick = { showCreateProgramDialog = true },
                                        onSelectDayToEdit = { dayWithEx -> editingDay = dayWithEx },
                                        onStartWorkout = { dayWithEx, progTitle ->
                                            viewModel.startTraining(dayWithEx, progTitle)
                                        },
                                        onSwitchActiveProgram = { progId ->
                                            viewModel.setActiveProgram(progId)
                                        },
                                        onDeleteProgram = { progId ->
                                            viewModel.deleteProgram(progId)
                                        }
                                    )

                                    BottomTab.Nutrition -> NutritionScreen(
                                        uiState = uiState,
                                        searchResults = searchResults,
                                        favoriteFoods = favoriteFoods,
                                        recentFoods = recentFoods,
                                        myFoods = myFoods,
                                        multiLayerResult = multiLayerResult,
                                        isSearching = isSearching,
                                        onSearchQuery = { query -> viewModel.searchFoods(query) },
                                        onGenerateAiEstimate = { query -> viewModel.generateAiEstimate(query) },
                                        onScanLabel = { labelText -> viewModel.scanNutritionLabel(labelText) },
                                        onSaveRecipe = { recipe -> viewModel.saveRecipe(recipe) },
                                        onToggleFavoriteFood = { item -> viewModel.toggleFavoriteFood(item) },
                                        onSaveProfile = { profile -> viewModel.updateProfile(profile) },
                                        onLogWeight = { weight -> viewModel.logWeight(weight) },
                                        onAddFoodLogWithDetails = { log, item -> viewModel.addFoodToLogWithDetails(log, item) },
                                        onUpdateFoodLogDetails = { log -> viewModel.updateFoodLogDetails(log) },
                                        onReAddFoodLog = { log -> viewModel.reAddFoodLog(log) },
                                        onSaveCustomFood = { customItem -> viewModel.addCustomFood(customItem) },
                                        onDeleteFoodLog = { foodId -> viewModel.deleteFoodLog(foodId) }
                                    )

                                    BottomTab.Hydration -> HydrationScreen(
                                        uiState = uiState,
                                        onLogWater = { ml -> viewModel.logWater(ml) },
                                        onDeleteWaterLog = { id -> viewModel.deleteWaterLog(id) }
                                    )

                                    BottomTab.Volume -> VolumeScreen(
                                        uiState = uiState
                                    )

                                    BottomTab.History -> HistoryScreen(
                                        uiState = uiState,
                                        onDeleteLog = { logId -> viewModel.deleteLog(logId) }
                                    )

                                    BottomTab.Profile -> ProfileScreen(
                                        uiState = uiState,
                                        onSaveProfile = { profile -> viewModel.updateProfile(profile) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Modal for Creating Program
                if (showCreateProgramDialog) {
                    CreateProgramDialog(
                        onDismiss = { showCreateProgramDialog = false },
                        onCreateProgram = { title, selectedDays, cycleDays ->
                            viewModel.createProgram(title, selectedDays, cycleDays)
                            currentTab = BottomTab.Workout
                        },
                        onLoadApexFitHybrid = {
                            viewModel.loadApexFitHyperAthleteProgram()
                            currentTab = BottomTab.Workout
                        }
                    )
                }
            }
        }
    }
}
