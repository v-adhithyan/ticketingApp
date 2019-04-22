package ceg.avtechlabs.standticket.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.closeShift
import ceg.avtechlabs.standticket.utils.isShiftClosed
import ceg.avtechlabs.standticket.utils.isShiftOpen
import ceg.avtechlabs.standticket.utils.openShift

import kotlinx.android.synthetic.main.activity_shift.*

class ShiftActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift)

        title = "Open / Close shift"
        if(!isShiftOpen()) {
            button_close_shift.visibility = View.INVISIBLE
            button_summary.visibility = View.INVISIBLE
        } else {
            button_open_shift.visibility = View.INVISIBLE
        }

    }

    fun open() {
        val db = DbHelper(this)
        db.updateOpen(System.currentTimeMillis().toString())
        db.updateClose("")
        db.close()
        Toast.makeText(this, "Shift opened", Toast.LENGTH_LONG).show()

        openShift()
    }

    fun setOpen(v: View) {
        open()
        //finish()
        val db = DbHelper(this)
        Thread {
            db.removeAllClosed()
        }.start()

        startActivity(Intent(this@ShiftActivity, MainActivity::class.java))
        finish()
    }

    fun setClose(v: View) {
        val alert = AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Alert")
                .setMessage("Do you want to close the shift?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                    val db = DbHelper(this@ShiftActivity)
                    db.updateClose(System.currentTimeMillis().toString())
                    db.close()
                    Toast.makeText(this, "Shift closed", Toast.LENGTH_LONG).show()
                    closeShift()

                    button_close_shift.visibility = View.INVISIBLE
                    //open()
                })
                .create()
        alert.show()

    }

    fun summary(v: View) {
        if(!isShiftClosed()) {
            Toast.makeText(this, "Close shift to view summary", Toast.LENGTH_LONG).show()
        } else {
            val db = DbHelper(this)
            val count = db.summary()
            Toast.makeText(this, "${count.toString()} tokens issued.", Toast.LENGTH_LONG).show()
            button_open_shift.visibility = View.VISIBLE
            //open()
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
