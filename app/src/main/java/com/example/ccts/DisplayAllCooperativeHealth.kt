package com.example.ccts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAllCooperativeHealth(navController: NavHostController) {
    // Sample data for the table
    val checklistItems = listOf(
        ChecklistItem("2024-10-10", "CWS 1","5/20", 85,),
        ChecklistItem("2024-10-09", "CWS 2", "2/20",90),
        ChecklistItem("2024-10-08", "CWS 3", "10/20",70)
        // Add more items as needed
    )

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

                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id= R.color.turquoise)),


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
                        onClick = { navController.navigate("categories_health") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(colorResource(id= R.color.turquoise)),
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

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Date",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "CWS",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Total Answer ",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Score",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Content using LazyColumn
                LazyColumn {
                    items(checklistItems) { item ->
                        TableRow(item)
                    }
                }
            }
        }
    )
}

// Function to display a row in the table
@Composable
fun TableRow(item: ChecklistItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, shape = RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.date,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = item.cws,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = item.totalAnswer.toString(),
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = item.Score.toString(),
            modifier = Modifier.weight(1f),
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

// Data class to represent checklist items
data class ChecklistItem(val date: String, val cws: String, val totalAnswer:String, val Score: Int)
