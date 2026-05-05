package com.irvati.lecturebank.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.LectureViewModel
import com.irvati.lecturebank.viewmodel.SubjectViewModel

// Экран загрузки новой лекции (для администратора)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadLectureScreen(
    authViewModel: AuthViewModel,
    subjectViewModel: SubjectViewModel,
    lectureViewModel: LectureViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token by authViewModel.token.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()
    val isLoading by lectureViewModel.isLoading.collectAsState()
    val error by lectureViewModel.error.collectAsState()
    val uploadSuccess by lectureViewModel.uploadSuccess.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedSubjectIndex by remember { mutableStateOf(-1) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("Файл не выбран") }

    // Лончер для выбора файла (PDF, DOCX, TXT)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            // Получаем имя файла из URI через ContentResolver
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    selectedFileName = cursor.getString(nameIndex)
                }
            }
        }
    }

    // Загружаем список предметов
    LaunchedEffect(token) {
        token?.let { subjectViewModel.loadSubjects(it) }
    }

    // Сбрасываем поля после успешной загрузки
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            title = ""
            selectedSubjectIndex = -1
            selectedFileUri = null
            selectedFileName = "Файл не выбран"
            lectureViewModel.resetUploadSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Загрузить лекцию") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Выпадающий список предметов
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                OutlinedTextField(
                    value = if (selectedSubjectIndex >= 0)
                        subjects.getOrNull(selectedSubjectIndex)?.name ?: ""
                    else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Предмет") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    subjects.forEachIndexed { index, subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                selectedSubjectIndex = index
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            // Поле ввода названия лекции
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название лекции") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Кнопка выбора файла
            OutlinedButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null
                )
                Text(
                    text = selectedFileName,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Сообщение об ошибке
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Сообщение об успехе
            if (uploadSuccess) {
                Text(
                    text = "Лекция успешно загружена!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка загрузки
            Button(
                onClick = {
                    val subj = subjects.getOrNull(selectedSubjectIndex)
                    val uri = selectedFileUri
                    if (subj != null && uri != null && title.isNotBlank()) {
                        token?.let { t ->
                            lectureViewModel.uploadLecture(t, title.trim(), subj.id, uri, context)
                        }
                    }
                },
                enabled = !isLoading
                        && title.isNotBlank()
                        && selectedSubjectIndex >= 0
                        && selectedFileUri != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Загрузить")
                }
            }
        }
    }
}
