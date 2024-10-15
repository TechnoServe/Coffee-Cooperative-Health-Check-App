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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Categories_items(navController: NavHostController) {
    var respondentName by remember { mutableStateOf(TextFieldValue()) }
    var selectedCooperative by remember { mutableStateOf("Select cooperative") }
    var expanded by remember { mutableStateOf(false) }
    var cooperatives= listOf("COOPERATIVE 1","COOPERATIVE 2","COOPERATIVE 3")
    val categories = remember { mutableStateListOf<Category>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load categories once
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val loadedCategories = loadCategoriesAndQuestion(context)
            categories.addAll(loadedCategories)
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
                    label = { Text("Respondent Name") },
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        cursorColor = Color.Black,
                        focusedLabelColor = colorResource(id = R.color.turquoise), // Label color when focused
                        unfocusedLabelColor = Color.Gray,    // Label color when not focused
                        focusedIndicatorColor = colorResource(id = R.color.turquoise), // Border color when focused
                        unfocusedIndicatorColor = Color.Gray // Border color when not focused
                    )
                )

                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.width(300.dp)
                                .padding(16.dp)
                        ) {
                            cooperatives.forEach { cooperative ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedCooperative = cooperative
                                        expanded = false
                                    }, text = { Text(text = cooperative) })
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
                        CategoryButton(category = category, navController)
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
                        // Submit Button
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .height(50.dp)
                                .width(150.dp),

                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise))
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
fun CategoryButton(category: Category, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(6.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
            .clickable { navController.navigate("popup_coffee/${category.id}")  },
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
            tint = colorResource(id = R.color.turquoise),
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
            .padding(bottom= 4.dp)

    )
}


