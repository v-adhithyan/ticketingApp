package ceg.avtechlabs.standticket.utils

import android.content.Context
import android.graphics.Bitmap
import android.preference.Preference
import android.preference.PreferenceManager
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Adhithyan V on 18-01-2018.
 */

fun generateQr(time: Long): Bitmap? {
    //QRGEncoder()
    val qrEncoder = QRGEncoder("Adhi" + time.toString(), null, QRGContents.Type.TEXT, 200)

    try {
        return qrEncoder.encodeAsBitmap()
    } catch (ex: Exception) {

    }

    return null
}

fun getDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    return formatter.format(calendar.time)
}


fun Context.shiftOpened() {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = preference.edit()
    editor.putBoolean("close", false)
    editor.commit()
}

fun Context.shiftClosed() {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = preference.edit()
    editor.putBoolean("close", true)
    editor.commit()
}

fun Context.isShiftClosed(): Boolean {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    return preference.getBoolean("close", false)
}