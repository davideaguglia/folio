package com.personal.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.personal.financeapp.data.repository.NetWorthService
import com.personal.financeapp.ui.navigation.AppNavigation
import com.personal.financeapp.ui.theme.FolioTheme
import com.personal.financeapp.ui.theme.ThemeViewModel
import com.personal.financeapp.worker.PriceSyncWorker
import com.personal.financeapp.worker.RecurringTransactionWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var netWorthService: NetWorthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecurringTransactionWorker.enqueue(this)
        PriceSyncWorker.enqueue(this)
        lifecycleScope.launch { netWorthService.refreshSnapshot() }
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            FolioTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { themeViewModel.toggleTheme() }
                )
            }
        }
    }
}
