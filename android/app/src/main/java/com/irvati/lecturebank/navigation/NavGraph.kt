package com.irvati.lecturebank.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.irvati.lecturebank.ui.screens.AdminDashboard
import com.irvati.lecturebank.ui.screens.AiSearchScreen
import com.irvati.lecturebank.ui.screens.HomeScreen
import com.irvati.lecturebank.ui.screens.LectureScreen
import com.irvati.lecturebank.ui.screens.LoginScreen
import com.irvati.lecturebank.ui.screens.ManageSubjectsScreen
import com.irvati.lecturebank.ui.screens.RegisterScreen
import com.irvati.lecturebank.ui.screens.SubjectScreen
import com.irvati.lecturebank.ui.screens.UploadLectureScreen
import com.irvati.lecturebank.viewmodel.AiSearchViewModel
import com.irvati.lecturebank.viewmodel.AuthViewModel
import com.irvati.lecturebank.viewmodel.LectureViewModel
import com.irvati.lecturebank.viewmodel.SubjectViewModel

// Граф навигации приложения
@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val subjectViewModel: SubjectViewModel = viewModel()
    val lectureViewModel: LectureViewModel = viewModel()
    val aiSearchViewModel: AiSearchViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Экран входа
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { role ->
                    if (role == "admin") {
                        navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        // Экран регистрации
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Главный экран студента
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                subjectViewModel = subjectViewModel,
                onSubjectClick = { subjectId, subjectName ->
                    navController.navigate("subject/$subjectId/${subjectName}")
                },
                onAiSearchClick = { navController.navigate("ai_search") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран предмета с лекциями
        composable(
            route = "subject/{subjectId}/{subjectName}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.IntType },
                navArgument("subjectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: return@composable
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            SubjectScreen(
                subjectId = subjectId,
                subjectName = subjectName,
                authViewModel = authViewModel,
                lectureViewModel = lectureViewModel,
                onLectureClick = { lectureId, lectureTitle ->
                    navController.navigate("lecture/$lectureId/${lectureTitle}")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Экран содержимого лекции
        composable(
            route = "lecture/{lectureId}/{lectureTitle}",
            arguments = listOf(
                navArgument("lectureId") { type = NavType.IntType },
                navArgument("lectureTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lectureId = backStackEntry.arguments?.getInt("lectureId") ?: return@composable
            val lectureTitle = backStackEntry.arguments?.getString("lectureTitle") ?: ""
            LectureScreen(
                lectureId = lectureId,
                lectureTitle = lectureTitle,
                authViewModel = authViewModel,
                lectureViewModel = lectureViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Экран AI-поиска
        composable("ai_search") {
            AiSearchScreen(
                authViewModel = authViewModel,
                aiSearchViewModel = aiSearchViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Панель администратора
        composable("admin_dashboard") {
            AdminDashboard(
                authViewModel = authViewModel,
                subjectViewModel = subjectViewModel,
                onUploadLecture = { navController.navigate("upload_lecture") },
                onManageSubjects = { navController.navigate("manage_subjects") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран загрузки лекции (для администратора)
        composable("upload_lecture") {
            UploadLectureScreen(
                authViewModel = authViewModel,
                subjectViewModel = subjectViewModel,
                lectureViewModel = lectureViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Экран управления предметами (для администратора)
        composable("manage_subjects") {
            ManageSubjectsScreen(
                authViewModel = authViewModel,
                subjectViewModel = subjectViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
