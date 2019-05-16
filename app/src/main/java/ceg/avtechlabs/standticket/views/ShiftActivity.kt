package ceg.avtechlabs.standticket.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.presenters.ShiftPresenter
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_shift.*
import org.jetbrains.anko.toast
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class ShiftActivity : AppCompatActivity(), ShiftPresenter.View {
    private val presenter:ShiftPresenter = ShiftPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift)

        title = getString(R.string.title_open_close_shift)

        presenter.attachView(this)
        checkAndEnableButtons()
    }

    override fun open() {
        showAlertDialog(getString(R.string.alert_title),
                getString(R.string.confirm_open_shift_now),
                getString(R.string.yes),
                getString(R.string.no),
                ::updateDbAndOpenShift,
                ::dismiss
        )
    }

    fun setOpen(v: View) {
        presenter.doOpen()
    }

    fun setClose(v: View) {
        presenter.doClose()
    }

    override fun close() {
        showAlertDialog(getString(R.string.alert_title),
                getString(R.string.confirm_close_shift_now),
                getString(R.string.yes),
                getString(R.string.no),
                ::updateDbAndCloseShift,
                ::dismiss
        )
    }

    override fun showToastToCloseShiftForViewingSummary() {
        toast(getString(R.string.close_shift_to_view_summary))
    }

    override fun showSummaryDetails() {
        val db = DbHelper(this)
        val count = db.summary()
        toast("${count.toString()} tokens issued.")
        button_open_shift.visibility = View.VISIBLE
    }

    fun summary(v: View) {
        presenter.showSummary(isShiftClosed())
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
        hideCloseShiftButton()
    }

    override fun hideCloseShiftButton() {
        button_close_shift.visibility = View.INVISIBLE
    }

    override fun hideOpenShiftButton() {
        button_open_shift.visibility = View.INVISIBLE
    }

    override fun hideSummaryButton() {
        button_summary.visibility = View.INVISIBLE
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
        startMainActivity()
    }

    override fun startMainActivity() {
        startActivity(Intent(this@ShiftActivity, MainActivity::class.java))
        finish()
    }

    private fun checkAndEnableButtons() {
        if(!isShiftOpen()) {
            hideSummaryButton()
            hideCloseShiftButton()
        } else {
            hideOpenShiftButton()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
  
    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
