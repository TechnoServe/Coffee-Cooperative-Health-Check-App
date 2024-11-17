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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.Question
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
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    var totalScore by remember { mutableStateOf(0.00) }
    var isToday by remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)





    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val survey = viewModel.getSurveyById(surveyId) // Get the survey directly
            survey?.let {
                // Convert timestamp to LocalDate
                val surveyDate =
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                isToday = surveyDate.isEqual(LocalDate.now())
            }
        }

//        if (selectedCategory != null) {
//            selectedCategory.questions?.forEach { question ->
//
//                val savedAnswer =
//                    getAnswerFromSharedPreferences(context, selectedCategory.id, question)
//                Log.d("From shared", "anserssss from shared $savedAnswer")
//                answers[question.id.toString()] = savedAnswer[question.id.toString()] ?: ""
//                answers.putAll(savedAnswer)
//
//                Log.d(
//                    "LoadedAnswers",
//                    "Question ${question.id}: ${savedAnswer[question.id.toString()]}"
//                )
//            }
//            totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
//
//        }


//        viewModel.getAnswersForCategory(surveyId, categoryId).collect { answerList ->
//            answers.clear() // Clear existing answers
//            println("answers nowww $answerList")
//            answerList.forEach { answer ->
//                // Add to answers map
//                answers[answer.questionId.toString()] = answer.answerText ?: ""
//
//
//                // Save each answer to SharedPreferences
//                if (selectedCategory != null) {
////                    Log.d("selectedCategory","ansers from shared $selectedCategory")
//
//                    selectedCategory.questions?.forEach { question ->
//                        Log.d("question","ansers from shared $question")
//                        val savedAnswer = getAnswerFromSharedPreferences(context, selectedCategory.id, question)
////                        Log.d("From shared","ansers from shared $savedAnswer")
//
////                        if (savedAnswer.isNotEmpty()) {
//
//                            answers.putAll(savedAnswer)
////                            answers[question.id.toString()] = savedAnswer
////                            Log.d("LoadedAnswers", "Question ${question.id}: ${savedAnswer[question.id.toString()]}")
//                            Log.d("SharedPrefDebug", "Overridden with SharedPreferences: $answers")
////                        }
////                        else {
////                            Log.d("SharedPrefDebug", "No data found in SharedPreferences for question ${question.id}")
////                        }
//                    }
//
////                    totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
//                }
//            }
//
//            Log.d("DatabaseDebug", "Loaded from database: $answers")
//        }
//
//
//    }

        viewModel.getAnswersForCategory(surveyId, categoryId).collect { answerList ->
            // Load answers from the database
            answerList.forEach { answer ->
                // Add database answers to the `answers` map
                answers[answer.questionId.toString()] = answer.answerText ?: ""
            }

            // If a category is selected, load answers from SharedPreferences
            if (selectedCategory != null) {
                selectedCategory.questions?.forEach { question ->
                    // Load answers for the selected category from SharedPreferences
                    val savedAnswer =
                        getAnswerFromSharedPreferences(context, selectedCategory.id, question)

                    // Merge SharedPreferences answers into the existing `answers` map
                    savedAnswer.forEach { (key, value) ->
                        answers[key] = value // This will override or add new answers
                    }

                    Log.d("SharedPrefDebug", "Merged SharedPreferences: $answers")
                }
            }

            // Log the final merged answers
            Log.d("DatabaseDebug", "Final answers after merging: $answers")

//            // Save updated answers to the database
//            answers.forEach { (questionId, answerText) ->
//                // Create or update the answer in the database
//                val answer = Answer(
//                    questionId = questionId.toInt(),
//                    surveyId = surveyId,
//                    categoryId = categoryId,
//                    answerText = answerText
//                )
//                viewModel.upsertAnswer(answer)
//            }

            // Optionally, update total score if needed
            // totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
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
                                        "text" -> TextFieldQuestionEdit(question, answers)
                                        "number" -> NumberFieldQuestionEdit(question, answers)
                                        "percentage" -> PercentageSliderQuestionEdit(question, answers)
                                        "yes_no" -> YesNoQuestionEdit(question, answers)
                                        "checkbox" -> CheckboxQuestionEdit(question, answers)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldQuestionEdit(question: Question, answers: MutableMap<String, Any>) {
    val savedAnswer = answers[question.id.toString()]?.toString() ?: ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = question.question)
        TextField(
            value = userAnswer,
            onValueChange = { newValue ->
                userAnswer = newValue
                answers[question.id.toString()] = newValue
            },
            label = { Text("Your answer") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                focusedLabelColor = colorResource(id = R.color.turquoise),
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = colorResource(id = R.color.turquoise),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberFieldQuestionEdit(question: Question, answers: MutableMap<String, Any>) {
    val savedAnswer = answers[question.id.toString()]?.toString() ?: ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = question.question)
        TextField(
            value = userAnswer,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    userAnswer = newValue
                    answers[question.id.toString()] = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Your answer") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                focusedLabelColor = colorResource(id = R.color.turquoise),
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = colorResource(id = R.color.turquoise),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}

@Composable
fun PercentageSliderQuestionEdit(question: Question, answers: MutableMap<String, Any>) {
    val savedValue = when (val value = answers[question.id.toString()]) {
        is Float -> value
        is String -> value.toFloatOrNull() ?: 50f
        else -> 50f
    }
    var userAnswer by remember(savedValue) { mutableStateOf(savedValue) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = question.question)
        Slider(
            value = userAnswer,
            onValueChange = { newValue ->
                userAnswer = newValue
                answers[question.id.toString()] = newValue
            },
            valueRange = 0f..100f,
            steps = 100,
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.turquoise),
                activeTrackColor = colorResource(id = R.color.turquoise),
                inactiveTrackColor = Color.LightGray,
            )
        )
        Text(text = "${userAnswer.toInt()}%", modifier = Modifier.align(Alignment.End))
    }
}

@Composable
fun YesNoQuestionEdit(question: Question, answers: MutableMap<String, Any>) {
    val savedAnswer = answers[question.id.toString()]?.toString() ?: ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = question.question)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = userAnswer == "yes",
                onClick = {
                    userAnswer = "yes"
                    answers[question.id.toString()] = "yes"
                },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.turquoise)
                )
            )
            Text(text = "Yes")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = userAnswer == "no",
                onClick = {
                    userAnswer = "no"
                    answers[question.id.toString()] = "no"
                },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.turquoise)
                )
            )
            Text(text = "No")
        }
    }
}

@Composable
fun CheckboxQuestionEdit(question: Question, answers: MutableMap<String, Any>) {
    // Get the saved checkbox selections from answers
    val savedSelections = when (val value = answers[question.id.toString()]) {
        is Map<*, *> -> value.mapNotNull { (key, value) ->
            if (key is String && value is Boolean) key to value
            else null
        }.toMap()
        else -> emptyMap()
    }

    // Initialize the state with saved selections
    val selectedOptions = remember(savedSelections) {
        mutableStateMapOf<String, Boolean>().apply {
            question.options?.forEach { option ->
                this[option] = savedSelections[option] ?: false
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = question.question)
        question.options?.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Checkbox(
                    checked = selectedOptions[option] ?: false,
                    onCheckedChange = { isChecked ->
                        selectedOptions[option] = isChecked
                        answers[question.id.toString()] = selectedOptions.toMap()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(id = R.color.turquoise)
                    )
                )
                Text(text = option)
            }
        }
    }
}





