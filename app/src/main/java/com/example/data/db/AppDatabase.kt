package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.WorkoutDao
import com.example.data.db.entities.*

@Database(
    entities = [
        WorkoutProgram::class,
        WorkoutDay::class,
        ProgramExercise::class,
        WorkoutLog::class,
        LoggedExercise::class,
        LoggedSet::class,
        UserProfile::class,
        WeightLog::class,
        FoodLog::class,
        FoodItem::class,
        RecipeEntity::class,
        WaterLog::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apexfit_eg.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
