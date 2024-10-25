

//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import com.example.ccts.data.AnswersViewModel
//import com.example.ccts.adapter.AnswersViewModelFactory
//import com.example.ccts.data.Answers
//import com.example.ccts.data.AppDatabase
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DisplayAllCooperativeHealth(navController: NavHostController) {
//    // Sample data for the cards
//    val checklistItems = listOf(
//        ChecklistItem("2024-10-10", "CWS 1","5/20", 85),
//        ChecklistItem("2024-10-09", "CWS 2", "2/20", 90),
//        ChecklistItem("2024-10-08", "CWS 3", "10/20", 70)
//    )
//    val context = LocalContext.current
//    val database = AppDatabase.getDatabase(context) // Get the database instance
//    val answerDao = database?.answerDao()
//    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(context))
//    val answersList = viewModel.answersBySurveyId.collectAsState()
//
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "CCTS CHECKLIST",
//                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
//                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id = R.color.turquoise)),
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
//                        onClick = { navController.navigate("categories_health") },
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
//
//                // Display content using LazyColumn with cards
//                LazyColumn {
//                    items(answersList.value) { item ->
//                        ChecklistCard(item,navController)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//            }
//        }
//    )
//}
//
//@Composable
//fun ChecklistCard(item: Answers,navController: NavHostController) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .background(Color.White, shape = RoundedCornerShape(8.dp)),
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
//                    text = "Checklist Summary",
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
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Card Content in Column
//            Text(text = "Date: ${item.date}", style = TextStyle(fontSize = 14.sp))
//            Text(text = "Cooperative: ${item.coperativeName}", style = TextStyle(fontSize = 14.sp))
//
//        }
//    }
//}
//
//// Data class for checklist items
//data class ChecklistItem(val date: String, val cws: String, val totalAnswer: String, val Score: Int)
package com.example.ccts

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
//import com.example.ccts.adapter.AnswersViewModel
//import com.example.ccts.adapter.AnswersViewModelFactory
//import com.example.ccts.data.AnswerDao
//import com.example.ccts.data.Answers
import com.example.ccts.data.AppDatabase
import com.example.ccts.data.Survey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAllCooperativeHealth(navController: NavHostController) {
//    val context = LocalContext.current
//    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(context))

    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))


    val surveys by viewModel.surveys.collectAsState(initial = emptyList())

    val selectedSurveyId = surveys.firstOrNull()?.surveyId ?: 0





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
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle overflow menu */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.dots_horizontal_svgrepo_com__1_),
                            contentDescription = "More options",
                            tint = Color.White
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
                            navController.navigate("categories_health?surveyId=$surveyId&isEditMode=false")},
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
                        SurveyCard(survey){
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
fun SurveyCard(survey: Survey,onClick: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnswersViewModel = viewModel(factory = AnswersViewModelFactory(application))
    val hasAnswersForSurvey by viewModel.getAnswersForSurvey(survey.surveyId).collectAsState(initial = emptyList())
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
            Text(text = "Date: ${formatTimestamp(survey.timestamp)}", style = TextStyle(fontSize = 14.sp))
            Text(text = "Coperative: ${survey.cooperativeName}", style = TextStyle(fontSize = 14.sp))
            // Text(text = "Cooperative: ${survey.cooperativeName}", style = TextStyle(fontSize = 14.sp))
        }
    }
}

// Utility function to format the timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
