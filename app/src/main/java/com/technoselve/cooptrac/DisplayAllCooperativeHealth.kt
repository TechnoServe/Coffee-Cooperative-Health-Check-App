
package com.technoserve.cooptrac

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cooptrac.loadCategoriesAndQuestion
import com.technoserve.cooptrac.data.AnswersViewModel
import com.technoserve.cooptrac.data.AnswersViewModelFactory
import com.technoserve.cooptrac.data.AppDatabase
import com.technoserve.cooptrac.data.Category
import com.technoserve.cooptrac.data.CategoryDao
import com.technoserve.cooptrac.data.CategoryDb
import com.technoserve.cooptrac.data.QuestionDao
import com.technoserve.cooptrac.data.QuestionDb
import com.technoserve.cooptrac.data.Survey
import com.technoserve.cooptrac.data.SurveyAnswer
import com.technoserve.cooptrac.data.SurveyAnswerDao
import com.technoserve.cooptrac.data.SurveyDao


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAllCooperativeHealth(navController: NavHostController,viewModel: AnswersViewModel) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"

    Log.d("Languageeees", "Language changed to $selectedLanguage")
    val surveys by viewModel.surveys.collectAsState(initial = emptyList())
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var selectAll by remember { mutableStateOf(false) }




    val surveyDao = viewModel.surveyDao
    val surveyAnswerDao = viewModel.surveyAnswerDao
    val categoryDao = viewModel.categoryDao
    val questionDao = viewModel.questionDao
    val coroutineScope = rememberCoroutineScope()
    val selectedSurveys = remember { mutableStateListOf<String>() }


    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val filteredSurveys = if (searchText.isNotBlank()) {
        surveys.filter { survey ->
            survey.cooperativeName.contains(searchText, ignoreCase = true) ||
                    formatTimestamp(survey.timestamp).contains(searchText, ignoreCase = true) ||
                    (survey.totalScore * 100).toInt().toString().contains(searchText)||
                    survey.respondentName.contains(searchText)
        }
    }else{
        surveys
    }
    fun processCsvFile(
        context: Context,
        uri: Uri,
        surveyDao: SurveyDao,
        categoryDao: CategoryDao,
        questionDao: QuestionDao,
        surveyAnswerDao: SurveyAnswerDao,
        loadCategoriesFromJson: () -> List<Category> // Function to load categories and questions from JSON
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            if (inputStream != null) {
                val csvData = inputStream.bufferedReader().readLines()

                if (csvData.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "CSV file is empty", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val header = csvData.first().split(",").map { it.trim() }
                val expectedHeader = listOf(
                    "Survey Title", "Surveyor Name", "Cooperative Name",
                    "Timestamp", "Category Name", "Question Text", "Answer Text",
                    "Score", "UID", "Comment"
                )

                if (header != expectedHeader) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Invalid CSV format", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val jsonCategories = loadCategoriesAndQuestion(context, selectedLanguage)
                jsonCategories.flatMap { it.questions }

                val db = AppDatabase.getDatabase(context)

                // First time, load categories and questions from JSON if not already in DB
                if (db.surveyCategoryDao().getAllCategories().isEmpty()) {
                    jsonCategories.forEach { jsonCategory ->
                        val categoryId = db.surveyCategoryDao().insertCategory(CategoryDb(name = jsonCategory.category)).toInt()

                        jsonCategory.questions.forEach { jsonQuestion ->
                            db.surveyQuestionDao().insertQuestion(QuestionDb(categoryId = categoryId, questionText = jsonQuestion.question))
                        }
                    }
                }

                // Process each row in CSV file
                csvData.drop(1).forEach { row ->
                    val fields = row.split(",").map { it.trim().replace("\"", "") }
                    if (fields.size != expectedHeader.size) {
                        Log.e("CSV Import", "Skipping malformed row: $row")
                        return@forEach
                    }

                    val surveyTitle = fields[0]
                    val respondentName = fields[1]
                    val cooperativeName = fields[2]
                    val timestamp = fields[3].toLongOrNull() ?: System.currentTimeMillis()
                    val categoryName = fields[4]
                    val questionText = fields[5]
                    val answerText = fields[6]
                    val score = fields[7].replace("%", "").toDoubleOrNull()?.div(100) ?: 0.0
                    val uid = fields[8]
                    val comment = fields[9]

                    // Handle survey for each UID separately
                    val existingSurvey = db.surveyDao().getSurveyByUID(uid)

                    val surveyId = if (existingSurvey != null) {
                        // Update existing survey
                        val updatedSurvey = existingSurvey.copy(
                            surveyTitle = surveyTitle,
                            respondentName = respondentName,
                            cooperativeName = cooperativeName,
                            comment = comment,
                            timestamp = timestamp,
                            totalScore = score
                        )
                        db.surveyDao().updateSurvey(updatedSurvey)
                        Log.d("CSV Import", "Updated survey with UID '$uid'.")
                        existingSurvey.surveyId
                    } else {
                        // Insert new survey for unique UID
                        val newSurvey = Survey(
                            surveyId = 0, // Auto-generated
                            surveyTitle = surveyTitle,
                            respondentName = respondentName,
                            cooperativeName = cooperativeName,
                            comment = comment,
                            timestamp = timestamp,
                            totalScore = score,
                            uid = uid
                        )
                        val newSurveyId = db.surveyDao().insertSurvey(newSurvey).toInt()
                        Log.d("CSV Import", "Inserted new survey with UID '$uid'.")
                        newSurveyId
                    }

                    // Handle categories and questions for each survey
                    val category = categoryDao.getCategoryByName(categoryName) ?: CategoryDb(name = categoryName).also {
                        it.categoryId = categoryDao.insertCategory(it).toInt()
                    }

                    val question = questionDao.getQuestionByTextAndCategory(questionText, category.categoryId)
                        ?: QuestionDb(categoryId = category.categoryId, questionText = questionText).also {
                            it.questionId = questionDao.insertQuestion(it).toInt()
                        }

                    // Insert answers for this row
                    val answer = SurveyAnswer(
                        surveyId = surveyId,
                        questionId = question.questionId,
                        answerText = answerText,
                        answerScore = 0
                    )
                    db.surveyAnswerDao().insertAnswer(answer)

                    // Log the insert/update action for debugging
                    Log.d("CSV Import", "Processed answer for survey ID $surveyId, question ID ${question.questionId}.")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, if (selectedLanguage == "English") {
                        "CSV imported successfully."
                    } else {
                        "CSV yinjijwe neza"
                    }, Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to open the file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }






//    fun processCsvFile(
//        context: Context,
//        uri: Uri,
//        surveyDao: SurveyDao,
//        categoryDao: CategoryDao,
//        questionDao: QuestionDao,
//        surveyAnswerDao: SurveyAnswerDao,
//        loadCategoriesFromJson: () -> List<Category> // Function to load JSON categories
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val contentResolver = context.contentResolver
//            val inputStream = contentResolver.openInputStream(uri)
//
//            if (inputStream != null) {
//                val csvData = inputStream.bufferedReader().readLines()
//
//                if (csvData.isEmpty()) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "CSV file is empty", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                val header = csvData.first().split(",").map { it.trim() }
//                val expectedHeader = listOf(
//                    "Survey ID",
//                    "Survey Title",
//                    "Surveyor Name",
//                    "Cooperative Name",
//                    "Timestamp",
//                    "Category Name",
//                    "Question Text",
//                    "Answer Text",
//                    "Score",
//                    "UID",
//                    "Comment"
//                )
//
//                if (header != expectedHeader) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Invalid CSV format", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                // Load all categories and questions from JSON
//                val jsonCategories = loadCategoriesFromJson()
//
//                // Process rows in the CSV
//                val rows = csvData.drop(1)
//                val processedSurveyIds = mutableSetOf<String>() // Keep track of processed surveys
//
//                for (row in rows) {
//                    val fields = row.split(",").map { it.trim().replace("\"", "") }
//                    if (fields.size != expectedHeader.size) {
//                        Log.e("CSV Import", "Skipping malformed row: $row")
//                        continue
//                    }
//
//                    val surveyId = fields[0].toIntOrNull()
//                    if (surveyId == null) {
//                        Log.e("CSV Import", "Skipping row with invalid Survey ID: $row")
//                        continue
//                    }
//
//                    val surveyTitle = fields[1]
//                    val respondentName = fields[2]
//                    val cooperativeName = fields[3]
//                    val timestamp = fields[4].toLongOrNull() ?: System.currentTimeMillis()
//                    val categoryName = fields[5]
//                    val questionText = fields[6]
//                    val answerText = fields[7]
//                    val score = fields[8].replace("%", "").toDoubleOrNull()?.div(100) ?: 0.0
//                    val uid = fields[9]
//                    val comment = fields[10]
//
//                    // Insert the survey if not already processed
//                    if (uid !in processedSurveyIds) {
//                        val existingSurvey = surveyDao.getSurveyById(surveyId)
//                        if (existingSurvey != null) {
//                            val newSurvey = Survey(
//                                surveyId = surveyId,
//                                surveyTitle = surveyTitle,
//                                respondentName = respondentName,
//                                cooperativeName = cooperativeName,
//                                comment= comment,
//                                timestamp = timestamp,
//                                totalScore = score,
//                                uid=uid
//                            )
//                            surveyDao.updateSurvey(newSurvey)
//
//                            Log.d("CSV Import", "Survey with ID $surveyId updated successfully exists. Import rejected.",)
//
//                        }
//
//                        val newSurvey = Survey(
//                            surveyId = surveyId,
//                            surveyTitle = surveyTitle,
//                            respondentName = respondentName,
//                            cooperativeName = cooperativeName,
//                            comment=comment,
//                            timestamp = timestamp,
//                            totalScore = score,
//                            uid=uid
//                        )
//                        surveyDao.insertSurvey(newSurvey)
//                        Log.d("CSV Import", "Inserted new survey with ID $uid.")
//                        processedSurveyIds.add(uid)
//                    }
//
//                    // Insert categories and questions from JSON
//                    jsonCategories.forEach { jsonCategory ->
//                        val existingCategory = categoryDao.getCategoryByName(jsonCategory.category)
//                        val categoryId = if (existingCategory != null) {
//                            existingCategory.categoryId
//                        } else {
//                            categoryDao.insertCategory(CategoryDb(name = jsonCategory.category)).toInt()
//                        }
//
//                        jsonCategory.questions.forEach { jsonQuestion ->
//                            val existingQuestion = questionDao.getQuestionByTextAndCategory(
//                                jsonQuestion.question,
//                                categoryId
//                            )
//                            if (existingQuestion == null) {
//                                questionDao.insertQuestion(
//                                    QuestionDb(
//                                        questionId = jsonQuestion.id,
//                                        categoryId = categoryId,
//                                        questionText = jsonQuestion.question
//                                    )
//                                )
//                            }
//                        }
//                    }
//
//                    // Insert the answer
//                    val category = categoryDao.getCategoryByName(categoryName)
//                    if (category == null) {
//                        Log.e("CSV Import", "Category '$categoryName' not found. Skipping row: $row")
//                        continue
//                    }
//
//                    val question = questionDao.getQuestionByTextAndCategory(questionText, category.categoryId)
//                    if (question == null) {
//                        Log.e("CSV Import", "Question '$questionText' not found in category '$categoryName'. Skipping row: $row")
//                        continue
//                    }
//
//                    val existingAnswer = surveyAnswerDao.getAnswer(surveyId, question.questionId)
//                    if (existingAnswer == null) {
//                        surveyAnswerDao.insertAnswer(
//                            SurveyAnswer(
//                                answerId = 0,
//                                surveyId = surveyId,
//                                questionId = question.questionId,
//                                answerText = answerText,
//                                answerScore = 0
//                            )
//                        )
//                        Log.d(
//                            "CSV Import",
//                            "Inserted new answer for survey ID $surveyId, question ID ${question.questionId}, answer: $answerText"
//                        )
//                    } else {
//                        existingAnswer.answerText = answerText
//                        existingAnswer.answerScore = 0
//                        surveyAnswerDao.updateAnswer(existingAnswer)
//                        Log.d(
//                            "CSV Import",
//                            "Answer already exists for survey ID $surveyId and question ID ${question.questionId}. Skipping insertion."
//                        )
//                    }
//                }
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, if (selectedLanguage=="English"){"CSV imported successfully."}else{"CSV yinjijwe neza"}, Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Failed to open the file", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }


    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            processCsvFile(
                                context = context,
                                uri = uri,
                                surveyDao = surveyDao,
                                categoryDao = categoryDao,
                                questionDao = questionDao,
                                surveyAnswerDao = surveyAnswerDao,
                                loadCategoriesFromJson = {
                                    loadCategoriesAndQuestion(context,selectedLanguage)
                                }
                            )
                        }
                    } catch (e: Exception) {
                        // Handle error
                        withContext(Dispatchers.IO) {
                            Toast.makeText(
                                context,
                                "Error processing CSV: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }


            } else {

                Toast.makeText(context, if(selectedLanguage=="English"){"No file selected."}else{"Nta dosiye yatoranijwe"}, Toast.LENGTH_SHORT).show()
            }
        }
    )

    suspend fun createFile(context: Context, uri: Uri): Boolean {
        return try {
            val selectedSurveyId = selectedSurveys.first()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val csvFile = createSurveyCsvFile(
                    context,
                    surveyDao,
                    surveyAnswerDao,
                    categoryDao,
                    questionDao,
                    selectedSurveys

                )
                val inputStream = FileInputStream(csvFile)
                inputStream.copyTo(outputStream)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (createFile(context, uri)) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    if(selectedLanguage=="English"){"Data exported Successfully"}else{"Amakuru yoherejwe neza"},
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }


    suspend fun saveFileToLocation(
        context: Context,
        surveyDao: SurveyDao,
        surveyAnswerDao: SurveyAnswerDao,
        categoryDao: CategoryDao,
        questionDao: QuestionDao,
        selectedSurveyId: List<String>
    ) {
        val csvFile =
            createSurveyCsvFile(context, surveyDao, surveyAnswerDao, categoryDao, questionDao,selectedSurveyId)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, csvFile.name)
        }

        createDocumentLauncher.launch(intent)
    }


    suspend fun shareFile(
        context: Context,
        surveyDao: SurveyDao,
        surveyAnswerDao: SurveyAnswerDao,
        categoryDao: CategoryDao,
        questionDao: QuestionDao,
        selectedSurveyId: List<String>
    ) {
        val csvFile =
            createSurveyCsvFile(context, surveyDao, surveyAnswerDao, categoryDao, questionDao,selectedSurveyId)
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "coffee Cooperative Health  Survey Results ")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share CSV File via"))
    }


    fun importCsv(importLauncher: ActivityResultLauncher<String>) {
        // Launch the file picker for CSV files
        importLauncher.launch("*/*")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (LocalConfiguration.current.screenWidthDp > 600) 24.dp else 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                placeholder = {
                                    Text(
                                        text = if (selectedLanguage == "English") "Search..." else "Shakisha...",
                                        textAlign = TextAlign.Start,  // Aligns text to start
                                        fontSize = 16.sp,  // Increases font size for readability
                                        color = Color.Gray  // Keeps placeholder text distinguishable
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)  // Ensures proper height
                                    .padding(vertical = 5.dp)
                                    .padding(bottom = 5.dp),
                                singleLine = true,
                                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
                                maxLines = 1
                            )
                        }
                    }

                    else
                    Text(
                        text =if(selectedLanguage=="English"){ "CoopTrac CHECKLIST"}else{"Liste y'igenzura rya CoopTrac"},
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Overflow menu icon
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { dropdownMenuExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.dots_horizontal_svgrepo_com__1_),
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.export_content_svgrepo_com),
                                        contentDescription = "Export CSV",
                                        tint = colorResource(id = R.color.turquoise)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (selectedLanguage == "English") "Export CSV" else "ohereza CSV")
                                }
                            },
                            onClick = {
                                dropdownMenuExpanded = false
                                if (selectedSurveys.isNotEmpty()) {
                                    CoroutineScope(Dispatchers.Main).launch {

                                            // Handle CSV export for each selected survey
                                            saveFileToLocation(
                                                context,
                                                surveyDao,
                                                surveyAnswerDao,
                                                categoryDao,
                                                questionDao,
                                                selectedSurveys  // Pass the survey ID for each selected survey
                                            )

                                    }
                                } else {
                                    Toast.makeText(context, if(selectedLanguage=="English"){"No surveys selected"}else{"nta Survey yatoranyijwe"}, Toast.LENGTH_SHORT).show()
                                }

                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_share_24),
                                        contentDescription = "Share CSV",
                                        tint = colorResource(id = R.color.turquoise)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (selectedLanguage == "English") "Share CSV" else "Sangiza CSV")

                                }
                            },
                            onClick = {
                                dropdownMenuExpanded = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (selectedSurveys.isNotEmpty()) {
                                        shareFile(
                                            context,
                                            surveyDao,
                                            surveyAnswerDao,
                                            categoryDao,
                                            questionDao,
                                            selectedSurveys
                                        )
                                    } else {
                                        Toast.makeText(context, if(selectedLanguage=="English"){"No surveys selected"}else{"nta Survey yatoranyijwe"}, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.import_content_svgrepo_com),
                                        contentDescription = "Import CSV",
                                        tint = colorResource(id = R.color.turquoise)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (selectedLanguage == "English") "Import CSV" else "Injiza CSV")


                                }
                            },
                            onClick = {
                                dropdownMenuExpanded = false
                                importCsv(importLauncher)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id = R.color.turquoise))
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // "Add New" Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (surveys.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .weight(1f) // Ensure the Checkbox row takes remaining space
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Checkbox(
                                checked = selectAll,
                                onCheckedChange = { isChecked ->
                                    selectAll = isChecked
                                    selectedSurveys.clear()
                                    if (isChecked) {
                                        selectedSurveys.addAll(surveys.map { it.uid })
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(id = R.color.turquoise)
                                )
                            )
                            Text(
                                text = if (selectedLanguage == "English") "Select All" else "Hitamo byose",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Ensures Button does not collapse
                    }

                    Button(
                        onClick = {
                            navController.navigate("categories_health?isEditMode=false")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "Add New",
                            tint = Color.White
                        )
                        Text(text = if (selectedLanguage == "English") "Add New" else "Ongera", color = Color.White)
                    }
                }

                // Display content using LazyColumn with cards
                LazyColumn {
                    if (filteredSurveys.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedLanguage == "English") "No data available." else "Nta byabonetse.",
                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                )
                            }
                        }
                    } else {
                        items(filteredSurveys) { survey ->
                            SurveyCard(
                                survey,
                                isSelected = selectedSurveys.contains(survey.uid),
                                onToggleSelect = { selectedSurvey ->
                                    if (selectedSurveys.contains(selectedSurvey.uid)) {
                                        selectedSurveys.remove(selectedSurvey.uid)
                                    } else {
                                        selectedSurveys.add(selectedSurvey.uid)
                                    }
                                    selectAll = selectedSurveys.size == surveys.size
                                },
                            ) {
                                val surveyId = survey.surveyId
                                viewModel.selectSurvey(survey)
                                navController.navigate("category_list/${survey.surveyId}?timestamp=${survey.timestamp}")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

//        content = { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(horizontal = 16.dp)
//            ) {
//                Spacer(modifier = Modifier.height(16.dp))
//
//
//                // "Add New" Button
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    if (surveys.isNotEmpty()) {
//
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Start
//                        ) {
//                            Checkbox(
//                                checked = selectAll,
//                                onCheckedChange = { isChecked ->
//                                    selectAll = isChecked
//                                    selectedSurveys.clear()
//                                    if (isChecked) {
//                                        selectedSurveys.addAll(surveys.map { it.uid })
//                                    }
//                                },
//                                colors = CheckboxDefaults.colors(
//                                    checkedColor = colorResource(id = R.color.turquoise)
//                                )
//                            )
//                            Text(
//                                text = if (selectedLanguage == "English") "Select All" else "Hitamo byose",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }else {
//                        Spacer(modifier = Modifier.weight(1f)) // Ensures Button does not collapse
//                    }
//
//
//                    Button(
//                        onClick = {
//                            val surveyId = surveys.firstOrNull()?.surveyId ?: 0L
//                            navController.navigate("categories_health?surveyId=$surveyId&isEditMode=false")
//                        },
//                        shape = RoundedCornerShape(12.dp),
//                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
//                        modifier = Modifier.height(48.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.baseline_add_24),
//                            contentDescription = "Add New",
//                            tint = Color.White
//                        )
//                    }
//                }
//
//
//
//
//
//                // Display content using LazyColumn with cards
//                LazyColumn {
//                    if (filteredSurveys.isEmpty()) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(16.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = if (selectedLanguage == "English") "No data available." else "Nta byabonetse.",
//                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
//                                )
//                            }
//                        }
//                    }else
//                    items(filteredSurveys) { survey ->
//                        SurveyCard(
//                            survey,
//                            isSelected = selectedSurveys.contains(survey.uid),
//                            onToggleSelect = { selectedSurvey ->
//                                if (selectedSurveys.contains(selectedSurvey.uid)) {
//                                    selectedSurveys.remove(selectedSurvey.uid)
//                                } else {
//                                    selectedSurveys.add(selectedSurvey.uid)
//                                }
//                                selectAll = selectedSurveys.size == surveys.size
//                            },
//                        ) {
//                            val surveyId = survey.surveyId
//                            viewModel.selectSurvey(survey)
//                            navController.navigate("category_list/${survey.surveyId}?timestamp=${survey.timestamp}")
//                        }
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//            }
//        }
    )
}



@Composable
fun SurveyCard(
    survey: Survey,
    isSelected: Boolean,
    onToggleSelect: (Survey) -> Unit,
    onClick: () -> Unit
) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"




    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected, 
                onCheckedChange = { onToggleSelect(survey) },
                colors = CheckboxDefaults.colors(
                        checkedColor = colorResource(id = R.color.turquoise)
                        )
        )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (selectedLanguage == "English") survey.cooperativeName else " ${survey.cooperativeName}",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),


                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(if (selectedLanguage == "English") "Surveyor: " else "Izina: ")
                                }
                                append(survey.respondentName)
                            },
                            style = TextStyle(fontSize = 14.sp)
                        )


                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(if (selectedLanguage == "English") "Date: " else "Itariki: ")
                                }
                                append(formatTimestamp(survey.timestamp))
                            },
                            style = TextStyle(fontSize = 14.sp)
                        )

                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {

                        Box(contentAlignment = Alignment.Center) {
                            val scorePercentage = (survey.totalScore * 100).toInt()

                            // Determine circle color based on score percentage
                            val circleColor = when {
                                scorePercentage < 50 -> Color.Red
                                scorePercentage in 50 until 80 -> Color.Yellow
                                else -> Color(0xFF26275C)
                            }

                            // Background circle (outer circle, full progress)
                            CircularProgressIndicator(
                                progress = 1f, // Full circle as background
                                modifier = Modifier.size(60.dp), // Size of the circle
                                color = Color.LightGray, // Light background color
                                strokeWidth = 8.dp // Thickness of the circle
                            )

                            // Foreground circle (actual progress)
                            CircularProgressIndicator(
                                progress = scorePercentage / 100f, // Progress percentage (e.g., 70% -> 0.7)
                                modifier = Modifier.size(60.dp), // Same size as background circle
                                color = circleColor, // Dynamic foreground color
                                strokeWidth = 8.dp // Thickness of the circle
                            )

                            // Score percentage text
                            Text(
                                text = "$scorePercentage%",
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }


                    }



                }


            }
        }
    }
}

