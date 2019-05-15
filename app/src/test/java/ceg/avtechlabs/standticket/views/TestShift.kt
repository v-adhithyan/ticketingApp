package ceg.avtechlabs.standticket.views

import ceg.avtechlabs.standticket.presenters.PinPresenter
import ceg.avtechlabs.standticket.presenters.ShiftPresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class TestShift {
    private lateinit var presenter: ShiftPresenter
    private lateinit var view: ShiftPresenter.View

    @Before
    fun setup() {
        presenter = ShiftPresenter()
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
}
