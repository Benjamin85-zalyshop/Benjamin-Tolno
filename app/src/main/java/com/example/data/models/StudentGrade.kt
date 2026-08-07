package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades")
data class StudentGrade(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int = 0,
    val studentId: Int = 0,
    val studentRemoteId: String = "",
    val subjectId: Int = 0,
    val subjectRemoteId: String = "",
    val term: String = "1er Trimestre",
    val evaluationScore: Float? = null,
    val examScore: Float? = null,
    val teacherComment: String? = null,
    val remoteId: String = ""
)
