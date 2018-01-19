package ceg.avtechlabs.standticket

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
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
        /*inputLayout.isDrawingCacheEnabled = true
        inputLayout.buildDrawingCache()
        generatePdf(inputLayout.drawingCache)*/
        val drawable = qrView.drawable as BitmapDrawable

        generatePdf(drawable.bitmap)
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
                //val bitmap = Bitmap.createScaledBitmap(qrCode, (qrCode.width*0.5).toInt(), (qrCode.height*0.5).toInt(), true)
                //val image = LosslessFactory.createFromImage(doc, bitmap)

                //val image = qrCode
                val image = LosslessFactory.createFromImage(doc, qrCode)
                // positioned the image at this point (0, 575) after many trial and errors
                content.drawImage(image, 0F, 575F)

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 20f);
                content.newLineAtOffset(200F, 700f);
                content.showText("POLLACHI MUNICIPALITY BIKE STAND");
                content.endText()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 10F);
                content.newLineAtOffset(200F, 685f);
                content.showText("(Near Nachimuthu nursing home, Market Road, Pollachi)");
                content.endText()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 15F);
                content.newLineAtOffset(200f, 678F)
                content.showText("---------------------------------------------------------");
                content.endText()
                content.close()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 12F);
                content.newLineAtOffset(200f, 670F)
                content.showText("DATE AND TIME: ${dateTimeView.text.toString()}");
                content.endText()
                content.close()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 15F);
                content.newLineAtOffset(200f, 655F)
                content.showText("VEHICLE NO: ${editTextVehicleNo.text.toString()}");
                content.endText()
                content.close()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 15F);
                content.newLineAtOffset(200f, 648F)
                content.showText("---------------------------------------------------------");
                content.endText()
                content.close()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 8f);
                content.newLineAtOffset(200f, 640F)
                content.showText("1. If you lose this token, please produce valid ownership document to retake vehicle");
                content.endText()
                content.close()

                content.beginText();
                content.setNonStrokingColor(0, 0, 0);
                content.setFont(font, 8f);
                content.newLineAtOffset(200f, 625f)
                content.showText("2. Don't side lock the vehicle.");
                content.endText()
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

                    val uri = FileProvider.getUriForFile(this,  this.applicationContext.packageName + ".provider",
                            File(path))
                    intent.setDataAndType(uri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
