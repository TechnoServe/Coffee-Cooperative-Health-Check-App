//package com.example.ccts
//
//import android.app.Application
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import com.example.ccts.data.AnswersViewModel
//import com.example.ccts.data.AnswersViewModelFactory
//import com.example.ccts.data.Survey
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DisplayAllCooperativeHealth(navController: NavHostController) {
//
//    val application = LocalContext.current.applicationContext as Application
//    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
//
//    val surveys by viewModel.surveys.collectAsState(initial = emptyList())
//
//    // val selectedSurveyId = surveys.firstOrNull()?.surveyId ?: 0
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "CCTS CHECKLIST",
//                        style = TextStyle(
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigate("home") }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
//                            contentDescription = "Back",
//                            tint = Color.White
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* Handle overflow menu */ }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.dots_horizontal_svgrepo_com__1_),
//                            contentDescription = "More options",
//                            tint = Color.White
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.turquoise))
//            )
//        },
//        content = { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(horizontal = 16.dp)
//            ) {
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // "Add New" Button
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
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
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Display content using LazyColumn with cards
//                LazyColumn {
//                    if (surveys.isEmpty()) {
//                        item {
//                            Text(
//                                text = "No Survey yet",
//                                style = TextStyle(
//                                    fontSize = 16.sp,
//                                    fontStyle = FontStyle.Italic,
//                                    fontWeight = FontWeight.Bold
//                                ),
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp),
//                                color = Color.Gray,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    } else {
//                        items(surveys) { survey ->
//                            SurveyCard(survey) {
//                                survey.surveyId
//                                navController.navigate("category_list/${survey.surveyId}")
//                            }
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//                }
//            }
//        }
//    )
//}
//
//@Composable
//fun SurveyCard(survey: Survey, onClick: () -> Unit) {
//    LocalContext.current.applicationContext as Application
//    // val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
//    // val hasAnswersForSurvey by viewModel.getFullSurveyData(survey.surveyId).collectAsState(initial = emptyList())
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth()
//        ) {
//            // Title and Edit Icon Row
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = survey.surveyTitle,
//                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
//                )
//                IconButton(onClick = { /* Handle edit action */ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.baseline_edit_24),
//                        contentDescription = "Edit",
//                        tint = colorResource(id = R.color.turquoise)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Card Content
//            Text(
//                text = "Date: ${formatTimestamp(survey.timestamp)}",
//                style = TextStyle(fontSize = 14.sp)
//            )
//            Text(
//                text = "Cooperative: ${survey.cooperativeName}",
//                style = TextStyle(fontSize = 14.sp)
//            )
//            // Text(text = "Cooperative: ${survey.cooperativeName}", style = TextStyle(fontSize = 14.sp))
//        }
//    }
//}
//
//// Utility function to format the timestamp
//fun formatTimestamp(timestamp: Long): String {
//    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//    return sdf.format(Date(timestamp))
//}
package com.example.ccts

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
import com.example.ccts.data.Category
import com.example.ccts.data.CategoryDao
import com.example.ccts.data.CategoryDb
import com.example.ccts.data.Question
import com.example.ccts.data.QuestionDao
import com.example.ccts.data.QuestionDb
import com.example.ccts.data.Survey
import com.example.ccts.data.SurveyAnswer
import com.example.ccts.data.SurveyAnswerDao
import com.example.ccts.data.SurveyDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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
import kotlin.contracts.ExperimentalContracts


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAllCooperativeHealth(navController: NavHostController) {

    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))


    val surveys by viewModel.surveys.collectAsState(initial = emptyList())
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current


    val surveyDao = viewModel.surveyDao
    val surveyAnswerDao = viewModel.surveyAnswerDao
    val categoryDao = viewModel.categoryDao
    val questionDao = viewModel.questionDao
    val coroutineScope = rememberCoroutineScope()


