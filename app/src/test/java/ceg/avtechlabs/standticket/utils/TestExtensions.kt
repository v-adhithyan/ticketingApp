package ceg.avtechlabs.standticket.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
}