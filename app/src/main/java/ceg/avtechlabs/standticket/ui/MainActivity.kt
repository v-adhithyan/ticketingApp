package ceg.avtechlabs.standticket.ui

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
    val ALL_PERMISSIONS = 10000
    val PERMISSIONS = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
    var printerConnected = false

    val BT_PRINTER_TURNED_OFF = "Printer is turned off or connection lost with printer. Turn on the printer and tap connect Printer button."
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOrAskPermissions()

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
        if(editTextVehicleNo.text.toString().length < 4) {
            Toast.makeText(this, "Vehicle no should be greater than 4 characters to print ticket.", Toast.LENGTH_LONG).show()
        } else {
            val millis = System.currentTimeMillis()
            val qrCode = generateQr(millis)
            generatePdf(millis, qrCode!!)
        }
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
        val dateTime = "TIME: ${getDateTime(millis)}\n"
        val instructions = ""

        val bitmap = Bitmap.createScaledBitmap(qrCode, (qrCode.width).toInt(), (qrCode.height).toInt(), true)
        val header = "$standName$address"

        val ticketString = "$tokenNo$vechicleNo$dateTime"

        if (outStream == null) {
            progress.dismiss()
            Toast.makeText(this@MainActivity, BT_PRINTER_TURNED_OFF, Toast.LENGTH_LONG).show()
            return;
        }

        try {
            outStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            outStream?.write(header.toByteArray())
            outStream?.write(Utils.decodeBitmap(qrCode))
            outStream?.write("\n".toByteArray())
            outStream?.write(PrinterCommands.ESC_ALIGN_LEFT)
            outStream?.write(ticketString.toByteArray())
            outStream?.write(("\n\n\n" +
                    "").toByteArray())
        } catch(ex: IOException) {
            Toast.makeText(this@MainActivity, BT_PRINTER_TURNED_OFF, Toast.LENGTH_LONG).show()
            printerConnected = false;
        }

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
            printerConnected = false
            //Toast.makeText(this, "Printer connection lost. Quit the app and open again.", Toast.LENGTH_LONG).show()
        }
    }

    fun connectPrinter() {
        val progress = ProgressDialog(this)
        progress.setTitle("Connecting with printer")
        progress.setMessage("Please wait..")
        progress.isIndeterminate = true
        progress.setCancelable(false)
        progress.show()
        val pairedDevices = bluetoothAdapter.bondedDevices
        for(d in pairedDevices) {
            if(d.name.equals("BlueTooth Printer")) {
                device = d
                Toast.makeText(this, "Printer found", Toast.LENGTH_LONG).show()
                break
            }
        }

        if(device == null) {
            Toast.makeText(this@MainActivity, "Unable to find BlueTooth Printer in paired devices. Please pair the printer and try again.", Toast.LENGTH_LONG).show()
        } else {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            socket = device?.createRfcommSocketToServiceRecord(uuid)
            if(socket == null) {
                progress.dismiss()
                Toast.makeText(this@MainActivity, BT_PRINTER_TURNED_OFF, Toast.LENGTH_LONG).show()
                return;
            }
            try {
                socket?.connect()
            } catch (ex: Exception) {
                progress.dismiss()
                Toast.makeText(this@MainActivity, BT_PRINTER_TURNED_OFF, Toast.LENGTH_LONG).show()
                printerConnected = false
                return;
            }
            outStream = socket?.outputStream
            inStream = socket?.inputStream
            Toast.makeText(this, "Stream opened", Toast.LENGTH_LONG).show()
            beginListenForData()
            try {
                val msg = "READY.\n\n\n\n"
                outStream?.write(msg.toByteArray())
                Toast.makeText(this, "Connection established with printer.", Toast.LENGTH_LONG).show()
                printerConnected = true
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(this, "Unable to establish connection. Quit the app and open again to reestablish connection.", Toast.LENGTH_LONG).show()
                progress.dismiss()
                printerConnected = false
            }
        }
        progress.dismiss()
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
                        printerConnected = false
                        stopWorker = true
                    }

                }
            })

            workerThread?.start()

        } catch (e: Exception) {
            printerConnected = false
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
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.menu_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_summary -> {
                val db = DbHelper(this)
                Toast.makeText(this, "${db.summaryEmployee()} tokens issued", Toast.LENGTH_LONG).show()

            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    private val progress by lazy {
        ProgressDialog(this)
    }

    companion object {
        val START = "start"
        val SEARCH = "SEARCH"
        val CLOSE = "close"
        val SUMMARY = "summary"
    }

    fun permissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun checkOrAskPermissions(){
        var permissionNotGranted = false
        for (permission in PERMISSIONS) {
            if (!permissionGranted(permission)) {
                permissionNotGranted = true
                break
            }
        }

        if(permissionNotGranted) {
            ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS, ALL_PERMISSIONS)
        } else {
            startShift()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ALL_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startShift()
                } else {
                    Toast.makeText(this@MainActivity, "Enable permissions manually to run the app.", Toast.LENGTH_LONG).show()
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", this.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            } else -> { }
        }
    }

    private fun startShift() {
        if(!isShiftOpen()) {
            val alert = AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Shift not opened")
                    .setMessage("Admin should open the shift to print tickets.")
                    .setPositiveButton("Ok", null)
                    .create()
            buttonGenTicket.visibility = View.INVISIBLE
            alert.show()
        } else {
            buttonGenTicket.visibility = View.VISIBLE

            enableBluetoothAndPrinterSetup()
        }

        reset()

        progress.setTitle("Please wait")
        progress.setMessage("Connecting with printer")
    }

    fun printerConnect(v: View) {
        if(!printerConnected) {
            enableBluetoothAndPrinterSetup()
            return
        }

        Toast.makeText(this@MainActivity, "Printer already connected.", Toast.LENGTH_LONG).show()
    }
}
