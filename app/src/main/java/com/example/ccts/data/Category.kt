package com.example.ccts.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

data class Category(
    val id: Int,
    val icon_path: String,
    val category: String,
    val questions: List<Question>
)

//data class SurveyWithCategories(
//    @Embedded val survey: Survey,
//    @Relation(
//        entity = SurveyCategory::class,
//        parentColumn = "surveyId",
//        entityColumn = "surveyId"
//    )
//    val categories: List<CategoryWithQuestions>
//)
//
//data class CategoryWithQuestions(
//    @Embedded val category: SurveyCategory,
//    @Relation(
//        entity = SurveyQuestion::class,
//        parentColumn = "categoryId",
//        entityColumn = "categoryId"
//    )
//    val questions: List<SurveyQuestion>
//)
//
//
//
//@Entity(
//    tableName = "survey_categories",
//    foreignKeys = [
//        ForeignKey(entity = Survey::class, parentColumns = ["surveyId"], childColumns = ["surveyId"], onDelete = ForeignKey.CASCADE)
//    ],
//    indices = [Index("surveyId")]
//)
//data class SurveyCategory(
//    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
//    val surveyId: Int, // Links this category to the survey
//    val categoryName: String
//)
//
//
//@Entity(
//    tableName = "survey_questions",
//    foreignKeys = [
//        ForeignKey(entity = SurveyCategory::class, parentColumns = ["categoryId"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)
//    ],
//    indices = [Index("categoryId")]
//)
//data class SurveyQuestion(
//    @PrimaryKey(autoGenerate = true) val questionId: Int = 0,
//    val categoryId: Int, // Links this question to a specific category
//    val questionText: String,
//    val answerText: String? = null // Stores the user's answer for this question
//)

// Entities
@Entity(tableName = "categories")
data class CategoryDb(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val name: String
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryDb::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"]
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class QuestionDb(
    @PrimaryKey(autoGenerate = true)
    val questionId: Int = 0,
    val categoryId: Int,
    val questionText: String,
)
@Entity(tableName = "surveys")
data class Survey(
    @PrimaryKey(autoGenerate = true) val surveyId: Int = 0,
    val surveyTitle: String,
    val respondentName:String,
    val cooperativeName: String,
    val totalScore: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "survey_answers",
    foreignKeys = [
        ForeignKey(
            entity = Survey::class,
            parentColumns = ["surveyId"],
            childColumns = ["surveyId"]
        ),
        ForeignKey(
            entity = QuestionDb::class,
            parentColumns = ["questionId"],
            childColumns = ["questionId"]
        )
    ],
    indices = [
        Index(value = ["surveyId", "questionId"], unique = true),
    ]
)
data class SurveyAnswer(
    @PrimaryKey(autoGenerate = true)
    val answerId: Int = 0,
    val surveyId: Int,
    val questionId: Int,
    val answerText: String?,
    val answerScore: Int = 0
)

//// SurveyCategory Entity with foreign key reference to Survey
//@Entity(
//    tableName = "survey_categories",
//    foreignKeys = [
//        ForeignKey(entity = Survey::class, parentColumns = ["surveyId"], childColumns = ["surveyId"], onDelete = ForeignKey.CASCADE)
//    ],
//    indices = [Index("surveyId")]
//)
//data class SurveyCategory(
//    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
//    val surveyId: Int, // Links this category to the survey
//    val categoryName: String
//)
//
//// SurveyQuestion Entity with foreign key reference to SurveyCategory
//@Entity(
//    tableName = "survey_questions",
//    foreignKeys = [
//        ForeignKey(entity = SurveyCategory::class, parentColumns = ["categoryId"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE),
//
//    ],
//    indices = [Index("categoryId")]
//)
//data class SurveyQuestion(
//    @PrimaryKey(autoGenerate = true) val questionId: Int = 0,
//
//    val categoryId: Int, // Foreign key to SurveyCategory
//    val surveyId: Int, // Links the question to the survey for reference
//    val questionText: String,
//    val answerText: String
//)
