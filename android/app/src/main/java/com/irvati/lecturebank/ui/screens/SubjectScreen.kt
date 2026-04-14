package com.irvati.lecturebank.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.irvati.lecturebank.ui.components.LectureCard
import com.irvati.lecturebank.ui.components.LoadingIndicator
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.LectureViewModel

// Экран предмета со списком лекций
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    subjectId: Int,
    subjectName: String,
    authViewModel: AuthViewModel,
    lectureViewModel: LectureViewModel,
    onLectureClick: (Int, String) -> Unit,
    onBack: () -> Unit
) {
    val token by authViewModel.token.collectAsState()
    val lectures by lectureViewModel.lectures.collectAsState()
    val isLoading by lectureViewModel.isLoading.collectAsState()

    // Загружаем лекции при первом отображении экрана
    LaunchedEffect(subjectId, token) {
        token?.let { lectureViewModel.loadLectures(it, subjectId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subjectName,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    // Кнопка возврата
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingIndicator()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(lectures) { lecture ->
                        LectureCard(
                            lecture = lecture,
                            onClick = { onLectureClick(lecture.id, lecture.title) }
                        )
                    }
                }
            }
        }
    }
}