// Utility function to format the timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

//suspend fun createSurveyCsvFile( context: Context, surveyDao: SurveyDao, surveyAnswerDao: SurveyAnswerDao, categoryDao: CategoryDao, questionDao: QuestionDao, selectedSurveyIds: List<Int>
//// Update to handle a list of selected survey IDs
//): File { // Prepare the file to write the CSV
//     val csvFile = File(context.getExternalFilesDir(null), "selected_surveys.csv")
//    val fileOutputStream = withContext(Dispatchers.IO) { FileOutputStream(csvFile) }
//    val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))
//    // Write the CSV headers
//     withContext(Dispatchers.IO) {
//         writer.write("Survey ID,Survey Title,Surveyor Name,Cooperative Name,Timestamp,Category Name,Question Text,Answer Text,Score\n")
//     }
//    // Process each selected survey
//     for (selectedSurveyId in selectedSurveyIds) {
//     // Retrieve the selected survey
//      val survey = surveyDao.getSurveyById(selectedSurveyId)
//          ?: throw IllegalArgumentException("Survey with ID $selectedSurveyId not found.")
//     // Retrieve answers for the survey
//      val answers = surveyAnswerDao.getAnswersForSurvey(survey.surveyId).first()
//     // For each answer, get the corresponding question and category
//      for (answer in answers) {
//          val question = questionDao.getQuestionById(answer.questionId)
//          val category = categoryDao.getAllCategories().firstOrNull { it.categoryId == question?.categoryId }
//      // Write the data to CSV
//       if (question != null && category != null) {
//           withContext(Dispatchers.IO) {
//               val surveyScore = "${survey.totalScore * 100}%"
//               writer.write("\"${survey.surveyId}\",\"${survey.surveyTitle}\",\"${survey.respondentName}\",\"${survey.cooperativeName}\",\"${survey.timestamp}\",\"${category.name}\",\"${question.questionText}\",\"${answer.answerText}\",\"${surveyScore}\"\n")
//           }
//       }
//      }
//     }
//    // Close the writer and return the file
//     withContext(Dispatchers.IO) { writer.close() }
//return csvFile
//}

