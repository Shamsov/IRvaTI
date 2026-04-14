package com.irvati.lecturebank.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.irvati.lecturebank.ui.components.LoadingIndicator
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.LectureViewModel

// Экран просмотра текстового содержимого лекции
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureScreen(
    lectureId: Int,
    lectureTitle: String,
    authViewModel: AuthViewModel,
    lectureViewModel: LectureViewModel,
    onBack: () -> Unit
) {
    val token by authViewModel.token.collectAsState()
    val lectureContent by lectureViewModel.currentLectureContent.collectAsState()
    val isLoading by lectureViewModel.isLoading.collectAsState()
    val error by lectureViewModel.error.collectAsState()

    // Загружаем содержимое лекции при первом отображении
    LaunchedEffect(lectureId, token) {
        token?.let { lectureViewModel.loadLectureContent(it, lectureId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lectureTitle,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
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
            when {
                isLoading -> LoadingIndicator()
                error != null -> {
                    Text(
                        text = "Ошибка: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                lectureContent != null -> {
                    // Прокручиваемый текст лекции
                    Text(
                        text = lectureContent!!.text_content
                            ?: "Текстовое содержимое недоступно.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
                else -> {
                    Text(
                        text = "Содержимое лекции не найдено.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
