package ceg.avtechlabs.standticket.utils

import android.content.Context
import android.graphics.Bitmap
import android.preference.PreferenceManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Adhithyan V on 18-01-2018.
 */

fun generateQr(time: Long): Bitmap? {
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

fun Context.openShift() {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = preference.edit()
    editor.putBoolean("close", false)
    editor.putBoolean("open", true)
    editor.commit()
}

fun Context.closeShift() {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = preference.edit()
    editor.putBoolean("close", true)
    editor.putBoolean("open", false)
    editor.commit()
}

fun Context.isShiftOpen(): Boolean {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    return preference.getBoolean("open", false)
}

fun Context.isShiftClosed(): Boolean {
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    return preference.getBoolean("close", false)
}