//suspend fun createSurveyCsvFile(
//    context: Context,
//    surveyDao: SurveyDao,
//    surveyAnswerDao: SurveyAnswerDao,
//    categoryDao: CategoryDao,
//    questionDao: QuestionDao,
//    selectedUid: List<String> // Export surveys by this UID
//): File {
//    val csvFile = File(context.getExternalFilesDir(null), "surveys_by_uid.csv")
//    val fileOutputStream = withContext(Dispatchers.IO) { FileOutputStream(csvFile) }
//    val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))
//
//    // Write CSV headers
//    withContext(Dispatchers.IO) {
//        writer.write("Survey ID,Survey Title,Surveyor Name,Cooperative Name,Timestamp,Category Name,Question Text,Answer Text,Score,UID,Comment")
//    }
//
//    // Fetch surveys by UID
//    val surveys = surveyDao.getSurveysByUID(selectedUid).first()
//    for (survey in surveys) {
//        val answers = surveyAnswerDao.getAnswersForSurvey(survey.surveyId).first()
//        for (answer in answers) {
//            val question = questionDao.getQuestionById(answer.questionId)
//            val category = categoryDao.getAllCategories().firstOrNull { it.categoryId == question?.categoryId }
//            if (question != null && category != null) {
//                withContext(Dispatchers.IO) {
//                    val surveyScore = "${survey.totalScore * 100}%"
//                    writer.write("\"${survey.surveyId}\",\"${survey.surveyTitle}\",\"${survey.respondentName}\",\"${survey.cooperativeName}\",\"${survey.timestamp}\",\"${category.name}\",\"${question.questionText}\",\"${answer.answerText}\",\"${surveyScore}\",\"${survey.uid}\",\"${survey.comment}\"")
//                }
//            }
//        }
//    }
//
//    // Close writer and return file
//    withContext(Dispatchers.IO) { writer.close() }
//    return csvFile
//}

