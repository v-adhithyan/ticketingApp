package ceg.avtechlabs.standticket.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.presenters.PinPresenter
import ceg.avtechlabs.standticket.utils.getPassword
import ceg.avtechlabs.standticket.utils.logIncorrectPasswordAttempt
import ceg.avtechlabs.standticket.utils.showLongToast
import kotlinx.android.synthetic.main.activity_pin.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class PinActivity : AppCompatActivity(), PinPresenter.View {
    private val presenter: PinPresenter = PinPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        presenter.attachView(this)
        title = getString(R.string.button_manage)
    }

    fun go(v: View) {
       presenter.processPin(password.text.toString(), getPassword())
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun startShiftActivity() {
        startActivity(Intent(this, ManageActivity::class.java))
        finish()
    }

    override fun notifyWrongPassword() {
        showLongToast(getString(R.string.wrong_password))
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun recordIncorrectPasswordAttempt() {
        logIncorrectPasswordAttempt()
    }
}
