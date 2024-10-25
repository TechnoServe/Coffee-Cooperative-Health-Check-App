package com.example.ccts


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.example.ccts.data.Category
import com.example.ccts.data.Question
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException




@Composable
fun CategoryDetailScreen(navController: NavHostController, viewModel: AnswersViewModel, surveyId: Int, categoryId: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val answers = remember { mutableStateMapOf<String, Any>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories = loadCategoriesAndQuestion(context)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    var totalScore by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {

        // Now load answers from SharedPreferences, which will override database answers if any
//        if (selectedCategory != null) {
//
//        }
        viewModel.getAnswersForSurveyByCategory(surveyId, categoryId).collect { answerList ->
            answers.clear() // Clear existing answers
            answerList.forEach { answer ->
                // Add to answers map
                answers[answer.questionId.toString()] = answer.answerText ?: ""
                // Save each answer to SharedPreferences
                if (selectedCategory != null) {
                    selectedCategory.questions?.forEach { question ->
                        val savedAnswer = getAnswerFromSharedPreferences(context, selectedCategory.id, question)
                        if (savedAnswer.isNotEmpty()) {
                            answers.putAll(savedAnswer)
                            Log.d("SharedPrefDebug", "Overridden with SharedPreferences: $answers")
                        } else {
                            Log.d("SharedPrefDebug", "No data found in SharedPreferences for question ${question.id}")
                        }
                    }

                }
            }
            Log.d("DatabaseDebug", "Loaded from database: $answers")
        }

    }



    if (showDialog && selectedCategory != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showDialog = false
            navController.popBackStack()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
            ) {
                val scrollState = rememberScrollState()
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = selectedCategory.category,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val categoryQuestions = selectedCategory.questions ?: emptyList()

                        if (categoryQuestions.isNotEmpty()) {
                            categoryQuestions.forEach { question ->
                                key(question.id) {  // Add key for proper recomposition
                                    when (question.type) {
                                        "text" -> TextFieldQuestion(question, answers)
                                        "number" -> NumberFieldQuestion(question, answers)
                                        "percentage" -> PercentageSliderQuestion(question, answers)
                                        "yes_no" -> YesNoQuestion(question, answers)
                                        "checkbox" -> CheckboxQuestion(question, answers)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            Text("No questions available for this category.")
                        }

                        Button(
                            onClick = {
                                if (areAllQuestionsAnswered(categoryQuestions, answers)) {
//                                    totalScore = calculateTotalScore(categoryQuestions, answers)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        saveAnswersToSharedPreferences(context, selectedCategory, answers)


                                    }
                                    Toast.makeText(
                                        context,
                                        "Answer saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showDialog = false
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please answer all questions.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise))
                        ) {
                            Text(text = "Save")
                        }
                    }
                    IconButton(
                        onClick = {
                            showDialog = false
                            navController.popBackStack()
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = "close",
                            tint = colorResource(id = R.color.turquoise)
                        )
                    }
                }
            }
        }
    }
}





