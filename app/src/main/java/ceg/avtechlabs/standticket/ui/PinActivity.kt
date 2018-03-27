package ceg.avtechlabs.standticket.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import kotlinx.android.synthetic.main.activity_pin.*

class PinActivity : AppCompatActivity() {

    var extra = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        extra = intent.getStringExtra(MainActivity.START)
    }

    fun go(v: View) {
        if(password.text.toString().equals("6672")) {
            if(extra.equals(MainActivity.SEARCH)) {
                val Intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            } else if(extra.equals(MainActivity.CLOSE)) {
                val Intent = Intent(this, ShiftActivity::class.java)
                startActivity(intent)
            } else {

            }
        } else {
            Toast.makeText(this, "wrong password", Toast.LENGTH_LONG).show()
        }
    }
}
