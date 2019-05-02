package ceg.avtechlabs.standticket.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_shift.*
import org.jetbrains.anko.toast

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
        showAlertDialog(getString(R.string.alert_title),
                getString(R.string.confirm_open_shift_now),
                getString(R.string.yes),
                getString(R.string.no),
                ::updateDbAndOpenShift,
                ::dismiss
        )
    }

    fun setOpen(v: View) {
        open()
    }

    fun setClose(v: View) {
        showAlertDialog(getString(R.string.alert_title),
                getString(R.string.confirm_close_shift_now),
                getString(R.string.yes),
                getString(R.string.no),
                ::updateDbAndCloseShift,
                ::dismiss
        )
    }

    fun summary(v: View) {
        if(!isShiftClosed()) {
            toast(getString(R.string.close_shift_to_view_summary))
        } else {
            val db = DbHelper(this)
            val count = db.summary()
            toast("${count.toString()} tokens issued.")
            button_open_shift.visibility = View.VISIBLE
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun updateDbAndCloseShift() {
        val db = DbHelper(this@ShiftActivity)
        db.updateClose(System.currentTimeMillis().toString())
        db.close()
        toast(getString(R.string.shift_closed))
        closeShift()
        button_close_shift.visibility = View.INVISIBLE
    }

    private fun updateDbAndOpenShift() {
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
    }
}
