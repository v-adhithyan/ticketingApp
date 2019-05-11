package ceg.avtechlabs.standticket.models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


/**
 * Created by Adhithyan V on 26-03-2018.
 */

open class DbHelper(context: Context) : SQLiteOpenHelper(context, DBModel.DB_NAME, null, 1) {
    val TABLE_VEHICLES = "vehicles";
    val TOKEN_NO = "token_no"
    val VEHICLE_NO = "vehicle_no"
    val DATE_TIME = "date_time"
    val TAKEN = "taken"

    val vehicleTaken = true
    val vehicleNotTaken = false

    val TABLE_OP_CL = "open_close"
    val NAME = "name"
    val VALUE = "value"
    var sql = ""

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table $TABLE_VEHICLES ( id INTEGER PRIMARY KEY AUTOINCREMENT, $TOKEN_NO integer," +
                "$VEHICLE_NO text," +
                "$DATE_TIME text," +
                "$TAKEN integer" +
                ")")

        db?.execSQL("create table $TABLE_OP_CL ($NAME text, $VALUE text)")
        db?.execSQL("insert into $TABLE_OP_CL values ('open', '')")
        db?.execSQL("insert into $TABLE_OP_CL values ('close', '')")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun add(token: String, vehicleNo: String, dateTime: String) {
        val cv = ContentValues()
        cv.put(TOKEN_NO, token.toLong())
        cv.put(VEHICLE_NO, vehicleNo)
        cv.put(DATE_TIME, dateTime)
        cv.put(TAKEN, vehicleNotTaken)

        val db = this.writableDatabase
        db.insert(TABLE_VEHICLES, null, cv)
        db.close()
    }

    fun close(token: String, isId: Boolean, id: Int): Boolean {
        var readableDb = this.readableDatabase

        if(isId) {
            sql = "select taken from $TABLE_VEHICLES where id = '$id'"
        } else {
            sql = "select taken from $TABLE_VEHICLES where $TOKEN_NO = '$token'"
        }

        val resultSet = readableDb.rawQuery(sql, null)

        if(resultSet.moveToFirst()) {
            val isTicketClosed = resultSet.getInt(0)
            if(isTicketClosed == 0) {
                val db = this.writableDatabase
                val cv = ContentValues()
                cv.put(TAKEN, vehicleTaken)
                if(isId) {
                    db.update(TABLE_VEHICLES, cv, "id = ?", arrayOf(id.toString()))
                } else {
                    db.update(TABLE_VEHICLES, cv, "$TOKEN_NO = ?", arrayOf(token))
                }
                readableDb.close()
                db.close()
                return false
            }
        }

        readableDb.close()
        return true
    }


    fun removeAllClosed() {
        val db = this.writableDatabase
        db.delete(TABLE_VEHICLES, "$TAKEN = ?", arrayOf("1") )
        db.close()
    }

    private fun updateOpenClose(value: String, whereClause: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(VALUE, value)

        db.update(TABLE_OP_CL, cv, "$NAME = ?", arrayOf(whereClause))
        db.close()
    }

    fun updateOpen(value: String) {
        updateOpenClose(value, "open")
    }

    fun updateClose(value: String) {
        updateOpenClose(value, "close")
    }

    private fun getOpenClose(selection: String): String {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_OP_CL, arrayOf(VALUE), "$NAME = ?", arrayOf(selection), null, null, null, null)

        if(cursor != null) {
            cursor.moveToFirst()
            return cursor.getString(cursor.getColumnIndex(VALUE))
        }

        return ""
    }

    fun getOpen(): String {
        return getOpenClose("open")
    }

    fun getClose(): String {
        return getOpenClose("close")
    }

    fun summaryHelper(sql: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(sql,null)
        if(cursor != null){
            cursor.moveToFirst()
            return cursor.getString(0)
        }

        return "0"
    }

    fun summary(): String? {
        return summaryHelper("SELECT count(*) from $TABLE_VEHICLES where $TOKEN_NO between ${getOpen()} and  ${getClose()}")
    }

    fun summaryEmployee(): String? {
        return summaryHelper("SELECT count(*) from $TABLE_VEHICLES where $TOKEN_NO > ${getOpen()}")
    }

    fun searchVehicle(vehicleNo: String): LinkedList<Stand>? {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_VEHICLES, arrayOf("id", TOKEN_NO, VEHICLE_NO, DATE_TIME, TAKEN),
                "$VEHICLE_NO LIKE ?", arrayOf("%$vehicleNo%"), null, null, null, null)

        if(cursor == null){
            return null
        }

        if(cursor.count == 0){
            return null
        }

        cursor.moveToFirst()
        val results = LinkedList<Stand>()
        do {
            results.add(
                    Stand(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4)))

        } while(cursor.moveToNext())

        return results
    }

}