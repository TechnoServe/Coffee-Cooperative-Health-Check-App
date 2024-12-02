package com.technoserve.cooptrac.data

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
                Log.d("answersss text","$answerText")
                //val text = answerText as? String // Safely cast to String
               //  Log.d("text to update","$text")
                if (answerText !=null) { // Skip null or blank answers
                    try {
                        val answer = SurveyAnswer(
                            surveyId = surveyId,
                            questionId = key,
                            answerText = answerText.toString(),

                        )
                        surveyAnswerDao.upsertAnswer(answer)
                    } catch (e: Exception) {
                        Log.e("updateAnswers", "Unexpected error processing key: $key", e)
                    }
                } else {
                    Log.d("updateAnswers", "Skipping unanswered question ID: $key")
                }
            }
        }
    }

    private val _comment = MutableLiveData<String>()
    val comment: LiveData<String> = _comment

    private val db = AppDatabase.getDatabase(application).surveyDao()  // Your database instance

    fun fetchComment(surveyId: Int) {
        viewModelScope.launch {
            try {
                val fetchedComment = db.getCommentBySurveyId(surveyId)
                _comment.postValue(fetchedComment ?: "No comment found")  // Default to a fallback if null
            } catch (e: Exception) {
                _comment.postValue("Error fetching comment")  // Handle errors
            }
        }
    }

    fun updateComment(surveyId: Int, newComment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Retrieve the survey object first
                val survey = db.getSurveyById(surveyId)

                // If survey is found, update its comment
                survey?.let {

                    it.comment = newComment
                    Log.d("it", "updateSurveyComment: $it")
                    db.updateSurvey(it)  // Update the survey in the database
                }

                // Update the LiveData to reflect the new comment in the UI
                _comment.postValue(newComment)

            } catch (e: Exception) {
                // Handle error if any issue occurs while updating
                _comment.postValue("Error updating comment")
            }
        }
    }

    //language
    private val sharedPreferences = application.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val _selectedLanguage = MutableStateFlow(sharedPreferences.getString("selected_language", "English") ?: "English")
    val selectedLanguage: StateFlow<String> get() = _selectedLanguage
    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
        sharedPreferences.edit().putString("selected_language", language).apply()
    }


    private val _selectedSurvey = MutableStateFlow<Survey?>(null)
    val selectedSurvey: StateFlow<Survey?> = _selectedSurvey

    // Derived state to get the timestamp
    val timestamp: StateFlow<Long> = _selectedSurvey
        .map { survey -> survey?.timestamp ?: 0L } // Transform Survey to get timestamp
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L) // Default to 0L if no survey is selected

    // Function to set selected survey
    fun selectSurvey(survey: Survey) {
        if (survey != null) {
            Log.d("Survey", "Selected survey with timestamp: ${survey.timestamp}")
            _selectedSurvey.value = survey
            val selectedSurveyValue = selectedSurvey.value
            Log.d("Survey", "Selected Survey: $selectedSurveyValue with timestamp: ${selectedSurveyValue?.timestamp}")

        } else {
            Log.d("Survey", "Attempted to select a null survey")
        }
    }

    fun clearTimestamp() {
        _selectedSurvey.value = _selectedSurvey.value?.copy(timestamp = 0L)
    }

    private val _selectedCategory = mutableStateOf<Category?>(null)
    val selectedCategory: State<Category?> = _selectedCategory

    // Recommendations are stored in a mutable list
    private val _recommendations = mutableStateListOf<String>()
    val recommendations: List<String> get() = _recommendations

    // Function to set the selected category
    fun setSelectedCategory(category: Category) {
        _selectedCategory.value = category
    }

    // Function to set recommendations
    fun setRecommendations(newRecommendations: List<String>) {
        // Append the new recommendations to the existing list
        _recommendations.addAll(newRecommendations)
    }

    // Optionally, you can reset recommendations if needed
    fun resetRecommendations() {
        _recommendations.clear()
    }








//    suspend fun updateAnswers(
//        categories: List<Category>,
//        answers: Map<Int, Any?>,
//        surveyId: Int
//    ) {
//        withContext(Dispatchers.IO) {
//            answers.forEach { (key, answerText) ->
//                try {
//                    val answer = SurveyAnswer(
//                        surveyId = surveyId,
//                        questionId = key,
//                        answerText = answerText.toString()
//                    )
//
//                    surveyAnswerDao.upsertAnswer(answer)
//                } catch (e: Exception) {
//                    Log.e("updateAnswers", "Unexpected error processing key: $key", e)
//                }
//            }
//        }
//    }
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