//    fun processCsvFile(
//        context: Context,
//        uri: Uri,
//        surveyDao: SurveyDao,
//        categoryDao: CategoryDao,
//        questionDao: QuestionDao,
//        surveyAnswerDao: SurveyAnswerDao
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val contentResolver = context.contentResolver
//            val inputStream = contentResolver.openInputStream(uri)
//
//            if (inputStream != null) {
//                val csvData = inputStream.bufferedReader().readLines()
//
//                // Ensure the CSV is not empty
//                if (csvData.isEmpty()) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "CSV file is empty", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                // Parse header and validate structure
//                val header = csvData.first().split(",").map { it.trim() }
//                val expectedHeader = listOf(
//                    "Survey ID",
//                    "Survey title",
//                    "Surveyor name",
//                    "Cooperative Name",
//                    "Timestamp",
//                    "Category Name",
//                    "Question Text",
//                    "Answer Text",
//                    "Score"
//                )
//
//                if (header != expectedHeader) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Invalid CSV format", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                val categoryMap = mutableMapOf<String, Int>()
//                val questionMap = mutableMapOf<String, Int>()
//
//                // Process rows
//                val rows = csvData.drop(1)
//                val firstRowFields = rows.firstOrNull()?.split(",")?.map { it.trim().replace("\"", "") }
//
//                if (firstRowFields.isNullOrEmpty() || firstRowFields.size < expectedHeader.size) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "No valid data in CSV file", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                val surveyId = firstRowFields[0].toIntOrNull() ?: -1
//                if (surveyId == -1) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Invalid Survey ID in CSV", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                // Check if the survey already exists
//                val existingSurvey = surveyDao.getSurveyById(surveyId) // Assuming this function exists in SurveyDao
//                if (existingSurvey != null) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Survey with ID $surveyId already exists. Import rejected.", Toast.LENGTH_SHORT).show()
//                    }
//                    return@launch
//                }
//
//                // Process each row and add the new survey, categories, questions, and answers
//                for (row in rows) {
//                    val fields = row.split(",").map { it.trim().replace("\"", "") }
//
//                    if (fields.size != expectedHeader.size) {
//                        Log.e("CSV Import", "Skipping malformed row: $row")
//                        continue
//                    }
//
//                    val surveyTitle = fields[1]
//                    val surveyName = fields[2]
//                    val cooperativeName = fields[3]
//                    val timestamp = fields[4].toLongOrNull() ?: System.currentTimeMillis()
//                    val categoryName = fields[5]
//                    val questionText = fields[6]
//                    val answerText = fields[7]
//                    val score = fields[8].replace("%", "").toDoubleOrNull()?.div(100) ?: 0.0
//                    Log.d("processCsvFile", "processCsvFile: $score")
//
//                    // Insert new survey
//                    val survey = Survey(
//                        surveyId = surveyId,
//                        surveyTitle = surveyTitle,
//                        respondentName = surveyName,
//                        cooperativeName = cooperativeName,
//                        timestamp = timestamp,
//                        totalScore = score // Assuming you calculate total score later
//                    )
//                    surveyDao.insertSurvey(survey)
//
//                    val categoryId = categoryMap.getOrPut(categoryName) {
//                        categoryDao.insertCategory(
//                            CategoryDb(name = categoryName)
//                        ).toInt()
//                    }
//
//                    // Insert Question if not already saved
//                    val questionId = questionMap.getOrPut(questionText) {
//                        questionDao.insertQuestion(
//                            QuestionDb(
//                                categoryId = categoryId,
//                                questionText = questionText
//                            )
//                        ).toInt()
//                    }
//
//
//                    // Insert or update Answer
//                    val answer = SurveyAnswer(
//                        answerId = 0, // Replace with auto-generated ID logic if needed
//                        surveyId = surveyId,
//                        questionId = questionId.toInt(),
//                        answerText = answerText
//                    )
//                    surveyAnswerDao.insertAnswer(answer)
//                }
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "CSV imported successfully", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Failed to open the file", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
fun processCsvFile(
    context: Context,
    uri: Uri,
    surveyDao: SurveyDao,
    categoryDao: CategoryDao,
    questionDao: QuestionDao,
    surveyAnswerDao: SurveyAnswerDao
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
                "Survey ID",
                "Survey title",
                "Surveyor name",
                "Cooperative Name",
                "Timestamp",
                "Category Name",
                "Question Text",
                "Answer Text",
                "Score"
            )

            if (header != expectedHeader) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Invalid CSV format", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val rows = csvData.drop(1)

            for (row in rows) {
                val fields = row.split(",").map { it.trim().replace("\"", "") }
                if (fields.size != expectedHeader.size) {
                    Log.e("CSV Import", "Skipping malformed row: $row")
                    continue
                }

                val surveyId = fields[0].toIntOrNull() ?: continue
                val surveyTitle = fields[1]
                val respondentName = fields[2]
                val cooperativeName = fields[3]
                val timestamp = fields[4].toLongOrNull() ?: System.currentTimeMillis()
                val categoryName = fields[5]
                val questionText = fields[6]
                val answerText = fields[7]
                val score = fields[8].replace("%", "").toDoubleOrNull()?.div(100) ?: 0.0

                // Check and insert survey if not exists
                val existingSurvey = surveyDao.getSurveyById(surveyId)
                if (existingSurvey == null) {
                    val survey = Survey(
                        surveyId = surveyId,
                        surveyTitle = surveyTitle,
                        respondentName = respondentName,
                        cooperativeName = cooperativeName,
                        timestamp = timestamp,
                        totalScore = score
                    )
                    surveyDao.insertSurvey(survey)
                }

                // Check and insert category if not exists
                val existingCategory = categoryDao.getCategoryByName(categoryName)
                val categoryId = if (existingCategory == null) {
                    categoryDao.insertCategory(
                        CategoryDb(
                            categoryId = 0,
                            name = categoryName
                        )
                    )
                } else {
                    existingCategory.categoryId.toLong()
                }

                // Check and insert question if not exists
                val existingQuestion = questionDao.getQuestionByTextAndCategory(questionText, categoryId.toInt())
                val questionId = if (existingQuestion == null) {
                    questionDao.insertQuestion(
                        QuestionDb(
                            questionId = 0,
                            categoryId = categoryId.toInt(),
                            questionText = questionText
                        )
                    )
                } else {
                    existingQuestion.questionId.toLong()
                }

                // Check and insert answer if not exists
                val existingAnswer = surveyAnswerDao.getAnswer(surveyId, questionId.toInt())
                if (existingAnswer == null) {
                    surveyAnswerDao.insertAnswer(
                        SurveyAnswer(
                            answerId = 0,
                            surveyId = surveyId,
                            questionId = questionId.toInt(),
                            answerText = answerText,
                            answerScore = (score * 100).toInt()
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "CSV imported successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to open the file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}




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
                                surveyAnswerDao = surveyAnswerDao
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


//                val contentResolver = context.contentResolver
//                contentResolver.openInputStream(uri)?.use { inputStream ->
//                    val csvData = inputStream.bufferedReader().use { it.readText() }
//                    Log.d("CSV Import", csvData)
//                    Toast.makeText(context, "CSV imported successfully!", Toast.LENGTH_SHORT).show()
//                }

            } else {

                Toast.makeText(context, "No file selected.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    suspend fun createFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val csvFile = createSurveyCsvFile(
                    context,
                    surveyDao,
                    surveyAnswerDao,
                    categoryDao,
                    questionDao
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
                                    "Data exported Successfully",
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
        questionDao: QuestionDao
    ) {
        val csvFile =
            createSurveyCsvFile(context, surveyDao, surveyAnswerDao, categoryDao, questionDao)
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
        questionDao: QuestionDao
    ) {
        val csvFile =
            createSurveyCsvFile(context, surveyDao, surveyAnswerDao, categoryDao, questionDao)
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
                    Text(
                        text = "CoopTrac CHECKLIST",
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
                                    Text("Export CSV")
                                }
                            },
                            onClick = {
                                dropdownMenuExpanded = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    saveFileToLocation(
                                        context,
                                        surveyDao,
                                        surveyAnswerDao,
                                        categoryDao,
                                        questionDao
                                    )
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
                                    Text("Share CSV")
                                }
                            },
                            onClick = {
                                dropdownMenuExpanded = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    shareFile(
                                        context,
                                        surveyDao,
                                        surveyAnswerDao,
                                        categoryDao,
                                        questionDao
                                    )
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
                                    Text("Import CSV")
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
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val surveyId = surveys.firstOrNull()?.surveyId ?: 0L
                            navController.navigate("categories_health?surveyId=$surveyId&isEditMode=false")
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
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                // Display content using LazyColumn with cards
                LazyColumn {
                    items(surveys) { survey ->
                        SurveyCard(survey) {
                            val surveyId = survey.surveyId
                            navController.navigate("category_list/${survey.surveyId}")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    )
}

@Composable
fun SurveyCard(survey: Survey, onClick: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    // val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    // val hasAnswersForSurvey by viewModel.getFullSurveyData(survey.surveyId).collectAsState(initial = emptyList())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Title and Edit Icon Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = survey.surveyTitle,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* Handle edit action */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Edit",
                        tint = colorResource(id = R.color.turquoise)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card Content
            Text(
                text = "Date: ${formatTimestamp(survey.timestamp)}",
                style = TextStyle(fontSize = 14.sp)
            )
            Text(
                text = "Coperative: ${survey.cooperativeName}",
                style = TextStyle(fontSize = 14.sp)
            )
            // Text(text = "Cooperative: ${survey.cooperativeName}", style = TextStyle(fontSize = 14.sp))
        }
    }
}

// Utility function to format the timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


// Function to create the CSV file
suspend fun createSurveyCsvFile(
    context: Context,
    surveyDao: SurveyDao,
    surveyAnswerDao: SurveyAnswerDao,
    categoryDao: CategoryDao,
    questionDao: QuestionDao
): File {
    // Step 1: Retrieve all surveys
    val surveys =
        surveyDao.getAllSurveys().first() // Use .first() to collect the Flow data as a list

    // Step 2: Prepare the file to write the CSV
    val csvFile = File(context.getExternalFilesDir(null), "surveys.csv")
    val fileOutputStream = withContext(Dispatchers.IO) {
        FileOutputStream(csvFile)
    }
    val writer = BufferedWriter(OutputStreamWriter(fileOutputStream))

    // Write the CSV headers
    withContext(Dispatchers.IO) {
        writer.write("Survey ID,Survey title,Surveyor name,Cooperative Name,Timestamp,Category Name,Question Text,Answer Text,Score\n")
    }

    // Step 3: Loop through all surveys and their answers
    for (survey in surveys) {
        // Retrieve answers for the survey
        val answers = surveyAnswerDao.getAnswersForSurvey(survey.surveyId).first()

        // For each answer, get the corresponding question and category
        for (answer in answers) {
            // Retrieve the question
            val question = questionDao.getQuestionById(answer.questionId)

            // Retrieve the category
            val category =
                categoryDao.getAllCategories().firstOrNull { it.categoryId == question?.categoryId }

            // Write the data to CSV
            if (question != null && category != null) {
                withContext(Dispatchers.IO) {
                    val surveyScore = "${survey.totalScore * 100}%"
                    Log.d("CSV Data", "${survey.surveyId},${survey.surveyTitle},${survey.respondentName},${survey.cooperativeName}")
                    writer.write("\"${survey.surveyId}\",\"${survey.surveyTitle}\",\"${survey.respondentName}\",\"${survey.cooperativeName}\",\"${survey.timestamp}\",\"${category.name}\",\"${question.questionText}\",\"${answer.answerText}\",\"${surveyScore}\"\n")

//                    writer.write("${survey.surveyId},${survey.surveyTitle},${survey.respondentName},${survey.cooperativeName},${survey.timestamp},${category.name},${question.questionText},${answer.answerText},${surveyScore}\n")
                }
            }
        }
    }

    // Step 4: Close the writer and return the file
    withContext(Dispatchers.IO) {
        writer.close()
    }
    return csvFile
}
