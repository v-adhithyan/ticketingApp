package ceg.avtechlabs.standticket.presenters

class ShiftPresenter: BasePresenter<ShiftPresenter.View>() {

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

    interface View {
        fun open()
        fun close()
        fun showToastToCloseShiftForViewingSummary()
        fun showSummaryDetails()
        fun hideCloseShiftButton()
        fun hideOpenShiftButton()
        fun hideSummaryButton()
        fun startMainActivity()
    }
}