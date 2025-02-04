package com.technoserve.cooptrac

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cooptrac.PopupActivity
import com.example.cooptrac.areAllQuestionsAnswered
import com.example.cooptrac.loadCategoriesAndQuestion
import com.example.cooptrac.showCustomToast

import com.technoserve.cooptrac.R
import com.technoserve.cooptrac.data.AnswersViewModel
import com.technoserve.cooptrac.data.AnswersViewModelFactory
import com.technoserve.cooptrac.data.AppDatabase
import com.technoserve.cooptrac.data.Category
import com.technoserve.cooptrac.data.CategoryDb
import com.technoserve.cooptrac.data.Cooperative
import com.technoserve.cooptrac.data.QuestionDb
import com.technoserve.cooptrac.data.Survey
import com.technoserve.cooptrac.data.SurveyAnswer
import com.technoserve.cooptrac.data.calculateTotalScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Categories_items(navController: NavHostController,viewModel: AnswersViewModel) {
    var respondentName by remember { mutableStateOf("") }
    var Comment by remember { mutableStateOf("") }
    var toggleCoopFields by remember { mutableStateOf(true) }
    var coopName by remember { mutableStateOf("") }

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
//    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"
    var selectedCooperative by remember { mutableStateOf(if (selectedLanguage == "English") "Select cooperative" else "Hitamo koperative") }

    // Load categories once
    LaunchedEffect(Unit) {
        cooperatives = cooperativeDao.getAllCooperative()
        categories.clear()
        categories.addAll(loadCategoriesAndQuestion(context,selectedLanguage))
    }






    LaunchedEffect(respondentName, selectedCooperative, answers, showPopup) {
        Log.d("Debug", "answer: $answers")
        submitEnabled =
            respondentName.isNotEmpty() && selectedCooperative != if (selectedLanguage == "English") {"Select cooperative" } else{ "Hitamo koperative"} && selectedCooperative.isNotEmpty() && categories.any { category ->
                Log.d("answers", "depenanswers: $answers")

                areAllQuestionsAnswered(category.questions, answers)
            }
        Log.d("submitEnabled", " answer: $submitEnabled")
    }

    LaunchedEffect(showPopup) {
        if (!showPopup) {
//            update score
            var totalWeight = 0.0
            score = 0.0
            categories.forEach { category ->
                val categoryScore = calculateTotalScore(context,
                    category, answers
                )

                score += categoryScore.first
                category.questions.forEach { question ->
                    val questionWeight = question.weight.toDouble()
                    totalWeight += questionWeight

                    scorePercentage = (score / totalWeight)
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = if(selectedLanguage=="English"){"CoopTrac CHECKLIST"}else{"Igenzura rya CoopTrac "}, style = TextStyle(
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
    },
        content = {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(id = R.color.LightPink1)),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            TextField(
                value = respondentName,
                onValueChange = { respondentName = it },
                isError = respondentName.isEmpty(),
                supportingText = { if (respondentName.isEmpty()) {if (selectedLanguage == "English") "Please enter your name" else "Andika izina ryawe" } else null },
                label = { Text(
                    if (selectedLanguage == "English") "Surveyor Name" else "Izina") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 0.dp),
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
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
//                    Row(
//                        modifier = Modifier.padding(10.dp)
//                    ) {
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
                                        .padding(horizontal = 10.dp),
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
                                                        showCustomToast(context, if(selectedLanguage=="English"){"This cooperative has already been surveyed today. Please select another."}else{"Iyi koperative yamaze gukoreshwa uyu munsi. Nyabuneka hitamo undi"}, 10000)
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
                                                            .size(20.dp)
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
                        }
                        else {
                            TextField(
                                value = coopName,
                                onValueChange = { coopName = it },
                                label = { Text(if (selectedLanguage == "English") "Cooperative Name" else "Izina rya koperative"
                                    ) },
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
                                                    if(selectedLanguage=="English"){"Cooperative registered successfully!"}else{ "koperative yiyongeyemo neza"},
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                coopName = ""
                                                cooperatives = cooperativeDao.getAllCooperative()
                                                toggleCoopFields = !toggleCoopFields
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context, if(selectedLanguage=="English"){"Name cannot be empty"}else{"Izina ntirishobora kuba ubusa"}, Toast.LENGTH_SHORT
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
            val minHeightForTwoRows = 500.dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // This specifies 2 buttons per row
                contentPadding = PaddingValues(6.dp),
                modifier = Modifier.weight(1f)
                    .heightIn(min = minHeightForTwoRows),
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
                        viewModel
                    )
                }
            }

            Column (
                modifier = Modifier
                    .imePadding() // Automatically adjusts padding when the keyboard appears
                    .fillMaxWidth()
            ){
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    // Get device screen width to determine if it's a tablet
                    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
                    Log.d("screenWidth", "Categories_items:$screenWidth ")
                    val isTablet = screenWidth >= 1200 // Adjust the threshold if needed

                    // Adjust dynamic height based on the device size
                    val dynamicHeight = if (isTablet) {
                        maxHeight * 0.3f // Increase height for tablets (30% of available height)
                    } else {
                        maxHeight * 0.2f // Default height for phones (20% of available height)
                    }

                    TextField(
                        value = Comment,
                        onValueChange = { Comment = it },
                        label = { Text(if (selectedLanguage == "English") "Observation" else "Igitekerezo") },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(if (isTablet) 100.dp else 90.dp, max = dynamicHeight) // Adaptive height
                            .padding(0.dp),
                        colors = TextFieldDefaults.colors(
                            cursorColor = Color.Black,
                            focusedLabelColor = colorResource(id = R.color.turquoise),
                            unfocusedLabelColor = Color.Gray,
                            focusedIndicatorColor = colorResource(id = R.color.turquoise),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        maxLines = Int.MAX_VALUE,
                        singleLine = false
                    )

                }



                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=0.dp, bottom = 0.dp)


                    ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.padding(start= 10.dp,end= 10.dp)
                        ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),


                    ) {

                    Text(text = if (selectedLanguage == "English") "Score" else "Amanota", color = Color.Black, fontSize = 16.sp)
                    // Score Indicator (Simulating with a CircularProgressIndicator)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {

                            val circleColor = when {
                                scorePercentage < 0.5 -> Color.Red
                                scorePercentage.toFloat() in 0.5..0.79 -> Color.Yellow
                                else -> Color(0xFF26275C)
                            }
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
                                color = circleColor, // Foreground color (e.g., turquoise)
                                strokeWidth = 8.dp // Thickness of the circle
                            )
                            Text(
                                text = "${(scorePercentage * 100).toInt()}%",
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }

                    }
                }

                Button(
                    onClick = {
                        if (respondentName.isEmpty()) {
                            Toast.makeText(
                                context, if (selectedLanguage == "English") {"Please enter your name"}else{"andikamo izina ryawe"}, Toast.LENGTH_SHORT
                            ).show()
                        } else if (selectedCooperative == if (selectedLanguage == "English") {"Select cooperative" } else{ "Hitamo koperative"} || selectedCooperative.isEmpty()) {
                            Toast.makeText(
                                context, if(selectedLanguage=="English"){"Please select a cooperative"}else{ "Hitamo koperative"}, Toast.LENGTH_SHORT
                            ).show()

                        }
                        else {
                            if (answers.isNotEmpty()) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val db = AppDatabase.getDatabase(context)
                                        val uid = UUID.randomUUID().toString()

                                        // 1. Load categories and questions from JSON
                                        val jsonCategories = loadCategoriesAndQuestion(context,selectedLanguage)
                                        jsonCategories.flatMap { it.questions }
                                        // 2. Calculate the total score
                                        val totalScore = scorePercentage

                                        // Check if questions and categories have already been loaded
                                        if (db.surveyCategoryDao().getAllCategories().isEmpty()) {
                                            // Load categories and questions from JSON if they are not already in the DB
                                            val jsonCategories = loadCategoriesAndQuestion(context,selectedLanguage)
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
                                                comment = Comment,
                                                timestamp = System.currentTimeMillis(),
                                                uid = uid
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
                                                if(selectedLanguage=="English") {"Survey submitted successfully"}else{"Survey yemejwe neza"},
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
                                    context, if(selectedLanguage=="English"){"No answers to submit"}else{"Ntagisubizo cyatanzwe"}, Toast.LENGTH_SHORT
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
                    Text(text = if(selectedLanguage=="English"){"Submit"}else{"Emeza"}, color = Color.White)
                }
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
    answers: MutableMap<Int, Any?>,
    viewModel: AnswersViewModel
) {
    val selectedCategory = remember { mutableStateOf<Category?>(null) }
    val hasAnswers = hasSingleAnswer(answers, category.questions)
    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White

    // BoxWithConstraints to get the available height dynamically
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        val dynamicHeight = maxHeight * 0.3f // Adjust height as 30% of available height (can be changed)

        Column(
            modifier = Modifier
                .heightIn(min = 130.dp, max = dynamicHeight) // Set dynamic height for the card
                .background(backgroundColor, shape = RoundedCornerShape(10.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
                .clickable(enabled = enabled) {
                    if (enabled) {
                        viewModel.clearTimestamp()
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
                        answers = answers,
                        viewModel
                    )
                }
            }
        }
    }
}

//@Composable
//fun CategoryButton(
//    category: Category,
//    navController: NavHostController,
//    enabled: Boolean = true,
//    showPopup: Boolean,
//    onShowPopup: (Boolean) -> Unit,
//    answers: MutableMap<Int, Any?>,
//    viewModel: AnswersViewModel
//) {
//    val selectedCategory = remember { mutableStateOf<Category?>(null) }
//
//    val hasAnswers = hasSingleAnswer(answers, category.questions)
//    val backgroundColor = if (hasAnswers) colorResource(id = R.color.turquoise) else Color.White
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(150.dp)
//            .padding(6.dp)
//            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
//            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
//            .clickable(enabled = enabled) {
//                if (enabled) {
//                    viewModel.clearTimestamp()
//                    onShowPopup(true)
//                    selectedCategory.value = category
//                }
//            },
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//        val resourceId = LocalContext.current.resources.getIdentifier(
//            category.icon_path, // e.g., "icon_add"
//            "drawable", LocalContext.current.packageName
//        )
//
//        Icon(
//            painter = painterResource(id = resourceId),
//            contentDescription = "Add New cooperative",
//            tint = colorResource(id = R.color.black),
//            modifier = Modifier.padding(top = 30.dp)
//        )
//
//        Text(text = category.category)
//        ProgressedBar()
//        if (showPopup && selectedCategory.value == category) {
//            Dialog(
//                onDismissRequest = {
//                    onShowPopup(false)
//                    selectedCategory.value = null
//                },
//                properties = DialogProperties(dismissOnClickOutside = false)
//            ) {
//                PopupActivity(
//                    navController = navController,
//                    categoryId = selectedCategory.value!!.id,
//                    onClose = {
//                        onShowPopup(false)
//                        selectedCategory.value = null
//                    },
//                    answers = answers,
//                    viewModel
//                )
//            }
//        }
//    }
//
//}

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