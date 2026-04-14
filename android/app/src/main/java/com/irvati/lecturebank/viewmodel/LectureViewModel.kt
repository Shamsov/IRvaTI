package com.irvati.lecturebank.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irvati.lecturebank.data.model.Lecture
import com.irvati.lecturebank.data.model.LectureContent
import com.irvati.lecturebank.data.repository.LectureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel для управления лекциями
class LectureViewModel : ViewModel() {

    private val repository = LectureRepository()

    private val _lectures = MutableStateFlow<List<Lecture>>(emptyList())
    val lectures: StateFlow<List<Lecture>> = _lectures.asStateFlow()

    private val _currentLectureContent = MutableStateFlow<LectureContent?>(null)
    val currentLectureContent: StateFlow<LectureContent?> = _currentLectureContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()

    // Загрузка лекций для конкретного предмета
    fun loadLectures(token: String, subjectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getLectures(token, subjectId)
                .onSuccess { _lectures.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Загрузка файла лекции на сервер
    fun uploadLecture(
        token: String,
        title: String,
        subjectId: Int,
        fileUri: Uri,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _uploadSuccess.value = false
            repository.uploadLecture(token, title, subjectId, fileUri, context)
                .onSuccess { newLecture ->
                    _lectures.value = _lectures.value + newLecture
                    _uploadSuccess.value = true
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Удаление лекции по ID
    fun deleteLecture(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.deleteLecture(token, id)
                .onSuccess {
                    _lectures.value = _lectures.value.filter { it.id != id }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Загрузка текстового содержимого лекции
    fun loadLectureContent(token: String, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _currentLectureContent.value = null
            repository.getLectureContent(token, id)
                .onSuccess { _currentLectureContent.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    // Сброс флага успешной загрузки
    fun resetUploadSuccess() {
        _uploadSuccess.value = false
    }

    // Сброс сообщения об ошибке
    fun clearError() {
        _error.value = null
    }
}
