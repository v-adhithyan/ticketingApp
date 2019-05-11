package ceg.avtechlabs.standticket

import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.models.Stand
import ceg.avtechlabs.standticket.presenters.SearchPresenter
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class TestSearch {
    private lateinit var presenter: SearchPresenter
    private lateinit var view: SearchPresenter.View
    private lateinit var dbHelper: DbHelper

    @Before
    fun setup() {
        presenter = SearchPresenter()
        view = mock()
        presenter.attachView(view)
    }

    @After
    fun teardown() {
        presenter.detachView()
    }

    @Test
    fun test_search_with_empty_query_calls_show_query_required_message() {
        presenter.doSearch("")
        verify(view).showQueryRequiredMessage()
    }

    @Test
    fun test_search_with_less_than_2_characters_query_calls_show_query_required_message() {
        presenter.doSearch("1")
        verify(view).showQueryRequiredMessage()
    }

    @Test
    fun test_search_with_empty_or_less_than_2_chars_never_calls_search_results() {
        presenter.doSearch("")
        verify(view, never()).showSearchResults(LinkedList<Stand>())
    }

    @Test
    fun test_search_with_valid_data_no_results() {
        val query = "00"
        presenter.doSearch(query)
        verify(view).search(query)
        verify(view, never()).showSearchResults(LinkedList<Stand>())
    }
}

