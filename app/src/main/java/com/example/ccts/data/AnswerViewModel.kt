package com.example.ccts.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


//class AnswersViewModel(application: Application) : AndroidViewModel(application) {
//    private val surveyDao = AppDatabase.getDatabase(application).surveyDao()
//
//    // Observes the list of surveys, running in the background using flowOn
//    val surveys: Flow<List<Survey>> = surveyDao.getAllSurveys()
//        .flowOn(Dispatchers.IO)
//}

class AnswersViewModel(application: Application) : AndroidViewModel(application) {
    private val surveyDao = AppDatabase.getDatabase(application)?.surveyDao()
    private val surveyQuestionDao = AppDatabase.getDatabase(application)?.surveyQuestionDao()
    private val surveyCategoryDao = AppDatabase.getDatabase(application)?.surveyCategoryDao()


    // Fetch all surveys for the DisplayAllCooperativeHealth screen
    val surveys: Flow<List<Survey>> = surveyDao?.getAllSurveys()
        ?.flowOn(Dispatchers.IO) ?: flowOf(emptyList())

    // Fetch answers based on surveyId
    fun getAnswersForSurvey(surveyId: Int): Flow<List<SurveyQuestion>> {
        return surveyQuestionDao?.getAnswersForSurvey(surveyId)
            ?.flowOn(Dispatchers.IO) ?: flowOf(emptyList())
    }

    // Fetch answers based on surveyId and categoryId
    fun getAnswersForSurveyByCategory(surveyId: Int, categoryId: Int): Flow<List<SurveyQuestion>> {
        return surveyQuestionDao?.getAnswersForSurvey(surveyId)
            ?.map { questions ->
                questions.filter { it.categoryId == categoryId }
            }
            ?.flowOn(Dispatchers.IO) ?: flowOf(emptyList())
    }

    // Fetch categories for a specific survey
    fun getCategoriesForSurvey(surveyId: Int): Flow<List<SurveyCategory>> {
        return surveyCategoryDao?.getCategoriesForSurvey(surveyId) // This function should be defined in the surveyCategoryDao
            ?.flowOn(Dispatchers.IO) ?: flowOf(emptyList())
    }

     fun getSurveyById(surveyId: Int): Survey? {
        return surveyDao?.getSurveyById(surveyId) // Implement this in your repository
    }

    suspend fun upsertAnswer(answers: Map<String, Any?>) {
        answers.forEach { (questionId, answer) ->
            surveyQuestionDao?.updateAnswer(questionId.toInt(), answer.toString())
        }
    }

    fun hasAnswersForCategory(surveyId: Int, categoryId: Int): Flow<Boolean> {
        return surveyQuestionDao?.hasAnswersForCategory(surveyId, categoryId)
            ?: flowOf(false) // Default to false if surveyQuestionDao is null
    }


    }





class AnswersViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnswersViewModel::class.java)) {
            return AnswersViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


