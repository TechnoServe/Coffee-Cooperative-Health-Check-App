package com.example.ccts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
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
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException




@Composable
fun PopupActivity(navController: NavController, categoryId: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val answers = remember { mutableStateMapOf<String, Any>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories = loadCategoriesAndQuestion(context)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    var totalScore by remember { mutableStateOf(0.00) }
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)

    // Load saved answers when the component is first created
    LaunchedEffect(Unit) {
        if (selectedCategory != null) {
            selectedCategory.questions?.forEach { question ->
                val savedAnswer = getAnswerFromSharedPreferences(context, selectedCategory.id, question)
                Log.d("From shared","ansers from shared $savedAnswer")
                answers.putAll(savedAnswer)
                Log.d("LoadedAnswers", "Question ${question.id}: ${savedAnswer[question.id.toString()]}")
            }
            totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
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
                                    totalScore = calculateTotalScore(selectedCategory, sharedPreferences)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldQuestion(question: Question, answers: MutableMap<String, Any>) {
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
fun NumberFieldQuestion(question: Question, answers: MutableMap<String, Any>) {
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
fun PercentageSliderQuestion(question: Question, answers: MutableMap<String, Any>) {
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
fun YesNoQuestion(question: Question, answers: MutableMap<String, Any>) {
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
fun CheckboxQuestion(question: Question, answers: MutableMap<String, Any>) {
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

fun saveAnswersToSharedPreferences(context: Context, category: Category, answers: Map<String, Any?>) {
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    category.questions.forEach { question ->
        val answer = answers[question.id.toString()]
        when (question.type) {
            "percentage","number" -> {
                val percentageValue = (answer as? Float) ?: 50f
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
fun getAnswerFromSharedPreferences(context: Context, categoryId: Int, question: Question): Map<String, Any> {
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    val answersMap = mutableMapOf<String, Any>()
    val answerKey = "answer_${categoryId}_${question.id}" // Updated key format

    when (question.type) {
        "percentage","number" -> {
            // Retrieve percentage values saved as floats
            val savedValue = sharedPreferences.getFloat(answerKey, 50f)
            answersMap[question.id.toString()] = savedValue
        }
        "checkbox" -> {
            // Retrieve and parse JSON strings for checkbox answers
            val jsonString = sharedPreferences.getString(answerKey, null)
            if (jsonString != null) {
                val gson = Gson()
                val type = object : TypeToken<Map<String, Boolean>>() {}.type
                try {
                    val checkboxAnswers = gson.fromJson<Map<String, Boolean>>(jsonString, type)
                    answersMap[question.id.toString()] = checkboxAnswers
                } catch (e: Exception) {
                    Log.e("SharedPreferences", "Error parsing checkbox answers", e)
                }
            }
        }
        else -> {
            // Retrieve other answers as strings
            val savedAnswer = sharedPreferences.getString(answerKey, "")
            if (!savedAnswer.isNullOrEmpty()) {
                answersMap[question.id.toString()] = savedAnswer
            }
        }
    }

    return answersMap
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
//  Log.d("json", "loadCategories: $json ")
    return if (json != null) {
        val categoryType = object : TypeToken<List<Category>>() {}.type
        val categories= Gson().fromJson<List<Category>>(json, categoryType)
//        Log.d("questionType", "questionType:$categories")
        categories
    } else {
        emptyList() // return an empty list if the file is not found
    }
}

fun areAllQuestionsAnswered(questions: List<Question>, answers: Map<String, Any>): Boolean {
    return questions.all { question ->
        when (question.type) {
            "checkbox" -> {
                val checkboxAnswers = answers[question.id.toString()] as? Map<*, *>
                checkboxAnswers != null && checkboxAnswers.isNotEmpty()
            }
            else -> {
                answers.containsKey(question.id.toString()) &&
                        answers[question.id.toString()].toString().isNotBlank()
            }
        }
    }
}