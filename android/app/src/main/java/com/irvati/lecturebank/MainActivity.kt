package com.irvati.lecturebank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.irvati.lecturebank.ui.theme.LectureBankTheme

// Точка входа в приложение
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LectureBankTheme {
                LectureBankApp()
            }
        }
    }
}
