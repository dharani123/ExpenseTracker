package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetracker.ui.analysis.AnalysisScreen
import com.example.expensetracker.ui.expenses.ExpenseListScreen

@Composable
fun ExpenseTrackerNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.ExpenseList.route) {
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen()
        }
        composable(Screen.Analysis.route) {
            AnalysisScreen()
        }
    }
}
