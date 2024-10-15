package com.example.ccts.data

data class Category(
    val id: Int,
    val icon_path: String,
    val category: String,
    val questions: List<Question>
)
