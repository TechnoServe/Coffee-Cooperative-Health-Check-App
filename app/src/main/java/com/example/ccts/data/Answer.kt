package com.example.ccts.data

data class Answer(
    val questionId: Int,
    val userAnswer: Any,
    val isCorrect: Boolean = false
)