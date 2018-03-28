package ceg.avtechlabs.standticket.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseUtils
import android.util.Log
import java.util.*


/**
 * Created by Adhithyan V on 26-03-2018.
 */

class DbHelper(context: Context) : SQLiteOpenHelper(context, DBModel.DB_NAME, null, 1) {
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

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table $TABLE_VEHICLES ( $TOKEN_NO integer primary key," +
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
        Log.e("adhi", "insert" + db.insert(TABLE_VEHICLES, null, cv))
        db.close()
    }

    fun close(token: String) {
        //val query = "delete from $VEHICLE_NO where $TOKEN_NO = '$token'"
        val db = this.writableDatabase
        //db.execSQL(query)

        val cv = ContentValues()
        cv.put(TAKEN, vehicleTaken)
        db.update(TABLE_VEHICLES, cv, "$TOKEN_NO = ?", arrayOf(token))
        db.close()
    }

    fun listAll(open: String, close: String) {
        val db = this.readableDatabase
    }

    fun listAll(open: String) {

    }

    fun search(vehicleNo: String) {

    }

    fun updateOpen(value: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(VALUE, value)

        db.update(TABLE_OP_CL, cv, "$NAME = ?", arrayOf("open"))
    }

    fun updateClose(value: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(VALUE, value)

        db.update(TABLE_OP_CL, cv, "$NAME = ?", arrayOf("close"))
    }

    fun getOpen(): String {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_OP_CL, arrayOf(VALUE), "$NAME = ?", arrayOf("open"), null, null, null, null)

        if(cursor != null) {
            cursor.moveToFirst()
            return cursor.getString(cursor.getColumnIndex(VALUE))
        } else {
            return ""
        }
    }

    fun getClose(): String {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_OP_CL, arrayOf(VALUE), "$NAME = ?", arrayOf("close"), null, null, null, null)

        if(cursor != null) {
            cursor.moveToFirst()
            return cursor.getString(cursor.getColumnIndex(VALUE))
        } else {
            return ""
        }

    }

    fun summary(): String? {
        val open = getOpen().toString()
        val close = getClose().toString()

        Log.e("adhi", open)
        Log.e("adhi", close)
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT count(*) from $TABLE_VEHICLES where $TOKEN_NO between ${open.toLong()} and  ${close.toLong()}",null)
        if(cursor != null){
            cursor.moveToFirst()
            return cursor.getString(0)
        } else {
            return "0"
        }

        //return count.toString()
    }

    fun summaryEmployee(): String? {
        val open = getOpen().toString()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT count(*) from $TABLE_VEHICLES where $TOKEN_NO > ${open}",null)
        if(cursor != null){
            cursor.moveToFirst()
            return cursor.getString(0)
        } else {
            return "0"
        }
    }

    fun searchVehicle(vehicleNo: String): LinkedList<Stand>? {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_VEHICLES, arrayOf(TOKEN_NO, VEHICLE_NO, DATE_TIME, TAKEN),
                "$VEHICLE_NO LIKE ?", arrayOf("%$vehicleNo"), null, null, null, null)

        if(cursor == null){
            return null
        }

        if(cursor.count == 0){
            return null
        }

        cursor.moveToFirst()
        val results = LinkedList<Stand>()

        do {

            results.add( Stand(cursor.getInt(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getInt(3)))

        }while(cursor.moveToNext())

        return results
    }
}