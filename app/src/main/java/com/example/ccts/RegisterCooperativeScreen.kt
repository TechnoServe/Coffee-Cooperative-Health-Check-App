package com.example.ccts


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCooperativeScreen(navController: NavHostController) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var ownerName by remember { mutableStateOf(TextFieldValue()) }
    var location by remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Station", fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("categories_health")}) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24), // Replace with actual back arrow icon
                            contentDescription = "Back",
                            tint= Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorResource(id= R.color.turquoise)) // Brown color for top bar
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Banner
                Image(
                    painter = painterResource(id = R.drawable.coffee_coperative), // Replace with your image resource
                    contentDescription = "Banner Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Form Title
                Text(
                    text = "Register the Cooperative",
                    fontSize = 22.sp,
                    color = colorResource(id= R.color.turquoise),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name TextField
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(containerColor = colorResource(id= R.color.grey))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Owner Name TextField
                TextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(containerColor = colorResource(id= R.color.grey))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location TextField
                TextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(containerColor = colorResource(id= R.color.grey))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = { /* Handle register action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(colorResource(id= R.color.turquoise))
                ) {
                    Text(text = "Register", color = Color.White)
                }
            }
        }
    )
}
