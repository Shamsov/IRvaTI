package com.irvati.lecturebank.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.irvati.lecturebank.data.api.RetrofitClient
import com.irvati.lecturebank.data.model.AuthResponse
import com.irvati.lecturebank.data.model.LoginRequest
import com.irvati.lecturebank.data.model.RegisterRequest
import com.irvati.lecturebank.data.model.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Расширение для создания DataStore на уровне Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

// Репозиторий аутентификации: регистрация, вход, хранение сессии
class AuthRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    // Ключи для DataStore
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    // Регистрация нового пользователя
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password, role))
            if (response.isSuccessful) {
                val body = response.body()!!
                saveSession(body)
                Result.success(body)
            } else {
                Result.failure(Exception("Ошибка регистрации: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Вход в систему
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                saveSession(body)
                Result.success(body)
            } else {
                Result.failure(Exception("Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Нет соединения с сервером: ${e.message}"))
        }
    }

    // Сохранение токена и данных пользователя в DataStore
    private suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = auth.access_token
            prefs[KEY_USER_ID] = auth.user.id.toString()
            prefs[KEY_USER_NAME] = auth.user.name
            prefs[KEY_USER_EMAIL] = auth.user.email
            prefs[KEY_USER_ROLE] = auth.user.role
        }
    }

    // Получение сохранённого токена
    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[KEY_TOKEN] }.first()
    }

    // Получение токена в виде Flow
    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { it[KEY_TOKEN] }
    }

    // Получение информации о текущем пользователе
    suspend fun getUser(): UserInfo? {
        val prefs = context.dataStore.data.first()
        val id = prefs[KEY_USER_ID]?.toIntOrNull() ?: return null
        val name = prefs[KEY_USER_NAME] ?: return null
        val email = prefs[KEY_USER_EMAIL] ?: return null
        val role = prefs[KEY_USER_ROLE] ?: return null
        return UserInfo(id, name, email, role)
    }

    // Очистка сессии при выходе
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
