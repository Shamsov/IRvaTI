package com.irvati.lecturebank.data.model

// Запрос на регистрацию нового пользователя
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student"
)

// Запрос на вход в систему
data class LoginRequest(
    val email: String,
    val password: String
)

// Ответ сервера после успешной аутентификации
data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user: UserInfo
)

// Информация о пользователе
data class UserInfo(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)
