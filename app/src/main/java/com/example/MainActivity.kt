package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.AppDatabase
import com.example.data.local.preferences.AppPreferences
import com.example.data.repository.SignalRepositoryImpl
import com.example.data.speedtest.SpeedTestEngine
import com.example.data.telephony.TelephonyDataSource
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val telephonySource = TelephonyDataSource(applicationContext)
        val speedEngine = SpeedTestEngine(applicationContext)
        val appPreferences = AppPreferences(applicationContext)
        val repository = SignalRepositoryImpl(
            context = applicationContext,
            telephonyDataSource = telephonySource,
            diagnosticDao = database.signalDiagnosticDao(),
            speedTestResultDao = database.speedTestResultDao(),
            speedTestEngine = speedEngine
        )
        DashboardViewModel.Factory(repository, appPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
