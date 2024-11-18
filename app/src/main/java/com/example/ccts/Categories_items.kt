package com.example.ccts

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Category
import com.example.ccts.data.CategoryDb
import com.example.ccts.data.Cooperative
import com.example.ccts.data.QuestionDb
import com.example.ccts.data.Survey
import com.example.ccts.data.SurveyAnswer
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Categories_items(navController: NavHostController) {
    var respondentName by remember { mutableStateOf("") }
    var toggleCoopFields by remember { mutableStateOf(true) }
    var coopName by remember { mutableStateOf("") }
    var selectedCooperative by remember { mutableStateOf("Select cooperative") }
    var expanded by remember { mutableStateOf(false) }
    var cooperatives by remember { mutableStateOf(listOf<Cooperative>()) }
    val categories = remember { mutableStateListOf<Category>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cooperativeDao = AppDatabase.getDatabase(context).coopDao()
    var submitEnabled by remember { mutableStateOf(false) }
    var usedToday by remember { mutableStateOf(false) }
    var scorePercentage by remember { mutableDoubleStateOf(0.00) }
    var score by remember { mutableDoubleStateOf(0.00) }
    var answers = remember { mutableStateMapOf<Int, Any?>() }
    var showPopup by remember { mutableStateOf(false) }

    // Load categories once
    LaunchedEffect(Unit) {
        cooperatives = cooperativeDao.getAllCooperative()
        categories.addAll(loadCategoriesAndQuestion(context))
    }

    LaunchedEffect(respondentName, selectedCooperative, answers, showPopup) {
        submitEnabled =
            respondentName.isNotEmpty() && selectedCooperative != "Select cooperative" && selectedCooperative.isNotEmpty() && categories.any { category ->
                areAllQuestionsAnswered(category.questions, answers)
            }
    }

    LaunchedEffect(showPopup) {
        if (!showPopup) {
//            update score
            var totalWeight = 0.0
            score = 0.0
            categories.forEach { category ->
                val categoryScore = calculateTotalScore(
                    category, answers
                )

                score += categoryScore
                category.questions.forEach { question ->
                    val questionWeight = question.weight.toDouble()
                    totalWeight += questionWeight

                    scorePercentage = score / totalWeight
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "CoopTrac CHECKLIST", style = TextStyle(
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("cooperative_health") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.turquoise))
        )
    }, content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(id = R.color.LightPink1)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(
                value = respondentName,
                onValueChange = { respondentName = it },
                isError = respondentName.isEmpty(),
                supportingText = { if (respondentName.isEmpty()) "Please enter your name" else null },
                label = { Text("Surveyor Name") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = TextFieldDefaults.colors(
                    cursorColor = Color.Black,
                    focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
                    unfocusedLabelColor = Color.Gray,    // Label color when not focused
                    focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
                    unfocusedIndicatorColor = Color.Gray // Border color when not focused
                )
            )

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (toggleCoopFields) {
                            Row(
                                modifier = Modifier
                                    .weight(.7f)
                                    .height(50.dp)
                                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { expanded = true },
                                verticalAlignment = Alignment.CenterVertically // Align the icon and text vertically
                            ) {
                                Text(
                                    text = selectedCooperative,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    color = Color.Black
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24), // Use your icon here
                                    contentDescription = "Dropdown Icon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp) // Size for the icon
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(.7f)
                                        .padding(16.dp)
                                ) {
                                    cooperatives.forEach { cooperative ->
                                        DropdownMenuItem(onClick = {
                                            selectedCooperative = cooperative.name
                                            expanded = false
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val count =
                                                    AppDatabase.getDatabase(context).surveyDao()
                                                        .checkIfCooperativeUsedToday(cooperative.name)
                                                withContext(Dispatchers.Main) {
                                                    usedToday = count > 0
                                                    if (usedToday) {
                                                        // Display Toast
                                                        Toast.makeText(
                                                            context,
                                                            "This cooperative has already been used today. Please select another.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }, text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = cooperative.name, color = Color.Black)
                                                // Check if this cooperative has been used today and display the icon
                                                if (AppDatabase.getDatabase(context).surveyDao()
                                                        .checkIfCooperativeUsedToday(cooperative.name) > 0
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.baseline_warning_amber_24),
                                                        contentDescription = "Warning: Cooperative Used Today",
                                                        tint = Color.Red,
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        })
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    toggleCoopFields = !toggleCoopFields
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(.2f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_add_24),
                                    contentDescription = "Add New Cooperative",
                                    tint = Color.White
                                )
                            }
                        } else {
                            TextField(
                                value = coopName,
                                onValueChange = { coopName = it },
                                label = { Text("Cooperative Name") },
                                modifier = Modifier.weight(.6f),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = colorResource(
                                        id = R.color.grey
                                    )
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                            )

                            Button(
                                onClick = {
                                    coopName = ""
                                    toggleCoopFields = !toggleCoopFields
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(.2f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_close_24),
                                    contentDescription = "Cancel Add Cooperative",
                                    tint = Color.White
                                )
                            }
                            Button(
                                onClick = {
                                    if (coopName.isNotBlank()) {
                                        coroutineScope.launch {
//                                    check if name exists regardless of case
                                            val db = AppDatabase.getDatabase(context).coopDao()
                                            val coopExists = db.getAllCooperative().any {
                                                it.name.equals(coopName, ignoreCase = true)
                                            }
                                            if (coopExists) {
                                                Toast.makeText(
                                                    context,
                                                    "Cooperative already exists",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val newCooperative = Cooperative(
                                                    name = coopName, ownerName = "", location = ""
                                                )
                                                db.insertCooperative(newCooperative)
                                                Toast.makeText(
                                                    context,
                                                    "Cooperative registered successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                coopName = ""
                                                cooperatives = cooperativeDao.getAllCooperative()
                                                toggleCoopFields = !toggleCoopFields
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context, "Name cannot be empty", Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(.2f)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_check_24),
                                    contentDescription = "Add New Cooperative",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // This specifies 2 buttons per row
                contentPadding = PaddingValues(6.dp),
                modifier = Modifier.weight(1f),
                state = rememberLazyGridState(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { category ->
                    CategoryButton(
                        category = category,
                        navController,
                        enabled = !usedToday,
                        showPopup,
                        onShowPopup = { showPopup = it },
                        answers = answers,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),

                    ) {

                    Text(text = "Score", color = Color.Black, fontSize = 16.sp)
                    // Score Indicator (Simulating with a CircularProgressIndicator)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Background circle (outer circle, full progress)
                            CircularProgressIndicator(
                                progress = 1f, // Full circle as background
                                modifier = Modifier
                                    .size(60.dp), // Size of the circle
                                color = Color.LightGray, // Light background color
                                strokeWidth = 8.dp // Thickness of the circle
                            )

                            // Foreground circle (actual progress)
                            CircularProgressIndicator(
                                progress = (scorePercentage * 100).toFloat() / 100, // Progress percentage (e.g., 70% -> 0.7)
                                modifier = Modifier
                                    .size(60.dp), // Same size as background circle
                                color = colorResource(id = R.color.turquoise), // Foreground color (e.g., turquoise)
                                strokeWidth = 8.dp // Thickness of the circle
                            )
                        }
                        Text(
                            text = "${(scorePercentage * 100).toInt()}%",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (respondentName.isEmpty()) {
                            Toast.makeText(
                                context, "Please enter your name", Toast.LENGTH_SHORT
                            ).show()
                        } else if (selectedCooperative == "Select cooperative" || selectedCooperative.isEmpty()) {
                            Toast.makeText(
                                context, "Please select a cooperative", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (answers.isNotEmpty()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val db = AppDatabase.getDatabase(context)

                                        // 1. Load categories and questions from JSON
                                        val jsonCategories = loadCategoriesAndQuestion(context)
                                        jsonCategories.flatMap { it.questions }
                                        // 2. Calculate the total score
                                        val totalScore = scorePercentage

                                        // Check if questions and categories have already been loaded
                                        if (db.surveyCategoryDao().getAllCategories().isEmpty()) {
                                            // Load categories and questions from JSON if they are not already in the DB
                                            val jsonCategories = loadCategoriesAndQuestion(context)
                                            jsonCategories.forEach { jsonCategory ->
                                                // Insert category if it doesn't exist
                                                val categoryId =
                                                    db.surveyCategoryDao().insertCategory(
                                                        CategoryDb(name = jsonCategory.category)
                                                    ).toInt()

                                                // Insert each question under the category
                                                jsonCategory.questions.forEach { jsonQuestion ->
                                                    val question = QuestionDb(
                                                        categoryId = categoryId,
                                                        questionText = jsonQuestion.question
                                                    )
                                                    db.surveyQuestionDao().insertQuestion(question)
                                                }
                                            }
                                        }

                                        // Create new survey
                                        val surveyId = db.surveyDao().insertSurvey(
                                            Survey(
                                                surveyTitle = "Survey to $selectedCooperative",
                                                respondentName = respondentName,
                                                cooperativeName = selectedCooperative,
                                                totalScore = totalScore,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        ).toInt()

                                        // Loop through categories and save answers
                                        db.surveyCategoryDao().getAllCategories()
                                            .forEach { category ->
                                                val categoryId = category.categoryId

                                                // Get all questions under this category
                                                db.surveyQuestionDao()
                                                    .getQuestionsForCategory(categoryId)
                                                    .forEach { question ->
                                                        val answerText =
                                                            answers[question.questionId]?.toString()
                                                                ?: ""

                                                        // Only insert answer if it's not empty
                                                        if (answerText.isNotEmpty()) {
                                                            val answer = SurveyAnswer(
                                                                surveyId = surveyId,
                                                                questionId = question.questionId,
                                                                //categoryId = categoryId,
                                                                answerText = answerText
                                                            )
                                                            db.surveyAnswerDao()
                                                                .insertAnswer(answer)
                                                        }
                                                    }
                                            }

                                        // Show success toast on the main thread
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Survey submitted successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("cooperative_health")
                                        }
                                        submitEnabled = false
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Error saving survey: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                submitEnabled = false
                                Toast.makeText(
                                    context, "No answers to submit", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    enabled = submitEnabled && !usedToday,
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (submitEnabled && !usedToday) colorResource(id = R.color.turquoise) else Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Submit", color = Color.White)
                }
            }
        }
    })
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun CategoryButton(
    category: Category,
    navController: NavHostController,
    enabled: Boolean = true,
    showPopup: Boolean,
    onShowPopup: (Boolean) -> Unit,
    answers: MutableMap<Int, Any?>
) {
    val selectedCategory = remember { mutableStateOf<Category?>(null) }

    val hasAnswers = hasSingleAnswer(answers, category.questions)
    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(6.dp)
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) {
                if (enabled) {
                    onShowPopup(true)
                    selectedCategory.value = category
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val resourceId = LocalContext.current.resources.getIdentifier(
            category.icon_path, // e.g., "icon_add"
            "drawable", LocalContext.current.packageName
        )

        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = "Add New cooperative",
            tint = colorResource(id = R.color.black),
            modifier = Modifier.padding(top = 30.dp)
        )

        Text(text = category.category)
        ProgressedBar()
        if (showPopup && selectedCategory.value == category) {
            Dialog(
                onDismissRequest = {
                    onShowPopup(false)
                    selectedCategory.value = null
                },
                properties = DialogProperties(dismissOnClickOutside = false)
            ) {
                PopupActivity(
                    navController = navController,
                    categoryId = selectedCategory.value!!.id,
                    onClose = {
                        onShowPopup(false)
                        selectedCategory.value = null
                    },
                    answers = answers
                )
            }
        }
    }

}

@Composable
fun ProgressedBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(15.dp)
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(Color.Gray)
            .padding(bottom = 4.dp)
    )
}