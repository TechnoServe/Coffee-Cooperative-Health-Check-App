package com.example.ccts

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ccts.data.AnswersViewModel
import com.example.ccts.data.AnswersViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("SurveyAnswers", Context.MODE_PRIVATE)
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

@Composable
fun NavigationSetup(navController: NavHostController, viewModel: AnswersViewModel) {
    NavHost(
        navController = navController,
        startDestination = "home") {
        // Splash screen route
//        composable("splash") {
//            SplashScreen(navController = navController)
//        }
        // Home screen route (main content)
        composable("home") {
            CoffeLandingScreen(navController = navController)
        }
        composable("cooperative_health") {
            DisplayAllCooperativeHealth(navController = navController) }
        composable("categories_health") {backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId")?.toIntOrNull() ?: 0
            Categories_items(
                navController = navController,

                ) }
        composable("register_coffee") { RegisterCooperativeScreen(navController = navController) }
        composable("popup_coffee/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
            PopupActivity(navController = navController,categoryId = categoryId) }

        composable(
            "category_list/{surveyId}",
            arguments = listOf(navArgument("surveyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getInt("surveyId")
            if (surveyId != null) {
                val survey = viewModel.getSurveyById(surveyId)
                if (survey != null) {
                    CategoryListScreen(navController, viewModel, survey) // Pass the Survey object
                }
            }
        }
        composable("category_detail/{surveyId}/{categoryId}", arguments = listOf(
            navArgument("surveyId") { type = NavType.IntType },
            navArgument("categoryId") { type = NavType.IntType }
        )) { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getInt("surveyId") ?: return@composable
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: return@composable
            CategoryDetailScreen(navController = navController,viewModel, surveyId, categoryId)
        }


    }
}





