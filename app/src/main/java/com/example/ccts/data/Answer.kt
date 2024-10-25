package com.example.ccts.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.sql.Date


//@Entity(
//    tableName = "Answers",
//            foreignKeys = [
//        ForeignKey(
//            entity = Survey::class,
//            parentColumns = ["surveyId"],
//            childColumns = ["surveyId"],
//            onDelete = ForeignKey.CASCADE // Optional: cascade delete answers if the survey is deleted
//        )
//    ]
//)
//data class Answers(
//    @PrimaryKey(autoGenerate = true) val id: Int = 0,
//    val surveyId: Long,
//    val questionId: Int,
//    val respondentName: String,
//    val coperativeName: String,
//    val userAnswers: String,
//    @ColumnInfo(name = "date") val date: String = java.util.Date().toString(),
//    val groupedAnswerId: String
//
//
//
//
//)