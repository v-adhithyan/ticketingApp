package ceg.avtechlabs.standticket.presenters

import ceg.avtechlabs.standticket.models.Stand
import java.util.*

// Reference: https://www.raywenderlich.com/195-android-unit-testing-with-mockito
class SearchPresenter : BasePresenter<SearchPresenter.View>() {

    fun doSearch(query: String) {
        if (query.trim().isBlank() or (query.trim().length < 2)) {
            view?.showQueryRequiredMessage()
        } else {
            view?.search(query)
        }
    }

    interface View {
        fun showQueryRequiredMessage()
        fun search(query: String)
        fun showSearchResults(results: LinkedList<Stand>)
        fun showEmptySearchResult()
    }
}