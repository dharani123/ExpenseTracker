package com.example.expensetracker.ui.expenses

import com.example.expensetracker.data.local.dao.ExpenseWithCategory

sealed class ExpenseListItem {
    data class MonthHeader(val label: String) : ExpenseListItem()
    data class DateHeader(val label: String, val totalAmount: Double) : ExpenseListItem()
    data class ExpenseItem(val expense: ExpenseWithCategory) : ExpenseListItem()
}
