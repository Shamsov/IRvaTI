package com.irvati.lecturebank.data.repository

import com.irvati.lecturebank.data.api.RetrofitClient
import com.irvati.lecturebank.data.model.Subject
import com.irvati.lecturebank.data.model.SubjectCreate

// Репозиторий для работы с предметами
class SubjectRepository {

    private val apiService = RetrofitClient.apiService

    // Получение списка всех предметов
    suspend fun getSubjects(token: String): Result<List<Subject>> {
        return try {
            val response = apiService.getSubjects("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Ошибка загрузки предметов: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Создание нового предмета
    suspend fun createSubject(token: String, name: String, description: String?): Result<Subject> {
        return try {
            val response = apiService.createSubject(
                "Bearer $token",
                SubjectCreate(name, description)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка создания предмета: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Удаление предмета по ID
    suspend fun deleteSubject(token: String, id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteSubject("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка удаления предмета: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }
}
