package com.irvati.lecturebank.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.irvati.lecturebank.ui.components.SubjectCard
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.SubjectViewModel

// Главный экран студента со списком предметов
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    subjectViewModel: SubjectViewModel,
    onSubjectClick: (Int, String) -> Unit,
    onAiSearchClick: () -> Unit,
    onLogout: () -> Unit
) {
    val token by authViewModel.token.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val isLoading by subjectViewModel.isLoading.collectAsState()

    // Загружаем предметы при первом отображении экрана
    LaunchedEffect(token) {
        token?.let { subjectViewModel.loadSubjects(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Банк лекций",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Приветствие пользователя
                    currentUser?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    // Кнопка выхода
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выйти"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // FAB для перехода к AI-поиску
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAiSearchClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI-поиск",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
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
                    items(subjects) { subject ->
                        SubjectCard(
                            subject = subject,
                            onClick = { onSubjectClick(subject.id, subject.name) }
                        )
                    }
                }
            }
        }
    }
}
