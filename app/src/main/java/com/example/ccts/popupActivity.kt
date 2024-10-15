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
import java.io.InputStreamReader
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.ccts.adapter.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


@Composable
fun PopupActivity(navController: NavController,categoryId: Int) {
    var showDialog by remember { mutableStateOf(true) } // To show the popup dialog

    var totalScore by remember { mutableStateOf(0) }


    val answers = remember { mutableStateMapOf<String, Any>() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val categories = loadCategoriesAndQuestion(context)
    val selectedCategory = categories.firstOrNull { it.id == categoryId }


    // Load categories once


    if (showDialog && selectedCategory != null) {
        Dialog(onDismissRequest = {
            showDialog = false
            navController.popBackStack()

        }) {
            Card (
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Make the width smaller
                    .fillMaxHeight(0.8f) // Limit height to 80% of screen
            ) {
                val scrollState = rememberScrollState()
                Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState) // Enable scrolling for the popup
                        .fillMaxSize() // Make sure the column fills the available space
                ) {

                    Text(
                        text = selectedCategory.category,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) // Category header
                    Spacer(modifier = Modifier.height(8.dp))

                    // Iterate through the questions in this category
                    val categoryQuestions = selectedCategory.questions ?: emptyList()

                    if (categoryQuestions.isNotEmpty()) {
                        categoryQuestions.forEach { question ->
                            when (question.type) {
                                "text" -> TextFieldQuestion(question, answers)
                                "number" -> NumberFieldQuestion(question, answers)
                                "percentage" -> PercentageSliderQuestion(question, answers)
                                "yes_no" -> YesNoQuestion(question, answers)
                                "checkbox" -> CheckboxQuestion(question, answers)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        Text("No questions available for this category.")
                    }

                    Button(
                        onClick = {
                            if (areAllQuestionsAnswered(categoryQuestions, answers)) {
                                totalScore = calculateTotalScore(categoryQuestions, answers)
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
                            navController.popBackStack() // Close the dialog and navigate back
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
//    else{
//        // After dialog is dismissed, show score or navigate away
//        Text(text = "Total Score: $totalScore", modifier = Modifier.padding(16.dp))
//        // You can also navigate back if needed
//        Button(onClick = { navController.popBackStack() }) {
//            Text(text = "Go Back", color =colorResource(id = R.color.white ))
//        }
//    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldQuestion(question: Question,answers: MutableMap<String, Any>) {
    var userAnswer by remember { mutableStateOf("") }
    Text(text = question.question)
    TextField(
        value = userAnswer,
        onValueChange = { newValue ->
            userAnswer = newValue
            answers[question.id.toString()] = userAnswer
        },
        label = { Text("Your answer") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            cursorColor = Color.Black,
            focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
            unfocusedLabelColor = Color.Gray,    // Label color when not focused
            focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
            unfocusedIndicatorColor = Color.Gray

        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberFieldQuestion(question: Question,answers: MutableMap<String, Any>) {
    var userAnswer by remember { mutableStateOf("") }
    Text(text = question.question)
    TextField(
        value = userAnswer,
        onValueChange = { newValue ->
            userAnswer = newValue
            answers[question.id.toString()] = userAnswer
        },
        label = {Text("Your answer") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            cursorColor = Color.Black,
            focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
            unfocusedLabelColor = Color.Gray, // Label color when not focused
            focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
            unfocusedIndicatorColor = Color.Gray

        )
    )
}

@Composable
fun PercentageSliderQuestion(question: Question,answers: MutableMap<String, Any>) {
    var userAnswer by remember { mutableStateOf(50f) }
    Column (modifier = Modifier.fillMaxWidth().padding(8.dp)){
        Text(text = question.question)
        Slider(
            value = userAnswer,
            onValueChange = { newValue ->
                userAnswer = newValue
                answers[question.id.toString()] = userAnswer
            },
            valueRange = 0f..100f,
            steps = 100,
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.turquoise),    // Customize the thumb color
                activeTrackColor = colorResource(id = R.color.turquoise), // Color of the active part of the slider
                inactiveTrackColor = Color.LightGray,  // Color of the inactive part
        )
        )
        Text(text = "${userAnswer.toInt()}%",modifier = Modifier.align(Alignment.End))
    }
}

@Composable
fun YesNoQuestion(question: Question, answers: MutableMap<String, Any>) {
    var userAnswer by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = question.question)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = userAnswer == "yes",
                onClick = {
                    userAnswer = "yes"
                    answers[question.id.toString()] = userAnswer
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
                    answers[question.id.toString()] = userAnswer
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
    val selectedOptions = remember { mutableStateMapOf<String, Boolean>() }
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = question.question)
        question.options?.forEach { option ->
            Row {
                Checkbox(
                    checked = selectedOptions[option] == true,
                    onCheckedChange = { isChecked ->
                        selectedOptions[option] = isChecked
                        answers[question.id.toString()] = selectedOptions.filterValues { it }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(id = R.color.turquoise), // Change to your desired checked color
                        uncheckedColor = Color.Gray, // Change to your desired unchecked color
                        checkmarkColor = Color.White // Color of the checkmark
                    )
                )
                Text(text = option)
            }
        }
    }
}



fun loadCategoriesAndQuestion(context: Context): List<Category> {
    val json = loadJsonFromAssets(context, "data_en.json")
//  Log.d("json", "loadCategories: $json ")
    return if (json != null) {
        val categoryType = object : TypeToken<List<Category>>() {}.type
       val categories= Gson().fromJson<List<Category>>(json, categoryType)
        Log.d("questionType", "questionType:$categories")
        categories
    } else {
        emptyList() // return an empty list if the file is not found
    }
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

fun areAllQuestionsAnswered(questions: List<Question>, answers: Map<String, Any>): Boolean {
    questions.forEach { question ->
        if (!answers.containsKey(question.id.toString()) || answers[question.id.toString()].toString().isBlank()) {
            return false // If any question is not answered, return false
        }
    }
    return true // All questions are answered
}