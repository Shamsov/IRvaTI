package com.irvati.lecturebank.data.model

// Запрос к AI-поиску
data class AIQuery(
    val question: String,
    val subject_id: Int? = null
)

// Ответ AI-поиска с источниками
data class AIResponse(
    val answer: String,
    val sources: List<AISource>
)

// Источник (фрагмент лекции), использованный при генерации ответа
data class AISource(
    val lecture_title: String,
    val excerpt: String
)
