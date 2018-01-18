package ceg.avtechlabs.standticket

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import ceg.avtechlabs.standticket.ui.CloseTicketActivity
import ceg.avtechlabs.standticket.utils.generateQr
import ceg.avtechlabs.standticket.utils.getDateTime
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setup()
        reset()
        /*val qrCode = generateQr()
        if(qrCode != null) {
            qrView.setImageBitmap(qrCode)
        } else {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show()
        }*/
    }

    fun setup() {
        PDFBoxResourceLoader.init(applicationContext)
    }

    fun reset() {
        //val vehicleNo = editTextVehicleNo.text.toString()
        editTextVehicleNo.setText("", TextView.BufferType.EDITABLE)
        val millis = System.currentTimeMillis()
        val qrCode = generateQr(millis)
        qrView.setImageBitmap(qrCode)
        var dateTime = getDateTime(millis)
        dateTimeView.text = dateTime

    }
    fun generateTicket(v: View) {
        //inputLayout.getDrawingCache()
        inputLayout.isDrawingCacheEnabled = true
        inputLayout.buildDrawingCache()
        generatePdf(inputLayout.drawingCache)
    }

    fun generatePdf(qrCode: Bitmap) {
        val progress = ProgressDialog(this)
        progress.setTitle("Printing")
        progress.setMessage("Please wait..")
        progress.isIndeterminate = true
        progress.setCancelable(false)
        progress.show()

        Thread {
            val doc = PDDocument()
            val page = PDPage()
            doc.addPage(page)
            val font = PDType0Font.load(doc, assets.open("times.ttf"))
            val root = Environment.getExternalStorageDirectory()
            try {
                val content = PDPageContentStream(doc, page)
                val bitmap = Bitmap.createScaledBitmap(qrCode, (qrCode.width*0.5).toInt(), (qrCode.height*0.5).toInt(), true)
                val image = LosslessFactory.createFromImage(doc, bitmap)
                content.drawImage(image, 20F, 20F)

                content.close()

                val random = Random().nextInt().toString()
                val path = root.absolutePath + "/Documents/$random.pdf"
                doc.save(path)
                doc.close()

                runOnUiThread {
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                    clear()
                    progress.dismiss()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.fromFile(File(path))
                    startActivity(intent)
                }


            } catch (ex: Exception) {
                ex.printStackTrace()

                runOnUiThread {
                    Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
                    clear()
                    progress.dismiss()
                }
            }

        }.start()
    }

    fun closeTicket(v: View) {
        startActivity(Intent(this, CloseTicketActivity::class.java))
    }

    fun clear() {
        editTextVehicleNo.setText("", TextView.BufferType.EDITABLE)

    }
}
