package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.ui.navigation.ExpenseTrackerNavGraph
import com.example.expensetracker.ui.navigation.Screen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == Screen.ExpenseList.route,
                                onClick = {
                                    navController.navigate(Screen.ExpenseList.route) {
                                        popUpTo(Screen.ExpenseList.route) { inclusive = true }
                                    }
                                },
                                icon = { Icon(Icons.Default.List, contentDescription = "Expenses") },
                                label = { Text("Expenses") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Analysis.route,
                                onClick = {
                                    navController.navigate(Screen.Analysis.route) {
                                        popUpTo(Screen.ExpenseList.route)
                                    }
                                },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Analysis") },
                                label = { Text("Analysis") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ExpenseTrackerNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
