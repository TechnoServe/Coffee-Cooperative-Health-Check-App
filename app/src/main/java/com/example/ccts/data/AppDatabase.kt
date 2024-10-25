package com.example.ccts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//@Database(entities = [Answers::class,  Cooperative::class,Survey::class], version = 18)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun answerDao(): AnswerDao
//    abstract fun coopDao(): CooperativeDao
//    abstract fun surveyDao(): SurveyDao
//
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase? {
//            // if the INSTANCE is not null, then return it,
//            // if it is, then create the database
//            if (INSTANCE == null) {
//                synchronized(this) {
//                    // Pass the database to the INSTANCE
//                    INSTANCE = buildDatabase(context)
//                }
//            }
//            // Return database.
//            return INSTANCE
//        }
//
//        private fun buildDatabase(context: Context): AppDatabase {
//            return Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                "ccts_db"
//            ).allowMainThreadQueries()
//                .fallbackToDestructiveMigration()
//                .build()
//        }
//    }
//}


@Database(entities = [Survey::class, SurveyCategory::class, SurveyQuestion::class, Cooperative::class], version = 19)
abstract class AppDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
    abstract fun surveyCategoryDao(): SurveyCategoryDao
    abstract fun surveyQuestionDao(): SurveyQuestionDao
    abstract fun coopDao(): CooperativeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ccts_db"
            )
                .allowMainThreadQueries()  // Use cautiously, ideally in background threads
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
