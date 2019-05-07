package ceg.avtechlabs.standticket.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.utils.showLongToast
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        title = getString(R.string.button_manage)
    }

    fun go(v: View) {
        if(password.text.toString().equals("6672")) {
            startActivity(Intent(this, ShiftActivity::class.java))
            finish()
        } else {
            showLongToast(getString(R.string.wrong_password))
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
