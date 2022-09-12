package com.example.MiBand

import MiBand.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.CheckedTextView
import android.widget.TextView

//the punch through ultimate guide to bluetooth was immensely helpful

//end of scan result

class MainActivity : AppCompatActivity() {

    var globalDevice: BluetoothDevice? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)

//        gloabal_database.insertUser("mihai", "parola_de_test") // test
        globalContext.setGlobalContext(this.applicationContext)
        // initializam tot ce avem nevoie pentru alerte

        //temp hardcoded user


        val intent = intent
        // AICI SA TE UITI ANDREI
        current_user.username = intent.getStringExtra("username")
        current_user.device_mac = intent.getStringExtra("mac")
        globalIsKnownDevice.isKnown = intent.getStringExtra("previousConnected").toBoolean()
//        current_user.username = "tudor"
//        current_user.device_mac = "FC:71:A2:68:2D:CB"

        findViewById<CheckedTextView>(R.id.usernameID).text = current_user.username
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()

        promptEnableBluetooth()
        promptEnableLocation()


        //for debug
        Handler(Looper.getMainLooper()).postDelayed({
            startBleScan()
        }, 2000)
    }

    override fun onStop() {
        super.onStop()
        stopBleScan()
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy { //defining bt adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy { //defining BLE scanner
        bluetoothAdapter.bluetoothLeScanner
    }

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()  //scan settings, which are necessaryva

    val scanCallBack = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {

            if (result.device.address == current_user.device_mac) {
                stopBleScan() //adauga cod care verifica daca a mai fost conectat
                intent = Intent(
                    globalContext.context, //mizerie de context
                    miband_view_activity::class.java
                )//nu inteleg exact ce face scope res operatorul aici dar whatever
                intent.putExtra("bt_device", result.device)
                startActivity(intent)
            }
        }
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallBack)
    }

    fun startBleScan() {
        findViewById<TextView>(R.id.loginId).text = "Scanning for..."
        findViewById<CheckedTextView>(R.id.usernameID).text = current_user.device_mac
        bleScanner.startScan(null, settings, scanCallBack)
        //call the bleScanner startScan function
    }

    private fun promptEnableBluetooth() { //prompt for enabling bluetooth
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableLocation() {

        val locManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locManager.isLocationEnabled) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }
    }

}

