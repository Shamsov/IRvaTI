package com.irvati.lecturebank.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irvati.lecturebank.data.model.UserInfo
import com.irvati.lecturebank.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel для управления аутентификацией пользователя
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application.applicationContext)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    init {
        // Проверяем сохранённую сессию при запуске
        viewModelScope.launch {
            val savedToken = repository.getToken()
            val savedUser = repository.getUser()
            if (savedToken != null && savedUser != null) {
                _token.value = savedToken
                _currentUser.value = savedUser
                _isLoggedIn.value = true
            }
        }
    }

    // Вход в систему
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.login(email, password)
            result.onSuccess { auth ->
                _token.value = auth.access_token
                _currentUser.value = auth.user
                _isLoggedIn.value = true
            }.onFailure { e ->
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    // Регистрация нового пользователя
    fun register(name: String, email: String, password: String, role: String = "student") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.register(name, email, password, role)
            result.onSuccess { auth ->
                _token.value = auth.access_token
                _currentUser.value = auth.user
                _isLoggedIn.value = true
            }.onFailure { e ->
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    // Выход из системы и очистка сессии
    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _token.value = null
            _currentUser.value = null
            _isLoggedIn.value = false
            _error.value = null
        }
    }

    // Сброс сообщения об ошибке
    fun clearError() {
        _error.value = null
    }
}
