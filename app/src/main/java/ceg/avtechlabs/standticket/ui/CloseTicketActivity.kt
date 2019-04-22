package ceg.avtechlabs.standticket.ui

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
        //Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        var token = text.replace("Adhi", "")

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
        //alert.show()

        Toast.makeText(this, "Token $token closed successfully", Toast.LENGTH_LONG).show()
        Thread {
            val db = DbHelper(this@CloseTicketActivity)
            db.close(token)
            db.close()
            finish()
        }.start()
        //
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_ticket)

        qrDecoderView.setOnQRCodeReadListener(this)
        qrDecoderView.setQRDecodingEnabled(true)
        qrDecoderView.setBackCamera()

    }
}
