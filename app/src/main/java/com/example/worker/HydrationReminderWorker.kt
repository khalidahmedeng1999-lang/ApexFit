package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.MainActivity
import com.example.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HydrationReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Only remind between 7am and 10pm
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < 7 || hour >= 22) return@withContext Result.success()

        val db = AppDatabase.getInstance(context)
        val dao = db.workoutDao()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val consumed = dao.getTotalWaterForDate(dateStr).first()
        val profile = dao.getUserProfile().first()
        val weightKg = profile?.weightKg ?: 75.0
        val activityMult = when (profile?.dailyActivity) {
            "Sedentary" -> 1.0
            "Lightly Active" -> 1.1
            "Very Active" -> 1.4
            "Extremely Active" -> 1.6
            else -> 1.2
        }
        val target = ((weightKg * 35.0 * activityMult) / 50).toInt() * 50

        // Don't remind if goal is already met
        if (consumed >= target) return@withContext Result.success()

        // Check if a workout finished recently (within 2 hours) for a smarter message
        val lastWorkout = dao.getAllWorkoutLogs().first().firstOrNull()
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)
        val isPostWorkout = lastWorkout != null && lastWorkout.log.timestamp > twoHoursAgo

        val remaining = target - consumed
        val title = if (isPostWorkout) "Post-Workout Hydration" else "Time to Hydrate"
        val message = if (isPostWorkout)
            "Great workout! Drink ${formatMl(remaining)} more to hit your daily goal."
        else
            "You've had ${formatMl(consumed)} today. ${formatMl(remaining)} left to reach your goal."

        showNotification(title, message)
        Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "hydration_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, "Hydration Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Reminds you to drink water throughout the day"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "hydration")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "hydration_periodic_reminder"
        const val POST_WORKOUT_WORK_NAME = "hydration_post_workout_reminder"

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(90, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().build())
                .setInitialDelay(10, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun schedulePostWorkoutReminder(context: Context) {
            val request = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
                .setInitialDelay(30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                POST_WORKOUT_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

private fun formatMl(ml: Int): String = if (ml >= 1000) "${"%.1f".format(ml / 1000.0)}L" else "${ml}ml"
