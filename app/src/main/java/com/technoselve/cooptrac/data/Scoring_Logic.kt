package com.technoserve.cooptrac.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
//fun calculateQuartersUpToNow(): Int {
//    val now = LocalDate.now()
//
//    // Define the quarter mapping explicitly
//    val monthToQuarter = mapOf(
//        1 to 1, 2 to 1, 3 to 1,  // Q1: January to March
//        4 to 2, 5 to 2, 6 to 2,  // Q2: April to June
//        7 to 3, 8 to 3, 9 to 3,  // Q3: July to September
//        10 to 4, 11 to 4, 12 to 4 // Q4: October to December
//    )
//
//    // Use the mapping to determine the current quarter
//    val currentQuarter = monthToQuarter[now.monthValue] ?: 0
//    Log.d("currentQuarter", "calculateQuartersUpToNow:$currentQuarter ")
//    return currentQuarter
//}

fun calculateCurrentQuarterAndDaysLeft(): Pair<Int, Int> {
    val now = LocalDate.now()

    // Define the start dates of each quarter
    val quarterStartDates = listOf(
        LocalDate.of(now.year, 1, 1),   // Q1: January 1
        LocalDate.of(now.year, 4, 1),   // Q2: April 1
        LocalDate.of(now.year, 7, 1),   // Q3: July 1
        LocalDate.of(now.year, 10, 1)   // Q4: October 1
    )

    val currentQuarter = quarterStartDates.indexOfLast { it <= now } + 1
    Log.d("currentQuarter", "calculateCurrentQuarterAndDaysLeft:$currentQuarter ")

    // Calculate the end date of the current quarter
    val nextQuarterStart = if (currentQuarter < 4) quarterStartDates[currentQuarter] else LocalDate.of(now.year + 1, 1, 1)
    val daysLeftInQuarter = now.until(nextQuarterStart).days

    return Pair(currentQuarter, daysLeftInQuarter)
}




