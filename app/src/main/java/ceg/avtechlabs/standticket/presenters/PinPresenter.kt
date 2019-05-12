package ceg.avtechlabs.standticket.presenters

import ceg.avtechlabs.standticket.utils.Constants.PASSWORD

open class PinPresenter: BasePresenter<PinPresenter.View>() {

    fun processPin(pin: String) {
        if(pin.equals(PASSWORD)) {
            view?.startShiftActivity()
        } else {
            view?.notifyWrongPassword()
        }
    }

    interface View {
        fun startShiftActivity()
        fun notifyWrongPassword()
    }
}