package com.example.ccts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.ccts.data.Category
import com.example.ccts.data.Question
import com.example.ccts.data.calculateTotalScore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun PopupActivity(
    navController: NavController,
    categoryId: Int,
    onClose: () -> Unit = {},
    answers: MutableMap<Int, Any?>

) {
    var showDialog by remember { mutableStateOf(true) }
    rememberCoroutineScope()
    val context = LocalContext.current
    val categories = loadCategoriesAndQuestion(context)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    var totalScore by remember { mutableDoubleStateOf(0.00) }
    val tempAnswers = remember { mutableStateMapOf<Int, Any?>() }
    var hasErrors by remember { mutableStateOf(false) }
    val errorStateMap = remember { mutableStateMapOf<Int, Boolean>() }


    // Load answers from the provided answers state
    LaunchedEffect(Unit) {
        if (selectedCategory != null) {
            totalScore = calculateTotalScore(selectedCategory, answers)
        }

        selectedCategory?.questions?.forEach { question ->
            val answer = answers[question.id]
            tempAnswers.put(question.id, answer)
        }
    }

    if (showDialog && selectedCategory != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showDialog = false
            onClose()
        }) {
            // Calculate dynamic height based on the number of questions
            val dynamicHeight = when (val questionCount = selectedCategory.questions.size) {
                0 -> 0.4f  // Default height for no questions
                in 1..5 -> 0.5f  // Moderate height for few questions
                else -> 0.8f  // Maximum height for many questions
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(dynamicHeight)
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

                        val categoryQuestions = selectedCategory.questions

                        if (categoryQuestions.isNotEmpty()) {
                            categoryQuestions.forEach { question ->
                                key(question.id) {  // Add key for proper recomposition
                                    when (question.type) {
                                        "text" -> TextFieldQuestion(
                                            question,
                                            tempAnswers,
                                            question == categoryQuestions.last()
                                        )

                                        "number" -> NumberFieldQuestion(
                                            question,
                                            tempAnswers,
                                            question == categoryQuestions.last()
                                        ){ hasError ->
                                            errorStateMap[question.id] = hasError
                                        }

                                        "percentage" -> PercentageSliderQuestion(
                                            question,
                                            tempAnswers,
                                        )

                                        "yes_no" -> YesNoQuestion(
                                            question,
                                            tempAnswers,
                                        )

                                        "checkbox" -> CheckboxQuestion(
                                            question,
                                            tempAnswers,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            Text("No questions available for this category.")
                        }
                        val hasErrors = errorStateMap.values.any { it }
                        val showError by remember {
                            derivedStateOf {
                                hasErrors || selectedCategory?.questions?.any { question ->
                                    val answer = tempAnswers[question.id]?.toString()
                                    answer.isNullOrEmpty() || (answer == "-1") // Invalid answer condition
                                } == true

                            }
                        }

                        Button(
                            onClick = {
                                if (areAllQuestionsAnswered(categoryQuestions, tempAnswers)) {
                                    tempAnswers.forEach {
                                        answers.put(it.key, it.value)
                                    }
                                    Toast.makeText(
                                        context, "Answer saved", Toast.LENGTH_SHORT
                                    ).show()
                                    showDialog = false
                                    onClose()
                                } else {
                                    Toast.makeText(
                                        context, "Please answer all questions.", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !hasErrors,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise))
                        ) {
                            Text(text = "Save")
                        }
                    }
                    IconButton(
                        onClick = {
                            showDialog = false
                            onClose()
                        }, modifier = Modifier.align(Alignment.TopEnd)
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

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun TextFieldQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>,
    isLastQuestion: Boolean,
//    highlightUnanswered: Boolean
) {
    val savedAnswer = if (answers[question.id] != null) answers[question.id].toString() else ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }
    val isUnanswered by remember { mutableStateOf(userAnswer.isEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = question.question,)
        TextField(
            value = userAnswer,
            onValueChange = { newValue ->
                userAnswer = newValue

                answers.put(question.id, userAnswer)
            },
            label = { Text("Your answer") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = if (!isLastQuestion) ImeAction.Next else ImeAction.Done),
            colors = TextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedLabelColor = colorResource(id = R.color.turquoise),
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = colorResource(id = R.color.turquoise),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NumberFieldQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>,
    isLastQuestion: Boolean,
    onErrorStateChanged: (Boolean) -> Unit
) {
    val savedAnswer =
        if (answers[question.id] != null && answers[question.id] != "null") answers[question.id].toString() else ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }
    val previousAnswerDouble3 = (answers[3] as? String)?.toDoubleOrNull()
    val previousAnswerDouble2 = (answers[2] as? String)?.toDoubleOrNull()
    val previousAnswerDouble5 = (answers[5] as? String)?.toDoubleOrNull()
    val previousAnswerDouble6 = (answers[6] as? String)?.toDoubleOrNull()
    val previousAnswerDouble11 = (answers[11] as? String)?.toDoubleOrNull()
    val previousAnswerDouble12 = (answers[12] as? String)?.toDoubleOrNull()
    var errorMessage by remember { mutableStateOf("") }
    val isReadOnly = question.id == 4 ||question.id == 7 ||question.id == 13
    LaunchedEffect(answers[question.id]) {
        val updatedAnswer = answers[question.id]?.toString() ?: ""
        if (updatedAnswer != userAnswer) {
            userAnswer = updatedAnswer

        }
    }
    if (question.id == 4 && previousAnswerDouble2 != null && previousAnswerDouble3 != null) {
        val calculatedValue = previousAnswerDouble2 - previousAnswerDouble3
        if (calculatedValue < 0) {
            answers[question.id] = calculatedValue
            errorMessage = "Calculation result cannot be negative."
            onErrorStateChanged(true)
        } else {
            answers[question.id] = calculatedValue
            userAnswer = calculatedValue.toString()
            errorMessage = "" // Clear error since valid
            onErrorStateChanged(false)
        }
    }

   else if (question.id == 7 && previousAnswerDouble5 != null && previousAnswerDouble6 != null) {
        val calculatedValue = previousAnswerDouble5 - previousAnswerDouble6
        if (calculatedValue < 0) {
            answers[question.id] = calculatedValue
            errorMessage = "Calculation result cannot be negative."
            onErrorStateChanged(true)
        } else {
            answers[question.id] = calculatedValue
            userAnswer = calculatedValue.toString()
            errorMessage = "" // Clear error since valid
            onErrorStateChanged(false)
        }
    }
    else if (question.id == 13 && previousAnswerDouble11 != null && previousAnswerDouble12 != null) {
        val calculatedValue = previousAnswerDouble11 - previousAnswerDouble12

        if (calculatedValue < 0) {
            answers[question.id] = calculatedValue
            errorMessage = "Calculation result cannot be negative."
            onErrorStateChanged(true)
        } else {
            answers[question.id] = calculatedValue
            userAnswer = calculatedValue.toString()
            errorMessage = "" // Clear error since valid
            onErrorStateChanged(false)

        }
        Log.d("NumberFieldQuestion", "Updated answers[13]: ${answers[13]}")
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = question.question,)
        TextField(
            value = userAnswer,
            onValueChange = { newValue ->
                if (!isReadOnly) {
                    errorMessage = ""
                    userAnswer = newValue
                    answers.put(question.id, userAnswer)
                    val currentAnswer = newValue.toDoubleOrNull()
                    Log.d("currentAnswer", "currentAnswer: $currentAnswer previousAnswerDouble3 $previousAnswerDouble3")
                    if (question.id == 3) {
                        if (currentAnswer == null) {
                            errorMessage = "Answer cannot be empty."
                            onErrorStateChanged(true)
                        } else if (previousAnswerDouble2 != null && currentAnswer > previousAnswerDouble2) {
                            errorMessage = "Answer must be less than or equal to the previous answer ($previousAnswerDouble2)."
                            onErrorStateChanged(true)
                        } else {
                            if (errorMessage.isNotEmpty()) {
                                onErrorStateChanged(true)
                            } else {
                                answers[question.id] = newValue
                                onErrorStateChanged(false)
                            }
                        }
                    }

                    else if (question.id == 6) {
                        if (currentAnswer != null) {
                            if (previousAnswerDouble6 != null && currentAnswer > previousAnswerDouble5!!) {
                                // Validation failed
                                errorMessage =
                                    "Answer must be less than or equal to the previous answer ($previousAnswerDouble5)."
                                onErrorStateChanged(true)
                            }
                            else if (previousAnswerDouble6 != null && currentAnswer == previousAnswerDouble6) {
                                // Valid case where the current answer equals the previous answer
                                errorMessage = ""
                                answers[question.id] = newValue
                                onErrorStateChanged(false)
                            }
                            else {
                                if (errorMessage.isEmpty()) {
                                    answers[question.id] = newValue
                                }
                                onErrorStateChanged(false)
                            }
                        }
                    }
                    else if (question.id == 12) {
                        if (currentAnswer != null) {
                            if (previousAnswerDouble12 != null && currentAnswer > previousAnswerDouble11!!) {
                                // Validation failed
                                errorMessage =
                                    "Answer must be less than or equal to the previous answer ($previousAnswerDouble11)."
                                onErrorStateChanged(true)
                            }
                            else if (previousAnswerDouble12 != null && currentAnswer == previousAnswerDouble12) {
                                // Valid case where the current answer equals the previous answer
                                errorMessage = ""
                                answers[question.id] = newValue
                                onErrorStateChanged(false)
                            }
                            else {
                                if (errorMessage.isEmpty()) {
                                    answers[question.id] = newValue
                                }
                                onErrorStateChanged(false)
                            }
                        }
                    }
                    else {
                        errorMessage = ""
                        answers[question.id] = newValue
                        onErrorStateChanged(false)
                    }

                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(imeAction = if (!isLastQuestion) ImeAction.Next else ImeAction.Done),
            label = { Text("Your answer") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isReadOnly,
//                .border(
//                    width = 2.dp,
//                    color = if (isUnanswered) Color.Red else Color.Gray
//                ),
            colors = TextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedLabelColor = colorResource(id = R.color.turquoise),
                unfocusedLabelColor = Color.Gray,
                focusedIndicatorColor = colorResource(id = R.color.turquoise),
                unfocusedIndicatorColor = Color.Gray
            )
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun PercentageSliderQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>
) {
    val savedValue = when (val value = answers[question.id]) {
        is Float -> value
        is String -> value.toFloatOrNull() ?: 0f
        else -> 0f
    }
    var userAnswer by remember { mutableFloatStateOf(savedValue) }

    val previousAnswerDouble11 = (answers[11] as? String)?.toDouble()
    Log.d("PercentageSlider", "answers[13]: ${answers[13]}, type: ${answers[13]?.javaClass?.name}")
    val previousAnswerDouble13 = (answers[13] as? Double)?.toDouble()
    val isReadOnly = question.id == 14

    Log.d("PercentageSlider", "previousAnswerDouble11: $previousAnswerDouble11")
    Log.d("PercentageSlider", "previousAnswerDouble13: $previousAnswerDouble13")
    Log.d("PercentageSlider", "previousAnswerDouble11: ${answers[13]}")

    // Update userAnswer when dependencies change
    LaunchedEffect(previousAnswerDouble11, answers[13]) {
        Log.d("PercentageSlider", "previousAnswerDouble11: $previousAnswerDouble11")
        Log.d("PercentageSlider", "previousAnswerDouble13: $previousAnswerDouble13")
        if   (question.id == 14 && previousAnswerDouble11 != null && previousAnswerDouble11 > 0 && previousAnswerDouble13 != null){
            val calculatedValue = (previousAnswerDouble13 / previousAnswerDouble11) * 100
            userAnswer = calculatedValue.toFloat()
            answers[question.id] = userAnswer
            Log.d("PercentageSlider", "calculatedValue: $calculatedValue")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = question.question)
        Slider(
            value = userAnswer,
            onValueChange = { newValue ->
                if (!isReadOnly) {
                    userAnswer = newValue
                    answers[question.id] = userAnswer
                }
            },
            enabled = !isReadOnly,
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

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun YesNoQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>
) {
    val savedAnswer = answers[question.id]?.toString() ?: ""
    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = question.question)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = userAnswer == "yes", onClick = {
                    userAnswer = "yes"
                    if (answers[question.id].toString() != "null") {
                        answers[question.id] = userAnswer
                    } else {
                        answers[question.id] = userAnswer
                    }
                }, colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.turquoise)
                )
            )
            Text(text = "Yes")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = userAnswer == "no", onClick = {
                    userAnswer = "no"

                    answers.put(question.id, userAnswer)
                }, colors = RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.turquoise)
                )
            )
            Text(text = "No")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun CheckboxQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>,
) {
    val savedSelections = when (val value = answers[question.id]) {
        is Map<*, *> -> value.mapNotNull { (key, value) ->
            if (key is String && value is Boolean) key to value
            else null
        }.toMap()

        else -> emptyMap()
    }

    val selectedOptions = remember(savedSelections) {
        mutableStateMapOf<String, Boolean>().apply {
            question.options?.forEach { option ->
                this[option] = savedSelections[option] == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = question.question)
        question.options?.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedOptions[option] == true,
                    onCheckedChange = {
                        selectedOptions[option] = it

                        answers.put(question.id, selectedOptions)
                    },
                    colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.turquoise))
                )
                Text(text = option)
            }
        }
    }
}

