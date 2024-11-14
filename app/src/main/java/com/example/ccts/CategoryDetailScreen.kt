package com.example.ccts


import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoryDetailScreen(navController: NavHostController, viewModel: AnswersViewModel, surveyId: Int, categoryId: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val answers = remember { mutableStateMapOf<String, Any>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories = loadCategoriesAndQuestion(context)
    val selectedCategory = categories.first { it.id == categoryId }
    var totalScore by remember { mutableStateOf(0.00) }
    var isToday by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)





    LaunchedEffect(Unit) {

        coroutineScope.launch(Dispatchers.IO) {
            val survey = viewModel.getSurveyById(surveyId) // Get the survey directly
            survey?.let {
                // Convert timestamp to LocalDate
                val surveyDate = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                isToday = surveyDate.isEqual(LocalDate.now())
            }
        }


        viewModel.getAnswersForCategory(surveyId, categoryId).collect { answerList ->
            answers.clear() // Clear existing answers
            answerList.forEach { answer ->
                // Add to answers map
                answers[answer.questionId.toString()] = answer.answerText ?: ""
                saveAnswersToSharedPreferences(
                    context,
                    selectedCategory,
                    answers
                )



                // Save each answer to SharedPreferences


                selectedCategory.questions?.forEach { question ->
//                        val savedAnswer = sharedPreferences.getString("answer_${categoryId}_${question.id}", "") ?: ""
//                        Log.d("PopupDialog", "Saved answer in dialog: $savedAnswer")

                    val savedAnswer = getAnswerFromSharedPreferences(context, selectedCategory.id, question)



//                        Log.d("From shared","ansers from shared $savedAnswer")

                    if (savedAnswer.isNotEmpty()) {

                        answers[question.id.toString()] = savedAnswer
                        answers.putAll(savedAnswer)

                        Log.d("SharedPrefDebug", "Overridden with SharedPreferences: $answers")
                    } else {
                        Log.d("SharedPrefDebug", "No data found in SharedPreferences for question ${question.id}")
                    }
                }

                totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
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
                        if (isToday) {
                        Button(
                            onClick = {
                                if (areAllQuestionsAnswered(categoryQuestions, answers)) {
                                    totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        saveAnswersToSharedPreferences(
                                            context,
                                            selectedCategory,
                                            answers
                                        )


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





