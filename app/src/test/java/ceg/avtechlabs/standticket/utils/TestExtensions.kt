package ceg.avtechlabs.standticket.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ceg.avtechlabs.standticket.utils.Constants.PASSWORD
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class TestExtensions {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testCloseShift() {
        assertFalse(context.isShiftClosed())

        context.closeShift()
        assert(context.isShiftClosed())

        context.openShift()
        assertFalse(context.isShiftClosed())
    }

    @Test
    fun testOpenShift() {
        assertFalse(context.isShiftOpen())

        context.openShift()
        assert(context.isShiftOpen())

        context.closeShift()
        assertFalse(context.isShiftOpen())
    }

    @Test
    fun testGetDateTime() {
        val millis = 1557455220L

        assertEquals(getDateTime(millis, false), "19/01/1970 06:07:35")
    }

    @Test
    fun testGetSetLogManageSpace() {
        assertEquals(0, context.getLogManageSpace())

        context.logManageSpace()
        assertEquals(1, context.getLogManageSpace())

        for(i in 1..10) {
            context.logManageSpace()
        }
        assertEquals(11, context.getLogManageSpace())
    }

    @Test
    fun testGetLogIncorrectPasswordAttempt() {
        assertEquals(0, context.getIncorrectPasswordAttempt())

        context.logIncorrectPasswordAttempt()
        assertEquals(1, context.getIncorrectPasswordAttempt())

        for(i in 1..10) {
            context.logIncorrectPasswordAttempt()
        }
        assertEquals(11, context.getIncorrectPasswordAttempt())
    }

    @Test
    fun testGetSetPassword() {
        assertEquals(PASSWORD, context.getPassword())

        context.setPassword("1234")
        assertEquals("1234", context.getPassword())
    }

    @Test
    fun testGetSetOverstay() {
        assertEquals(0, context.getOverstay())

        context.addOverstay()
        assertEquals(1, context.getOverstay())

        for(i in 1..10) {
            context.addOverstay()
        }
        assertEquals(11, context.getOverstay())

        context.clearOverstay()
        assertEquals(0, context.getOverstay())
    }
}