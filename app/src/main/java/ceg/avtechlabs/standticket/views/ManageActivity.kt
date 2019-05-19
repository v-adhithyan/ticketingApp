package ceg.avtechlabs.standticket.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.presenters.ManagePresenter
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_shift.*
import org.jetbrains.anko.toast
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class ManageActivity : AppCompatActivity(), ManagePresenter.View {
    private val presenter:ManagePresenter = ManagePresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shift)

        title = getString(R.string.title_open_close_shift)

        presenter.attachView(this)
        checkAndEnableButtons()
        displayActivityLog()
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
        val db = DbHelper(this@ManageActivity)
        db.updateClose(System.currentTimeMillis().toString())
        db.close()
        toast(getString(R.string.shift_closed))
        closeShift()
        hideCloseShiftButton()
        showOpenShiftButton()
    }

    override fun hideCloseShiftButton() {
        button_close_shift.visibility = View.INVISIBLE
    }

    override fun hideOpenShiftButton() {
        button_open_shift.visibility = View.INVISIBLE
    }

    fun showOpenShiftButton() {
        button_open_shift.visibility = View.VISIBLE
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
        startActivity(Intent(this@ManageActivity, MainActivity::class.java))
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

    fun displayActivityLog() {
        val clearDataAttempts = "${getString(R.string.attempts_to_clear_data)}: ${getLogManageSpace()}\n"
        val wrongPasswordAttempts = "${getString(R.string.wrong_password_attempts)}: ${getIncorrectPasswordAttempt()}"
        textview_activity_log.text = clearDataAttempts + wrongPasswordAttempts
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_manage, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change_password -> {
                presenter.showPasswordChangeDialog(this, getString(R.string.menu_change_password), getString(R.string.positive_button), getString(R.string.no))
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    override fun changePassword(password: String) {
        if(password.length != 4) {
            showLongToast(getString(R.string.password_constraints_not_met))
        } else {
            setPassword(password)
            showLongToast(getString(R.string.password_change_success))
        }
    }
}