@RequiresApi(Build.VERSION_CODES.O)
fun calculateQuestionScore(context: Context, question: Question, userAnswer: Any?, answersMap: Map<Int, Any?>): Pair<Int, String?> {
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val selectedLanguage = sharedPreferences.getString("selected_language", "English") ?: "English"

    if (!question.scorable) return Pair(0, null)
    if (userAnswer == null) { return Pair(0, null) }

    return when (question.type) {
        "number" -> {
            val userAnswerInt = when (userAnswer) {
                is String -> userAnswer.toDoubleOrNull() ?: return Pair(0, null)
                is Double -> userAnswer ?: return Pair(0, null)
                is Float -> userAnswer.toDouble() ?: return Pair(0, null)
                is Int -> userAnswer.toDouble() ?: return Pair(0, null)
                else -> return Pair(0, null) // Return 0 if it's not a valid number
            }

            val min = question.minScore?.toDouble() ?: Double.MIN_VALUE
            val max = question.maxScore?.toDouble() ?: Double.MAX_VALUE

            when (question.id) {
                5, 8 -> {
                    val previousAnswer = answersMap[if (question.id == 5) 5 else 8]?.toString()?.toDoubleOrNull() ?: 0.0
                    val threshold = previousAnswer * 0.30
                    if (userAnswerInt >= threshold) Pair(1, null) else Pair(0, question.recommendation)
                }
                9 -> {
                    val previousAnswer = answersMap[6]?.toString()?.toDoubleOrNull() ?: 0.0
                    if (previousAnswer <= userAnswerInt) Pair(1, null) else Pair(0, question.recommendation)
                }
                16 -> {
                    if (userAnswerInt >= min && userAnswerInt % 2 != 0.0) Pair(4, null)
//
                    else Pair(0, question.recommendation)
                }
                18 -> {
                    val requiredMeetingsPerQuarter = 1
                    val maxScore = 4

                    // Calculate the current quarter and remaining days in the quarter
                    val (currentQuarter, daysLeftInQuarter) = calculateCurrentQuarterAndDaysLeft()

                    // Calculate how many quarters the user has covered with their meetings
                    val quartersMet = (userAnswerInt / requiredMeetingsPerQuarter).toInt()

                    // Ensure the number of quartersMet doesn't exceed the current quarter
                    val effectiveQuartersMet = minOf(quartersMet, currentQuarter)

                    // Calculate partial score based on covered quarters
                    val partialScore = (effectiveQuartersMet.toDouble() / currentQuarter) * maxScore

                    return when {
                        // If the user hasn't held any meetings and there's time left in the quarter, recommend a meeting
                        userAnswerInt.toInt() == 0 && daysLeftInQuarter > 0 -> Pair(maxScore, if (selectedLanguage=="English") {"You still have time to conduct the meeting for this quarter."}else{"Uracyafite umwanya wo gukora inama muri iki gihembwe"})

                        // Full marks if they met all quarters up to now
                        effectiveQuartersMet == currentQuarter -> Pair(partialScore.toInt(), null)

                        // If there’s still time in the current quarter, give a chance for full marks
                        effectiveQuartersMet < currentQuarter && daysLeftInQuarter > 0 -> Pair(partialScore.toInt(), if (selectedLanguage=="English") {"You still have time to conduct the meeting for this quarter."}else{"Uracyafite umwanya wo gukora inama muri iki gihembwe"})

                        // Partial score if they covered some quarters but missed others
                        effectiveQuartersMet < currentQuarter -> Pair(partialScore.toInt(), "You missed meetings for some quarters.")

                        // If all quarters are missed
                        else -> Pair(0, "You missed the meetings for all required quarters.")
                    }
                }
                20 -> {
                val requiredMeetingsPerQuarter = 1
                val maxScore = 4

                // Calculate the current quarter and remaining days in the quarter
                val (currentQuarter, daysLeftInQuarter) = calculateCurrentQuarterAndDaysLeft()

                // Calculate how many quarters the user has covered with their meetings
                val quartersMet = (userAnswerInt / requiredMeetingsPerQuarter).toInt()
                    Log.d("quartersMet", "calculateCurrentQuarterAndDaysLeft:$quartersMet ")

                // Ensure the number of quartersMet doesn't exceed the current quarter
                val effectiveQuartersMet = minOf(quartersMet, currentQuarter)

                // Calculate partial score based on covered quarters
                val partialScore = (effectiveQuartersMet.toDouble() / currentQuarter) * maxScore

                return when {
                    // If the user hasn't held any meetings and there's time left in the quarter, recommend a meeting
                    userAnswerInt.toInt() == 0 && daysLeftInQuarter > 0 -> Pair(maxScore, "You still have time to conduct the meeting for this quarter.")

                    // Full marks if they met all quarters up to now
                    effectiveQuartersMet == currentQuarter -> Pair(partialScore.toInt(), null)

                    // If there’s still time in the current quarter, give a chance for full marks
                    effectiveQuartersMet < currentQuarter && daysLeftInQuarter > 0 -> Pair(partialScore.toInt(), "You still have time to conduct the meeting for this quarter.")

                    // Partial score if they covered some quarters but missed others
                    effectiveQuartersMet < currentQuarter -> Pair(partialScore.toInt(), "You missed meetings for some quarters.")

                    // If all quarters are missed
                    else -> Pair(0, "You missed the meetings for all required quarters.")
                }
            }

                21->{
                    if ( 0.0<userAnswerInt && userAnswerInt <=max) Pair(5, null) else Pair(0, question.recommendation)
                }
                22 -> {
                   if (0.0<userAnswerInt && userAnswerInt <=max) Pair(5, null) else Pair(0, question.recommendation)
                }
                31 ->{
                    if(userAnswerInt > 0.0 && userAnswerInt < min){
                       return Pair(3,question.recommendation)
                    }
                    else if (userAnswerInt in min..max){
                       return Pair(5,null)
                    }
                    else Pair(0,question.recommendation)
                }
                else ->
                    if (userAnswerInt >= min) Pair(4, null)
//                    else if (userAnswerInt == 0.0) {
//                        // Mark dependent questions as hidden (implementation may vary)
//                        Pair(0, null) // No score and no recommendation
//                    }
                    else Pair(0, question.recommendation)
            }
        }

        "percentage" -> {
            val userAnswerFloat = userAnswer.toString().toFloatOrNull() ?: return Pair(0, null)
            val min = question.minScore?.toFloat() ?: Float.NEGATIVE_INFINITY

            when (question.id) {
                17 -> if (userAnswerFloat >= min) Pair(1, null)
                else Pair(0, question.recommendation)
                30 -> if (userAnswerFloat >= min) Pair(5, null)
                else Pair(0, question.recommendation)
                else -> Pair(0, question.recommendation)
            }
        }

        "yes_no" -> {
            val correctAnswer = question.correctAnswer ?: ""
            if (userAnswer is String && userAnswer.equals(correctAnswer, ignoreCase = true)) {
                if (question.id == 24)
//                    3
                    Pair(3, null)
                else
//                    5
                    Pair(5, null)
            }
            else if (userAnswer==null){
                Pair(0, null)
            }
                else {
                Pair(0, question.recommendation)
            }
        }

        "checkbox" -> {

            val userSelections = when (val value =  userAnswer) {
                is Map<*, *> -> value.mapNotNull { (key, value) ->
                    if (key is String) {
                        // Handle both raw string and toString() representation of the map entry
                        val cleanKey = key.trim().lowercase().removeSurrounding("{", "}")
                        cleanKey to (value as? Boolean ?: false)
                    } else null
                }.toMap()
                is String -> {
                    // Handle string format: remove curly braces and split on comma
                    value.removeSurrounding("{", "}")
                        .split(",")
                        .associate {
                            val parts = it.split("=")
                            val option = parts[0].trim().lowercase()
                            val isChecked = parts.getOrNull(1)?.trim()?.toBoolean() ?: false
                            option to isChecked
                        }
                }
                else -> emptyMap()
            } ?: return Pair(0, null)
            val selectedCount = userSelections.count { it.value == true && it.key != "none" }
            if (selectedCount==question.weight){
                return Pair(selectedCount, null)
            }
           return  Pair(selectedCount, question.recommendation)
        }

        "text" -> {
            val minLength = question.lowerLength ?: 0
            if (userAnswer is String && userAnswer.length >= minLength) Pair(1, null) else Pair(0, question.recommendation)
        }

        else -> Pair(0, question.recommendation)
    }
}

