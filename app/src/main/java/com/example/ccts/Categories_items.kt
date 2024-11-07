package com.example.ccts

import android.app.Application
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory

import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Cooperative
import com.example.ccts.data.Survey
import com.example.ccts.data.SurveyCategory
import com.example.ccts.data.SurveyQuestion
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


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
    val surveyDao = AppDatabase.getDatabase(context)?.surveyDao()
    val surveyCategoryDao = AppDatabase.getDatabase(context)?.surveyCategoryDao()
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    var showNameError by remember { mutableStateOf(false) }
    var submitEnabled by remember { mutableStateOf(sharedPreferences.all.isNotEmpty()) }
    val savedCategoryIds = sharedPreferences.all.keys.toSet()
    val groupedAnswerId = UUID.randomUUID().toString()
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    var usedToday by remember { mutableStateOf(false) }
    var totalScore by remember { mutableStateOf(0) }
    var maxScore by remember { mutableStateOf(100) } // Example maximum score
    var scorePercentage by remember { mutableStateOf(0f) }
    val totalQuestions = categories.sumBy { it.questions.size }
    var answeredQuestions by remember { mutableStateOf(0) }


    // Load categories once
    LaunchedEffect(Unit) {
        cooperatives = cooperativeDao?.getAllCooperative() ?: emptyList()

        // Load categories from JSON or another source for view mode
        categories.addAll(loadCategoriesAndQuestion(context))
        categories.forEach { category ->
            val categoryScore = calculateTotalScore(category, sharedPreferences)
            scorePercentage += categoryScore
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
//                        .padding(10.dp),
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
                        }
                    }

                        Button(
                            onClick = {
                                if (respondentName.text.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Please enter your name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (selectedCooperative == "Select cooperative" || selectedCooperative.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Please select a cooperative",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                            val answersMap = sharedPreferences.all
                                            if (answersMap.isNotEmpty()) {
                                                coroutineScope.launch(Dispatchers.IO) {

                                                    val db = AppDatabase.getDatabase(context)
                                                    val surveyDao = db?.surveyDao()
                                                    val categoryDao = db?.surveyCategoryDao()
                                                    val questionDao = db?.surveyQuestionDao()


                                                    // 1. Insert a new survey and get the generated surveyId
                                                    val surveyId = surveyDao?.insertSurvey(
                                                        Survey(
                                                            surveyTitle = "Survey to ${selectedCooperative}",
                                                            respondentName = respondentName.text,
                                                            cooperativeName = selectedCooperative,
                                                            timestamp = System.currentTimeMillis()
                                                        )
                                                    )?.toInt() ?: 0

                                                    // 2. Load categories and questions from JSON
                                                    val jsonCategories =
                                                        loadCategoriesAndQuestion(context)
                                                    Log.d(
                                                        "jsonCategories",
                                                        "jsonCategories:$jsonCategories "
                                                    )

                                                    jsonCategories.forEach { jsonCategory ->
                                                        Log.d(
                                                            "SurveySubmission",
                                                            "Respondent Name: ${respondentName.text}, Cooperative: $selectedCooperative"
                                                        )

                                                        // Insert the category and use the categoryId from JSON
                                                        val categoryId =
                                                            categoryDao?.insertCategory(
                                                                SurveyCategory(
                                                                    surveyId = surveyId,
                                                                    categoryId = jsonCategory.id, // Use JSON's categoryId
                                                                    categoryName = jsonCategory.category

                                                                )
                                                            )?.toInt()
                                                                ?: jsonCategory.id // Fallback to JSON id

                                                        jsonCategory.questions.forEach { jsonQuestion ->

                                                            val answerKey =
                                                                "answer_${jsonCategory.id}_${jsonQuestion.id}"
                                                            val answerValue =
                                                                answersMap[answerKey]?.toString()
                                                                    ?: ""


                                                            Log.d(
                                                                "answerText",
                                                                "answerText:$answerValue "
                                                            )

                                                            // Insert question with answer using JSON's questionId
                                                            questionDao?.insertQuestion(
                                                                SurveyQuestion(

                                                                    surveyId = surveyId,
                                                                    categoryId = categoryId,
                                                                    questionId = jsonQuestion.id, // Use JSON's questionId
                                                                    questionText = jsonQuestion.question,
                                                                    answerText = answerValue ?: ""
                                                                )
                                                            )
                                                        }
                                                    }


                                                    // Clear shared preferences after saving answers
                                                    sharedPreferences.edit().clear().apply()

                                                    // Show success toast on the main thread
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Answers saved successfully",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navController.navigate("cooperative_health")
                                                    }
                                                    submitEnabled = false
                                                }
                                            } else {
                                                submitEnabled = false
                                            }
//                                        }
//                                    }
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
//@Composable
//fun CategoryButton(category: Category, navController: NavHostController) {
//    Button(
//        onClick = { navController.navigate("popup_coffee/${category.id}") },
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .height(200.dp)
//            .background(colorResource(id = R.color.white), shape = RoundedCornerShape(8.dp)),
//        colors= ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise))
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//
//        ) {
//            Text(text = category.category, modifier = Modifier.padding(16.dp))
//            ProgressedBar()
//        }
//    }
//}

@Composable
fun CategoryButton(category: Category, navController: NavHostController,hasAnswers: Boolean,enabled: Boolean = true) {
    val context = LocalContext.current
    val answers = remember { mutableStateMapOf<String, Any>() }
    var categoryScore by remember { mutableStateOf(0) }
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)

    // Load answers and calculate category score
    LaunchedEffect(Unit) {
        category.questions?.forEach { question ->
            val savedAnswer = getAnswerFromSharedPreferences(context, category.id, question)
            answers.putAll(savedAnswer)
        }
        categoryScore = calculateTotalScore(category, sharedPreferences )
    }
    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White
    Text(
        text = "Score: $categoryScore",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Blue,
        modifier = Modifier.padding(4.dp)
    )
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


