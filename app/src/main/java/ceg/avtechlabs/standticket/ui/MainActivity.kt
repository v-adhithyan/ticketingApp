package ceg.avtechlabs.standticket.ui

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.PrinterCommands
import ceg.avtechlabs.standticket.utils.Utils
import ceg.avtechlabs.standticket.utils.generateQr
import ceg.avtechlabs.standticket.utils.getDateTime
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
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

        reset()
        enableBluetoothAndPrinterSetup()
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

        val millis = System.currentTimeMillis()
        val qrCode = generateQr(millis)
        generatePdf(millis, qrCode!!)
    }

    fun generatePdf(millis: Long, qrCode: Bitmap) {
        val progress = ProgressDialog(this)
        progress.setTitle("Printing")
        progress.setMessage("Please wait..")
        progress.isIndeterminate = true
        progress.setCancelable(false)
        progress.show()

        val thread = Thread {
            val DbHelper = DbHelper(this@MainActivity)
            DbHelper.add(millis.toString(), editTextVehicleNo.text.toString(), getDateTime(millis))
        }
        thread.start()

        val standName = "POLLACHI MUNICIPALITY\nTWO WHEELER PARKING\n"
        val address = "(Behind Nachimuthu nursing home)\n"
        val tokenNo = "TOKEN: $millis\n"
        val vechicleNo = "Vehicle Number: ${editTextVehicleNo.text.toString()}\n"
        val dateTime = "TIME: ${dateTimeView.text.toString()}\n"
        val instructions = ""

        val bitmap = Bitmap.createScaledBitmap(qrCode, (qrCode.width).toInt(), (qrCode.height).toInt(), true)
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

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_close_shift -> {
                val intent = Intent(this, ShiftActivity::class.java)
                intent.putExtra(START, CLOSE)
                startActivity(intent)
            }

            R.id.menu_search -> {
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra(START, SEARCH)
                startActivity(intent)
            }

            R.id.menu_summary -> {
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra(START, SUMMARY)
                startActivity(intent)
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    companion object {
        val START = "start"
        val SEARCH = "SEARCH"
        val CLOSE = "close"
        val SUMMARY = "summary"
    }
}
