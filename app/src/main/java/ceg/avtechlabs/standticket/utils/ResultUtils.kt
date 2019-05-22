package ceg.avtechlabs.standticket.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.models.Stand
import android.net.Uri
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread


fun Context.formatVehicle(result: Stand): String {
    return "${getString(R.string.vehicle_no)}:\t${result.vehicleNo}\n"
}

fun Context.getDateTime(result: Stand): String {
    return "${getString(R.string.date_and_time)}:\t${result.dateTime}\n"
}

fun Context.isInStand(result: Stand): String {
    val takenMap = mapOf<Int, String>(0 to getString(R.string.yes), 1 to getString(R.string.no))
    return "${getString(R.string.is_in_stand)}:\t${takenMap.get(result.taken)}"
}

fun Context.getPaymentDue(vehicleTimestamp: Long): String {
    val currentTimestamp = System.currentTimeMillis()
    val day = 24 * 60 * 60
    val elapsedTime = (currentTimestamp - vehicleTimestamp) / (day * 1000)

    if (elapsedTime <= 0) {
        return ""
    }

    return "${String.format(getString(R.string.overstay), elapsedTime)}\n"
}


fun Context.backup() {
    val progress = createProgressDialog("")
    val db = DbHelper(this)
    progress.show()

    doAsync {
        val body = db.getShiftData()

        runOnUiThread {
            this.writeCsv(body)

            val intent = Intent(Intent.ACTION_VIEW)
            val subject = "Data for ${getDate(System.currentTimeMillis())}"
            val data = Uri.parse("mailto:aavispeaks@gmail.com?subject=$subject&body=$body")
            intent.setData(data)

            progress.dismiss()

            startActivity(intent)
        }

    }
}