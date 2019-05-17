package ceg.avtechlabs.standticket.views

import ceg.avtechlabs.standticket.presenters.ManageSpacePresenter
import ceg.avtechlabs.standticket.presenters.PinPresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class TestManageSpace {
    private lateinit var presenter: ManageSpacePresenter
    private lateinit var view: ManageSpacePresenter.View

    @Before
    fun setup() {
        presenter = ManageSpacePresenter()
        view = mock()
        presenter.attachView(view)
    }

    @Test
    fun test_log_manage_space_called() {
        verify(view).logActivity()
    }
}