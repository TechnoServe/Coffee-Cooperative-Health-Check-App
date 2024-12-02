package com.technoserve.cooptrac.data

data class Question(
    val id: Int,
    val categoryid: Int,
    val question: String,
    val type: String,
    val dependsOn: DependsOn? = null,
    val scorable: Boolean,
    val maxScore: Double? = null,
    val minScore: Double? = null,
    val lowerLength: Int? = null,
    val options: List<String>? = null,
    val correctAnswer: String? = null,
    val correctAnswers: List<String>? = null,
    val recommendation: String? = null,

    val weight: Int

)

data class DependsOn(
    val questionId: Int,  // ID of the question that determines visibility
    val value: String     // The value of the answer to that question that triggers visibility
)