package com.example.expensetracker

import com.example.expensetracker.sms.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmsParserTest {

    private lateinit var parser: SmsParser

    @Before
    fun setup() {
        parser = SmsParser()
    }

    // --- Sender detection ---

    @Test
    fun `transactional sender - bank format`() {
        assertTrue(parser.isTransactionalSender("VM-HDFCBK"))
        assertTrue(parser.isTransactionalSender("AD-SBIBNK"))
        assertTrue(parser.isTransactionalSender("JD-ICICIB"))
        assertTrue(parser.isTransactionalSender("AX-AXISBK"))
    }

    @Test
    fun `transactional sender - numeric short code`() {
        assertTrue(parser.isTransactionalSender("524250"))
        assertTrue(parser.isTransactionalSender("101010"))
    }

    @Test
    fun `non-transactional sender - personal number`() {
        val result = parser.isTransactionalSender("+919876543210")
        assertEquals(false, result)
    }

    // --- Debit patterns ---

    @Test
    fun `parse HDFC debit SMS`() {
        val body = "Rs.1500.00 debited from A/c XX1234 to AMAZON on 22-Mar-26. Avl Bal Rs.5000."
        val result = parser.parse("1", body, 0L)
        assertNotNull(result)
        assertEquals(1500.0, result!!.amount, 0.01)
        assertTrue(result.merchant.contains("AMAZON", ignoreCase = true))
    }

    @Test
    fun `parse SBI debit SMS with commas in amount`() {
        val body = "INR 1,500.00 debited from SBI A/c XXXX5678 to SWIGGY on 22-03-2026. Bal INR 8,000.00"
        val result = parser.parse("2", body, 0L)
        assertNotNull(result)
        assertEquals(1500.0, result!!.amount, 0.01)
        assertTrue(result.merchant.contains("SWIGGY", ignoreCase = true))
    }

    @Test
    fun `parse UPI payment SMS`() {
        val body = "Paid Rs.200 to ZOMATO via UPI. Ref 123456789. -PhonePe"
        val result = parser.parse("3", body, 0L)
        assertNotNull(result)
        assertEquals(200.0, result!!.amount, 0.01)
        assertTrue(result.merchant.contains("ZOMATO", ignoreCase = true))
    }

    @Test
    fun `parse card spent SMS`() {
        val body = "Rs.3500.00 spent on HDFC Card XX4321 at FLIPKART on 22-Mar-26. Available Limit Rs.46500."
        val result = parser.parse("4", body, 0L)
        assertNotNull(result)
        assertEquals(3500.0, result!!.amount, 0.01)
        assertTrue(result.merchant.contains("FLIPKART", ignoreCase = true))
    }

    @Test
    fun `parse sent SMS`() {
        val body = "Dear Customer, Rs 500 has been transferred to RAHUL SHARMA. UPI Ref: 987654321."
        val result = parser.parse("5", body, 0L)
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse Kotak UPI sent SMS`() {
        val body = "Sent Rs.100.00 from Kotak Bank AC X8993 to prasads.63402210@hdfcbank on 21-03-26.UPI Ref 399333649131. Not you, https://kotak.com/KBANKT/Fraud"
        val result = parser.parse("10", body, 0L)
        assertNotNull(result)
        assertEquals(100.0, result!!.amount, 0.01)
        assertEquals("prasads.63402210@hdfcbank", result.merchant)
    }

    @Test
    fun `parse SBI UPI debited by format`() {
        val body = "Dear UPI user A/C X6061 debited by 187.00 on date 09Mar26 trf to Blinkit Refno 834965771000 If not u? call-1800111109 for other services-18001234-SBI"
        val result = parser.parse("12", body, 0L)
        assertNotNull(result)
        assertEquals(187.0, result!!.amount, 0.01)
        assertEquals("Blinkit", result.merchant)
    }

    @Test
    fun `parse Kotak UPI with larger amount`() {
        val body = "Sent Rs.1,250.00 from Kotak Bank AC X8993 to merchant@oksbi on 21-03-26.UPI Ref 123456789. Not you, https://kotak.com/KBANKT/Fraud"
        val result = parser.parse("11", body, 0L)
        assertNotNull(result)
        assertEquals(1250.0, result!!.amount, 0.01)
        assertEquals("merchant@oksbi", result.merchant)
    }

    @Test
    fun `ignore credit SMS`() {
        val body = "Rs.5000.00 credited to your A/c XXXX1234 on 22-Mar-26."
        val result = parser.parse("6", body, 0L)
        assertNull(result)
    }

    @Test
    fun `ignore OTP SMS`() {
        val body = "Your OTP for transaction is 123456. Valid for 10 minutes. Do not share."
        val result = parser.parse("7", body, 0L)
        assertNull(result)
    }

    @Test
    fun `parse amount without decimal`() {
        val body = "Rs 250 paid to UBER via UPI on 22-Mar-26."
        val result = parser.parse("8", body, 0L)
        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse ICICI bank format`() {
        val body = "ICICI Bank: Rs 899.00 debited from A/c XX7890 to NETFLIX on 22-MAR-2026. Available Balance Rs 12000.00."
        val result = parser.parse("9", body, 0L)
        assertNotNull(result)
        assertEquals(899.0, result!!.amount, 0.01)
        assertTrue(result.merchant.contains("NETFLIX", ignoreCase = true))
    }

    @Test
    fun `smsId is preserved in result`() {
        val body = "Rs.100 debited from A/c XX0001 to CAFE COFFEE DAY on 22-Mar-26."
        val result = parser.parse("sms_42", body, 1234567890L)
        assertNotNull(result)
        assertEquals("sms_42", result!!.smsId)
        assertEquals(1234567890L, result.transactionDate)
    }
}
