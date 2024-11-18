package com.example.ccts


import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun CoffeLandingScreen(navController: NavHostController) {
    var selectedLanguage by remember {
        mutableStateOf("English")
    }
    val languages = listOf("English", "Kinyarwanda")
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_4),
            contentDescription = "Coffee background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        DropdownMenu(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { selectedLanguage = it },
            languages = languages
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.padding(top = 200.dp)

        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Makes the box take full width
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.7f))

                    .padding(16.dp),

                contentAlignment = Alignment.Center,

                ) {

                BasicText(
                    text = "Welcome to the Coffee Cooperative Tracking System",

                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    modifier = Modifier.padding(16.dp)


                )
            }

        }


        Button(
            onClick = {
                navController.navigate("cooperative_health")
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.turquoise)),
            modifier = Modifier
                .align(Alignment.Center)
                .width(150.dp)
                .height(50.dp)
        ) {
            Text(
                text = "Get Started",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
        }


    }
}

@Composable
fun DropdownMenu(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    languages: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .padding(top = 8.dp),

        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,

            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                .clickable { expanded = true }
                .padding(8.dp)

        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_language_24),
                contentDescription = "Language Icon",
                modifier = Modifier.size(24.dp),
                tint = Color.Black,

                )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = selectedLanguage,
                modifier = Modifier
                    .clickable { expanded = true }
                    .background(Color.White)
                    .padding(16.dp),
                style = TextStyle(
                    color = Color.Black
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }, text = {
                        Text(text = language)
                    })
                }
            }
        }
    }
}


