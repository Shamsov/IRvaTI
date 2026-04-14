package com.irvati.lecturebank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irvati.lecturebank.data.model.Subject
import com.irvati.lecturebank.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel для управления предметами
class SubjectViewModel : ViewModel() {

    private val repository = SubjectRepository()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Загрузка списка предметов
    fun loadSubjects(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getSubjects(token)
                .onSuccess { _subjects.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Создание нового предмета
    fun createSubject(token: String, name: String, description: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.createSubject(token, name, description)
                .onSuccess { newSubject ->
                    _subjects.value = _subjects.value + newSubject
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Удаление предмета по ID
    fun deleteSubject(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.deleteSubject(token, id)
                .onSuccess {
                    _subjects.value = _subjects.value.filter { it.id != id }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Сброс сообщения об ошибке
    fun clearError() {
        _error.value = null
    }
}
