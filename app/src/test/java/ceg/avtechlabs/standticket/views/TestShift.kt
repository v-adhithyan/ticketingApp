package ceg.avtechlabs.standticket.views

import ceg.avtechlabs.standticket.presenters.ManagePresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class TestShift {
    private lateinit var presenter: ManagePresenter
    private lateinit var view: ManagePresenter.View

    @Before
    fun setup() {
        presenter = ManagePresenter()
        view = mock()
        presenter.attachView(view)
    }

    @Test
    fun test_open_shift() {
        presenter.doOpen()
        verify(view).open()
    }

    @Test
    fun test_close_shift() {
        presenter.doClose()
        verify(view).close()
    }

    @Test
    fun test_summary_with_shift_not_closed() {
        presenter.showSummary(false)
        verify(view).showToastToCloseShiftForViewingSummary()
        verify(view, never()).showSummaryDetails()
    }

    @Test
    fun test_summary_with_shift_closed() {
        presenter.showSummary(true)
        verify(view, never()).showToastToCloseShiftForViewingSummary()
        verify(view).showSummaryDetails()
    }

    @Test
    fun test_change_password() {

    }
}
