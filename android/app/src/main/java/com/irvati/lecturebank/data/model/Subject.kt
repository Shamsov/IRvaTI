package com.irvati.lecturebank.data.model

// Модель учебного предмета
data class Subject(
    val id: Int,
    val name: String,
    val description: String?,
    val created_at: String
)

// Запрос на создание нового предмета
data class SubjectCreate(
    val name: String,
    val description: String? = null
)