fun saveAnswersToSharedPreferences(
    context: Context, category: Category, answers: Map<Int, Any?>
) {
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    category.questions.forEach { question ->
        val answer = answers[question.id]
        when (question.type) {
            "percentage", "number" -> {
                val percentageValue = (answer as? Float) ?: 0f
                editor.putFloat("answer_${category.id}_${question.id}", percentageValue)
            }

            "checkbox" -> {
                if (answer is Map<*, *>) {
                    val gson = Gson()
                    val jsonString = gson.toJson(answer)
                    editor.putString("answer_${category.id}_${question.id}", jsonString)
                }
            }

            else -> {
                editor.putString("answer_${category.id}_${question.id}", answer?.toString() ?: "")
            }
        }
    }

    editor.apply()
}

fun loadJsonFromAssets(context: Context, fileName: String): String? {
    return try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun loadCategoriesAndQuestion(context: Context): List<Category> {
    val json = loadJsonFromAssets(context, "data_en.json")
    return if (json != null) {
        val categoryType = object : TypeToken<List<Category>>() {}.type
        val categories = Gson().fromJson<List<Category>>(json, categoryType)
        categories
    } else {
        emptyList()
    }
}

// Function to check if all questions are answered
fun areAllQuestionsAnswered(
    questions: List<Question>, answers: MutableMap<Int, Any?>
): Boolean {
    return questions.all { question ->
        when (question.type) {
            "checkbox" -> {
                val selected = answers[question.id] as? Map<*, *>
                selected?.isNotEmpty() == true
            }

            else -> !answers[question.id].toString()
                .isBlank() && answers[question.id].toString() != "null"
        }
    }
}