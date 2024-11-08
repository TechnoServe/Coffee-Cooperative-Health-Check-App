package com.example.ccts.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AnswersViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val surveyDao = database.surveyDao()
    private val categoryDao = database.surveyCategoryDao()
    private val questionDao = database.surveyQuestionDao()
    private val surveyAnswerDao = database.surveyAnswerDao()

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
                    questionCategory == categoryId && answer.answerText?.isNotBlank() ?: false
                }
            }
            .flowOn(Dispatchers.IO)
    }

    // Helper function to retrieve the category for a question
    private suspend fun getQuestionCategory(questionId: Int) : Int {
        val question = questionDao.getQuestionById(questionId)  // Assume this function exists in QuestionDao
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

    suspend fun updateAnswers(answers: Map<String, Any?>, surveyId: Int) {
        withContext(Dispatchers.IO) {
            answers.forEach { (key, answerText) ->

                try {
                    // Attempt to parse questionId and categoryId
                    val (questionId,categoryId)= key.split(" ")
                    Log.d("Update Details", "Question Id: $questionId, Category Id: $categoryId in Survey $surveyId")
                    val answer = SurveyAnswer(
                        surveyId = surveyId,
                        questionId = questionId.toInt(),
                        // categoryId = categoryId.toInt(),
                        answerText = answerText.toString()
                    )
                    Log.d("Update Answers", answer.toString())
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
