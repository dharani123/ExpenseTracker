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

enum class AnalysisTab { MONTHLY, WEEKLY, DAILY }

data class MonthSelection(val year: Int, val month: Int)
data class WeekSelection(val year: Int, val week: Int)
data class DaySelection(val year: Int, val month: Int, val day: Int)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val now = Calendar.getInstance()

    // --- Tab ---
    private val _selectedTab = MutableStateFlow(AnalysisTab.MONTHLY)
    val selectedTab: StateFlow<AnalysisTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: AnalysisTab) { _selectedTab.value = tab }

    // --- Monthly ---
    private val _monthSelection = MutableStateFlow(
        MonthSelection(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    )
    val monthSelection: StateFlow<MonthSelection> = _monthSelection.asStateFlow()

    val monthlyCategoryTotals: StateFlow<List<CategoryTotal>> = _monthSelection
        .flatMapLatest { (year, month) ->
            expenseRepository.getMonthlyCategoryTotals(year, month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotal: StateFlow<Double> = monthlyCategoryTotals
        .map { it.sumOf { c -> c.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(_monthSelection.value.year, _monthSelection.value.month, 1)
            add(Calendar.MONTH, -1)
        }
        _monthSelection.value = MonthSelection(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(_monthSelection.value.year, _monthSelection.value.month, 1)
            add(Calendar.MONTH, 1)
        }
        val nowCal = Calendar.getInstance()
        if (cal.get(Calendar.YEAR) < nowCal.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) <= nowCal.get(Calendar.MONTH))
        ) {
            _monthSelection.value = MonthSelection(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }

    // --- Weekly ---
    private val _weekSelection = MutableStateFlow(
        WeekSelection(now.get(Calendar.YEAR), now.get(Calendar.WEEK_OF_YEAR))
    )
    val weekSelection: StateFlow<WeekSelection> = _weekSelection.asStateFlow()

    val weeklyCategoryTotals: StateFlow<List<CategoryTotal>> = _weekSelection
        .flatMapLatest { (year, week) ->
            expenseRepository.getWeeklyCategoryTotals(year, week)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyTotal: StateFlow<Double> = weeklyCategoryTotals
        .map { it.sumOf { c -> c.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun previousWeek() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _weekSelection.value.year)
            set(Calendar.WEEK_OF_YEAR, _weekSelection.value.week)
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        _weekSelection.value = WeekSelection(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
    }

    fun nextWeek() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _weekSelection.value.year)
            set(Calendar.WEEK_OF_YEAR, _weekSelection.value.week)
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        val nowCal = Calendar.getInstance()
        if (!cal.after(nowCal)) {
            _weekSelection.value = WeekSelection(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
        }
    }

    // --- Daily ---
    private val _daySelection = MutableStateFlow(
        DaySelection(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
    )
    val daySelection: StateFlow<DaySelection> = _daySelection.asStateFlow()

    val dailyCategoryTotals: StateFlow<List<CategoryTotal>> = _daySelection
        .flatMapLatest { (year, month, day) ->
            expenseRepository.getDailyCategoryTotals(year, month, day)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotal: StateFlow<Double> = dailyCategoryTotals
        .map { it.sumOf { c -> c.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun previousDay() {
        val cal = Calendar.getInstance().apply {
            set(_daySelection.value.year, _daySelection.value.month, _daySelection.value.day)
            add(Calendar.DAY_OF_MONTH, -1)
        }
        _daySelection.value = DaySelection(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun nextDay() {
        val cal = Calendar.getInstance().apply {
            set(_daySelection.value.year, _daySelection.value.month, _daySelection.value.day)
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val nowCal = Calendar.getInstance()
        // Don't allow future dates
        if (!cal.after(nowCal)) {
            _daySelection.value = DaySelection(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
