package com.example.ui.data

data class MuscleVolumeData(
    val muscle: String,
    val weeklySets: Int,
    val weeklyVolumeKg: Double,
    val previousWeekSets: Int = 0,
    val previousWeekVolumeKg: Double = 0.0
)
