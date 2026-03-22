package com.example.expensetracker.sms

import com.example.expensetracker.sms.model.ParsedSms
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    private data class SmsPattern(
        val regex: Regex,
        val amountGroup: Int,
        val merchantGroup: Int
    )

    private val patterns = listOf(
        // Pattern 0a (Kotak/UPI): "Sent Rs.100.00 from Kotak Bank AC X8993 to prasads.63402210@hdfcbank on 21-03-26.UPI Ref ..."
        SmsPattern(
            regex = Regex(
                """(?:sent|paid|transferred)\s+Rs\.?([0-9,]+(?:\.[0-9]{1,2})?)\s+from\s+.+?\s+to\s+([A-Za-z0-9._@\-]+)\s+on\s+""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 0b (SBI UPI): "A/C X6061 debited by 187.00 on date 09Mar26 trf to Blinkit Refno ..."
        SmsPattern(
            regex = Regex(
                """debited by ([0-9,]+(?:\.[0-9]{1,2})?) on date .+? trf to ([A-Za-z0-9\s&.''\-]+?) (?:Refno|Ref|ref)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 1: "Rs.500 debited from A/c XX1234 to MERCHANT on ..."
        // Pattern 1b: "INR 1,500 debited ... to MERCHANT"
        SmsPattern(
            regex = Regex(
                """(?:Rs\.?|INR)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:has been\s*)?(?:debited|deducted)(?:[^.]*?)(?:to|at)\s+([A-Za-z0-9\s&.''\-/]+?)(?:\s+on\s+|\s+ref|\s*\.\s*|\s*via|\s*$)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 2: "spent Rs.500 at MERCHANT" / "INR 1,500 spent ... at MERCHANT"
        SmsPattern(
            regex = Regex(
                """(?:Rs\.?|INR)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:spent|used)(?:[^.]*?)(?:at|on)\s+([A-Za-z0-9\s&.''\-/]+?)(?:\s+on\s+\d|\s+ref|\s*\.\s*|\s*$)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 3: "Paid Rs.200 to MERCHANT via UPI" / "sent Rs 500 to MERCHANT"
        SmsPattern(
            regex = Regex(
                """(?:paid|sent|transferred)\s+(?:Rs\.?|INR)?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:to|at)\s+([A-Za-z0-9\s&.''\-/@]+?)(?:\s+via|\s+ref|\s+on\s+\d|\s*\.\s*|\s*$)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 4: "transaction of Rs.500 at/to MERCHANT"
        SmsPattern(
            regex = Regex(
                """transaction\s+of\s+(?:Rs\.?|INR)\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:at|to|for)\s+([A-Za-z0-9\s&.''\-/]+?)(?:\s+on\s+|\s+ref|\s*\.\s*|\s*$)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        ),
        // Pattern 5: Broad catch-all — amount then merchant after "to/at/for"
        SmsPattern(
            regex = Regex(
                """(?:Rs\.?|INR)\s*([0-9,]+(?:\.[0-9]{1,2})?)(?:[^.]{0,80}?)(?:to|at|for)\s+([A-Za-z][A-Za-z0-9\s&.''\-/]{2,40}?)(?:\s+on\s+\d|\s+ref|\s+UPI|\s*\.\s*|\s*$)""",
                RegexOption.IGNORE_CASE
            ),
            amountGroup = 1, merchantGroup = 2
        )
    )

    // Indian bank/transactional SMS senders follow patterns like VM-HDFCBK, AD-SBIBNK, JD-ICICIB
    // Also numeric short codes (e.g. "524250") used by some banks
    private val transactionalSenderRegex = Regex("""^[A-Z]{2}-[A-Z0-9]{3,}$""")
    private val numericShortCodeRegex = Regex("""^\d{5,6}$""")

    fun isTransactionalSender(address: String): Boolean {
        val upper = address.trim().uppercase()
        return transactionalSenderRegex.matches(upper) || numericShortCodeRegex.matches(upper)
    }

    fun parse(smsId: String, body: String, dateMillis: Long): ParsedSms? {
        // Only attempt parsing on debit/payment keywords to avoid false positives
        val lowerBody = body.lowercase()
        val hasDebitKeyword = listOf(
            "debited", "deducted", "spent", "paid", "sent", "transferred", "payment", "transaction", "purchase"
        ).any { lowerBody.contains(it) }

        if (!hasDebitKeyword) return null

        // Skip credit transactions
        val hasCreditKeyword = listOf("credited", "received", "credit").any { lowerBody.contains(it) }
        // If it has a credit keyword but no debit keyword, skip it
        if (hasCreditKeyword && !listOf("debited", "deducted", "spent").any { lowerBody.contains(it) }) return null

        for (pattern in patterns) {
            val match = pattern.regex.find(body) ?: continue
            val amountStr = match.groupValues[pattern.amountGroup].replace(",", "").trim()
            val amount = amountStr.toDoubleOrNull() ?: continue
            if (amount <= 0) continue

            val merchant = match.groupValues[pattern.merchantGroup]
                .trim()
                .trimEnd('.', ',', ' ')
                .replace(Regex("""\s+"""), " ")

            if (merchant.length < 2) continue

            return ParsedSms(
                smsId = smsId,
                amount = amount,
                merchant = merchant,
                transactionDate = dateMillis,
                originalBody = body
            )
        }
        return null
    }
}
