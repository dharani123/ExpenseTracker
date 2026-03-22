package com.example.expensetracker.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.dao.ExpenseWithCategory
import com.example.expensetracker.data.local.entity.CategoryEntity
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dateFmt  = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())

    private val expenses: StateFlow<List<ExpenseWithCategory>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedExpenses: StateFlow<List<ExpenseListItem>> = expenses
        .map { buildGroupedList(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private fun buildGroupedList(expenses: List<ExpenseWithCategory>): List<ExpenseListItem> {
        // Pre-compute daily totals keyed by date label
        val dailyTotals = mutableMapOf<String, Double>()
        for (expense in expenses) {
            val cal = Calendar.getInstance().apply { timeInMillis = expense.transactionDate }
            val dateLabel = dateFmt.format(cal.time)
            dailyTotals[dateLabel] = (dailyTotals[dateLabel] ?: 0.0) + expense.amount
        }

        val items = mutableListOf<ExpenseListItem>()
        var currentMonth = ""
        var currentDate = ""

        for (expense in expenses) {
            val cal = Calendar.getInstance().apply { timeInMillis = expense.transactionDate }
            val monthLabel = monthFmt.format(cal.time)
            val dateLabel  = dateFmt.format(cal.time)

            if (monthLabel != currentMonth) {
                items.add(ExpenseListItem.MonthHeader(monthLabel))
                currentMonth = monthLabel
                currentDate = ""
            }

            if (dateLabel != currentDate) {
                items.add(ExpenseListItem.DateHeader(dateLabel, dailyTotals[dateLabel] ?: 0.0))
                currentDate = dateLabel
            }

            items.add(ExpenseListItem.ExpenseItem(expense))
        }

        return items
    }

    fun syncSms() {
        viewModelScope.launch {
            _isLoading.value = true
            _snackbarMessage.value = null
            try {
                val count = expenseRepository.syncSmsExpenses()
                _snackbarMessage.value = if (count > 0)
                    "Found $count transaction SMS"
                else
                    "No new transactions found in SMS"
            } catch (e: Exception) {
                _snackbarMessage.value = "Sync failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(expenseId: Long, categoryId: Long) {
        viewModelScope.launch {
            expenseRepository.updateCategory(expenseId, categoryId)
        }
    }

    fun updateAmount(expenseId: Long, amount: Double) {
        viewModelScope.launch {
            expenseRepository.updateAmount(expenseId, amount)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            categoryRepository.addCategory(name)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
