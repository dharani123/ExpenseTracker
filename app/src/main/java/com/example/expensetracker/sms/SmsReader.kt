package com.example.expensetracker.sms

import android.content.Context
import android.net.Uri
import com.example.expensetracker.sms.model.ParsedSms
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun readAndParse(): List<ParsedSms> {
        val results = mutableListOf<ParsedSms>()

        val cursor = context.contentResolver.query(
            smsUri,
            projection,
            null,
            null,
            "date DESC"
        ) ?: return results

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

                if (!parser.isTransactionalSender(address)) continue

                val parsed = parser.parse(id, body, date) ?: continue
                results.add(parsed)
            }
        }

        return results
    }
}
