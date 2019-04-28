package ceg.avtechlabs.standticket.ui

import android.app.ProgressDialog
import android.content.Context
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AlertDialog
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.getDateTime
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_close_ticket.*
import kotlinx.android.synthetic.main.activity_main.*

class CloseTicketActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {
    override fun onQRCodeRead(text: String, points: Array<out PointF>?) {
        qrDecoderView.setQRDecodingEnabled(false)

        //Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        var token = text.replace("Adhi", "")

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)

        Toast.makeText(this, "Token $token closed successfully", Toast.LENGTH_LONG).show()
        val progressBar = ProgressDialog(this)
        progressBar.setCancelable(false)
        progressBar.setMessage("Closing ticket ..")
        progressBar.isIndeterminate = true
        progressBar.setTitle("Please wait")
        progressBar.show()

        val db = DbHelper(this)
        val ticketClosed = db.close(token)
        progressBar.dismiss()
        if(ticketClosed) {
            Toast.makeText(this, "Ticket is already closed.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ticket closed successfully.", Toast.LENGTH_LONG).show()
        }

        qrDecoderView.setQRDecodingEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_ticket)

        qrDecoderView.setOnQRCodeReadListener(this)
        qrDecoderView.setQRDecodingEnabled(true)
        qrDecoderView.setBackCamera()

    }
}