//fun calculateQuestionScore(question: Question, userAnswer: Any?, answersMap: Map<Int, Any?>): Pair<Int, String?> {
//    if (!question.scorable) return Pair(0, null)
//
//    val score = when (question.type) {
//        "number" -> {
//
//
//            val userAnswerInt = when (userAnswer) {
//                is String -> userAnswer.toDouble()?:0.0
//                is Double -> userAnswer
//                is Float -> userAnswer.toDouble()
//                is Int -> userAnswer.toDouble()
//                else -> return Pair(0, null) // Return 0 if it's not a valid number
//            }
//
//            Log.d("userAnswerIn", "userAnswerIn: $userAnswerInt")
//            val min = question.minScore?.toDouble()?: Double.MIN_VALUE
//            val max = question.maxScore?.toDouble() ?: Double.MAX_VALUE
//
//            // Log values to check what's happening
//            Log.d("answersMap", "answersMap[4]: ${answersMap[5]?.javaClass?.name}")
//            val previousAnswerDouble = answersMap[5]?.toString()?.toDoubleOrNull() ?: 0.0
//            val  previousAnswerDouble8=answersMap[8]?.toString()?.toDoubleOrNull() ?: 0.0
//
//
//            if (question.id == 4) {
//                if (previousAnswerDouble== null) {
//                    Log.d("Error", "previousAnswerDouble5 is null")
////                    return 0
//                    return Pair(0, question.recommendation)
//                }
//                Log.d("previousAnswerDouble___", "previousAnswerDouble:$previousAnswerDouble ")
//                // Calculate 30% of previousAnswerDouble
//                val thirtyPercentOfPrevious = previousAnswerDouble * 0.30
//                if (userAnswerInt >= thirtyPercentOfPrevious) {
//                    // If userAnswerInt is at least 30% of previousAnswerDouble, return 1
//                    Log.d(
//                        "userAnswerInt-----",
//                        "userAnswerInt is at least 30% of previousAnswerDouble: $userAnswerInt"
//                    )
////                    return 1
//                    return Pair(1, null)
//                }
//                else {
//                    Log.d(
//                        "userAnswerInt-----",
//                        "userAnswerInt is at least 30% of previousAnswerDouble: $userAnswerInt"
//                    )
//                    Pair(0, question.recommendation)
////                    0
//                }
//
//
//
//            }
//
//            else if (question.id == 7) {
//                if (previousAnswerDouble8 == null) {
//                    Log.d("Error", "previousAnswerDouble5 is null")
////                    return 0
//                    return Pair(0, question.recommendation)
//                }
//                Log.d("previousAnswerDouble___", "previousAnswerDouble:$previousAnswerDouble8 ")
//                // Calculate 30% of previousAnswerDouble
//                val thirtyPercentOfPrevious = previousAnswerDouble8 * 0.30
//                if (userAnswerInt >= thirtyPercentOfPrevious) {
//                    // If userAnswerInt is at least 30% of previousAnswerDouble, return 1
//                    Log.d(
//                        "userAnswerInt-----",
//                        "userAnswerInt is at least 30% of previousAnswerDouble: $userAnswerInt"
//                    )
////                    return 1
//                    return Pair(1, null)
//                }
//                else {
//                    Log.d(
//                        "userAnswerInt-----",
//                        "userAnswerInt is at least 30% of previousAnswerDouble: $userAnswerInt"
//                    )
////                    return 0
//                    return Pair(0, question.recommendation)
//                }
//
//
//
//            }
//            else if(question.id == 8){
//
//                if ( previousAnswerDouble != null && previousAnswerDouble < userAnswerInt) {
//                    Log.d("userAnswerInt---", "previousAnswerDouble:$userAnswerInt ")
////                    return 1 // Score 1 if the previous answer is less than the current answer
//                    return Pair(1, null)
//                }
//                else{
////                    0
//                    Pair(0, question.recommendation)
//                }
//            }
//
//            else  if  (question.id == 14) {
//                val userAnswerInt1 = userAnswerInt?.toString()?.toDoubleOrNull() ?: return Pair(0, question.recommendation)
//                if (userAnswerInt1 >= min && (userAnswerInt % 2) != 0.0) {
//                    Log.d("answersMapssss", "answersMap[4]: ${userAnswerInt1?.javaClass?.name}")
//                    Log.d("AnswerCheck", "Answer for question 11 is odd and greater than 5: $userAnswerInt1")
////                    4
//                    Pair(4, null)
//                } else {
////                    0
//                    Pair(0, question.recommendation)
//                }
//            }
//           else if (question.id == 19) {
//                val requiredMeetingsPerQuarter = 1 // Customize as needed
//
//                // Calculate the number of quarters covered up to the current date
//                val coveredQuarters = calculateQuartersUpToNow()
//
//                // Determine how many quarters the user fully meets
//                val quartersMet = (userAnswerInt / requiredMeetingsPerQuarter).toInt()
//                val effectiveQuartersMet = minOf(quartersMet, coveredQuarters) // Cap at total covered quarters
//
//                // Calculate proportional score
//                val maxScore = 4
//                val partialScore = (effectiveQuartersMet.toDouble() / coveredQuarters) * maxScore
//
////                return partialScore.toInt() // Return the score as an integer
//                return Pair(partialScore.toInt(), null)
//            }
//
//            // Other question ID logic can remain unchanged
////            else if (question.id == 20) {
////                val requiredMeetingsPerQuarter = 1 // Customize as needed
////
////                // Calculate the number of quarters covered up to the current date
////                val coveredQuarters = calculateQuartersUpToNow()
////
////                // Determine how many quarters the user fully meets
////                val quartersMet = (userAnswerInt / requiredMeetingsPerQuarter).toInt()
////                val effectiveQuartersMet = minOf(quartersMet, coveredQuarters) // Cap at total covered quarters
////
////                // Calculate proportional score
////                val maxScore = 4
////                val partialScore = (effectiveQuartersMet.toDouble() / coveredQuarters) * maxScore
////
////                return partialScore.toInt() // Return the score as an integer
////            }
//
//
//
//            else  if  (question.id == 20) {
//                if (userAnswerInt in min..max){
////                    5
//                    Pair(5, null)
//                }
//                else{
////                    0
//                    Pair(0, question.recommendation)
//                }
//            }
//            else if (userAnswerInt >= min) {
//                Log.d("AnswerChec", "Answer for question 11 is odd and greater than 5: $userAnswerInt")
////                4
//                Pair(4, null)
//
//            }
//
//
//            else
////                0
//                Pair(0, question.recommendation)
//            // Score 0 otherwise
//
//
//        }
//
//        "percentage" -> {
//            if (userAnswer == null || userAnswer == "null") return Pair(0, question.recommendation)
//            val userAnswerFloat = userAnswer.toString().toFloat()
//            val min = question.minScore?.toFloat() ?: Float.NEGATIVE_INFINITY
//
//                if (question.id == 15) {
//                    if (userAnswerFloat >= min) {
//                        1
//                    }else 0
//                } else if (question.id == 29){
//                    if (userAnswerFloat >= min) {
////                    5
//                        Pair(5, null)
//                } else
////                    0
//                        Pair(0, question.recommendation)
//
//            }
//            else
////                0
//                    Pair(0, question.recommendation)
//
//        }
//
//        "yes_no" -> {
//            // Adjusted logic for single correctAnswer as a string
//            val correctAnswer = question.correctAnswer ?: ""  // Use the single correct answer
//            if (userAnswer is String && userAnswer.equals(
//                    correctAnswer,
//                    ignoreCase = true
//                )
//            ) {
//                if(question.id==23)
////                    3
//                    Pair(3, null)
//                else
////                    5
//                    Pair(5, null)
//            }
//            else
////                0
//                Pair(0, question.recommendation)
//
//        }
//        "checkbox" -> {
//            // Ensure userAnswer is a Map (selections) and handle null gracefully
//            val userSelections = userAnswer as? Map<*, *> ?: return Pair(0, question.recommendation)
//
//            // Count the number of selected options
//            val selectedCount = userSelections.filter { it.value == true && it.key != "none" }.size
//
//            // Total options available for the question
//            val totalOptions = question.options?.size ?: 1
//            Log.d("totalOptions", "calculateQuestionScore:$totalOptions ")
//
//            val weight = question.weight ?: 1
//
//            // Calculate proportional score
//            val totalScore = selectedCount
//            Log.d("totalScore", "calculateQuestionScore:$totalScore ")
//
//            // Return the calculated score
////            totalScore
//            Pair(totalScore, question.recommendation)
//        }
//
//
//
//        "text" -> {
//            val minLength = question.lowerLength ?: 0
//            if (userAnswer is String && userAnswer.length >= minLength)
////                1
//                Pair(1, null)
//
//
//            else
////                0
//                question.recommendation
//
//        }
//
//        else ->
////            0
//            question.recommendation
//    }
//
//    return score
//}

