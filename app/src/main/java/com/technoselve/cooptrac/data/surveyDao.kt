package com.technoserve.cooptrac.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


//@Dao
//interface SurveyDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertSurvey(survey: Survey): Long // Returns surveyId after creation
//    // This function retrieves all surveys from the database as a Flow
//    @Query("SELECT * FROM surveys ORDER BY timestamp DESC")
//    fun getAllSurveys(): Flow<List<Survey>>
//
//    @Query("SELECT * FROM Surveys WHERE surveyId = :surveyId LIMIT 1")
//    fun getSurveyById(surveyId: Int): Survey?
//
//    @Query("SELECT COUNT(*) FROM Surveys WHERE cooperativeName = :cooperativeName AND DATE(timestamp / 1000, 'unixepoch') = DATE('now')")
//     fun checkIfCooperativeUsedToday(cooperativeName: String): Int
//
//}
//
//@Dao
//interface SurveyCategoryDao {
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insertCategory(category: SurveyCategory): Long // Returns categoryId
//
//    // Returns categories for a specific survey
//
//    @Query("SELECT * FROM survey_categories WHERE surveyId = :surveyId")
//    fun getCategoriesForSurvey(surveyId: Int): Flow<List<SurveyCategory>>
//
//}

//@Dao
//interface SurveyQuestionDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertQuestion(question: SurveyQuestion): Long // Returns questionId after insertion
//
//    @Query("UPDATE survey_questions SET answerText = :answer WHERE questionId = :questionId")
//    suspend fun updateAnswer(questionId: Int, answer: String) // Updates the answer for a specific question
//}


//@Dao
//interface SurveyQuestionDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertQuestion(question: SurveyQuestion)
//
//    @Query("UPDATE survey_questions SET answerText = :answer WHERE questionId = :questionId")
//   suspend  fun updateAnswer(questionId: Int, answer: String)
//
//    @Query("SELECT * FROM survey_questions WHERE surveyId = :surveyId")
//    fun getAnswersForSurvey(surveyId: Int): Flow<List<SurveyQuestion>>
//    @Query("SELECT COUNT(*) > 0 FROM survey_questions WHERE surveyId = :surveyId AND categoryId = :categoryId AND answerText IS NOT NULL AND answerText != ''")
//    fun hasAnswersForCategory(surveyId: Int, categoryId: Int): Flow<Boolean>
//
//}


@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryDb): Long

    // Changed Flow<List<CategoryDb>> to List<CategoryDb> for one-time check
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryDb>
    // New method to get a category by name
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryDb?

}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestion(question: QuestionDb): Long

    // Changed Flow<List<QuestionDb>> to List<QuestionDb> for one-time check
    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    suspend fun getQuestionsForCategory(categoryId: Int): List<QuestionDb>

    // New function to retrieve a question by its questionId
    @Query("SELECT * FROM questions WHERE questionId = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: Int): QuestionDb?

    @Query("SELECT * FROM questions WHERE questionText = :text AND categoryId = :categoryId LIMIT 1")
    suspend fun getQuestionByTextAndCategory(text: String, categoryId: Int): QuestionDb?
}


@Dao
interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: Survey): Long

    @Query("SELECT * FROM surveys ORDER BY timestamp DESC")
    fun getAllSurveys(): Flow<List<Survey>>

    @Query("SELECT * FROM surveys WHERE surveyId = :surveyId LIMIT 1")
    fun getSurveyById(surveyId: Int): Survey?

    @Query("SELECT COUNT(*) FROM surveys WHERE cooperativeName = :cooperativeName AND DATE(timestamp / 1000, 'unixepoch') = DATE('now')")
    fun checkIfCooperativeUsedToday(cooperativeName: String): Int

    @Update
    suspend fun updateSurvey(survey: Survey)

    @Query("UPDATE surveys SET totalScore = :newScore WHERE surveyId = :surveyId")
    suspend fun updateSurveyScore(surveyId: Int, newScore: Double)

    @Query("SELECT * FROM surveys WHERE uid = :uid LIMIT 1")
    fun getSurveyByUID(uid: String): Survey?

    // New DAO Methods for UID
    @Query("SELECT * FROM surveys WHERE uid IN (:uids) ORDER BY timestamp DESC")
    fun getSurveysByUID(uids: List<String>): Flow<List<Survey>>

    @Query("SELECT DISTINCT uid FROM surveys")
    fun getAllUIDs(): Flow<List<String>>

    @Query("UPDATE surveys SET uid = :uid WHERE surveyId = :surveyId")
    suspend fun updateSurveyUID(surveyId: Int, uid: String)

    @Query("SELECT comment FROM surveys WHERE surveyId = :surveyId")
    suspend fun getCommentBySurveyId(surveyId: Int): String?

    @Query("SELECT * FROM surveys WHERE cooperativeName = :cooperativeName AND timestamp = :timestamp LIMIT 1")
    fun getSurveyByCooperativeAndTimestamp(cooperativeName: String, timestamp: Long): Survey?

}

@Dao
interface SurveyAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: SurveyAnswer)

    @Update
    suspend fun updateAnswer(answer: SurveyAnswer)

    // Updated getAnswer function to remove categoryId parameter
    @Query("SELECT * FROM survey_answers WHERE surveyId = :surveyId AND questionId = :questionId LIMIT 1")
    suspend fun getAnswer(surveyId: Int, questionId: Int): SurveyAnswer?

    // New function to insert or update an answer without categoryId
    @Transaction
    suspend fun upsertAnswer(answer: SurveyAnswer) {
        val existingAnswer = getAnswer(answer.surveyId, answer.questionId)
        Log.d("upsertAnswer", "Existing answer for surveyId ${answer.surveyId}, questionId ${answer.questionId}: $existingAnswer")
        if (existingAnswer != null) {
            // Update the existing answer
            updateAnswer(answer.copy(answerId = existingAnswer.answerId))
        } else {
            // Insert new answer
            insertAnswer(answer)
        }
    }

    @Delete
    fun deleteAnswer(answer: SurveyAnswer)

    // Updated query to get all answers for a survey (no category filter needed)
    @Query(
        """
        SELECT * FROM survey_answers 
        WHERE surveyId = :surveyId
    """
    )
    fun getAnswersForSurvey(surveyId: Int): Flow<List<SurveyAnswer>>

    // Check if there are any answers in a survey for a specific question
    @Query(
        """
        SELECT COUNT(*) > 0 
        FROM survey_answers 
        WHERE surveyId = :surveyId AND questionId = :questionId 
        AND answerText IS NOT NULL AND answerText != ''
    """
    )
    fun hasAnswerForQuestion(surveyId: Int, questionId: Int): Flow<Boolean>

    @Query("SELECT * FROM survey_answers WHERE surveyId = :surveyId AND answerText IS NOT NULL")
    suspend fun getAnswersWithNonNullText(surveyId: Int): List<SurveyAnswer>

    // Get completion status for each category in a survey, based on question relationships
    @Query(
        """
        SELECT c.categoryId, c.name, 
        (SELECT COUNT(*) FROM survey_answers sa 
         INNER JOIN questions q ON sa.questionId = q.questionId
         WHERE sa.surveyId = :surveyId 
         AND q.categoryId = c.categoryId 
         AND sa.answerText IS NOT NULL 
         AND sa.answerText != '') > 0 as isCompleted
        FROM categories c
    """
    )
    fun getCategoryCompletionStatus(surveyId: Int): Flow<List<CategoryStatus>>
}

data class CategoryStatus(
    val categoryId: Int,
    val name: String,
    val isCompleted: Boolean
)