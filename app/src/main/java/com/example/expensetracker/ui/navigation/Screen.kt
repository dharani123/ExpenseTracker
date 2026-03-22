package com.example.expensetracker.ui.navigation

sealed class Screen(val route: String) {
    data object ExpenseList : Screen("expense_list")
    data object Analysis : Screen("analysis")
}
