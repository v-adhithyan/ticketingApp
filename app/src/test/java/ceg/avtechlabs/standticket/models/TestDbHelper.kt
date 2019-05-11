import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import ceg.avtechlabs.standticket.models.DbHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.hamcrest.CoreMatchers.`is` as Is


fun getToken(): String {
    return "1234567890"
}

fun getVehicleNo(): String {
    return "TN 41 AF 1234"
}

fun getDateTime(): String {
    return "01/05/2019 19:42:14"
}

fun addEntry(dbHelper: DbHelper) {
    dbHelper.add(getToken(), getVehicleNo(), getDateTime())
}

@RunWith(RobolectricTestRunner::class)
class TestDbHelper {
    val token = getToken()
    val vehicleNo = getVehicleNo()
    val fakeDateTime = getDateTime()
    val open = (token.toLong() - 1).toString()
    val close = (token.toLong() + 1).toString()

    private lateinit var context: Context
    private lateinit var dbHelper: DbHelper

    @Before
    fun setup() {
        context = getContext()
        dbHelper = DbHelper(context)
    }

    @After
    fun teardown() {
        dbHelper.close()
    }

    private fun getContext(): Context {
        return ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun test_db_search_vehicle() {
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
        assertEquals("", dbHelper.getOpen())

        dbHelper.updateOpen(open)
        assertEquals(open, dbHelper.getOpen())
    }

    @Test
    fun test_close() {
        // when db is first created, open or close returns empty value
        // open/close is used to track starting and ending tokens number for a shift.
        assertEquals("", dbHelper.getClose())

        dbHelper.updateClose(close)
        assertEquals(close, dbHelper.getClose())
    }

    @Test(expected = SQLiteException::class)
    fun test_summary_initial() {
        // exception is thrown since open and close is not set
        dbHelper.summary()
    }

    @Test
    fun test_summary() {
        dbHelper.updateOpen(open)
        dbHelper.updateClose(close)
        // since we use range between open and close, and there are no tokens added 0 is returned
        assertEquals(dbHelper.summary(), "0")

        addEntry(dbHelper)
        assertEquals(dbHelper.summary(), "1")
    }

    @Test
    fun test_summary_employee() {
        dbHelper.updateOpen(open)
        // since we use range between open and close, and there are no tokens added 0 is returned
        assertEquals(dbHelper.summaryEmployee(), "0")

        addEntry(dbHelper)
        assertEquals(dbHelper.summaryEmployee(), "1")
    }

    @Test
    fun test_close_token() {
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