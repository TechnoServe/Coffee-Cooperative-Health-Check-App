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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Category
import com.example.ccts.data.Cooperative
import com.example.ccts.data.Survey
import com.example.ccts.data.SurveyCategory
import com.example.ccts.data.SurveyQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import java.time.ZoneId
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(navController: NavHostController, viewModel: AnswersViewModel, survey: Survey) {
    var respondentName by remember { mutableStateOf(TextFieldValue()) }
    var selectedCooperative by remember { mutableStateOf("Select cooperative") }
    var expanded by remember { mutableStateOf(false) }
    var cooperatives by remember { mutableStateOf(listOf<Cooperative>()) }
    val categories = remember { mutableStateListOf<Category>() }


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)?.coopDao()
    val surveyDao = AppDatabase.getDatabase(context)?.surveyCategoryDao()
    val sharedPreferences = context.getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
    var showNameError by remember { mutableStateOf(false) }
    var submitEnabled by remember { mutableStateOf(sharedPreferences.all.isNotEmpty()) }
    val savedCategoryIds = sharedPreferences.all.keys.toSet()
    val groupedAnswerId = UUID.randomUUID().toString()
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    val categoriesList by viewModel.getCategoriesForSurvey(survey.surveyId).collectAsState(initial = emptyList())
    val hasAnswersForSurvey by viewModel.getAnswersForSurvey(survey.surveyId).collectAsState(initial = emptyList())
    val today = LocalDate.now()
    // Convert survey timestamp to LocalDate
    val surveyDate = Instant.ofEpochMilli(survey.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()


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

                    // Cooperative Name


//                TextField(
//                    value = respondentName,
//                    onValueChange = { respondentName = it },
//                    isError = respondentName.text.isBlank(),
//                    label = { Text("Respondent Name") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    colors = TextFieldDefaults.textFieldColors(
//                        cursorColor = Color.Black,
//                        focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
//                        unfocusedLabelColor = Color.Gray,    // Label color when not focused
//                        focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
//                        unfocusedIndicatorColor = Color.Gray // Border color when not focused
//                    )
//                )
//                if (respondentName.text.isBlank()) {
//                    Text("Please enter your name", color = Color.Red, fontSize = 12.sp , modifier = Modifier.padding(start = 16.dp))
//                }

//                Box {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(10.dp),
//                        horizontalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .width(300.dp)
//                                .height(50.dp)
//                                .background(Color.LightGray, RoundedCornerShape(8.dp))
//                                .clickable { expanded = true },
//                            verticalAlignment = Alignment.CenterVertically // Align the icon and text vertically
//                        ) {
//                            Text(
//                                text = selectedCooperative,
//                                modifier = Modifier
//                                    .weight(1f),
//                                color = Color.Black
//                            )
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_arrow_drop_down_24), // Use your icon here
//                                contentDescription = "Dropdown Icon",
//                                tint = Color.Black,
//                                modifier = Modifier.size(24.dp) // Size for the icon
//                            )
//                        }
//
//
//                        androidx.compose.material3.DropdownMenu(
//                            expanded = expanded,
//                            onDismissRequest = { expanded = false },
//                            modifier = Modifier
//                                .width(300.dp)
//                                .padding(16.dp)
//                        ) {
//                            cooperatives.forEach { cooperative ->
//                                DropdownMenuItem(
//                                    onClick = {
//                                        selectedCooperative = cooperative.name
//                                        expanded = false
//                                    }, text = { Text(text = cooperative.name) })
//                            }
//                        }
//                        Button(
//                            onClick = { navController.navigate("register_coffee")},
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
//                            modifier = Modifier.height(48.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_add_24),
//                                contentDescription = "Add New coperative",
//                                tint = Color.White
//                            )
//
//                        }
//                    }
//
//                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // This specifies 2 buttons per row
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier.weight(1f),
//                        .padding(10.dp),
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
                            Text(text = "70%", color = Color.Black, fontSize = 16.sp)
                        }
                    }
                    if (surveyDate.isEqual(today)) {
                        Log.d("surveyDate", "surveyDate:$surveyDate ")
                        Log.d("today", "today:$today ")

                        Button(
                            onClick = {
//                                val answersMap = sharedPreferences.all
                                val answersMap = sharedPreferences.all.mapKeys { entry ->
                                    entry.key.split("_").last()
                                        .toString() // Extract questionId from key
                                }
                                if (answersMap.isNotEmpty()) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.upsertAnswer(answersMap)


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
                    } else {
                        // Show alternative UI element when submit button shouldn't appear
                        Text(text = "No submissions allowed", color = Color.Gray)
                    }



                }

            }
        }
    )


}


@SuppressLint("SuspiciousIndentation")
@Composable
fun CategoryButtonEdit(category: SurveyCategory, navController: NavHostController, surveyId: Int, hasAnswers:Boolean) {
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


            Text(text = category.categoryName, modifier = Modifier.padding(16.dp))

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
