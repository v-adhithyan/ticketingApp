package ceg.avtechlabs.standticket.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TestResultUtils {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }


    @Test
    fun test_payment_due() {
        val millisInADay = (24 * 60 * 60 * 1000)
        val today = System.currentTimeMillis() - 10000L
        assertEquals("",  context.getPaymentDue(today))

        val oneday = System.currentTimeMillis() - (millisInADay * 1)
        assertEquals("Overstay: Extra 1 day(s) in stand.".trim(), context.getPaymentDue(oneday).trim())

        val tendays = System.currentTimeMillis() - (millisInADay * 10)
        assertEquals("Overstay: Extra 10 day(s) in stand.\n".trim(), context.getPaymentDue(tendays).trim())
    }
}