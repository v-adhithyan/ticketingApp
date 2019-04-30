package ceg.avtechlabs.standticket.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper

import kotlinx.android.synthetic.main.activity_search.*
import java.util.*

class SearchActivity : AppCompatActivity() {

    val tokens = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        title = "Search"

        registerForContextMenu(list_search)
    }


    fun search(v: View) {
        val vehicleNo = search_vehicle_no.text.toString()

        val progress = ProgressDialog(this)
        progress.setMessage("Searching for vehicle..")
        progress.show()

        if(vehicleNo.length > 3) {
            val db = DbHelper(this)
            val results = db.searchVehicle(vehicleNo)

            if(results == null) {
                setAdapter(arrayOf("No such vehicle found"))
            } else {
                val details = Array<String>(results.size, {""})
                for(i in 0..results.size-1) {
                    val r = results.get(i)
                    tokens.add(r.pk)
                    //val tokenNo = "Token No: ${r.id}\n"
                    val tokenNo = ""
                    val vehicle = "Vehicle No: ${r.vehicleNo}\n"
                    val dateTime = "Date and Time: ${r.dateTime}\n"
                    var taken = "In stand: "
                    if(r.taken == 0) {
                        taken = taken + "Yes"
                    } else {
                        taken = taken + "No"
                    }

                    details[i] = "$tokenNo$vehicle$dateTime$taken"
                }
                setAdapter(details)
            }

        } else {
            search_vehicle_no.text = SpannableStringBuilder("")
            Toast.makeText(this, "Vehicle no should have 4 digits", Toast.LENGTH_LONG).show()
        }

        if(progress.isShowing) {
            progress.dismiss()
        }
    }

    fun setAdapter(results: Array<String>) {
        list_search.invalidate()
       list_search.adapter = ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1,  results)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add("Close Token")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.title == "Close Token") {
            val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
            val position = info.position
            val id = tokens[position]
            CloseTicketActivity.closeTicket("", this@SearchActivity, true, id)
        }
        return super.onContextItemSelected(item)
    }
}
