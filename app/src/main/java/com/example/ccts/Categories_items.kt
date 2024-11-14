package com.example.ccts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.ccts.data.Category

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Cooperative
import com.example.ccts.data.Survey
import com.example.ccts.data.CategoryDb
import com.example.ccts.data.QuestionDb
import com.example.ccts.data.SurveyAnswer
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Categories_items(navController: NavHostController) {
    var respondentName by remember { mutableStateOf(TextFieldValue()) }
    var selectedCooperative by remember { mutableStateOf("Select cooperative") }
    var expanded by remember { mutableStateOf(false) }
    var cooperatives by remember { mutableStateOf(listOf<Cooperative>()) }
    val categories = remember { mutableStateListOf<Category>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cooperativeDao = AppDatabase.getDatabase(context)?.coopDao()
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    var submitEnabled by remember { mutableStateOf(sharedPreferences.all.isNotEmpty()) }
    val savedCategoryIds = sharedPreferences.all.keys.toSet()
    var usedToday by remember { mutableStateOf(false) }
    var scorePercentage by remember { mutableStateOf(0.00) }
    var score by remember { mutableStateOf(0.00) }




    // Load categories once
    LaunchedEffect(Unit) {

        cooperatives = cooperativeDao?.getAllCooperative() ?: emptyList()
        val answersMap = sharedPreferences.all
        answersMap.forEach { (key, value) ->
            Log.d("SharedPreferences Key", "Key: $key, Value: $value")
        }
        var totalWeight = 0.0
        // Load categories from JSON or another source for view mode
        categories.clear()
        categories.addAll(loadCategoriesAndQuestion(context))
            categories.forEach { category ->
                val categoryScore = calculateTotalScore(category, sharedPreferences)


                score += categoryScore
                category.questions.forEach { question ->

                    val questionWeight = question.weight.toDouble() ?: 0.0
                    totalWeight += questionWeight



                    scorePercentage = score / totalWeight

                }


        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CCTS CHECKLIST",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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


                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id= R.color.turquoise))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorResource(id = R.color.LightPink1)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = respondentName,
                    onValueChange = { respondentName = it },
                    isError = respondentName.text.isBlank(),
                    label = { Text("Survey Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        cursorColor = Color.Black,
                        focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
                        unfocusedLabelColor = Color.Gray,    // Label color when not focused
                        focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
                        unfocusedIndicatorColor = Color.Gray // Border color when not focused
                    )
                )
                if (respondentName.text.isBlank()) {
                    Text("Please enter your name", color = Color.Red, fontSize = 12.sp , modifier = Modifier.padding(start = 16.dp))
                }

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .width(300.dp)
                                .height(50.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { expanded = true },
                            verticalAlignment = Alignment.CenterVertically // Align the icon and text vertically
                        ) {
                            Text(
                                text = selectedCooperative,
                                modifier = Modifier
                                    .weight(1f),
                                color = Color.Black
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24), // Use your icon here
                                contentDescription = "Dropdown Icon",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp) // Size for the icon
                            )
                        }


                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(300.dp)
                                .padding(16.dp)
                        ) {
                            cooperatives.forEach { cooperative ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCooperative = cooperative.name
                                        expanded = false
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val count = AppDatabase.getDatabase(context)?.surveyDao()?.checkIfCooperativeUsedToday(cooperative.name) ?: 0
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
                                    }, text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = cooperative.name, color = Color.Black)
                                        // Check if this cooperative has been used today and display the icon
                                        if ((AppDatabase.getDatabase(context)?.surveyDao()
                                                ?.checkIfCooperativeUsedToday(cooperative.name)
                                                ?: 0) > 0
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
                                    } })
                            }
                        }
                        Button(
                            onClick = { navController.navigate("register_coffee")},
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_24),
                                contentDescription = "Add New coperative",
                                tint = Color.White
                            )

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
                        category.questions?.forEach { question ->

                            val prefixedCategoryId = "answer_${category.id}_${question.id}"
                            val hasDataInPreferences = savedCategoryIds.contains(prefixedCategoryId)
                            Log.d("savedCategoryIds", "savedCategoryIds: ${savedCategoryIds}")
                            Log.d(
                                "CategoryCheck",
                                "Category ID: ${category.category}, Has Data: $hasDataInPreferences"
                            )
                            CategoryButton(category = category, navController, hasDataInPreferences,enabled = !usedToday)
                        }
                    }


                }


                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
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
                            CircularProgressIndicator(
                                progress =
                                0.7f // Assuming 70% score
                                ,
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(8.dp),
                                color = colorResource(id = R.color.turquoise),
                                strokeWidth = 8.dp,
                            )
                            Text(text = "${(scorePercentage * 100).toInt()}%", color = Color.Black, fontSize = 16.sp)
                            // Text(text = totalScore.toString(), color = Color.Black, fontSize = 16.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (respondentName.text.isBlank()) {
                                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                            } else if (selectedCooperative == "Select cooperative" || selectedCooperative.isEmpty()) {
                                Toast.makeText(context, "Please select a cooperative", Toast.LENGTH_SHORT).show()
                            } else {
                                val answersMap = sharedPreferences.all
                                if (answersMap.isNotEmpty()) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val db = AppDatabase.getDatabase(context)

                                            // 1. Load categories and questions from JSON
                                            val jsonCategories = loadCategoriesAndQuestion(context)
                                            val questions = jsonCategories.flatMap { it.questions }
                                            // 2. Calculate the total score
                                            val totalScore= scorePercentage

                                            // Check if questions and categories have already been loaded
                                            if (db.surveyCategoryDao().getAllCategories().isEmpty()) {
                                                // Load categories and questions from JSON if they are not already in the DB
                                                val jsonCategories = loadCategoriesAndQuestion(context)
                                                jsonCategories.forEach { jsonCategory ->
                                                    // Insert category if it doesn't exist
                                                    val categoryId = db.surveyCategoryDao().insertCategory(
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
                                                    respondentName = respondentName.text,
                                                    cooperativeName = selectedCooperative,
                                                    totalScore = totalScore,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            ).toInt()

                                            // Loop through categories and save answers
                                            db.surveyCategoryDao().getAllCategories().forEach { category ->
                                                val categoryId = category.categoryId

                                                // Get all questions under this category
                                                db.surveyQuestionDao().getQuestionsForCategory(categoryId).forEach { question ->
                                                    val answerKey = "answer_${categoryId}_${question.questionId}"
                                                    val answerText = answersMap[answerKey]?.toString() ?: ""

                                                    // Only insert answer if it's not empty
                                                    if (answerText.isNotEmpty()) {
                                                        val answer = SurveyAnswer(
                                                            surveyId = surveyId,
                                                            questionId = question.questionId,
                                                            //categoryId = categoryId,
                                                            answerText = answerText
                                                        )
                                                        db.surveyAnswerDao().insertAnswer(answer)
                                                    }
                                                }
                                            }

                                            // Clear shared preferences after saving answers
                                            sharedPreferences.edit().clear().apply()

                                            // Show success toast on the main thread
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Survey submitted successfully", Toast.LENGTH_SHORT).show()
                                                navController.navigate("cooperative_health")
                                            }
                                            submitEnabled = false

                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error saving survey: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                            Log.e("SurveySubmission", "Error saving survey", e)
                                        }
                                    }
                                } else {
                                    submitEnabled = false
                                    Toast.makeText(context, "No answers to submit", Toast.LENGTH_SHORT).show()
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
        }
    )


}

@Composable
fun CategoryButton(category: Category, navController: NavHostController,hasAnswers: Boolean,enabled: Boolean = true) {
    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(6.dp)
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { // Only navigate if the button is enabled
                if (enabled) {
                    // Navigate to the detailed view of the category
                    navController.navigate("popup_coffee/${category.id}")
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val resourceId = LocalContext.current.resources.getIdentifier(
            category.icon_path, // e.g., "icon_add"
            "drawable",
            LocalContext.current.packageName
        )

        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = "Add New cooperative",
            tint = colorResource(id = R.color.black),
            modifier = Modifier.padding(top =30.dp)
        )

        Text(text = category.category, modifier = Modifier.padding(16.dp))
        ProgressedBar()
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


