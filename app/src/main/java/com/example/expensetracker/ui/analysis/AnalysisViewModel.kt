package com.example.expensetracker.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.dao.CategoryTotal
import com.example.expensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class MonthSelection(val year: Int, val month: Int)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val now = Calendar.getInstance()

    private val _selection = MutableStateFlow(
        MonthSelection(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    )
    val selection: StateFlow<MonthSelection> = _selection.asStateFlow()

    val categoryTotals: StateFlow<List<CategoryTotal>> = _selection
        .flatMapLatest { (year, month) ->
            expenseRepository.getMonthlyCategoryTotals(year, month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotal: StateFlow<Double> = categoryTotals
        .map { list -> list.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(_selection.value.year, _selection.value.month, 1)
            add(Calendar.MONTH, -1)
        }
        _selection.value = MonthSelection(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(_selection.value.year, _selection.value.month, 1)
            add(Calendar.MONTH, 1)
        }
        // Don't allow navigating into the future
        val nowCal = Calendar.getInstance()
        if (cal.get(Calendar.YEAR) < nowCal.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) <= nowCal.get(Calendar.MONTH))
        ) {
            _selection.value = MonthSelection(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }
}
