package ceg.avtechlabs.standticket

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.content.FileProvider
import android.text.Spannable
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import ceg.avtechlabs.standticket.ui.CloseTicketActivity
import ceg.avtechlabs.standticket.utils.PrinterCommands
import ceg.avtechlabs.standticket.utils.Utils
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
import java.io.*
import java.nio.file.Paths
import java.util.*

class MainActivity : AppCompatActivity() {

    val ENABLE_BLUETOOTH = 3592
    var device: BluetoothDevice ? = null
    var socket: BluetoothSocket? = null
    var outStream: OutputStream? = null
    var inStream: InputStream? = null

    var workerThread: Thread? = null
    var readBuffer: ByteArray? = null
    var readBufferPosition = 0
    var stopWorker = false
    val value = ""

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

        //password: ticket, alias: ticket
        //fname: adhi
        enableBluetoothAndPrinterSetup()
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

        /*Thread {
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

                //var uri = ""
                //runOnUiThread {
                Looper.prepare()
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                    clear()
                    progress.dismiss()

                    val intent = Intent(Intent.ACTION_VIEW)

                    val uri = FileProvider.getUriForFile(this,  this.applicationContext.packageName + ".provider",
                            File(path))

                    val file = File(path)
                    val fis = FileInputStream(file)
                val bos = ByteArrayOutputStream()
                val buf = ByteArray(1024)
                val printFormat = ByteArray(3)
                printFormat[0] = 27
                printFormat[1] = 33
                printFormat[2] = 0

                try {
                    //outStream?.write(printFormat)
                    while (true) {
                        val read = fis.read(buf)
                        if(read == -1) {
                            break
                        }
                        bos?.write(buf, 0, read)
                    }
                } catch (ex:Exception) {
                    Toast.makeText(this@MainActivity, ex.message, Toast.LENGTH_LONG).show()

                }
                val bytes = bos.toByteArray()
                outStream?.write(printFormat)
                outStream?.write(bytes)
                Toast.makeText(this@MainActivity, "printed", Toast.LENGTH_LONG).show()
                /*intent.setDataAndType(uri, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)*/
                //}



            } catch (ex: Exception) {
                ex.printStackTrace()

                runOnUiThread {
                    Toast.makeText(this, "Fail ${ex.message}", Toast.LENGTH_LONG).show()
                    clear()
                    progress.dismiss()
                }
            }

        }.start()*/

        val standName = "POLLACHI MUNICIPALITY\nTWO WHEELER PARKING\n"
        val address = "(Behind Nachimuthu nursing home)\n"
        val tokenNo = "TOKEN: 123\n"
        val vechicleNo = "Vehicle Number: ${editTextVehicleNo.text.toString()}\n"
        val dateTime = "TIME: ${dateTimeView.text.toString()}\n"
        val instructions = ""

        val bitmap = Bitmap.createScaledBitmap(qrCode, (qrCode.width).toInt(), (qrCode.height).toInt(), true)
        //bitmap.compress(Bitmap.CompressFormat.PNG, 100, qrCodeStream)

        val header = "$standName$address"

        val ticketString = "$tokenNo$vechicleNo$dateTime"

        outStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
        outStream?.write(header.toByteArray())
        outStream?.write(Utils.decodeBitmap(qrCode))
        outStream?.write("\n".toByteArray())
        outStream?.write(PrinterCommands.ESC_ALIGN_LEFT)
        outStream?.write(ticketString.toByteArray())
        outStream?.write(("\n\n\n" +
                "").toByteArray())
        clear()
        progress.dismiss()
    }

    fun closeTicket(v: View) {
        startActivity(Intent(this, CloseTicketActivity::class.java))
    }

    fun clear() {
        editTextVehicleNo.setText("", TextView.BufferType.EDITABLE)

    }

    public val bluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    fun enableBluetoothAndPrinterSetup() {
        try {
            if(!bluetoothAdapter.isEnabled) {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH)
            } else {
                connectPrinter()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    fun connectPrinter() {
        val pairedDevices = bluetoothAdapter.bondedDevices
        for(d in pairedDevices) {
            if(d.name.equals("BlueTooth Printer")) {
                device = d
                Toast.makeText(this, "Printer found", Toast.LENGTH_LONG).show()
                break
            }
        }

        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        socket = device?.createRfcommSocketToServiceRecord(uuid)
        socket?.connect()
        outStream = socket?.outputStream
        inStream = socket?.inputStream
        Toast.makeText(this, "Stream opened", Toast.LENGTH_LONG).show()
        beginListenForData()
        try {
            val msg = "Adhithyan Vijayakumar\n\n\n\n"
            outStream?.write(msg.toByteArray())
            Toast.makeText(this, "printed", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }

    }

    fun print() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ENABLE_BLUETOOTH -> {
                if(resultCode == Activity.RESULT_OK) { connectPrinter()
                } else {
                    Toast.makeText(this, "Try again", Toast.LENGTH_LONG).show()
                }
            }   else -> {
            Toast.makeText(this, "Turn on bluetooth to continue", Toast.LENGTH_LONG).show()
            }

        }
    }

    internal fun beginListenForData() {
        try {
            val handler = Handler()

            // this is the ASCII code for a newline character
            val delimiter: Byte = 10

            stopWorker = false
            readBufferPosition = 0
            readBuffer = ByteArray(1024)

            workerThread = Thread(Runnable {
                while (!Thread.currentThread().isInterrupted && !stopWorker) {

                    try {

                        val bytesAvailable = inStream!!.available()

                        if (bytesAvailable > 0) {

                            val packetBytes = ByteArray(bytesAvailable)
                            inStream!!.read(packetBytes)

                            for (i in 0 until bytesAvailable) {

                                val b = packetBytes[i]
                                if (b == delimiter) {

                                    val encodedBytes = ByteArray(readBufferPosition)
                                    System.arraycopy(
                                            readBuffer, 0,
                                            encodedBytes, 0,
                                            encodedBytes.size
                                    )

                                    // specify US-ASCII encoding
                                    val data = String(encodedBytes)
                                    readBufferPosition = 0

                                    // tell the user data were sent to bluetooth printer device
                                    handler.post { Log.d("e", data) }

                                } else {
                                    readBuffer!![readBufferPosition++] = b
                                }
                            }
                        }

                    } catch (ex: IOException) {
                        stopWorker = true
                    }

                }
            })

            workerThread?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
