package com.example.expensetracker.sms.model

data class ParsedSms(
    val smsId: String,
    val amount: Double,
    val merchant: String,
    val transactionDate: Long,
    val originalBody: String
)
