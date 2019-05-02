package ceg.avtechlabs.standticket.ui

import android.app.Activity
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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import ceg.avtechlabs.standticket.R
import ceg.avtechlabs.standticket.db.DbHelper
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

    var workerThread: Thread? = null
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
        val qrCode = generateQr(millis)
        qrView.setImageBitmap(qrCode)
        var dateTime = getDateTime(millis)
        dateTimeView.text = dateTime
    }

    fun generateTicket(v: View) {
        if(editTextVehicleNo.text.toString().length < 4) {
            showLongToast(getString(R.string.toast_vehicle_4_chars))
        } else {
            val millis = System.currentTimeMillis()
            val qrCode = generateQr(millis)
            generatePdf(millis, qrCode!!)
        }
    }

    fun generatePdf(millis: Long, qrCode: Bitmap) {
        val progress = createProgressDialog(getString(R.string.alert_title_printing))
        progress.show()

        val thread = Thread {
            val DbHelper = DbHelper(this@MainActivity)
            DbHelper.add(millis.toString(), editTextVehicleNo.text.toString(), getDateTime(millis))
        }
        thread.start()

        doAsync {
            val standName = "POLLACHI MUNICIPALITY\nTWO WHEELER PARKING\n"
            val address = "(Behind Nachimuthu nursing home)\n"
            val tokenNo = "TOKEN: $millis\n"
            val vechicleNo = "Vehicle Number: ${editTextVehicleNo.text.toString()}\n"
            val dateTime = "TIME: ${getDateTime(millis)}\n"
            val header = "$standName$address"
            val ticketString = "$tokenNo$vechicleNo$dateTime"

            if (outStream == null) {
                runOnUiThread {
                    progress.dismiss()
                    toast(getString(R.string.printer_turned_off))
                }
                return@doAsync;
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
                runOnUiThread {
                    toast(getString(R.string.printer_turned_off))
                    printerConnected = false;
                }
            } finally {
                runOnUiThread {
                    clear()
                    progress.dismiss()
                }
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
            if(!bluetoothAdapter.isEnabled) {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH)
            } else {
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
            val pairedDevices = bluetoothAdapter.bondedDevices
            for(d in pairedDevices) {
                if(d.name.equals("BlueTooth Printer")) {
                    device = d
                    runOnUiThread { showLongToast(getString(R.string.toast_printer_found)) }

                    break
                }
            }

            if(device == null) {
                showLongToast(getString(R.string.toast_printer_not_in_paired_devices))
            } else {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                socket = device?.createRfcommSocketToServiceRecord(uuid)
                if(socket == null) {
                    runOnUiThread {
                        progress.dismiss()
                        showLongToast(getString(R.string.printer_turned_off))
                        return@runOnUiThread
                    }
                }
                try {
                    socket?.connect()
                } catch (ex: Exception) {
                    runOnUiThread {
                        progress.dismiss()
                        showLongToast(getString(R.string.printer_turned_off))
                        printerConnected = false
                        return@runOnUiThread
                    }
                }
                outStream = socket?.outputStream
                inStream = socket?.inputStream
                beginListenForData()
                try {
                    val msg = "READY."
                    outStream?.write(msg.toByteArray())
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
}
