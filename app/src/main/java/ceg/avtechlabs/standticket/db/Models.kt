package ceg.avtechlabs.standticket.db

/**
 * Created by Adhithyan V on 26-03-2018.
 */

data class Stand(val id: Int, val vehicleNo: String, val dateTime: String, val taken: Int)

class DBModel {
    companion object {
        val DB_NAME = "POY_CYCLE_STAND"
    }
}
