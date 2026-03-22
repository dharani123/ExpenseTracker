package com.example.expensetracker.sms

import android.content.Context
import android.net.Uri
import com.example.expensetracker.sms.model.ParsedSms
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class RawSms(
    val id: String,
    val address: String,
    val body: String,
    val dateMillis: Long
)

@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: SmsParser
) {

    private val smsUri = Uri.parse("content://sms/inbox")
    private val projection = arrayOf("_id", "address", "body", "date")

    // Only process SMS from this date onwards
    private val startDateMillis: Long = java.util.Calendar.getInstance().apply {
        set(2026, java.util.Calendar.MARCH, 20, 0, 0, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Exclude clearly personal senders: Indian mobile numbers (+91XXXXXXXXXX or 10-digit 6-9 start)
    // Everything else (short codes, alphanumeric sender IDs) is allowed through
    private val personalNumberRegex = Regex("""^(\+91)?[6-9]\d{9}$""")

    private fun isPersonalNumber(address: String): Boolean =
        personalNumberRegex.matches(address.trim().replace(" ", ""))

    suspend fun readAndParse(): List<ParsedSms> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ParsedSms>()

        val cursor = context.contentResolver.query(
            smsUri,
            projection,
            "date >= ?",
            arrayOf(startDateMillis.toString()),
            "date DESC"
        ) ?: return@withContext results

        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("_id")
            val addressIdx = it.getColumnIndexOrThrow("address")
            val bodyIdx = it.getColumnIndexOrThrow("body")
            val dateIdx = it.getColumnIndexOrThrow("date")

            while (it.moveToNext()) {
                val id = it.getString(idIdx) ?: continue
                val address = it.getString(addressIdx) ?: continue
                val body = it.getString(bodyIdx) ?: continue
                val date = it.getLong(dateIdx)

                // Skip personal messages — only block clear personal numbers
                if (isPersonalNumber(address)) continue

                val parsed = parser.parse(id, body, date) ?: continue
                results.add(parsed)
            }
        }

        results
    }
}
