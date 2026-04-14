package com.irvati.lecturebank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irvati.lecturebank.data.model.AIResponse
import com.irvati.lecturebank.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel для AI-поиска по лекциям
class AiSearchViewModel : ViewModel() {

    private val repository = AiRepository()

    private val _answer = MutableStateFlow<AIResponse?>(null)
    val answer: StateFlow<AIResponse?> = _answer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Отправка вопроса на AI-поиск
    fun search(token: String, question: String, subjectId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _answer.value = null
            repository.search(token, question, subjectId)
                .onSuccess { _answer.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Сброс результата поиска
    fun clearResult() {
        _answer.value = null
        _error.value = null
    }
}
