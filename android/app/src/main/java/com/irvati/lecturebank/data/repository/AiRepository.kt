package com.irvati.lecturebank.data.repository

import com.irvati.lecturebank.data.api.RetrofitClient
import com.irvati.lecturebank.data.model.AIQuery
import com.irvati.lecturebank.data.model.AIResponse

// Репозиторий для AI-поиска по лекциям
class AiRepository {

    private val apiService = RetrofitClient.apiService

    // Отправка вопроса и получение ответа от AI
    suspend fun search(
        token: String,
        question: String,
        subjectId: Int? = null
    ): Result<AIResponse> {
        return try {
            val response = apiService.aiSearch(
                "Bearer $token",
                AIQuery(question, subjectId)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка AI-поиска: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }
}
