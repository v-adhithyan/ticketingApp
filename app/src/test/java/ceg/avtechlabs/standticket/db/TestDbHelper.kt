import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import ceg.avtechlabs.standticket.db.DbHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.hamcrest.CoreMatchers.`is` as Is


@RunWith(RobolectricTestRunner::class)
class TestDbHelper {
    val token = "1234567890"
    val vehicleNo = "TN 41 AF 1234"
    val fakeDateTime = "01/05/2019 19:42:14"
    val open = (token.toLong() - 1).toString()
    val close = (token.toLong() + 1).toString()

    private fun addEntry(dbHelper: DbHelper) {
        dbHelper.add(token, vehicleNo, fakeDateTime)
    }

    private fun getContext(): Context {
        return ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun test_db_search_vehicle() {

        val context = getContext()
        val dbHelper = DbHelper(context)

        // No data in db, so it should return null
        var result = dbHelper.searchVehicle("1234")
        assertNull(result)

        // add one entry to db
        addEntry(dbHelper)

        // assert when searched for the above entry, it is returned
        result = dbHelper.searchVehicle("1234")
        assertNotNull(result)
        assertEquals(result?.size, 1)
    }

    @Test
    fun test_open() {
        val context = getContext()
        val dbHelper = DbHelper(context)

        assertEquals("", dbHelper.getOpen())

        dbHelper.updateOpen(open)
        assertEquals(open, dbHelper.getOpen())
    }

    @Test
    fun test_close() {
        val context = getContext()
        val dbHelper = DbHelper(context)

        // when db is first created, open or close returns empty value
        // open/close is used to track starting and ending tokens number for a shift.
        assertEquals("", dbHelper.getClose())

        dbHelper.updateClose(close)
        assertEquals(close, dbHelper.getClose())
    }

    @Test(expected = SQLiteException::class)
    fun test_summary_initial() {
        // exception is thrown since open and close is not set
        val context = getContext()
        val dbHelper = DbHelper(context)
        dbHelper.summary()
    }

    @Test
    fun test_summary() {
        val context = getContext()
        val dbHelper = DbHelper(context)

        dbHelper.updateOpen(open)
        dbHelper.updateClose(close)
        // since we use range between open and close, and there are no tokens added 0 is returned
        assertEquals(dbHelper.summary(), "0")

        addEntry(dbHelper)
        assertEquals(dbHelper.summary(), "1")
    }

    @Test
    fun test_summary_employee() {
        // same as summary
        // added here for coverage
        val context = getContext()
        val dbHelper = DbHelper(context)

        dbHelper.updateOpen(open)
        // since we use range between open and close, and there are no tokens added 0 is returned
        assertEquals(dbHelper.summaryEmployee(), "0")

        addEntry(dbHelper)
        assertEquals(dbHelper.summaryEmployee(), "1")
    }

    @Test
    fun test_close_token() {
        val context = getContext()
        val dbHelper = DbHelper(context)

        // return true when there is no token in db
        assert(dbHelper.close(token, false, 0))

        addEntry(dbHelper)
        // when token is closed, assert False
        assertFalse(dbHelper.close(token, false, 0))

        // already closed token should not be closed again, assert true
        assert(dbHelper.close(token, false, 0))
    }

    @Test
    fun test_remove_all_closed() {
        val context = getContext()
        val dbHelper = DbHelper(context)

        addEntry(dbHelper)
        dbHelper.updateOpen(open)
        assertEquals("1", dbHelper.summaryEmployee())

        // remove all closed, will remove all closed tokens from db
        // so summary should return 0 after remove_all_closed is called
        // caveat: summary will not consider closed token since it is not present in the db
        // we make sure summary returns no of tokens issued during shift, since we remove all closed tokens during shift closure.
        // there is no other way from app to remove all closed tokens.
        dbHelper.close(token, false, 0)
        dbHelper.removeAllClosed()
        assertEquals("0", dbHelper.summaryEmployee())
    }
}