package ceg.avtechlabs.standticket.views

import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.presenters.PinPresenter
import ceg.avtechlabs.standticket.presenters.SearchPresenter
import ceg.avtechlabs.standticket.utils.Constants.PASSWORD
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class TestPin {
    private lateinit var presenter: PinPresenter
    private lateinit var view: PinPresenter.View

    @Before
    fun setup() {
        presenter = PinPresenter()
        view = mock()
        presenter.attachView(view)
    }

    @Test
    fun test_correct_password_opens_shift_activity() {
        presenter.processPin(PASSWORD)
        verify(view).startShiftActivity()
        verify(view, never()).notifyWrongPassword()
    }

    @Test
    fun test_wrong_password_shows_error_message() {
        presenter.processPin(PASSWORD.reversed())
        verify(view).notifyWrongPassword()
        verify(view, never()).startShiftActivity()
    }
}