package com.example.ccts.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AnswersViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val surveyDao = database.surveyDao()
    val categoryDao = database.surveyCategoryDao()
    val questionDao = database.surveyQuestionDao()
    val surveyAnswerDao = database.surveyAnswerDao()

    // Get all surveys
    val surveys: Flow<List<Survey>> = surveyDao.getAllSurveys()
        .flowOn(Dispatchers.IO)

    // MutableLiveData to hold categories
    private val _allCategories = MutableLiveData<List<CategoryDb>>()
    val allCategories: LiveData<List<CategoryDb>> = _allCategories

    init {
        // Load categories when the ViewModel is created
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch categories from the database
                val categories = categoryDao.getAllCategories()

                // Post the result to LiveData on the main thread
                withContext(Dispatchers.Main) {
                    _allCategories.value = categories
                }
            } catch (e: Exception) {
                Log.e("AnswersViewModel", "Error loading categories", e)
            }
        }
    }

    // Get answers for questions within a specific category in a survey
    fun getAnswersForCategory(surveyId: Int, categoryId: Int): Flow<List<SurveyAnswer>> {
        return surveyAnswerDao.getAnswersForSurvey(surveyId)
            .map { answers ->
                // Filter answers by questions that belong to the specified category
                answers.filter { answer ->
                    // Assume we have a method to get the category of a question
                    val questionCategory = getQuestionCategory(answer.questionId)
                    questionCategory == categoryId
                }
            }
            .flowOn(Dispatchers.IO)
    }

    // Check if there are answers for any questions within a specific category in a survey
    fun hasAnswersForCategory(surveyId: Int, categoryId: Int): Flow<Boolean> {
        return surveyAnswerDao.getAnswersForSurvey(surveyId)
            .map { answers ->
                answers.any { answer ->
                    // Check if the question belongs to the specified category and has a non-empty answer
                    val questionCategory = getQuestionCategory(answer.questionId)
                    questionCategory == categoryId && answer.answerText?.isNotBlank() == true
                }
            }
            .flowOn(Dispatchers.IO)
    }

    // Helper function to retrieve the category for a question
    private suspend fun getQuestionCategory(questionId: Int): Int {
        val question =
            questionDao.getQuestionById(questionId)  // Assume this function exists in QuestionDao
        if (question != null) {
            return question.categoryId
        }
        return -1 // Return -1 if the category cannot be found
    }


    // Get a specific survey by ID
    suspend fun getSurveyById(surveyId: Int): Survey? {
        return withContext(Dispatchers.IO) {
            surveyDao.getSurveyById(surveyId)
        }
    }


    fun getAnswersForSurvey(surveyId: Int): LiveData<List<SurveyAnswer>> {
        val answers = liveData(Dispatchers.IO) {
            val result = surveyAnswerDao.getAnswersWithNonNullText(surveyId)
            emit(result)  // Emit the result to the UI
        }
        return answers
    }
    fun updateSurveyScore(surveyId: Int, newScore: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            surveyDao.updateSurveyScore(surveyId, newScore)
        }
    }

    suspend fun updateAnswers(
        categories: List<Category>,
        answers: Map<Int, Any?>,
        surveyId: Int
    ) {
        withContext(Dispatchers.IO) {
            answers.forEach { (key, answerText) ->
                try {
                    val answer = SurveyAnswer(
                        surveyId = surveyId,
                        questionId = key,
                        answerText = answerText.toString()
                    )

                    surveyAnswerDao.upsertAnswer(answer)
                } catch (e: Exception) {
                    Log.e("updateAnswers", "Unexpected error processing key: $key", e)
                }
            }
        }
    }
}

// Factory remains largely the same
class AnswersViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnswersViewModel::class.java)) {
            return AnswersViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