fun escapeCsvValue(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}
suspend fun createSurveyCsvFile(
    context: Context,
    surveyDao: SurveyDao,
    surveyAnswerDao: SurveyAnswerDao,
    categoryDao: CategoryDao,
    questionDao: QuestionDao,
    selectedUid: List<String>
): File {
    val csvFile = File(context.getExternalFilesDir(null), "surveys_by_uid.csv")
    val fileOutputStream = withContext(Dispatchers.IO) { FileOutputStream(csvFile) }
    val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))

    // Write CSV headers
    withContext(Dispatchers.IO) {
        writer.write("Survey Title,Surveyor Name,Cooperative Name,Timestamp,Category Name,Question Text,Answer Text,Score,UID,Comment\n")
    }

    // Fetch surveys by UID
    val surveys = surveyDao.getSurveysByUID(selectedUid).first()
    for (survey in surveys) {
        val answers = surveyAnswerDao.getAnswersForSurvey(survey.surveyId).first()
        for (answer in answers) {
            val question = questionDao.getQuestionById(answer.questionId)
            val category = categoryDao.getAllCategories().firstOrNull { it.categoryId == question?.categoryId }
            if (question != null && category != null) {
                withContext(Dispatchers.IO) {
                    val surveyScore = "${survey.totalScore * 100}%"
                    val csvRow = listOf(
//                        survey.surveyId.toString(),
                        escapeCsvValue(survey.surveyTitle),
                        escapeCsvValue(survey.respondentName),
                        escapeCsvValue(survey.cooperativeName),
                        survey.timestamp.toString(),
                        escapeCsvValue(category.name),
                        escapeCsvValue(question.questionText),
                        answer?.answerText?.let { escapeCsvValue(it) },
                        surveyScore,
                        survey.uid,
                        escapeCsvValue(survey.comment)
                    ).joinToString(separator = ",")
                    writer.write("$csvRow\n")
                }
            }
        }
    }

    // Close writer and return file
    withContext(Dispatchers.IO) { writer.close() }
    return csvFile
}