//@RequiresApi(Build.VERSION_CODES.O)
//fun calculateTotalScore(category: Category, answersState: MutableMap<Int, Any?>): Double {
//    var categoryScore = 0.0
//
//    // Iterate through each question in the category and calculate scores
//    category.questions.forEach { question ->
//        val answerValue = answersState[question.id]
//        categoryScore += calculateQuestionScore(question, answerValue, answersState)
//        Log.d(" categoryScore", "Answer for ${question.id} is odd and greater than 5: $categoryScore")
//    }
//
//    return categoryScore
//}



@RequiresApi(Build.VERSION_CODES.O)
fun calculateTotalScore(
    context: Context,
    category: Category,
    answersState: MutableMap<Int, Any?>
): Pair<Double, List<String>> {
    var categoryScore = 0.0
    val recommendations = mutableListOf<String>()

    // Iterate through each question in the category and calculate scores
    category.questions.forEach { question ->
        val answerValue = answersState[question.id]
        val (score, recommendation) = calculateQuestionScore(context, question, answerValue, answersState)

        // Add the score
        categoryScore += score.toDouble()

        // Append recommendations if available
        recommendation?.let { recommendations.add(it) }

        Log.d("categoryScore", "Answer for ${question.id} updated the score to: $categoryScore")
    }

    return Pair(categoryScore, recommendations)
}

