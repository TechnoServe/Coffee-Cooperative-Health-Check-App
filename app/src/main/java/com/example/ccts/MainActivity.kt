package com.example.ccts

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory
import com.example.ccts.data.Survey

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("SurveyAnswers", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: AnswersViewModel =
                viewModel(factory = AnswersViewModelFactory(application)) // You can provide a factory if needed
            NavigationSetup(navController = navController, viewModel = viewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationSetup(navController: NavHostController, viewModel: AnswersViewModel) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Splash screen route
//        composable("splash") {
//            SplashScreen(navController = navController)
//        }
        // Home screen route (main content)
        composable("home") {
            CoffeLandingScreen(navController = navController)
        }
        composable("cooperative_health") {
            DisplayAllCooperativeHealth(navController = navController)
        }
        composable("categories_health") { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId")?.toIntOrNull() ?: 0
            Categories_items(
                navController = navController,

                )
        }
        composable("register_coffee") { RegisterCooperativeScreen(navController = navController) }
        composable("popup_coffee/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
            PopupActivity(
                navController = navController,
                categoryId = categoryId,
                {},
                mutableMapOf()
            )
        }

        composable(
            "category_list/{surveyId}",
            arguments = listOf(navArgument("surveyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getInt("surveyId")

            // State to hold the survey data
            var survey by remember { mutableStateOf<Survey?>(null) }

            // Launch a coroutine to fetch the survey data
            LaunchedEffect(surveyId) {
                if (surveyId != null) {
                    survey = viewModel.getSurveyById(surveyId)
                }
            }

            // Only render the screen if survey data is available
            survey?.let {
                CategoryListScreen(navController, viewModel, it)
            }
        }

    }
}





