package com.irvati.lecturebank.data.repository

import android.content.Context
import android.net.Uri
import com.irvati.lecturebank.data.api.RetrofitClient
import com.irvati.lecturebank.data.model.Lecture
import com.irvati.lecturebank.data.model.LectureContent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

// Репозиторий для работы с лекциями
class LectureRepository {

    private val apiService = RetrofitClient.apiService

    // Получение списка лекций для конкретного предмета
    suspend fun getLectures(token: String, subjectId: Int): Result<List<Lecture>> {
        return try {
            val response = apiService.getLectures("Bearer $token", subjectId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Ошибка загрузки лекций: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Загрузка файла лекции на сервер
    suspend fun uploadLecture(
        token: String,
        title: String,
        subjectId: Int,
        fileUri: Uri,
        context: Context
    ): Result<Lecture> {
        return try {
            // Копируем файл во временный кэш для отправки
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Не удалось открыть файл"))

            val fileName = getFileName(context, fileUri) ?: "lecture_file"
            val tempFile = File(context.cacheDir, fileName)
            FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }

            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)

            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val subjectIdBody = subjectId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadLecture("Bearer $token", titleBody, subjectIdBody, filePart)

            tempFile.delete()

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки лекции: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Получение текстового содержимого лекции
    suspend fun getLectureContent(token: String, id: Int): Result<LectureContent> {
        return try {
            val response = apiService.getLectureContent("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки содержимого лекции: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Удаление лекции по ID
    suspend fun deleteLecture(token: String, id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteLecture("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка удаления лекции: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Вспомогательный метод для получения имени файла из URI
    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name ?: uri.lastPathSegment
    }
}
