package ceg.avtechlabs.standticket.views

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.presenters.ManageSpacePresenter
import ceg.avtechlabs.standticket.utils.logManageSpace
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class ManageSpaceActivity : AppCompatActivity(), ManageSpacePresenter.View {
    var presenter: ManageSpacePresenter = ManageSpacePresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_space)
        presenter.attachView(this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun logActivity() {
        this.logManageSpace()
    }


    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
