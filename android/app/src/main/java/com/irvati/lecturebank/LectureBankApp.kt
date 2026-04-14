package com.irvati.lecturebank

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.irvati.lecturebank.navigation.NavGraph
import com.irvati.lecturebank.viewmodel.AuthViewModel

// Корневой composable приложения: создаёт NavController и общий AuthViewModel
@Composable
fun LectureBankApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavGraph(
        navController = navController,
        authViewModel = authViewModel
    )
}
