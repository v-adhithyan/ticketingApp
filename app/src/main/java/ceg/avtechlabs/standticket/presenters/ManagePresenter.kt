package ceg.avtechlabs.standticket.presenters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.widget.EditText

class ManagePresenter: BasePresenter<ManagePresenter.View>() {

    fun showSummary(shiftClosed: Boolean) {
        if(!shiftClosed) {
            view?.showToastToCloseShiftForViewingSummary()
        } else {
            view?.showSummaryDetails()
        }
    }

    fun doOpen() {
        view?.open()
    }

    fun doClose() {
        view?.close()
    }

    fun showPasswordChangeDialog(context: Context, title: String, positiveText: String, negativeText: String) {
        val input = EditText(context)
        input.inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setView(input)
        builder.setPositiveButton(positiveText, object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                view?.changePassword(input.text.toString())
            }
        })
        builder.setNegativeButton(negativeText, null)
        builder.create()
        builder.show()
    }

    interface View {
        fun open()
        fun close()
        fun showToastToCloseShiftForViewingSummary()
        fun showSummaryDetails()
        fun hideCloseShiftButton()
        fun hideOpenShiftButton()
        fun hideSummaryButton()
        fun startMainActivity()
        fun changePassword(text: String)
    }
}