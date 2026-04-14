package com.irvati.lecturebank.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irvati.lecturebank.ui.components.LoadingIndicator
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.SubjectViewModel

// Экран управления предметами (для администратора)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubjectsScreen(
    authViewModel: AuthViewModel,
    subjectViewModel: SubjectViewModel,
    onBack: () -> Unit
) {
    val token by authViewModel.token.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val isLoading by subjectViewModel.isLoading.collectAsState()
    val error by subjectViewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    var newSubjectDescription by remember { mutableStateOf("") }

    // Загружаем предметы при открытии экрана
    LaunchedEffect(token) {
        token?.let { subjectViewModel.loadSubjects(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление предметами") },
                navigationIcon = {
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
        },
        // FAB для добавления нового предмета
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить предмет",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Сообщение об ошибке
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (isLoading) {
                LoadingIndicator()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(subjects) { subject ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (!subject.description.isNullOrBlank()) {
                                        Text(
                                            text = subject.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                // Кнопка удаления предмета
                                IconButton(onClick = {
                                    token?.let { t ->
                                        subjectViewModel.deleteSubject(t, subject.id)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог добавления нового предмета
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newSubjectName = ""
                newSubjectDescription = ""
            },
            title = { Text("Новый предмет") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Название предмета") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubjectDescription,
                        onValueChange = { newSubjectDescription = it },
                        label = { Text("Описание (необязательно)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            token?.let { t ->
                                subjectViewModel.createSubject(
                                    t,
                                    newSubjectName.trim(),
                                    newSubjectDescription.ifBlank { null }
                                )
                            }
                            showAddDialog = false
                            newSubjectName = ""
                            newSubjectDescription = ""
                        }
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newSubjectName = ""
                    newSubjectDescription = ""
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}
