package com.example.ccts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ccts.ui.theme.CCTSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavigationSetup(navController = navController)

        }
    }
}

@Composable
fun NavigationSetup(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash") {
        // Splash screen route
        composable("splash") {
            SplashScreen(navController = navController)
        }
        // Home screen route (main content)
        composable("home") {
            CoffeLandingScreen(navController = navController)
        }
        composable("cooperative_health") { DisplayAllCooperativeHealth(navController = navController) }
        composable("categories_health") { Categories_items(navController = navController) }
        composable("register_coffee") { RegisterCooperativeScreen(navController = navController) }
        composable("popup_coffee/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
            PopupActivity(navController = navController,categoryId = categoryId) }


    }
}





