package ceg.avtechlabs.standticket.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_shift.*

class ShiftActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift)

        title = getString(R.string.title_open_close_shift)

        if(!isShiftOpen()) {
            button_close_shift.visibility = View.INVISIBLE
            button_summary.visibility = View.INVISIBLE
        } else {
            button_open_shift.visibility = View.INVISIBLE
        }
    }

    fun open() {
        val alert = AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.confirm_open_shift_now))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialogInterface, i ->
                    val db = DbHelper(this)
                    db.updateOpen(System.currentTimeMillis().toString())
                    db.updateClose("")
                    db.close()
                    showLongToast(getString(R.string.shift_opened))

                    openShift()

                    Thread {
                        db.removeAllClosed()
                    }.start()

                    startActivity(Intent(this@ShiftActivity, MainActivity::class.java))
                    finish()
                }).create()
        alert.show()
    }

    fun setOpen(v: View) {
        open()
    }

    fun setClose(v: View) {
        val alert = AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.confirm_close_shift_now))
                .setNegativeButton(getString(R.string.no), null)
                .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialogInterface, i ->
                    val db = DbHelper(this@ShiftActivity)
                    db.updateClose(System.currentTimeMillis().toString())
                    db.close()
                    showLongToast(getString(R.string.shift_closed))
                    closeShift()
                    button_close_shift.visibility = View.INVISIBLE
                })
                .create()
        alert.show()

    }

    fun summary(v: View) {
        if(!isShiftClosed()) {
            showLongToast(getString(R.string.close_shift_to_view_summary))
        } else {
            val db = DbHelper(this)
            val count = db.summary()
            showLongToast("${count.toString()} tokens issued.")
            button_open_shift.visibility = View.VISIBLE
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
