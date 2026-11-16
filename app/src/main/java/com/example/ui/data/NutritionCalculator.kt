package com.example.ui.data

import com.example.data.db.entities.UserProfile
import kotlin.math.log10
import kotlin.math.roundToInt

data class NutritionSummary(
    val bodyFatPct: Double?,        // null if missing waist or neck
    val leanMassKg: Double?,
    val fatMassKg: Double?,
    val bmr: Int,
    val dailyActivityKcal: Int,
    val exerciseKcal: Int,
    val maintenanceCalories: Int,
    val isDeficit: Boolean,         // true if Fat Loss, false if Muscle Gain/Maintenance
    val isSurplus: Boolean,         // true if Muscle Gain
    val adjustmentKcal: Int,        // Recommended Deficit or Surplus value
    val targetCalories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

object NutritionCalculator {

    fun calculateBodyFat(
        sex: String,
        heightCm: Double,
        weightKg: Double,
        waistCm: Double,
        neckCm: Double
    ): Double? {
        if (waistCm <= 0 || neckCm <= 0 || heightCm <= 0 || waistCm <= neckCm) {
            return null
        }

        return try {
            val isMale = sex.equals("Male", ignoreCase = true)
            val waistMinusNeck = waistCm - neckCm
            if (waistMinusNeck <= 0) return null

            val bf = if (isMale) {
                495.0 / (1.0324 - 0.19077 * log10(waistMinusNeck) + 0.15456 * log10(heightCm)) - 450.0
            } else {
                495.0 / (1.29579 - 0.35004 * log10(waistMinusNeck) + 0.22100 * log10(heightCm)) - 450.0
            }
            bf.coerceIn(3.0, 60.0)
        } catch (e: Exception) {
            null
        }
    }

    fun calculateBmr(
        sex: String,
        age: Int,
        heightCm: Double,
        weightKg: Double
    ): Int {
        val isMale = sex.equals("Male", ignoreCase = true)
        val genderOffset = if (isMale) 5.0 else -161.0
        val bmrVal = 10.0 * weightKg + 6.25 * heightCm - 5.0 * age + genderOffset
        return bmrVal.roundToInt().coerceAtLeast(800)
    }

    fun getActivityMultiplier(dailyActivity: String): Double {
        return when (dailyActivity) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.35
            "Moderately Active" -> 1.5
            "Very Active" -> 1.65
            "Extremely Active" -> 1.8
            else -> 1.5
        }
    }

    fun calculateSummary(profile: UserProfile): NutritionSummary {
        val sex = profile.sex
        val age = profile.age.coerceIn(12, 100)
        val heightCm = profile.heightCm.coerceAtLeast(100.0)
        val weightKg = profile.weightKg.coerceAtLeast(30.0)
        val waistCm = profile.waistCm
        val neckCm = profile.neckCm

        // 1. Body Fat
        val bfPct = calculateBodyFat(sex, heightCm, weightKg, waistCm, neckCm)
        val fatMass = bfPct?.let { weightKg * (it / 100.0) }
        val leanMass = bfPct?.let { weightKg - fatMass!! }

        // 2. BMR
        val bmr = calculateBmr(sex, age, heightCm, weightKg)

        // 3. Daily Activity Kcal
        val activityMult = getActivityMultiplier(profile.dailyActivity)
        val activityKcal = (bmr * (activityMult - 1.0)).roundToInt()

        // 4. Exercise Kcal
        val gymDays = profile.gymDaysPerWeek.coerceIn(2, 7)
        val exerciseKcal = ((gymDays * (weightKg * 3.8)) / 7.0).roundToInt()

        // 5. Maintenance / TDEE
        val maintenance = bmr + activityKcal + exerciseKcal

        // 6. Goal Adjustment
        val isFatLoss = profile.goal.equals("Fat Loss", ignoreCase = true)
        val isMuscleGain = profile.goal.equals("Muscle Gain", ignoreCase = true)

        val adjustment = when {
            isFatLoss -> {
                when (profile.deficitAggressiveness) {
                    "Conservative" -> (maintenance * 0.15).roundToInt().coerceAtLeast(250)
                    "Aggressive" -> (maintenance * 0.25).roundToInt().coerceAtLeast(600)
                    else -> (maintenance * 0.20).roundToInt().coerceAtLeast(400) // Moderate
                }
            }
            isMuscleGain -> {
                when (profile.deficitAggressiveness) {
                    "Conservative" -> 200
                    "Aggressive" -> 500
                    else -> 350 // Moderate
                }
            }
            else -> 0 // Maintenance
        }

        val targetCalories = when {
            isFatLoss -> (maintenance - adjustment).coerceAtLeast(1200)
            isMuscleGain -> maintenance + adjustment
            else -> maintenance
        }

        // 7. Macros
        val proteinG = (2.2 * weightKg).roundToInt()
        val fatG = ((targetCalories * 0.25) / 9.0).roundToInt().coerceAtLeast(35)
        val remainingCalForCarbs = (targetCalories - (proteinG * 4 + fatG * 9)).coerceAtLeast(0)
        val carbsG = (remainingCalForCarbs / 4.0).roundToInt()

        return NutritionSummary(
            bodyFatPct = bfPct,
            leanMassKg = leanMass,
            fatMassKg = fatMass,
            bmr = bmr,
            dailyActivityKcal = activityKcal,
            exerciseKcal = exerciseKcal,
            maintenanceCalories = maintenance,
            isDeficit = isFatLoss,
            isSurplus = isMuscleGain,
            adjustmentKcal = adjustment,
            targetCalories = targetCalories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )
    }
}
