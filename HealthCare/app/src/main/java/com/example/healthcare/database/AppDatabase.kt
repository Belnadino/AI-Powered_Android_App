package com.example.healthcare.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthcare.database.entities.UserEntity
import com.example.healthcare.database.Dao.*
import com.example.healthcare.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        MedicinePlan::class,
        MedicineLog::class,
        ExercisePlan::class,
        ExerciseLog::class,
        MealLog::class,
        MealPlan::class,
        TodayNotification::class,
        Feedback::class,
        CaregiverEntity::class,
    ],
    version = 11, // increment version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Existing DAOs
    abstract fun userDao(): UserDao
    abstract fun medicinePlanDao(): MedicinePlanDao
    abstract fun medicineLogDao(): MedicineLogDao

    // New DAOs for exercises
    abstract fun exercisePlanDao(): ExercisePlanDao
    abstract fun exerciseLogDao(): ExerciseLogDao

    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealLogDao(): MealLogDao

    abstract fun feedbackDao(): FeedbackDao

    abstract fun todayNotificationDao(): TodayNotificationDao

    abstract fun caregiverDao(): CaregiverDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_database"
                )
                    .fallbackToDestructiveMigration() // for version upgrade during testing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
