package ceg.avtechlabs.standticket.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import dmax.dialog.SpotsDialog
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

fun getDateTime(millis: Long, utc: Boolean): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    if(utc) {
        formatter.timeZone = TimeZone.getTimeZone("UTC")
    }
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

fun Context.showLongToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.createProgressDialog(text: String): AlertDialog {
    val dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage(text)
            .setCancelable(false)
            .build()

    return dialog
}

fun Context.dismiss() {}

fun checkOrCreateListener(func: ()->Unit?): DialogInterface.OnClickListener? {
    var listener: DialogInterface.OnClickListener? = null
    if (func != null) {
        listener = DialogInterface.OnClickListener { dialogInterface, i ->  func() }
    }
    return listener
}

fun Context.showAlertDialog(title:String, message: String, positiveButtonText: String, negativeButtonText: String, okListener: ()->Unit?, cancelListener: ()->Unit?) {
    val okAction = checkOrCreateListener(okListener)
    val cancelAction = checkOrCreateListener(cancelListener)

    val alert = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, okAction)
            .setNegativeButton(negativeButtonText, cancelAction)
            .create()
    alert.show()
}