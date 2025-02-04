package com.example.cooptrac

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.technoserve.cooptrac.R
import com.technoserve.cooptrac.data.AnswersViewModel
import com.technoserve.cooptrac.data.AnswersViewModelFactory
import com.technoserve.cooptrac.data.Category
import com.technoserve.cooptrac.data.Question
import com.technoserve.cooptrac.data.calculateTotalScore
import com.technoserve.cooptrac.hasSingleAnswer
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds



@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PopupActivity(
    navController: NavController,
    categoryId: Int,
    onClose: () -> Unit = {},
    answers: MutableMap<Int, Any?>,
    viewModel: AnswersViewModel
) {
    var showDialog by rememberSaveable { mutableStateOf(true) }
    rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
    Log.d("Languageeee", "Language changed to $selectedLanguage")
    var categories by remember(selectedLanguage) {
        mutableStateOf(loadCategoriesAndQuestion(context, selectedLanguage))
    }
    val selectedCategory = categories.firstOrNull { it.id == categoryId }
    var totalScore by remember { mutableDoubleStateOf(0.00) }
    val tempAnswers = remember { mutableStateMapOf<Int, Any?>() }
    val errorStateMap = remember { mutableStateMapOf<Int, Boolean>() }
    val recommendations= viewModel.recommendations
    Log.d("recommendationss", "answersState before saving: $recommendations")

//    val recommendations = remember { mutableStateListOf<String>() }
    val hasAnswersForCategory = remember { mutableStateOf(false) }
    var isTempAnswersInitialized by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val selectedSurvey by viewModel.selectedSurvey.collectAsState()
    val timestamp = selectedSurvey?.timestamp ?: 0L
//    var surveyDate by remember { mutableStateOf<LocalDate?>(null) }
    val surveyDate = if (timestamp != 0L) {
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    } else {
        LocalDate.now() // Use today's date if timestamp is missing
    }

    val validationErrorState = remember { mutableStateOf<Map<Int, String?>>(emptyMap()) }

    val isToday = surveyDate?.isEqual(today) == true




    var showResultDialog by rememberSaveable { mutableStateOf(false) }
    var totalWeight = 0.0
    selectedCategory?.questions?.forEach { question ->

        val questionWeight = question.weight.toDouble()
        totalWeight += questionWeight}
    LaunchedEffect(today,surveyDate) {

        Log.d("Survey", "today : $today")
        Log.d("Survey", "formattedDate : $surveyDate")

        // Update state with new categories or handle reloading logic
    }

    LaunchedEffect(selectedLanguage) {
        Log.d("Language", "Language changed to $selectedLanguage")
        categories = loadCategoriesAndQuestion(context, selectedLanguage)
        // Update state with new categories or handle reloading logic
    }


    // Filter questions based on the `dependsOn` logic

    LaunchedEffect(Unit) {
        if (selectedCategory != null) {

          val  score = calculateTotalScore(context,selectedCategory, answers)

            totalScore=score.first
            viewModel.resetRecommendations()
//            recommendations.addAll(score.second)
            viewModel.setRecommendations(score.second)
            Log.d("recommendations", "PopupActivity:$recommendations ")


            selectedCategory.questions.forEach { question ->
                tempAnswers[question.id] = answers[question.id] ?: tempAnswers[question.id]
            }
            tempAnswers.forEach { (key, value) ->
                Log.d("PopupActivity", "Answer loaded for question ID $key: $value withy type of value${value?.javaClass?.simpleName}")
            }
            isTempAnswersInitialized = true

        }

        selectedCategory?.questions?.forEach { question ->
            val answer = answers[question.id]
            tempAnswers[question.id]=answer

            Log.d("PopupActivity", "Dependent answerss: $tempAnswers")



        }

    }


    val filteredQuestions by remember {
        derivedStateOf {
            if (isTempAnswersInitialized) {
                selectedCategory?.questions?.filter { question ->
                    val dependentAnswer = tempAnswers[question.dependsOn?.questionId]?.toString()
                    Log.d("PopupActivity", "Dependent answer: $dependentAnswer")
                    Log.d("PopupActivity", "Dependent answer2: $tempAnswers")

                    if (dependentAnswer == question.dependsOn?.value) {
                        // If the dependent condition is met, retain the saved answer
                        Log.d("PopupActivity", "Does tempAnswers contain question ID ${question.id}? ${tempAnswers.containsKey(question.id)}")
                        if (!tempAnswers.containsKey(question.id)) {
                            val savedAnswer = answers[question.id] as? Map<String, Boolean>
                            tempAnswers[question.id] = savedAnswer ?: ""
                            Log.d("PopupActivity", "Dependent answer3: $tempAnswers")
                            Log.d("PopupActivity", "Dependent answer4: $answers")
                        }
                        tempAnswers[question.id]
                        Log.d("PopupActivity", "Dependent answer5: $tempAnswers")
                        Log.d("PopupActivity", "Dependent answer6: $answers")
                        true
                    } else {
                        // Only set a default value if no saved answer exists
                            when (question.type) {
                                "checkbox" -> tempAnswers[question.id] = question.options?.associateWith { false }
                                "number" -> tempAnswers[question.id] = 0
                                "text" -> tempAnswers[question.id] = ""
                                "percentage" -> tempAnswers[question.id] = 0
                            }

                        false
                    }
                } ?: listOf()
            } else {
                emptyList()
            }
        }
    }

                            LaunchedEffect(tempAnswers, selectedCategory) {
                                val questions = selectedCategory?.questions ?: listOf()
                                hasAnswersForCategory.value = hasSingleAnswer(tempAnswers, questions)
                            }



    if (showResultDialog) {

        AlertDialog(
            onDismissRequest = { showResultDialog = true },
            title = {
                Text(
                    text = if (selectedLanguage == "English") "Score & Recommendations" else "Amanota & Ibyifuzo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "${if (selectedLanguage == "English") "Score:" else "Amanota:"} $totalScore / $totalWeight",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    recommendations.forEach { recommendation ->
                        Text(text = "• $recommendation")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =colorResource(
                            id = R.color.turquoise
                        )
                    )
                ) {
                    Text(text = if (selectedLanguage == "English") "Close" else "Funga")
                }
            }
        )

    }





                                if (showDialog && selectedCategory != null) {
//        androidx.compose.ui.window.Dialog(onDismissRequest = {
//            showDialog = false
//            onClose()
//        }) {

                                // Dynamically calculate the height based on the number of filtered questions
                                val dynamicHeight = when (val questionCount = filteredQuestions.size) {
                                    0 -> 0.3f  // Default height for no questions
                                    in 1..4 -> 0.56f  // Moderate height for a few questions
                                    else -> 0.56f   // Maximum height for many questions
                                }

                                Card(
                                    modifier = Modifier
                                        .imePadding()
                                        .fillMaxWidth(0.9f)
                                        .fillMaxHeight(dynamicHeight)

                                ) {
                                    val scrollState = rememberScrollState()
                                    val contentHeight = 1000.dp // Example content height
                                    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                                    Log.d("screenHeight", "PopupActivity:$screenHeight")
                                    val containerHeight = if (screenHeight <= 800.dp) 290.dp else if (screenHeight == 777.dp) 380.dp else 660.dp
                                    Box(Modifier.fillMaxSize()) {
                                        val isEditable = isToday
                                        Log.d("Debug", "isEditable = $isEditable, isToday = $isToday")
                                        Column (
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {

                                            Text(
                                                text = selectedCategory.category,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 25.sp,
                                                modifier = Modifier

                                                    .padding(10.dp)
                                            )
                                            Box(modifier = Modifier.weight(1f)) {

                                            Column(
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .verticalScroll(scrollState)
                                                    .fillMaxSize()

                                            ) {


                                                if (filteredQuestions.isNotEmpty()) {
                                                    filteredQuestions.forEach { question ->
                                                        key(question.id) {
                                                            Log.d(
                                                                "PopupActivity",
                                                                "PopupActivity Before PercentageSliderQuestion: $tempAnswers"
                                                            )
                                                            when (question.type) {
                                                                "text" -> TextFieldQuestion(
                                                                    question,
                                                                    tempAnswers,
                                                                    question == filteredQuestions.last(),
                                                                    isEditable,
                                                                    validationErrorState
                                                                )

                                                                "number" -> NumberFieldQuestion(
                                                                    question,
                                                                    tempAnswers,
                                                                    question == filteredQuestions.last(),
                                                                    isEditable
                                                                ) { hasError ->
                                                                    errorStateMap[question.id] =
                                                                        hasError
                                                                }

                                                                "percentage" -> PercentageSliderQuestion(
                                                                    question,
                                                                    tempAnswers,
                                                                    isEditable
                                                                )

                                                                "yes_no" -> YesNoQuestion(
                                                                    question,
                                                                    tempAnswers,
                                                                    onAnswerChange = { answer ->
                                                                        tempAnswers[question.id] =
                                                                            answer
                                                                    },
                                                                    shouldShow = true, // Always show Yes/No questions if they're in filtered list
                                                                    isEditable
                                                                )

                                                                "checkbox" -> CheckboxQuestion(
                                                                    question,
                                                                    answers = tempAnswers,
                                                                    isEditable
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Text(
                                                        if (selectedLanguage == "English") {
                                                            "No questions available for this category."
                                                        } else {
                                                            "nta kibazo gihari muri iki cyikiro"
                                                        }
                                                    )
                                                }

                                                // Check for errors and ensure all questions are answered
                                                val error =
                                                    validationErrorState.value.values.any { it != null }
                                                Log.d("error", "PopupActivity:$error ")
                                                val hasErrors = errorStateMap.values.any { it }
                                                val showError by remember {
                                                    derivedStateOf {
                                                        error || hasErrors || filteredQuestions.any { question ->
                                                            val answer =
                                                                tempAnswers[question.id]?.toString()
                                                            answer.isNullOrEmpty() || (answer == "-1") // Invalid answer condition
                                                        }
                                                    }
                                                }

                                                // Recommendations Section
                                                if (recommendations.isNotEmpty()) {
                                                    Text(
                                                        text = if (selectedLanguage == "English") {
                                                            "Recommendations"
                                                        } else {
                                                            "Ibyifuzo"
                                                        },
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                    recommendations.forEach { recommendation ->
                                                        Text(
                                                            text = "• $recommendation",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    }
                                                } else {
                                                    if (hasAnswersForCategory.value && answers.isNotEmpty()) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Start,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp)
                                                        ) {
                                                            Text(
                                                                text = if (selectedLanguage == "English") {
                                                                    "Recommendations: "
                                                                } else {
                                                                    "Ibyifuzo: "
                                                                },
                                                                style = MaterialTheme.typography.headlineSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 17.sp,

                                                                modifier = Modifier.padding(vertical = 8.dp)

                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = if (selectedLanguage == "English") {
                                                                    "you comply"
                                                                } else {
                                                                    "Byubahirijwe"
                                                                },
                                                                style = MaterialTheme.typography.headlineSmall,
                                                                fontSize = 17.sp,

                                                                modifier = Modifier
                                                                    .padding(vertical = 8.dp)
                                                                    .weight(1f)

                                                            )
                                                        }

                                                    }
                                                }


                                            }
                                                VerticalScrollbar(
                                                    modifier = Modifier
                                                        .align(Alignment.CenterEnd)
                                                        .fillMaxHeight()
                                                        .padding(end = 4.dp), // Adjust scrollbar position
                                                    scrollState = scrollState,
                                                    contentHeight = contentHeight,
                                                    containerHeight = containerHeight

                                                )
                                        }
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp)

                                            ) {
//                            if (surveyDate.isEqual(today)) {
                                                Button(
                                                    onClick = {
                                                        if (isEditable) {
                                                            // Reset all answers in tempAnswers and answers
                                                            val categoryQuestionIds =
                                                                selectedCategory?.questions?.map { it.id }
                                                                    ?: listOf()
                                                            categoryQuestionIds.forEach { questionId ->
                                                                tempAnswers[questionId] = null

                                                            }
                                                            Toast.makeText(
                                                                context,
                                                                if (selectedLanguage == "English") {
                                                                    "All answers cleared"
                                                                } else {
                                                                    "ibisubizo byasibwe"
                                                                },
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    enabled = isEditable,
                                                    colors = ButtonDefaults.buttonColors(Color.DarkGray),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .width(100.dp)// Adjusts the space for the button
                                                ) {
                                                    Text(
                                                        text = if (selectedLanguage == "English") {
                                                            "Clear"
                                                        } else {
                                                            "Siba byose"
                                                        }
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp)) // Adds space between buttons

                                                Button(
                                                    onClick = {
                                                        if (isEditable) {
                                                            if (areAllQuestionsAnswered(
                                                                    filteredQuestions,
                                                                    tempAnswers
                                                                )
                                                            ) {
                                                                tempAnswers.forEach { (key, value) ->
                                                                    answers[key] = value
                                                                }
                                                                val scoreData = calculateTotalScore(
                                                                    context,
                                                                    selectedCategory!!,
                                                                    answers
                                                                )
                                                                totalScore = scoreData.first
//                                                            recommendations.clear()
//                                                            recommendations.addAll(scoreData.second)
                                                                viewModel.resetRecommendations()
                                                                viewModel.setRecommendations(
                                                                    scoreData.second
                                                                )
                                                                answers.forEach { (key, value) ->
                                                                    Log.d(
                                                                        "PercentageSliderSave",
                                                                        "Saved answer: Key = $key, Value = $value"
                                                                    )
                                                                }
                                                                Toast.makeText(
                                                                    context,
                                                                    if (selectedLanguage == "English") {
                                                                        "Answer saved"
                                                                    } else {
                                                                        "ibisubizo byemejwe"
                                                                    },
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                               //
                                                                showDialog = false
                                                                showResultDialog = true
                                                                Log.d("states", "showdialog=$showDialog,showresult=$showResultDialog")
                                                                // onClose()
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    if (selectedLanguage == "English") {
                                                                        "Please answer all questions."
                                                                    } else {
                                                                        "nyabuneka subiza ibibazo byose"
                                                                    },
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    },
                                                    enabled = validationErrorState.value.values.all { it == null } && isEditable,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .width(100.dp), // Adjusts the space for the button
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isEditable) colorResource(
                                                            id = R.color.turquoise
                                                        ) else Color.DarkGray,
                                                    )

                                                ) {
                                                    Text(
                                                        text = if (selectedLanguage == "English") {
                                                            "Save"
                                                        } else {
                                                            "Bika"
                                                        }
                                                    )

                                                }


                                            }

                                        }

                                        IconButton(
                                            onClick = {
                                                showDialog = false
                                               onClose()
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






                    @RequiresApi(Build.VERSION_CODES.N)
                    @Composable
                    fun TextFieldQuestion(
                        question: Question,
                        answers: MutableMap<Int, Any?>,
                        isLastQuestion: Boolean,
                        isEditable: Boolean,
                        validationErrorState: MutableState<Map<Int, String?>>
//    highlightUnanswered: Boolean
                    ) {
                        val context = LocalContext.current
                        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                        val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
                        val savedAnswer = if (answers[question.id] != null) answers[question.id].toString() else ""
                        var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }
                        val isUnanswered by remember { mutableStateOf(userAnswer.isEmpty()) }
                        var validationError by remember { mutableStateOf<String?>(null) }

                        // Function to validate phone number
                        fun isValidPhoneNumber(phone: String): Boolean {
                            val trimmedPhone = phone.trim()

                            if (trimmedPhone.isEmpty()) {
                                return false // ❌ Empty input is invalid
                            }

                            val phoneRegex = Regex("^[0-9]{10}$")   // Allows optional "+" and 8-15 digits
                            return phoneRegex.matches(trimmedPhone) // ✅ Returns true if valid
                        }


                        // If the question id is 3 (phone number), validate it
                        if (question.id == 3) {
                            isValidPhoneNumber(userAnswer) // Validate the phone number on each change
                        }


                        validationErrorState.value = validationErrorState.value.toMutableMap().apply {
                            this[question.id] = validationError // Use `this` for clarity
                        }.toMap() // Convert back to immutable Map

                        Log.d("ValidationDebug", "Question ${question.id}: Error = $validationError")
                        Log.d("ValidationDebug", "Validation State: ${validationErrorState.value}")


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = question.question,)
                            TextField(
                                value = userAnswer,
                                onValueChange = { newValue ->
                                    if (isEditable) {
                                        if (question.id == 3) {
                                            // Restrict to numbers only & max 10 digits
                                            if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                                                userAnswer = newValue
                                                answers[question.id] = userAnswer
                                            }
                                        } else {
                                            // For other questions, allow any input
                                            userAnswer = newValue
                                            answers[question.id] = userAnswer
                                        }

                                        validationError = if (question.id == 3 && !isValidPhoneNumber(newValue)) {
                                            if(selectedLanguage=="English"){"Phone number must be exactly 10 digits"}else{"Nomero ya telephone igomba kuba 10"}
                                        } else {
                                            null // ✅ Set to null if valid
                                        }
                                        validationErrorState.value = validationErrorState.value.toMutableMap().apply {
                                            this[question.id] = validationError
                                        }.toMap()




                                        // Validate the phone number for question ID 3

                                    }

                                },
                                enabled = isEditable,
                                label = { Text(if(selectedLanguage=="English"){"Your answer"}else{"igisubizo cyawe"}) },
                                isError = validationError != null,
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
                            if (validationError != null) {
                                Text(
                                    text = validationError!!,
                                    color = Color.Red,
                                    style = TextStyle(fontSize = 12.sp)
                                )
                            }
                        }
                    }

                    @RequiresApi(Build.VERSION_CODES.N)
                    @Composable
                    fun NumberFieldQuestion(
                        question: Question,
                        answers: MutableMap<Int, Any?>,
                        isLastQuestion: Boolean,
                        isEditable: Boolean,
                        onErrorStateChanged: (Boolean) -> Unit
                    ) {
                        val savedAnswer =
                            if (answers[question.id] != null && answers[question.id] != "null") answers[question.id].toString() else ""
                        var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }
                        val previousAnswerDouble4 = (answers[5] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble3 = (answers[4] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble6 = (answers[7] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble8 = (answers[9] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble7 = (answers[8] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble13 = (answers[14] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble14 = (answers[15] as? String)?.toDoubleOrNull()
                        val previousAnswerDouble18= (answers[19] as? String)?.toString()
                        var errorMessage by remember { mutableStateOf("") }
                        val isReadOnlys = question.id == 20 && previousAnswerDouble18?.equals("no", ignoreCase = true) == true
                        val isReadOnly = question.id == 6 ||question.id == 9 ||question.id == 16
                        val context = LocalContext.current
                        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                        val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
                        LaunchedEffect(answers[question.id]) {
                            val updatedAnswer = answers[question.id]?.toString() ?: ""
                            if (updatedAnswer != userAnswer) {
                                userAnswer = updatedAnswer

                            }
                        }
                        LaunchedEffect(previousAnswerDouble18) {
                            Log.d("NumberFieldQuestion", "Updated answers[13]:$previousAnswerDouble18")
                            if (question.id == 20) {
                                if (previousAnswerDouble18=="no") {
                                    userAnswer = 0.0.toString() // Automatically set to zero if previous answer is "No"
                                    answers[question.id] = 0.0
                                    Log.d("NumberFieldQuestion", "Updated answers[13]:$answers")
                                    errorMessage = "" // Clear error since valid
                                    onErrorStateChanged(false)
                                } else if ( previousAnswerDouble18.equals(
                                        "yes",
                                        ignoreCase = true
                                    )
                                ) {
                                    answers[question.id]= userAnswer
                                    // Allow editing
                                    errorMessage = ""
                                    onErrorStateChanged(false)
                                }
                            }
                        }

                        LaunchedEffect(answers[14], answers[15]) {
                            if (question.id == 16 && previousAnswerDouble13 != null && previousAnswerDouble14 != null) {
                                val calculatedValue = previousAnswerDouble13 + previousAnswerDouble14

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
                        }
                        if (question.id == 6 && previousAnswerDouble3 != null && previousAnswerDouble4 != null) {
                            val calculatedValue = previousAnswerDouble3 + previousAnswerDouble4
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
                        else if (question.id == 9 && previousAnswerDouble6 != null && previousAnswerDouble7 != null) {
                            val calculatedValue = previousAnswerDouble7 + previousAnswerDouble6
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


//   else if (question.id == 7 && previousAnswerDouble5 != null && previousAnswerDouble6 != null) {
//        val calculatedValue = previousAnswerDouble5 + previousAnswerDouble6
//        if (calculatedValue < 0) {
//            answers[question.id] = calculatedValue
//            errorMessage = "Calculation result cannot be negative."
//            onErrorStateChanged(true)
//        } else {
//            answers[question.id] = calculatedValue
//            userAnswer = calculatedValue.toString()
//            errorMessage = "" // Clear error since valid
//            onErrorStateChanged(false)
//        }
//    }


//    else if (question.id == 14 && previousAnswerDouble12 != null && previousAnswerDouble13 != null) {
//        val calculatedValue = previousAnswerDouble12 + previousAnswerDouble13
//
//        if (calculatedValue < 0) {
//            answers[question.id] = calculatedValue
//            errorMessage = "Calculation result cannot be negative."
//            onErrorStateChanged(true)
//        } else {
//            answers[question.id] = calculatedValue
//            userAnswer = calculatedValue.toString()
//            errorMessage = "" // Clear error since valid
//            onErrorStateChanged(false)
//
//        }
//
//    }
                        else if (question.id == 20) {
                            if (previousAnswerDouble18=="no") {
                                userAnswer = 0.0.toString() // Automatically set to zero if previous answer is "No"
                                answers[question.id] = userAnswer
                                Log.d("Num ", "Upda:$answers")
                                errorMessage = "" // Clear error since valid
                                onErrorStateChanged(false)
                            } else if ( previousAnswerDouble18.equals(
                                    "yes",
                                    ignoreCase = true
                                )
                            ) {
                                answers[question.id]= userAnswer
                                // Allow editing
                                errorMessage = ""
                                onErrorStateChanged(false)
                            }
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
                                    if (isEditable) {
                                        if (!isReadOnly) {
                                        errorMessage = ""
                                        userAnswer = newValue
                                        answers.put(question.id, userAnswer)
                                        val currentAnswer = newValue.toDoubleOrNull()
                                        Log.d(
                                            "currentAnswer",
                                            "currentAnswer: $currentAnswer previousAnswerDouble3 $previousAnswerDouble3"
                                        )

                                        if (question.id == 16) {
                                            if (currentAnswer != null) {

                                                if (previousAnswerDouble14 != null && currentAnswer == previousAnswerDouble14) {
                                                    // Valid case where the current answer equals the previous answer
                                                    errorMessage = ""
                                                    answers[question.id] = newValue
                                                    onErrorStateChanged(false)
                                                } else if (question.id == 20 && previousAnswerDouble18 != null) {


                                                    if (previousAnswerDouble18 == "yes") {
                                                        answers[question.id] = userAnswer
                                                        Log.d(
                                                            "NumberFieldQuestion",
                                                            "Updated answers[13]: ${answers[question.id]}"
                                                        )
                                                        errorMessage =
                                                            "Calculation result cannot be negative."
                                                        onErrorStateChanged(true)
                                                    } else {
                                                        answers[question.id] = 0.0
                                                        Log.d(
                                                            "NumberFieldQuestion",
                                                            "Updated: ${answers[question.id]}"
                                                        )

                                                        errorMessage = "" // Clear error since valid
                                                        onErrorStateChanged(false)

                                                    }

                                                } else {
                                                    if (errorMessage.isEmpty()) {
                                                        answers[question.id] = newValue
                                                    }
                                                    onErrorStateChanged(false)
                                                }
                                            }
                                        } else {
                                            errorMessage = ""
                                            answers[question.id] = newValue
                                            onErrorStateChanged(false)
                                        }

                                    } else if (!isReadOnlys) {
                                        userAnswer = newValue
                                        answers[question.id] = userAnswer
                                        newValue.toDoubleOrNull()
                                    }
                                }
                                },

                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number).copy(imeAction = if (!isLastQuestion) ImeAction.Next else ImeAction.Done),
                                label = { Text(if(selectedLanguage=="English"){"Your answer"}else{"igisubizo cyawe"}) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isReadOnly ||isEditable,
//                .border(
//                    width = 2.dp,
//                    color = if (isUnanswered) Color.Red else Color.Gray
//                ),
                                colors = TextFieldDefaults.colors(
                                    cursorColor = Color.Black,
                                    focusedLabelColor = colorResource(id = R.color.turquoise),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedIndicatorColor = colorResource(id = R.color.turquoise),
                                    unfocusedIndicatorColor = Color.Gray,
                                    disabledTextColor = Color.Black
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

                    @SuppressLint("LongLogTag")
                    @RequiresApi(Build.VERSION_CODES.N)
                    @Composable
// commented by shema
//fun PercentageSliderQuestion(
//    question: Question,
//    answers:  MutableMap<Int, Any?>
//) {
//    val savedValue = when (val value = answers[question.id]) {
//        is Float -> value
//        is String -> value.toFloatOrNull() ?: 0f
//        else -> 0f
//    }
//    var userAnswer by remember { mutableFloatStateOf(savedValue) }
//
//    val previousAnswerDouble13 = answers[13]?.toString()?.toDoubleOrNull()
//    Log.d("PercentageSlider", "answers[13]: ${answers[14]}, type: ${answers[14]?.javaClass?.name}")
//    val previousAnswerDouble14 = answers[14]?.toString()?.toDoubleOrNull()
//    val isReadOnly = question.id == 15
//
//    Log.d("PercentageSlider", "previousAnswerDouble11: $previousAnswerDouble13")
//    Log.d("PercentageSlider", "previousAnswerDouble13: $previousAnswerDouble14")
//    Log.d("PercentageSlider", "previousAnswerDouble11: ${answers[14]}")
//
//    // Update userAnswer when dependencies change
//    LaunchedEffect(previousAnswerDouble13, answers[14]) {
//        Log.d("PercentageSlider", "previousAnswerDouble11: $previousAnswerDouble13")
//        Log.d("PercentageSlider", "previousAnswerDouble13: $previousAnswerDouble13")
//        if   (question.id == 15 && previousAnswerDouble13 != null && previousAnswerDouble14 != null && previousAnswerDouble14 > 0 ){
//            val calculatedValue = (previousAnswerDouble13 / previousAnswerDouble14) * 100
//            userAnswer = calculatedValue.toFloat()
//            answers[question.id] = userAnswer
//            Log.d("PercentageSlider", "calculatedValue: $calculatedValue")
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Text(text = question.question)
//        Slider(
//            value = userAnswer,
//            onValueChange = { newValue ->
//                if (!isReadOnly) {
//                    userAnswer = newValue
//                    answers[question.id] = userAnswer
//                }
//            },
//            enabled = !isReadOnly,
//            valueRange = 0f..100f,
//            steps = 100,
//            colors = SliderDefaults.colors(
//                thumbColor = if (isReadOnly) colorResource(id = R.color.turquoise)  else colorResource(id = R.color.turquoise),
//                activeTrackColor =  if (isReadOnly) colorResource(id = R.color.turquoise) else colorResource(id = R.color.turquoise),
//                inactiveTrackColor = if (isReadOnly) Color.LightGray else Color.LightGray,
//            )
//        )
//        Text(text = "${userAnswer.toInt()}%", modifier = Modifier.align(Alignment.End))
//    }
//}

                    fun PercentageSliderQuestion(
                        question: Question,
                        answers: MutableMap<Int, Any?>,
                        isEditable: Boolean,
                    ) {
                        Log.d("PopupActivity", "Answers before passing: $answers")
                        // Retrieve the saved value from the answers map, convert it to Float if possible
                        val savedValue = answers[question.id]?.toString()?.toFloatOrNull() ?: 0f
                        val context = LocalContext.current
                        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                        val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"


                        Log.d("PercentageSliderTypeCheck", "savedValue: ${answers[question.id]}, Type: ${{ answers[question.id] }::class.simpleName}")


                        Log.d("PercentageSliderInit", "Initialized with saved value: $savedValue")
                        var userAnswer by remember { mutableFloatStateOf(0f) }
                        Log.d("PercentageSlideruserAnswer", "userAnswer with saved value: $userAnswer")

                        LaunchedEffect(savedValue) {
                            userAnswer = savedValue
                        }


                        // Previous answers for the dependent calculation (answers for question 14 and 15)
                        val previousAnswerDouble14 = answers[15]?.toString()?.toDoubleOrNull()
                        val previousAnswerDouble15 = answers[16]?.toString()?.toDoubleOrNull()

                        // Read-only flag for specific question ID (question.id == 16)
                        val isReadOnly = question.id == 17

                        // Log the previous answers for debugging purposes
                        LaunchedEffect(previousAnswerDouble14, previousAnswerDouble15) {
                            Log.d("PercentageSlider", "Previous answers: [13]: $previousAnswerDouble14, [15]: $previousAnswerDouble15")

                            // Only perform the calculation if the values for 14 and 15 are valid
                            if (question.id == 17 && previousAnswerDouble14 != null && previousAnswerDouble15 != null && previousAnswerDouble15 > 0) {
                                val calculatedValue = (previousAnswerDouble14 / previousAnswerDouble15) * 100
                                userAnswer = calculatedValue.toFloat()
                                answers[question.id] = userAnswer // Update the answers map with the calculated value
                                Log.d("PercentageSlider", "Calculated percentage: $calculatedValue")
                            }
                        }

                        // UI for the percentage slider question
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            // Display the question text
                            Text(text = question.question)

                            // The slider widget for percentage selection
                            Slider(
                                value = savedValue,
                                onValueChange =
//            { if (!isReadOnly) userAnswer = it },
                                { newValue ->
                                    if(isEditable) {
                                        if (!isReadOnly && userAnswer != newValue) {
                                            userAnswer = newValue
                                            // Update the answers map immediately when the value changes
                                            answers[question.id] = userAnswer
                                            Log.d(
                                                "PercentageSlider",
                                                "Slider value changed to: $userAnswer"
                                            )
                                        }
                                    }
                                },
                                enabled = !isReadOnly ||isEditable,
                                valueRange = 0f..100f,
                                steps = 100,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (isReadOnly) colorResource(id = R.color.turquoise) else colorResource(id = R.color.turquoise),
                                    activeTrackColor = if (isReadOnly) colorResource(id = R.color.turquoise) else colorResource(id = R.color.turquoise),
                                    inactiveTrackColor = if (isReadOnly) Color.LightGray else Color.LightGray,
                                )
                            )

                            // Display the percentage text on the right side
                            Text(text = "${userAnswer.toInt()}%", modifier = Modifier.align(Alignment.End))
                        }
                    }


//@Composable
//fun PercentageSliderQuestion(
//    question: Question,
//    answers: MutableMap<Int, Any?> // Assuming answers is state-backed like mutableStateMapOf
//) {
//    // State for the slider value
//    var userAnswer by remember { mutableStateOf(answers[question.id]?.toString()?.toFloatOrNull() ?: 0f) }
//
//    // Retrieve dependencies for calculation
//    val previousAnswerDouble13 = answers[13]?.toString()?.toDoubleOrNull()
//    val previousAnswerDouble14 = answers[14]?.toString()?.toDoubleOrNull()
//    Log.d("currentAnswer", "previousAnswerDouble3 $previousAnswerDouble14")
//
//    // Determine if the slider should be read-only
//    val isReadOnly = question.id == 15
//
//    // Perform calculation when dependencies change
//    LaunchedEffect(previousAnswerDouble13, previousAnswerDouble14) {
//        if (question.id == 15 && previousAnswerDouble13 != null && previousAnswerDouble14 != null && previousAnswerDouble14 > 0) {
//            val calculatedValue = (previousAnswerDouble13 / previousAnswerDouble14) * 100
//            userAnswer = calculatedValue.toFloat() // Update the slider value
//            answers[question.id] = userAnswer // Update the backing state
//        }
//    }
//
//    // UI Components
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        // Question Text
//        Text(text = question.question)
//
//        // Slider
//        Slider(
//            value = userAnswer,
//            onValueChange = { newValue ->
//                if (!isReadOnly) {
//                    userAnswer = newValue // Update slider state
//                    answers[question.id] = userAnswer // Update the backing state
//                }
//            },
//            enabled = !isReadOnly,
//            valueRange = 0f..100f,
//            steps = 100,
//            colors = SliderDefaults.colors(
//                thumbColor = if (isReadOnly) colorResource(id = R.color.turquoise) else colorResource(id = R.color.turquoise),
//                activeTrackColor = if (isReadOnly) colorResource(id = R.color.turquoise) else colorResource(id = R.color.turquoise),
//                inactiveTrackColor = Color.LightGray
//            )
//        )
//
//        // Display the percentage value
//        Text(text = "${userAnswer.toInt()}%", modifier = Modifier.align(Alignment.End))
//    }
//}


                    //@RequiresApi(Build.VERSION_CODES.N)
//@Composable
//fun YesNoQuestion(
//    question: Question,
//    answers: MutableMap<Int, Any?>,
//
//) {
//    val savedAnswer = answers[question.id]?.toString() ?: ""
//    var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Text(text = question.question)
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            RadioButton(
//                selected = userAnswer == "yes", onClick = {
//                    userAnswer = "yes"
//                    if (answers[question.id].toString() != "null") {
//                        answers[question.id] = userAnswer
//                    } else {
//                        answers[question.id] = userAnswer
//                    }
//                }, colors = RadioButtonDefaults.colors(
//                    selectedColor = colorResource(id = R.color.turquoise)
//                )
//            )
//            Text(text = "Yes")
//            Spacer(modifier = Modifier.width(16.dp))
//            RadioButton(
//                selected = userAnswer == "no", onClick = {
//                    userAnswer = "no"
//
//                    answers.put(question.id, userAnswer)
//                }, colors = RadioButtonDefaults.colors(
//                    selectedColor = colorResource(id = R.color.turquoise)
//                )
//            )
//            Text(text = "No")
//        }
//    }
//}
                    @RequiresApi(Build.VERSION_CODES.N)
                    @Composable
                    fun YesNoQuestion(
                        question: Question,
                        answers: MutableMap<Int, Any?>,
                        onAnswerChange: (String) -> Unit, // Notify parent of the selected answer
                        shouldShow: Boolean, // Determines if the question should be displayed
                        isEditable: Boolean
                    ) {
                        if (!shouldShow) return // Do not render the question if it shouldn't be displayed

                        val context = LocalContext.current
                        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                        val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
                        val savedAnswer = answers[question.id]?.toString() ?: ""
                        var userAnswer by remember(savedAnswer) { mutableStateOf(savedAnswer) }


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = question.question)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // "Yes" Button
                                RadioButton(
                                    selected = userAnswer == "yes",
                                    onClick = {
                                        if(isEditable) {
                                            userAnswer = "yes"
                                            answers[question.id] = userAnswer
                                            onAnswerChange(userAnswer) // Notify parent
                                            Log.d(
                                                "YesNoQuestion",
                                                "Updated answer to Question 13: $userAnswer"
                                            )
                                        }
                                    },
                                    enabled = isEditable,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(id = R.color.turquoise)
                                    )
                                )
                                Text(text = if (selectedLanguage == "English") {"Yes" } else{ "Yego"})

                                Spacer(modifier = Modifier.width(16.dp))

                                // "No" Button
                                RadioButton(
                                    selected = userAnswer == "no",
                                    onClick = {
                                        if(isEditable) {
                                            userAnswer = "no"
                                            answers[question.id] = userAnswer
                                            onAnswerChange(userAnswer) // Notify parent
                                        }
                                    },
                                    enabled = isEditable,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorResource(id = R.color.turquoise)
                                    )
                                )
                                Text(text = if (selectedLanguage == "English") {"No" } else{ "Oya"})
                            }
                        }
                    }

@Composable
@RequiresApi(Build.VERSION_CODES.N)
//fun CheckboxQuestion(
//    question: Question,
//    answers: MutableMap<Int, Any?>
//) {
//    val context = LocalContext.current
//    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
//    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
//    val selectedOptions = remember { mutableStateMapOf<String, Boolean>() }
//
//
//
//
//
//    val translationMap = mapOf(
//        "Cashbook" to if (selectedLanguage == "Kinyarwanda") "Igitabo Cyinjizwamo Amafaranga" else "Cashbook",
//        "Cherry book/ cherry collection payment " to if (selectedLanguage == "Kinyarwanda") "Igitabo Cyandikwamo Ikawa yakiriwe " else "Cherry book/ cherry collection payment ",
//        "Parchment book" to if (selectedLanguage == "Kinyarwanda")  "Igitabo Cyandikwamo Ikawa yamaganda" else "Parchment book",
//        "Labour record book" to if (selectedLanguage == "Kinyarwanda") "Igitabo cy'imibyizi ya abakozi" else  "Labour record book"
//    )
//
//    // Unified function to normalize the saved answers into a consistent format
//    fun parseSavedSelections(value: Any?): Map<String, Boolean> = when (value) {
//        is Map<*, *> -> value.mapNotNull { (key, value) ->
//            if (key is String) {
//                // Handle both raw string and toString() representation of the map entry
//                val cleanKey = key.trim().lowercase().removeSurrounding("{", "}")
//                cleanKey to (value as? Boolean ?: false)
//            } else null
//        }.toMap()
//        is String -> {
//            // Handle string format: remove curly braces and split on comma
//            value.removeSurrounding("{", "}")
//                .split(",")
//                .associate {
//                    val parts = it.split("=")
//                    val option = parts[0].trim().lowercase()
//                    val isChecked = parts.getOrNull(1)?.trim()?.toBoolean() ?: false
//                    option to isChecked
//                }
//        }
//        else -> emptyMap()
//    }
//
//    // Debug function to log state
//    fun logState(tag: String, data: Any?) {
//        Log.d("CheckboxQuestion", "$tag: $data")
//    }
//
//    // Sync selected options with saved answers
//    LaunchedEffect(answers[question.id]) {
//        val savedAnswer = answers[question.id]
//        logState("Loading saved answer", savedAnswer)
//
//        val savedSelections = parseSavedSelections(savedAnswer)
//        logState("Parsed selections", savedSelections)
//
//        // Initialize all options with saved values or defaults
//        question.options?.forEach { option ->
//            val normalizedOption = option.trim().lowercase()
//            val savedValue = savedSelections[normalizedOption] ?: false
//            selectedOptions[normalizedOption] = savedValue
//            logState("Setting option", "$normalizedOption = $savedValue")
//        }
//
//        // Ensure the answers map has the correct format
//        val updatedSelections = selectedOptions.toMap()
//        answers[question.id] = updatedSelections
//        logState("Updated answers", updatedSelections)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Text(
//            text = question.question,
//            style = MaterialTheme.typography.bodySmall,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        question.options?.forEach { option ->
//            val normalizedOption = option.trim().lowercase()
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Checkbox(
//                    checked = selectedOptions[normalizedOption] == true,
//                    onCheckedChange = { isChecked ->
//                        selectedOptions[normalizedOption] = isChecked
//                        // Update answers with the new map
//                        val updatedSelections = selectedOptions.toMap()
//                        answers[question.id] = updatedSelections
//                        logState("Checkbox changed", "Option: $option, Checked: $isChecked")
//                        logState("Updated answers", updatedSelections)
//                    },
//                    colors = CheckboxDefaults.colors(
//                        checkedColor = colorResource(id = R.color.turquoise)
//                    )
//                )
//                Text(
//                    text = normalizedOption,
//                    modifier = Modifier
//                        .padding(start = 8.dp)
//                        .weight(1f)
//                )
//            }
//        }
//    }
//}


fun CheckboxQuestion(
    question: Question,
    answers: MutableMap<Int, Any?>,
    isEditable: Boolean
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
    val selectedOptions = remember { mutableStateMapOf<String, Boolean>() }

    val translations = mapOf(

       27 to mapOf(
           "a copy of the Law governing cooperatives in Rwanda" to "kopi y’Itegeko rigenga amakoperative mu Rwanda",
        "its bylaws" to "amategeko shingiro yayo",
        "the code of conduct for leaders and staff of the cooperative" to "amategeko agenga imyitwarire y’abayobozi ba koperative n’abakozi bayo",
        "internal rules and regulations" to "amategeko ngengamikorere",
        "a certificate of legal personality" to  " icyemezo cy’ ubuzima gatozi",
        "a register of members and their shares" to "igitabo cyandikwamo abanyamuryango n’imigabane yabo"),
        29 to mapOf(
        "Cashbook" to "Igitabo Cyinjizwamo Amafaranga",
        "Cherry book/ cherry collection payment " to "Igitabo Cyandikwamo Ikawa yakiriwe ",
        "Parchment book" to  "Igitabo Cyandikwamo Ikawa yamaganda",
        "Labour record book" to "Igitabo cy'imibyizi ya abakozi")



    )
    val translationMap = translations[question.id] ?: emptyMap()

//    val translationMap = mapOf(
//        "Cashbook" to if (selectedLanguage == "Kinyarwanda") "Igitabo Cyinjizwamo Amafaranga" else "Cashbook",
//        "Cherry book/ cherry collection payment " to if (selectedLanguage == "Kinyarwanda") "Igitabo Cyandikwamo Ikawa yakiriwe " else "Cherry book/ cherry collection payment ",
//        "Parchment book" to if (selectedLanguage == "Kinyarwanda")  "Igitabo Cyandikwamo Ikawa yamaganda" else "Parchment book",
//        "Labour record book" to if (selectedLanguage == "Kinyarwanda") "Igitabo cy'imibyizi ya abakozi" else  "Labour record book"
//    )

    val reverseTranslationMap = translationMap.entries.associate { (key, value) -> value to key }
    val displayMap = if (selectedLanguage == "Kinyarwanda") translationMap else reverseTranslationMap

    fun parseSavedSelections(value: Any?): Map<String, Boolean> = when (value) {
        is Map<*, *> -> value.mapNotNull { (key, value) ->
            if (key is String) {
                val cleanKey = key.trim().lowercase().removeSurrounding("{", "}")
                cleanKey to (value as? Boolean ?: false)
            } else null
        }.toMap()
        is String -> {
            value.removeSurrounding("{", "}")
                .split(",")
                .associate {
                    val parts = it.split("=")
                    val option = parts[0].trim().lowercase()
                    val isChecked = parts.getOrNull(1)?.trim()?.toBoolean() ?: false
                    option to isChecked
                }
        }
        else -> emptyMap()
    }

    LaunchedEffect(answers[question.id]) {
        val savedAnswer = answers[question.id]
        val savedSelections = parseSavedSelections(savedAnswer)
        question.options?.forEach { option ->
            val key = if (selectedLanguage == "Kinyarwanda") reverseTranslationMap[option] else option
            val normalizedOption = key?.trim()?.lowercase()?:""
            val savedValue = savedSelections[normalizedOption] ?: false
            selectedOptions[normalizedOption] = savedValue
        }
        answers[question.id] = selectedOptions.toMap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = question.question,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        question.options?.forEach { option ->
            val key = if (selectedLanguage == "Kinyarwanda") reverseTranslationMap[option] else option
            val normalizedOption = key?.trim()?.lowercase()
            val translatedOption = displayMap[key ?: option] ?: option

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedOptions[normalizedOption] == true,
                    onCheckedChange = { isChecked ->
                        if(isEditable) {
                            selectedOptions[key ?: option] = isChecked
                            answers[question.id] = selectedOptions.toMap()
                        }
                    },
                    enabled = isEditable,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(id = R.color.turquoise)
                    )
                )
                Text(
                    text = translatedOption,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }
        }
    }
}











//fun CheckboxQuestion(
//    question: Question,
//    answers: MutableMap<Int, Any?>,
//) {
//    // Retrieve saved selections, ensuring valid keys and values
//    val savedSelections = when (val value = answers[question.id]) {
//        is Map<*, *> -> value.mapNotNull { (key, value) ->
//            if (key is String && value is Boolean) key to value
//            else null
//        }.toMap()
//        else -> emptyMap()
//    }
//
//    // Initialize selected options with default states
//    val selectedOptions = remember(savedSelections) {
//        mutableStateMapOf<String, Boolean>().apply {
//            question.options?.forEach { option ->
//                this[option] = savedSelections[option] == true
//            }
//        }
//    }
//
//    // UI for the checkbox question
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Text(text = question.question)
//
//        // Render checkboxes for each option
//        question.options?.forEach { option ->
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Checkbox(
//                    checked = selectedOptions[option] == true,
//                    onCheckedChange = { isChecked ->
//                        selectedOptions[option] = isChecked
//
//                        // Update answers with a snapshot of selectedOptions
//                        answers[question.id] = selectedOptions.toMap()
//                    },
//                    colors = CheckboxDefaults.colors(checkedColor = colorResource(id = R.color.turquoise))
//                )
//                Text(text = option)
//            }
//        }
//    }
//}



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

                    fun loadCategoriesAndQuestion(context: Context,language: String): List<Category> {
                        val fileName = if (language == "Kinyarwanda") "data_kn.json" else "data_en.json"
                        val json = loadJsonFromAssets(context, fileName)
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
                                    val selected =  when (val value = answers[question.id]) {
                                        is Map<*, *> -> value.mapNotNull { (key, value) ->
                                            if (key is String) key.trim() to (value as? Boolean ?: false) else null
                                        }.toMap()

                                        is String -> value.split(",").associate {
                                            val parts = it.split("=")
                                            val option = parts[0].trim()
                                            val isChecked = parts.getOrNull(1)?.toBoolean() ?: false
                                            option to isChecked
                                        }

                                        else -> emptyMap()
                                    }
                                    Log.d("selected", "areAllQuestionsAnswered:${selected} ")
                                    selected?.isNotEmpty() == true

                                }

                                else -> !answers[question.id].toString()
                                    .isBlank() && answers[question.id].toString() != "null"
                            }
                        }
                    }


                    fun showCustomToast(context: Context, message: String, durationInMillis: Long) {
                        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        val handler = Handler(Looper.getMainLooper())
                        val startTime = System.currentTimeMillis()

                        handler.post(object : Runnable {
                            override fun run() {
                                if (System.currentTimeMillis() - startTime < durationInMillis) {
                                    toast.show()
                                    handler.postDelayed(this, 10000) // Repeat every 2 seconds
                                }
                            }
                        })
                    }


@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    contentHeight: Dp, // The actual scrollable content height
    containerHeight: Dp // The visible container height
) {
    val isScrollable = scrollState.maxValue > 0
    if (isScrollable) {
        val scrollRatio = scrollState.value.toFloat() / scrollState.maxValue.toFloat()

        // Outer scrollbar height is the container height (not full screen)
        val scrollbarHeight = containerHeight

        // Thumb size adapts to content, ensuring visibility
        val thumbHeight = (containerHeight * (containerHeight / contentHeight))
            .coerceIn(20.dp, containerHeight * 0.7f) // Min 20.dp, max 50% of the container

        // Thumb position is constrained within scrollbar bounds
        val thumbOffset = with(LocalDensity.current) {
            val maxOffsetPx = (containerHeight.toPx() - thumbHeight.toPx()).coerceAtLeast(0f) // Ensure non-negative
            (scrollRatio * maxOffsetPx).coerceIn(0f, maxOffsetPx).toDp() // Convert final value to Dp


        }

        Box(
            modifier = modifier
                .width(8.dp)
                .height(scrollbarHeight) // Outer scrollbar fits the container
                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight) // Adaptive thumb size
                    .offset(y = with(LocalDensity.current) { thumbOffset }) // Prevents overflow
                    .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}





//@Composable
//fun VerticalScrollbar(
//    modifier: Modifier = Modifier,
//    scrollState: ScrollState
//) {
//    val isScrollable = scrollState.maxValue > 0
//    if (isScrollable) {
//        val scrollRatio = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
//        Box(
//            modifier = modifier
//                .width(8.dp)
//                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
//                .height(1000.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp) // Scrollbar thumb size
//                    .align(Alignment.TopStart)
//                    .offset(y = with(LocalDensity.current) { (scrollRatio * 300).dp }) // Adjust dynamically
//                    .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
//            )
//        }
//    }
//}





