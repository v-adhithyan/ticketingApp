package ceg.avtechlabs.standticket.presenters

class ManageSpacePresenter: BasePresenter<ManageSpacePresenter.View>() {

    override fun attachView(view: View) {
        super.attachView(view)
        view?.logActivity()
    }

    interface View {
        fun logActivity()
    }
}