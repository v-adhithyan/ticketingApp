package ceg.avtechlabs.standticket.utils

import android.content.Context
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.Stand

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