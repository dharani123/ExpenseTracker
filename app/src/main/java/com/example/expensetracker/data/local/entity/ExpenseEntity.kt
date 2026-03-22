package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["smsId"], unique = true)]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val categoryId: Long? = null,
    val smsId: String,
    val smsBody: String,
    val transactionDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)
