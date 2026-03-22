package com.example.expensetracker.ui.expenses

import androidx.compose.runtime.Immutable
import com.example.expensetracker.data.local.dao.ExpenseWithCategory

sealed class ExpenseListItem {
    @Immutable data class MonthHeader(val label: String) : ExpenseListItem()
    @Immutable data class DateHeader(val label: String, val totalAmount: Double) : ExpenseListItem()
    @Immutable data class ExpenseItem(val expense: ExpenseWithCategory) : ExpenseListItem()
}
