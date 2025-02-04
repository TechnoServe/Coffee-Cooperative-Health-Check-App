package com.technoserve.cooptrac.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

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


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(
    entities = [
        Survey::class,
        CategoryDb::class,
        QuestionDb::class,
        SurveyAnswer::class,
        Cooperative::class,
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
    abstract fun surveyCategoryDao(): CategoryDao
    abstract fun surveyQuestionDao(): QuestionDao
    abstract fun surveyAnswerDao(): SurveyAnswerDao
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
            val MIGRATION_1_2 = object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Add UID column to the existing surveys table
                    database.execSQL("ALTER TABLE surveys ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE surveys ADD COLUMN comment TEXT NOT NULL DEFAULT ''")

                }
            }
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ccts_db_v5"
            )
                .allowMainThreadQueries()  // Use cautiously, ideally in background threads
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
