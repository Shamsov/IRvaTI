package com.irvati.lecturebank.data.api

import com.irvati.lecturebank.data.model.AIQuery
import com.irvati.lecturebank.data.model.AIResponse
import com.irvati.lecturebank.data.model.AuthResponse
import com.irvati.lecturebank.data.model.Lecture
import com.irvati.lecturebank.data.model.LectureContent
import com.irvati.lecturebank.data.model.LoginRequest
import com.irvati.lecturebank.data.model.RegisterRequest
import com.irvati.lecturebank.data.model.Subject
import com.irvati.lecturebank.data.model.SubjectCreate
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Интерфейс Retrofit для всех API-эндпоинтов
interface ApiService {

    // --- Аутентификация ---

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // --- Предметы ---

    @GET("api/subjects")
    suspend fun getSubjects(
        @Header("Authorization") token: String
    ): Response<List<Subject>>

    @POST("api/subjects")
    suspend fun createSubject(
        @Header("Authorization") token: String,
        @Body subject: SubjectCreate
    ): Response<Subject>

    @DELETE("api/subjects/{id}")
    suspend fun deleteSubject(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // --- Лекции ---

    @GET("api/lectures/{subjectId}")
    suspend fun getLectures(
        @Header("Authorization") token: String,
        @Path("subjectId") subjectId: Int
    ): Response<List<Lecture>>

    @Multipart
    @POST("api/lectures/upload")
    suspend fun uploadLecture(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("subject_id") subjectId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Lecture>

    @GET("api/lectures/{id}/content")
    suspend fun getLectureContent(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<LectureContent>

    @DELETE("api/lectures/{id}")
    suspend fun deleteLecture(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // --- AI-поиск ---

    @POST("api/ai/search")
    suspend fun aiSearch(
        @Header("Authorization") token: String,
        @Body query: AIQuery
    ): Response<AIResponse>
}
