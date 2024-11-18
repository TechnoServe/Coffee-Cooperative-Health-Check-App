package com.example.ccts.data


fun calculateQuestionScore(question: Question, userAnswer: Any?, answersMap: Map<Int, Any?>): Int {
    if (!question.scorable) return 0

    val score = when (question.type) {
        "number" -> {
            val userAnswerInt = when (userAnswer) {
                is String -> userAnswer.toDoubleOrNull() ?: 0.0
                is Double -> userAnswer
                is Float -> userAnswer.toDouble()
                is Int -> userAnswer.toDouble()
                else -> 0.0
            }

            val min = question.minScore?.toDouble() ?: Double.MIN_VALUE

            // Log values to check what's happening
            val previousAnswerDouble = (answersMap[2] as? String)?.toDoubleOrNull()
            (answersMap[11] as? String)?.toDoubleOrNull()

            if(question.id == 5){
                if ( previousAnswerDouble != null && previousAnswerDouble < userAnswerInt) {
                    1 // Score 1 if the previous answer is less than the current answer
                }
                else{
                    0
                }
            }
            else if (question.id == 11) {
                val answer11 = (answersMap[11] as? String)?.toDoubleOrNull() ?: return 0
                return if (answer11 >= min && (answer11 % 2) != 0.0) {
                    1
                } else {
                    0
                }
            } else if (userAnswerInt >= min) {
                1
            } else 0
        }

        "percentage" -> {
            if (userAnswer == null || userAnswer == "null") return 0
            val userAnswerFloat = userAnswer.toString().toFloat()
            val min = question.minScore?.toFloat() ?: Float.NEGATIVE_INFINITY
            if (userAnswerFloat >= min) 1 else 0
        }

        "yes_no" -> {
            // Adjusted logic for single correctAnswer as a string
            val correctAnswer = question.correctAnswer ?: ""  // Use the single correct answer
            if (userAnswer is String && userAnswer.equals(
                    correctAnswer,
                    ignoreCase = true
                )
            ) 1 else 0
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

    return score
}

fun calculateTotalScore(category: Category, answersState: MutableMap<Int, Any?>): Double {
    var categoryScore = 0.0

    // Iterate through each question in the category and calculate scores
    category.questions.forEach { question ->
        val answerValue = answersState[question.id]
        categoryScore += calculateQuestionScore(question, answerValue, answersState)
    }

    return categoryScore
}
