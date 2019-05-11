package ceg.avtechlabs.standticket.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.models.Stand
import ceg.avtechlabs.standticket.presenters.SearchPresenter
import ceg.avtechlabs.standticket.utils.createProgressDialog
import ceg.avtechlabs.standticket.utils.showLongToast
import kotlinx.android.synthetic.main.activity_search.*
import java.util.*

class SearchActivity : AppCompatActivity(), SearchPresenter.View {
    private val tokens = mutableListOf<Int>()

    private val presenter: SearchPresenter = SearchPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        title = getString(R.string.title_search)

        presenter.attachView(this)
        registerForContextMenu(list_search)
    }


    fun search(v: View) {
        val vehicleNo = search_vehicle_no.text.toString()
        val progress = createProgressDialog(getString(R.string.searching_vehicle))
        progress.show()

        presenter.doSearch(vehicleNo)

        progress.dismiss()
    }

    fun setAdapter(results: Array<String>) {
        list_search.invalidate()
        list_search.adapter = ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1,  results)
    }

    private fun populateDetails(results: LinkedList<Stand>): Array<String> {
        val details = Array(results.size, {""})
        for(i in 0..results.size-1) {
            val r = results.get(i)
            tokens.add(r.pk)
            //val tokenNo = "Token No: ${r.id}\n"
            val tokenNo = ""
            val vehicle ="${getString(R.string.vehicle_no)}: ${r.vehicleNo}\n"
            val dateTime = "${getString(R.string.date_and_time)}: ${r.dateTime}\n"
            var taken = "${getString(R.string.is_in_stand)}: "
            if(r.taken == 0) {
                taken = taken + getString(R.string.yes)
            } else {
                taken = taken + getString(R.string.no)
            }

            details[i] = "$tokenNo$vehicle$dateTime$taken"
        }
        return details
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add(getString(R.string.close_token_option))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.title == getString(R.string.close_token_option)) {
            val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
            val position = info.position
            val id = tokens[position]
            CloseTicketActivity.closeTicket("", this@SearchActivity, true, id)
        }
        return super.onContextItemSelected(item)
    }

    override fun showQueryRequiredMessage() {
        showLongToast(getString(R.string.toast_enter_atleast_3_chars))
    }

    override fun search(query: String) {
        val db = DbHelper(this)
        val results = db.searchVehicle(query)

        if (results == null) {
            showEmptySearchResult()
        } else {
            showSearchResults(results)
        }
    }

    override fun showSearchResults(results: LinkedList<Stand>) {
        setAdapter(populateDetails(results))
    }

    override fun showEmptySearchResult() {
        setAdapter(arrayOf(getString(R.string.vehicle_not_found)))
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
