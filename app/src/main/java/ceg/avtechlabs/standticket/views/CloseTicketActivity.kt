package ceg.avtechlabs.standticket.views

import android.app.AlertDialog
import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.utils.*
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_close_ticket.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class CloseTicketActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {

    override fun onQRCodeRead(text: String, points: Array<out PointF>?) {
        qrDecoderView.setQRDecodingEnabled(false)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)

        val token = text.replace("Adhi", "")
        val details = DbHelper(this).getTokenDetails(token)
        val overstay = getPaymentDue(token.toLong())
        if(!overstay.equals("")) {
            this.addOverstay()
        }
        val message = "${formatVehicle(details!!)}${getDateTime(details!!)}$overstay"
        showTicketDetails(message, token)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_ticket)

        qrDecoderView.setOnQRCodeReadListener(this)
        qrDecoderView.setQRDecodingEnabled(true)
        qrDecoderView.setBackCamera()

    }

    companion object {
        fun closeTicket(token: String, context: Context, id: Boolean, idVal: Int) {
            val progressBar = context.createProgressDialog(context.getString(R.string.closing_ticket))
            progressBar.show()

            val db = DbHelper(context)
            val ticketClosed = db.close(token, id, idVal)

            progressBar.dismiss()

            if(ticketClosed) {
                context.showLongToast(context.getString(R.string.ticket_already_closed))
            } else {
                context.showLongToast(context.getString(R.string.ticket_closed_successfully))
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun showTicketDetails(message: String, token: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.ticket_details))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.positive_button), {dialog, i ->
            closeTicket(token, this@CloseTicketActivity, false, 0)
            finish()
        })
        builder.setCancelable(false)
        builder.create()
        builder.show()
    }
}
