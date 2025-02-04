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
                    val previousAnswer = answersMap[if (question.id == 5) 6 else 9]?.toString()?.toDoubleOrNull() ?: 0.0
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
                        userAnswerInt.toInt() == 0 && daysLeftInQuarter > 0 -> Pair(maxScore, if (selectedLanguage=="English") {"The Board of Directors committee still has time to conduct the meeting for this quarter."}else{"Komite y'Inama y'inama y'ubutegetsi iracyafite igihe cyo gukora inama y'igihembwe"})

                        // Full marks if they met all quarters up to now
                        effectiveQuartersMet == currentQuarter -> Pair(partialScore.toInt(), null)

                        // If there’s still time in the current quarter, give a chance for full marks
                        effectiveQuartersMet < currentQuarter && daysLeftInQuarter > 0 -> Pair(partialScore.toInt(), if (selectedLanguage=="English") {"The Board of Director committee still have time to conduct the meeting for this quarter."}else{"Komite y'Inama y'inama y'ubutegetsi iracyafite igihe cyo gukora inama y'igihembwe"})

                        // Partial score if they covered some quarters but missed others
                        effectiveQuartersMet < currentQuarter -> Pair(partialScore.toInt(), if (selectedLanguage=="English"){"The board of directors committee has missed meetings for some quarters."}else{"Komite y'inama y'ubutegetsi nta nama yakoze muri iki gihembwe"})

                        // If all quarters are missed
                        else -> Pair(0,if (selectedLanguage=="English"){ "The board of directors committee has not had the required meetings in each quarter."}else{"Komite y'inama y'ubutegetsi ntabwo yakoze inama zikenewe mu gihembwe."})
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
                    userAnswerInt.toInt() == 0 && daysLeftInQuarter > 0 -> Pair(maxScore, if (selectedLanguage=="English"){"The Supervisory committee still has time to conduct the meeting for this quarter."}else{"Komite ngenzuzi iracyafite igihe cyo gukora inama muri iki gihembwe."})

                    // Full marks if they met all quarters up to now
                    effectiveQuartersMet == currentQuarter -> Pair(partialScore.toInt(), null)

                    // If there’s still time in the current quarter, give a chance for full marks
                    effectiveQuartersMet < currentQuarter && daysLeftInQuarter > 0 -> Pair(partialScore.toInt(), if (selectedLanguage=="English"){"The Supervisory committee still have time to conduct the meeting for this quarter."}else{"Komite ngenzuzi ntabwo yakoze inama muri iki gihembwe"})

                    // Partial score if they covered some quarters but missed others
                    effectiveQuartersMet < currentQuarter -> Pair(partialScore.toInt(),if (selectedLanguage=="English"){ "The Supervisory committee has missed meetings for some quarters."}else{"Komite ngenzuzi ntabwo yakoze inama muri iki gihembwe"})

                    // If all quarters are missed
                    else -> Pair(0, if (selectedLanguage=="English"){"The Supervisory committee has not had the required meetings in each quarter."}else{"Koperative igomba kuba ifite izi ibyamgombwa byose nkuko bisabwa nI itegeko rya koperative."})
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
                if (question.id == 25)
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

