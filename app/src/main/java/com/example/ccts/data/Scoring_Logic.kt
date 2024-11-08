package com.example.ccts.data

fun scoreAnswer(question: Question, userAnswer: Any): Int {
    if (!question.scorable) return 0

    return when (question.type) {
        "number" -> {
            if (userAnswer is Int) {
                if (userAnswer in question.minScore!!..question.maxScore!!) 1 else 0
            } else 0
        }
        "percentage" -> {
            if (userAnswer is Int) {
                if (userAnswer in question.minScore!!..question.maxScore!!) 1 else 0
            } else 0
        }
        "yes_no" -> {
            if (userAnswer is String && userAnswer == question.correctAnswers.firstOrNull()) 1 else 0
        }
        "checkbox" -> {
            if (userAnswer is List<*>) {
                if (userAnswer.containsAll(question.correctAnswers!!)) 1 else 0
            } else 0
        }
        "text" -> {
            if (userAnswer is String) {
                val lengthCorrect = userAnswer.length >= question.lowerLength!!
                if (lengthCorrect) 1 else 0
            } else 0
        }
        else -> 0
    }
}


//fun calculateTotalScore(questions: List<Question>, answers: Map<String, Any>): Int {
//    var totalScore = 0
//    for (question in questions) {
//        val userAnswer = answers[question.id.toString()]
//        if (userAnswer != null) {
//            totalScore += scoreAnswer(question, userAnswer)
//        }
//    }
//    return totalScore
//}

fun calculateTotalScore(questions: List<Question>, answers: Map<String, *>): Int {
    val totalScore = 10
    return totalScore
}
