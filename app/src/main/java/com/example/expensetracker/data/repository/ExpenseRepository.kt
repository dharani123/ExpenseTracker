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
