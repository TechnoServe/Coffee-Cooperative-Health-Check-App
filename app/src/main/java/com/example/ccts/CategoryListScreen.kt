package com.example.ccts

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Category
import com.example.ccts.data.Cooperative
import com.example.ccts.data.Question
import com.example.ccts.data.Survey
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.time.Instant
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    navController: NavHostController, viewModel: AnswersViewModel, survey: Survey
) {
    var cooperatives by remember { mutableStateOf(listOf<Cooperative>()) }
    val categories = remember { mutableStateListOf<Category>() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var submitEnabled by remember { mutableStateOf(false) }
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    val today = LocalDate.now()
    val surveyDate =
        Instant.ofEpochMilli(survey.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    var scorePercentage by remember { mutableDoubleStateOf(0.00) }
    var score by remember { mutableDoubleStateOf(0.00) }
    val answersState = remember { mutableStateMapOf<Int, Any?>() }
    val oldAnswersState = mutableMapOf<Int, Any?>()
    var showPopup by remember { mutableStateOf(false) }

    val savedAnswers by viewModel.getAnswersForSurvey(survey.surveyId)
        .observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        cooperatives = db.coopDao().getAllCooperative()

        val answersMap = db.surveyAnswerDao().getAnswersWithNonNullText(survey.surveyId)
            .associateBy({ it.questionId }, { it.answerText })

        answersMap.forEach { (questionId, answerText: Any?) ->
            answersState[questionId] = answerText
        }

        // Load categories (e.g., from JSON)
        categories.addAll(loadCategoriesAndQuestion(context))

        // Merge answers from SharedPreferences and Database into `answersState`
        categories.forEach { category ->
            category.questions.forEach { question ->
                val dbAnswer = savedAnswers.firstOrNull { it.questionId == question.id }?.answerText
                answersState[question.id] = dbAnswer
                oldAnswersState[question.id] = dbAnswer
            }
        }

        generateScore(categories,
            answersState,
            score,
            onScoreChange = { score = it },
            scorePercentage,
            onPercentageChange = { scorePercentage = it })
    }

    LaunchedEffect(answersState, showPopup) {
        submitEnabled = categories.any { category ->
            areAllQuestionsAnswered(category.questions, answersState)
        }
    }

    LaunchedEffect(showPopup) {
        if (!showPopup) {
//            update score
            generateScore(categories,
                answersState,
                score,
                onScoreChange = { score = it

                },
                scorePercentage,
                onPercentageChange = { scorePercentage = it })
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "CCTS CHECKLIST", style = TextStyle(
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Respondent Name: ${survey.respondentName}",
                style = TextStyle(fontSize = 16.sp, color = Color.Black),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)

                    .background(Color.LightGray),

                ) {
                Text(
                    text = "Cooperative Name: ${survey.cooperativeName}",
                    style = TextStyle(fontSize = 16.sp, color = Color.Black),
                    modifier = Modifier.padding(16.dp)

                )
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
                    CategoryButtonEdit(
                        category,
                        navController,
                        showPopup,
                        onShowPopup = { showPopup = it },
                        answersState,
                    )
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
                                modifier = Modifier.size(60.dp), // Size of the circle
                                color = Color.LightGray, // Light background color
                                strokeWidth = 8.dp // Thickness of the circle
                            )

                            // Foreground circle (actual progress)
                            CircularProgressIndicator(
                                progress = (scorePercentage * 100).toFloat() / 100, // Progress percentage (e.g., 70% -> 0.7)
                                modifier = Modifier.size(60.dp), // Same size as background circle
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
                if (surveyDate.isEqual(today)) {
                    Button(
                        onClick = {
                            if (answersState.isNotEmpty()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    viewModel.updateAnswers(
                                        categories.toList(), answersState, survey.surveyId
                                    )
                                    viewModel.updateSurveyScore(survey.surveyId, scorePercentage)

                                    // Show success toast on the main thread
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Answers updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("cooperative_health")
                                    }
                                    submitEnabled = false
                                }
                            } else {
                                submitEnabled = false
                            }
                        },
                        enabled = submitEnabled,
                        modifier = Modifier
                            .height(50.dp)
                            .width(150.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (submitEnabled) colorResource(id = R.color.turquoise) else Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Submit", color = Color.White)
                    }
                }
            }
        }
    })
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SuspiciousIndentation")
@Composable
fun CategoryButtonEdit(
    category: Category,
    navController: NavHostController,
    showPopup: Boolean,
    onShowPopup: (Boolean) -> Unit,
    answersState: MutableMap<Int, Any?> = mutableMapOf(),
) {
    val selectedCategory = remember { mutableStateOf<Category?>(null) }
    val hasAnswersForCategory = hasSingleAnswer(answersState, category.questions)

    val backgroundColor =
        if (hasAnswersForCategory) colorResource(id = R.color.turquoise) else Color.White
    val context = LocalContext.current
    val jsonCategories = loadCategoriesAndQuestion(context)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(6.dp)
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
            .clickable {
                onShowPopup(true)
                selectedCategory.value = category
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val resourceId = jsonCategories.firstOrNull()?.let { jsonCategory ->
            LocalContext.current.resources.getIdentifier(
                jsonCategory.icon_path, // e.g., "icon_add"
                "drawable", LocalContext.current.packageName
            )
        } ?: 0

        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = "Add New cooperative",
            tint = colorResource(id = R.color.black),
            modifier = Modifier.padding(top = 30.dp)
        )


        Text(text = category.category, modifier = Modifier.padding(16.dp))

        ProgressedBarEdit()

        if (showPopup && selectedCategory.value == category) {
            Dialog(onDismissRequest = {
                onShowPopup(false)
                selectedCategory.value = null
            }) {
                PopupActivity(
                    navController,
                    selectedCategory.value!!.id,
                    onClose = {
                        onShowPopup(false)
                        selectedCategory.value = null
                    },
                    answersState,
                )
            }
        }
    }
}

fun hasSingleAnswer(
    answersState: MutableMap<Int, Any?>, questions: List<Question>
): Boolean {
    // Filter questions for the given category ID
    val categoryQuestionIds = questions.map { it.id }

    // Count the number of answered questions in the category
    val answeredCount = categoryQuestionIds.count { questionId ->
        answersState[questionId] != null && answersState[questionId] != "null"
    }

    return answeredCount > 0
}


fun generateScore(
    categories: List<Category>,
    answersState: MutableMap<Int, Any?>,
    score: Double,
    onScoreChange: (Double) -> Unit,
    scorePercentage: Double,
    onPercentageChange: (Double) -> Unit
) {
    var totalWeight = 0.0
    var scores = 0.0
    categories.forEach { category ->
        val categoryScore = calculateTotalScore(
            category, answersState.map {
                it.key to it.value
            }.toMap().toMutableMap()
        )

        scores = scores + categoryScore
        onScoreChange(scores)
        category.questions.forEach { question ->
            val questionWeight = question.weight.toDouble()
            totalWeight += questionWeight

            onPercentageChange(scores / totalWeight)
        }
    }
}

@Composable
fun ProgressedBarEdit() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(15.dp)
            .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
            .background(Color.Gray)
            .padding(bottom = 4.dp)

    )
}