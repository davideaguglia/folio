package com.personal.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.personal.financeapp.ui.navigation.AppNavigation
import com.personal.financeapp.ui.theme.FinanceAppTheme
import com.personal.financeapp.ui.theme.ThemeViewModel
import com.personal.financeapp.worker.RecurringTransactionWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecurringTransactionWorker.enqueue(this)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            FinanceAppTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}
