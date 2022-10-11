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
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.annotation.ColorInt
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.CheckedTextView
import android.widget.TextView
import android.widget.Toast

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



        val intent = intent
        // AICI SA TE UITI ANDREI
        current_user.username = intent.getStringExtra("username")
        current_user.device_mac = intent.getStringExtra("mac")
        globalIsKnownDevice.isKnown = intent.getStringExtra("previousConnected").toBoolean()
        Log.i("primit prevConn", "${globalIsKnownDevice.isKnown.toString()}")


        //teste
//        current_user.username = "tudor"
//        current_user.device_mac = "FC:71:A2:68:2D:CB"
//        current_user.device_mac = "DC:D9:40:49:26:EB" //chinezeasca
//        current_user.device_mac = "CC:71:A2:68:2D:CB" //test timeout
//        globalIsKnownDevice.isKnown = true

        findViewById<TextView>(R.id.usernameID).text = current_user.username
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()

        //rezolvam cu iesire

        promptEnableBluetooth()
        promptEnableLocation()
        startBleScan()
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
                flagMondialTimeout.neamConectat = 1 //modificam flagul mondial ptr timeut

                miband_global = MiBand(result.device) //mutat conectarea naine de launch


                intent = Intent(
                    globalContext.context, //mizerie de context
                    miband_view_activity::class.java
                )//nu inteleg exact ce face scope res operatorul aici dar whatever

                miband_global?.intent = intent


                miband_global?.connect()

                //acum aratam ca connecting

                findViewById<TextView>(R.id.loginId).text = "Connecting to..."
                findViewById<TextView>(R.id.loginId).setTextColor(Color.GREEN)
                findViewById<TextView>(R.id.usernameID).setTextColor(Color.GREEN)

                //




//                if (globalIsKnownDevice.isKnown == false) {
//                    Handler(Looper.getMainLooper()).postDelayed({
//
////                        startActivity(intent)
//
//                    }, 15000) //asteptam dupa caz pana sa incarcam uiul
//                } else {
//                    Handler(Looper.getMainLooper()).postDelayed({
//
//                        startActivity(intent)
//                    }, 8000)
//                }


            }
        }
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallBack)
    }

    fun startBleScan() {
        findViewById<TextView>(R.id.loginId).text = "Scanning for..."
        findViewById<TextView>(R.id.usernameID).text = current_user.device_mac
        bleScanner.startScan(null, settings, scanCallBack)
        //facem mare inginerie pentru timeout
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (flagMondialTimeout.neamConectat == 0){
                    stopBleScan() //oprim scanarea daca in 10 sec nu am gasit nimic

                    findViewById<TextView>(R.id.loginId).text = "TRY AGAIN!"
                    findViewById<TextView>(R.id.loginId).setTextColor(Color.RED)
                    findViewById<TextView>(R.id.usernameID).text = "Device not found"
                    findViewById<TextView>(R.id.usernameID).setTextColor(Color.RED)
                    //                    Toast.makeText(this, "Failed to find device. Try Again!", Toast.LENGTH_LONG).show() //anuntam useru
                    Handler(Looper.getMainLooper()).postDelayed({

                        finishAffinity() //inchidem app dupa ce vede useru mesaju destul
                    },4000)
                }

            }, 10000)
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

