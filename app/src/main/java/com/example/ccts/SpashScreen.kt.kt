package com.example.ccts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

// SplashScreen Composable function with one coffee image
@Composable
fun SplashScreen(navController: NavController) {
    // Create a variable to track animation state
    var startAnimation by remember { mutableStateOf(false) }

    // Define the animated values for scaling and alpha
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Start the animation with delay
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Splash screen duration
        // Navigate to next screen after splash screen ends
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Display splash screen with animated coffee image
    Surface(
        modifier = Modifier
            .fillMaxSize(),

        color = if (isSystemInDarkTheme()) Color.White else Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Load the coffee image from drawable using painterResource
            Image(
                painter = painterResource(id = R.drawable.splashscreen), // Ensure this is your image in drawable
                contentDescription = "Coffee Image",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value),
                contentScale = ContentScale.Fit
            )
        }
    }
}
