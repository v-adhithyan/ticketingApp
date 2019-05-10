package ceg.avtechlabs.standticket.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*


@RunWith(RobolectricTestRunner::class)
class TestExtensions {
    private fun getContext(): Context {
        return ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testCloseShift() {
        val context = getContext()
        assertFalse(context.isShiftClosed())

        context.closeShift()
        assert(context.isShiftClosed())

        context.openShift()
        assertFalse(context.isShiftClosed())
    }

    @Test
    fun testOpenShift() {
        val context = getContext()
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
}