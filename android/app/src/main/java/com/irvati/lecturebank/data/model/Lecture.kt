package com.irvati.lecturebank.data.model

// Модель лекции
data class Lecture(
    val id: Int,
    val title: String,
    val subject_id: Int,
    val file_path: String,
    val created_at: String
)

// Содержимое лекции (извлечённый текст)
data class LectureContent(
    val id: Int,
    val title: String,
    val text_content: String?
)
