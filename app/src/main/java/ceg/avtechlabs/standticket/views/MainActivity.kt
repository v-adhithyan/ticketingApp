package ceg.avtechlabs.standticket.views

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.models.DbHelper
import ceg.avtechlabs.standticket.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
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

    var readBuffer: ByteArray? = null
    var readBufferPosition = 0
    var stopWorker = false
    val value = ""
    val ALL_PERMISSIONS = 10000
    val PERMISSIONS = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
    var printerConnected = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOrAskPermissions()
    }

    fun reset() {
        editTextVehicleNo.setText("", TextView.BufferType.EDITABLE)
        val millis = System.currentTimeMillis()
        qrView.setImageBitmap(generateQr(millis))
        dateTimeView.text = getDateTime(millis, utc = false)
    }

    fun generateTicket(v: View) {
        if(editTextVehicleNo.text.toString().length < 4) {
            showLongToast(getString(R.string.toast_vehicle_4_chars))
        } else {
            val millis = System.currentTimeMillis()
            val qrCode = generateQr(millis)
            generateTicket(millis, qrCode!!)
        }
    }

    private fun generateTicket(millis: Long, qrCode: Bitmap) {
        val progress = createProgressDialog(getString(R.string.alert_title_printing))
        progress.show()

        doAsync {
            val standName = "POLLACHI MUNICIPALITY\nTWO WHEELER PARKING\n"
            val address = "(Behind Nachimuthu nursing home)\n"
            val tokenNo = "TOKEN: $millis\n"
            val vechicleNo = "Vehicle Number: ${editTextVehicleNo.text.toString()}\n"
            val dateTime = "TIME: ${getDateTime(millis, utc = false)}\n"
            val header = "$standName$address"
            val ticketString = "$tokenNo$vechicleNo$dateTime"

            checkStream(progress)
            if(writeToStream(progress, header, qrCode, ticketString)) {
                val thread = Thread {
                    val DbHelper = DbHelper(this@MainActivity)
                    DbHelper.add(millis.toString(), editTextVehicleNo.text.toString(), getDateTime(millis, utc = false))
                }
                thread.start()
            }
        }
    }

    fun closeTicket(v: View) {
        startActivity(Intent(this, CloseTicketActivity::class.java))
    }

    fun clear() {
        editTextVehicleNo.setText("", TextView.BufferType.EDITABLE)
    }

    val bluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    fun enableBluetoothAndPrinterSetup() {
        try {
            if(!bluetoothAdapter.isEnabled) { // Bluetooth not enabled, connect bluetooth
                connectBluetooth()
            } else { // bluetooth is already turned on, connect bluetooth printer
                connectPrinter()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            printerConnected = false
            showLongToast(getString(R.string.printer_turned_off))
        }
    }

    fun connectPrinter() {
        val progress = createProgressDialog(getString(R.string.connecting_with_printer))
        progress.show()

        doAsync {
            device = getBluetoothPrinter()
            if(device == null) {
                showLongToast(getString(R.string.toast_printer_not_in_paired_devices))
            } else {
                // if device is not null, we found the printer
                // establish socket connection with printer and notify background thread
                checkSocket(progress)
                connectSocket(progress)
                establishDataStreams(progress)
            }
            if (progress.isShowing) { progress.dismiss() }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ENABLE_BLUETOOTH -> {
                if(resultCode == Activity.RESULT_OK) { connectPrinter()
                } else {
                    showLongToast(getString(R.string.try_again))
                }
            }   else -> {
                showLongToast(getString(R.string.turn_on_bluetooth))
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

            AsyncTask.execute(Runnable {
                while (!stopWorker) {
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

        } catch (e: Exception) {
            printerConnected = false
            e.printStackTrace()
        }

    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) { }

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
                showLongToast("${db.summaryEmployee()} tokens issued")

            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
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
                    showLongToast(getString(R.string.enable_permissions_manually))
                    openSettings()
                }
            } else -> { }
        }
    }

    private fun startShift() {
        if(!isShiftOpen()) {
            buttonGenTicket.visibility = View.INVISIBLE
            showAlertDialog(getString(R.string.alert_shift_not_opened),
                    getString(R.string.admin_open_shift),
                    getString(R.string.yes),
                    getString(R.string.no),
                    ::openShiftActivity,
                    ::dismiss
            )
        } else {
            buttonGenTicket.visibility = View.VISIBLE
            enableBluetoothAndPrinterSetup()
        }
        reset()
    }

    fun printerConnect(v: View) {
        if(!printerConnected) {
            enableBluetoothAndPrinterSetup()
            return
        }

        showLongToast(getString(R.string.printer_already_connected))
    }

    private fun openShiftActivity() {
        val intent = Intent(this@MainActivity, PinActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun connectBluetooth() {
        val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH)
    }

    private fun getBluetoothPrinter(): BluetoothDevice? {
        val pairedDevices = bluetoothAdapter.bondedDevices // get all paired devices
        for(d in pairedDevices) { // iterate all paired devices
            if(d.name.equals("BlueTooth Printer")) { // We are looking for device named BlueTooth Printer
                runOnUiThread { showLongToast(getString(R.string.toast_printer_found)) }
                return d
            }
        }
        return null
    }

    private fun checkSocket(progress: AlertDialog) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        socket = device?.createRfcommSocketToServiceRecord(uuid) // establish socket connection with device
        if(socket == null) {
            runOnUiThread {
                progress.dismiss()
                showLongToast(getString(R.string.printer_turned_off))
                return@runOnUiThread
            }
        }
    }

    private fun connectSocket(progress: AlertDialog) {
        try {
            socket?.connect() // try connecting with device
        } catch (ex: Exception) {
            runOnUiThread {
                progress.dismiss()
                showLongToast(getString(R.string.printer_turned_off))
                printerConnected = false
                return@runOnUiThread
            }
        }
    }

    private fun establishDataStreams(progress: AlertDialog) {
        outStream = socket?.outputStream
        inStream = socket?.inputStream
        beginListenForData()
        try {
            val msg = "READY."
            outStream?.write(msg.toByteArray()) // on successful connection, print sample message
            runOnUiThread { showLongToast(getString(R.string.printer_connected)) }
            printerConnected = true
        } catch (ex: Exception) {
            ex.printStackTrace()
            runOnUiThread {
                showLongToast(getString(R.string.unable_to_connect_printer))
                progress.dismiss()
                printerConnected = false
            }
        }
    }

    private fun checkStream(progress: AlertDialog) {
        if (outStream == null) {
            runOnUiThread {
                progress.dismiss()
                toast(getString(R.string.printer_turned_off))
            }
            return;
        }
    }

    private fun writeToStream(progress: AlertDialog, header:String, qrCode: Bitmap, ticketString: String): Boolean {
        try {
            outStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            outStream?.write(header.toByteArray())
            outStream?.write(Utils.decodeBitmap(qrCode))
            outStream?.write("\n".toByteArray())
            outStream?.write(PrinterCommands.ESC_ALIGN_LEFT)
            outStream?.write(ticketString.toByteArray())
            outStream?.write(("\nBilling software powered by\n").toByteArray())
            outStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            outStream?.write("AV Tech Labs\n".toByteArray())
            outStream?.write(PrinterCommands.ESC_ALIGN_LEFT)
            outStream?.write("Contact avtechlabs@gmail.com for sales.\n\n\n".toByteArray())
            runOnUiThread {
                progress.dismiss()
                clear()
            }
            return true
        } catch(ex: IOException) {
            runOnUiThread {
                toast(getString(R.string.printer_turned_off))
                printerConnected = false;
                progress.dismiss()
            }
            return false
        }
    }
}
