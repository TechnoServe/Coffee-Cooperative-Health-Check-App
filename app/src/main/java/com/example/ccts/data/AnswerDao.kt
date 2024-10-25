package com.example.ccts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

//@Dao
//interface AnswerDao {
//
//
//    @Insert
//    suspend fun insert(answer: Answers)
//
//    @Query("SELECT * FROM Answers WHERE id = :id")
//    suspend fun getAnswerById(id: Int): Answers
//
//    @Query("SELECT * FROM Answers")
//    suspend fun getAllAnswers(): List<Answers>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertSurvey(survey: Survey): Long
//
//    @Update
//    suspend fun updateAnswer(answer: Answers)
//
//    @Query("SELECT * FROM Answers WHERE surveyId = :surveyId")
//    fun getAnswersBySurveyId(surveyId: Long): List<Answers>
//
//}
