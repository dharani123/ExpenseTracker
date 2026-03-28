package com.example.expensetracker.data.local.dao

import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Immutable
data class ExpenseWithCategory(
    val id: Long,
    val amount: Double,
    val merchant: String,
    val categoryId: Long?,
    val categoryName: String?,
    val smsBody: String,
    val transactionDate: Long
)

@Immutable
data class CategoryTotal(
    val categoryName: String,
    val total: Double
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("""
        SELECT e.id, e.amount, e.merchant, e.categoryId, c.name AS categoryName,
               e.smsBody, e.transactionDate
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        ORDER BY e.transactionDate DESC
    """)
    fun getAllExpensesWithCategory(): Flow<List<ExpenseWithCategory>>

    @Query("UPDATE expenses SET categoryId = :categoryId WHERE id = :expenseId")
    suspend fun updateCategory(expenseId: Long, categoryId: Long)

    @Query("UPDATE expenses SET amount = :amount WHERE id = :expenseId")
    suspend fun updateAmount(expenseId: Long, amount: Double)

    @Query("""
        SELECT c.name AS categoryName, SUM(e.amount) AS total
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        WHERE e.transactionDate BETWEEN :start AND :end
        GROUP BY e.categoryId
        ORDER BY total DESC
    """)
    fun getCategoryTotals(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE transactionDate BETWEEN :start AND :end ORDER BY transactionDate DESC")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>>
}
