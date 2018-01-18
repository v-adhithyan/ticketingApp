package ceg.avtechlabs.standticket.ui

import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_close_ticket.*

class CloseTicketActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {
    override fun onQRCodeRead(text: String?, points: Array<out PointF>?) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_ticket)

        qrDecoderView.setOnQRCodeReadListener(this)
        qrDecoderView.setQRDecodingEnabled(true)
        qrDecoderView.setBackCamera()

    }
}
