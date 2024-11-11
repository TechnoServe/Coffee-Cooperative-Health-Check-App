package com.example.ccts

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Category
import com.example.ccts.data.CategoryDb
import com.example.ccts.data.Cooperative
import com.example.ccts.data.Survey
import com.example.ccts.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(navController: NavHostController, viewModel: AnswersViewModel, survey: Survey) {
//    var respondentName by remember { mutableStateOf(TextFieldValue()) }
//    var selectedCooperative by remember { mutableStateOf("Select cooperative") }
//    var expanded by remember { mutableStateOf(false) }
    var cooperatives by remember { mutableStateOf(listOf<Cooperative>()) }
    val categories = remember { mutableStateListOf<Category>() }


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)?.coopDao()
    //val surveyDao = AppDatabase.getDatabase(context)?.surveyCategoryDao()
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    // var showNameError by remember { mutableStateOf(false) }
    var submitEnabled by remember { mutableStateOf(sharedPreferences.all.isNotEmpty()) }
    val savedCategoryIds = sharedPreferences.all.keys.toSet()
    // val groupedAnswerId = UUID.randomUUID().toString()
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    val categoriesList by viewModel.allCategories.observeAsState(emptyList())
    val today = LocalDate.now()
    val surveyDate = Instant.ofEpochMilli(survey.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()


    // val hasAnswersForSurvey by viewModel.getFullSurveyData(survey.surveyId).collectAsState(initial = emptyList())

    // Transforming the list of answers into a set of question IDs for easy lookup
    // val answeredQuestionIds = hasAnswersForSurvey.map { it.questions }.toSet()


    // Load categories once
    LaunchedEffect(Unit) {
        cooperatives = db?.getAllCooperative() ?: emptyList()

        // Load categories from JSON or another source for view mode
        categories.addAll(loadCategoriesAndQuestion(context))
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
                    items(categoriesList) { category ->
                        val hasAnswersForCategory = viewModel.hasAnswersForCategory(survey.surveyId, category.categoryId).collectAsState(initial = false).value

                        val selectedCategory = categories.firstOrNull { it.id == category.categoryId }
                        selectedCategory?.questions?.forEach{ question ->
                            val prefixedCategoryId = "answer_${category.categoryId}_${question.id}"
                            val hasDataInPreferences = sharedPreferences.contains(prefixedCategoryId)
                            val hasAnswers = hasAnswersForCategory ||hasDataInPreferences
                            Log.d("savedCategoryIds", "savedCategoryIds: ${savedCategoryIds}")
                            CategoryButtonEdit(category, navController, survey.surveyId,hasAnswers)
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
                            Text(
                                text = survey.totalScore.toString(),
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                    if(surveyDate.isEqual(today)){
                    Button(
                        onClick = {
//                                val answersMap = sharedPreferences.all.mapKeys { entry ->
//                                    Log.d("Entry", entry.toString())
//                                    entry.key.split("_").last().toString() // Extract questionId from key
//                                }
                            val answersMap = sharedPreferences.all.mapKeys { entry ->
                                Log.d("Entry", entry.toString())

                                // Remove the "answer_" prefix and split by "_"
                                val keyParts = entry.key.removePrefix("answer_").split("_")

                                // Check if key has both categoryId and questionId
                                if (keyParts.size == 2) {
                                    val categoryId = keyParts[0]
                                    val questionId = keyParts[1]
                                    "$categoryId $questionId"
                                } else {
                                    Log.e("answersMap", "Invalid key format: ${entry.key}")
                                    entry.key // return the original key if format is invalid
                                }
                            }
                                .mapValues { it.value.toString() } // Convert values to String if needed


                            if (answersMap.isNotEmpty()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    Log.d("Update Answers", answersMap.toString())
                                    viewModel.updateAnswers(answersMap, survey.surveyId)
                                    // Clear shared preferences after saving answers
                                    sharedPreferences.edit().clear().apply()

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
        }
    )


}


@SuppressLint("SuspiciousIndentation")
@Composable
fun CategoryButtonEdit(category: CategoryDb, navController: NavHostController, surveyId: Int, hasAnswers:Boolean) {
    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White
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
                navController.navigate("category_detail/$surveyId/${category.categoryId}")
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val resourceId = jsonCategories.firstOrNull()?.let { jsonCategory ->
            LocalContext.current.resources.getIdentifier(
                jsonCategory.icon_path, // e.g., "icon_add"
                "drawable",
                LocalContext.current.packageName
            )
        } ?: 0

        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = "Add New cooperative",
            tint = colorResource(id = R.color.black),
            modifier = Modifier.padding(top = 30.dp)
        )


        Text(text = category.name, modifier = Modifier.padding(16.dp))

        ProgressedBarEdit()

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
