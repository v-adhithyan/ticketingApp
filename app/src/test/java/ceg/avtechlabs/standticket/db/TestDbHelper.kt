import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ceg.avtechlabs.standticket.db.DbHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.hamcrest.CoreMatchers.`is` as Is


@RunWith(RobolectricTestRunner::class)
class TestDbHelper {

    @Test
    fun test_db_search_vehicle() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbHelper = DbHelper(context)

        // No data in db, so it should return null
        var result = dbHelper.searchVehicle("1234")
        assertNull(result)

        // add one entry to db
        val token = "1234567890"
        val vehicleNo = "TN 41 AF 1234"
        val fakeDateTime = "01/05/2019 19:42:14"
        dbHelper.add(token, vehicleNo, fakeDateTime)

        // assert when searched for the above entry, it is returned
        result = dbHelper.searchVehicle("1234")
        assertNotNull(result)
        assertEquals(result?.size, 1)
    }
}