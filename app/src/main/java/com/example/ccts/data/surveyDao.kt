package com.example.ccts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: Survey): Long // Returns surveyId after creation
    // This function retrieves all surveys from the database as a Flow
    @Query("SELECT * FROM surveys ORDER BY timestamp DESC")
    fun getAllSurveys(): Flow<List<Survey>>

    @Query("SELECT * FROM Surveys WHERE surveyId = :surveyId LIMIT 1")
    fun getSurveyById(surveyId: Int): Survey?

    @Query("SELECT COUNT(*) FROM Surveys WHERE cooperativeName = :cooperativeName AND DATE(timestamp / 1000, 'unixepoch') = DATE('now')")
     fun checkIfCooperativeUsedToday(cooperativeName: String): Int

}

@Dao
interface SurveyCategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: SurveyCategory): Long // Returns categoryId

    // Returns categories for a specific survey

    @Query("SELECT * FROM survey_categories WHERE surveyId = :surveyId")
    fun getCategoriesForSurvey(surveyId: Int): Flow<List<SurveyCategory>>

}

//@Dao
//interface SurveyQuestionDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertQuestion(question: SurveyQuestion): Long // Returns questionId after insertion
//
//    @Query("UPDATE survey_questions SET answerText = :answer WHERE questionId = :questionId")
//    suspend fun updateAnswer(questionId: Int, answer: String) // Updates the answer for a specific question
//}



@Dao
interface SurveyQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: SurveyQuestion)

    @Query("UPDATE survey_questions SET answerText = :answer WHERE questionId = :questionId")
   suspend  fun updateAnswer(questionId: Int, answer: String)

    @Query("SELECT * FROM survey_questions WHERE surveyId = :surveyId")
    fun getAnswersForSurvey(surveyId: Int): Flow<List<SurveyQuestion>>
    @Query("SELECT COUNT(*) > 0 FROM survey_questions WHERE surveyId = :surveyId AND categoryId = :categoryId AND answerText IS NOT NULL AND answerText != ''")
    fun hasAnswersForCategory(surveyId: Int, categoryId: Int): Flow<Boolean>

}
