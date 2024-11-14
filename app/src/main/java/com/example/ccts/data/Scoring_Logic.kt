package com.example.ccts.data

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson


fun calculateQuestionScore(question: Question, userAnswer: Any?, answersMap: Map<Int, Any?> ): Int {
    if (!question.scorable) return 0

    val score = when (question.type) {
        "number" -> {


            val userAnswerInt = when (userAnswer) {
                is String -> userAnswer.toDouble()
                is Double -> userAnswer
                is Float -> userAnswer.toDouble()
                is Int -> userAnswer.toDouble()
                else -> return 0 // Return 0 if it's not a valid number
            }

            Log.d("userAnswerIn", "userAnswerIn: $userAnswerInt")
            val min = question.minScore?.toDouble()?: Double.MIN_VALUE
//            val max = question.maxScore?.toDouble() ?: Double.MAX_VALUE

            // Log values to check what's happening
            val previousAnswerDouble = (answersMap[2] as? String)?.toDoubleOrNull()
            val answer11 = (answersMap[11] as? String)?.toDoubleOrNull()

            Log.d("previousAnswerDouble", "previousAnswerDouble:$previousAnswerDouble ")
            if (question.id == 5 && previousAnswerDouble != null && previousAnswerDouble < userAnswerInt) {
                1 // Score 1 if the previous answer is less than the current answer
            }
            if (question.id == 11) {
                val answer11 = (answersMap[11] as? String)?.toDoubleOrNull() ?: return 0
                if (answer11 >= min && (answer11 % 2) != 0.0) {
                    Log.d("AnswerCheck", "Answer for question 11 is odd and greater than 5: $answer11")
                    return 1
                } else {
                    return 0
                }
            }
            else if (userAnswerInt >= min) {
                1
                Log.d("AnswerChec", "Answer for question 11 is odd and greater than 5: $userAnswerInt")
            }



            else 0
            // Score 0 otherwise


        }
        "percentage" -> {
            val userAnswerFloat = (userAnswer as? Float) ?: return 0
            val min = question.minScore?.toFloat() ?: Float.NEGATIVE_INFINITY
//            val max = question.maxScore?.toFloat() ?: Float.POSITIVE_INFINITY
            if (userAnswerFloat >= min) 1 else 0
        }
        "yes_no" -> {
            // Adjusted logic for single correctAnswer as a string
            val correctAnswer = question.correctAnswer ?: ""  // Use the single correct answer
            if (userAnswer is String && userAnswer.equals(correctAnswer, ignoreCase = true)) 1 else 0
        }
        "checkbox" -> {
            val correctAnswers = question.correctAnswers ?: emptyList<Any>()
            if (userAnswer is List<*> && userAnswer.containsAll(correctAnswers)) 1 else 0
        }
        "text" -> {
            val minLength = question.lowerLength ?: 0
            if (userAnswer is String && userAnswer.length >= minLength) 1 else 0
        }
        else -> 0
    }

    Log.d("QuestionScore", "Question ID: ${question.id}, Type: ${question.type}, User Answer: $userAnswer, Score: $score")
    return score
}

fun calculateTotalScore(category: Category, sharedPreferences: SharedPreferences): Double {
    var categoryScore = 0.00
    val answersMap = mutableMapOf<Int, Any?>()

    // Load all answers into the map
    category.questions.forEach { question ->
        val answerKey = "answer_${category.id}_${question.id}"
        val answerValue: Any? = when (question.type) {
            "percentage","number" -> sharedPreferences.getFloat(answerKey, -1f).takeIf { it >= 0 }
            "checkbox" -> {
                val jsonString = sharedPreferences.getString(answerKey, null)
                jsonString?.let {
                    Gson().fromJson(it, List::class.java) ?: emptyList<String>()
                }

            }

            else -> sharedPreferences.getString(answerKey, null)
        }
        answersMap[question.id] = answerValue

    }

    // Calculate the score using answersMap to access previous answers by ID
    category.questions.forEach { question ->
        val answerValue = answersMap[question.id]
        Log.d("answerValue", "answerValue: $answerValue,Answer Type: ${answerValue?.javaClass?.simpleName}  answermap:$answersMap")
        categoryScore += calculateQuestionScore(question, answerValue, answersMap)
    }

    return categoryScore
}



//fun calculateTotalScore(category: Category, sharedPreferences: SharedPreferences): Double {
//    var categoryScore = 0.00
//
//    var previousAnswer: Any? = null
//
//    category.questions.forEach { question ->
//        val answerKey = "answer_${category.id}_${question.id}"
//
//        // Retrieve and process each answer based on question type
//        val answerValue: Any? = when (question.type) {
//            "percentage" -> sharedPreferences.getFloat(answerKey, -1f).takeIf { it >= 0 }
//            "checkbox" -> {
//                val jsonString = sharedPreferences.getString(answerKey, null)
//                jsonString?.let {
//                    Gson().fromJson(it, List::class.java) ?: emptyList<String>()
//                }
//            }
//            else -> sharedPreferences.getString(answerKey, null)
//        }
//
//
//        Log.d("RetrievedAnswers", "Key: $answerKey, Value: $answerValue")
//
//        // Calculate score if answer is not null or empty
//        if (answerValue != null) {
//            categoryScore += calculateQuestionScore(question, answerValue)
//
//        }
//    }
//    Log.d("Value", " Value: $categoryScore")
//    return categoryScore
//}

