package com.example.ccts.data

data class Question(
    val id: Int,
    val categoryid: Int,
    val question: String,
    val type: String,
    val scorable: Boolean,
    val maxScore: Int? = null,
    val minScore: Int? = null,
    val lowerLength: Int? = null,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val correctAnswers: List<String>? = null,
    val weight: Int

)