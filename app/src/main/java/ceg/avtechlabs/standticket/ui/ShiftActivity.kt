package ceg.avtechlabs.standticket.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper

import kotlinx.android.synthetic.main.activity_shift.*

class ShiftActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift)

    }

    fun setOpen(v: View) {
        val db = DbHelper(this)
        db.updateOpen(System.currentTimeMillis().toString())
        db.updateClose("")
        db.close()
    }

    fun setClose(v: View) {
        val db = DbHelper(this)
        db.updateClose(System.currentTimeMillis().toString())
        db.close()
    }

    fun summary(v: View) {
        val db = DbHelper(this)
        val count = db.summary()
        Toast.makeText(this, count.toString(), Toast.LENGTH_LONG).show()
    }

}
