package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.CategoryTotal
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.ExpenseWithCategory
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.sms.SmsReader
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val smsReader: SmsReader
) {
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>> =
        expenseDao.getAllExpensesWithCategory()

    fun getMonthlyCategoryTotals(year: Int, month: Int): Flow<List<CategoryTotal>> {
        val (start, end) = monthRange(year, month)
        return expenseDao.getCategoryTotals(start, end)
    }

    fun getDailyCategoryTotals(year: Int, month: Int, day: Int): Flow<List<CategoryTotal>> {
        val (start, end) = dayRange(year, month, day)
        return expenseDao.getCategoryTotals(start, end)
    }

    fun getWeeklyCategoryTotals(year: Int, week: Int): Flow<List<CategoryTotal>> {
        val (start, end) = weekRange(year, week)
        return expenseDao.getCategoryTotals(start, end)
    }

    // Returns number of new transactions inserted (duplicates are ignored by DB)
    suspend fun syncSmsExpenses(): Int {
        val parsed = smsReader.readAndParse()
        if (parsed.isEmpty()) return 0

        val entities = parsed.map {
            ExpenseEntity(
                amount = it.amount,
                merchant = it.merchant,
                smsId = it.smsId,
                smsBody = it.originalBody,
                transactionDate = it.transactionDate
            )
        }
        expenseDao.insertAll(entities)

        // Return how many SMS were parsed (includes duplicates — UI will show this for feedback)
        return parsed.size
    }

    suspend fun updateCategory(expenseId: Long, categoryId: Long) {
        expenseDao.updateCategory(expenseId, categoryId)
    }

    suspend fun updateAmount(expenseId: Long, amount: Double) {
        expenseDao.updateAmount(expenseId, amount)
    }

    private fun weekRange(year: Int, week: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
            add(Calendar.DAY_OF_WEEK, 6)
        }.timeInMillis
        return Pair(start, end)
    }

    private fun dayRange(year: Int, month: Int, day: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return Pair(start, end)
    }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(year, month, 1, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        return Pair(start, end)
    }
}
