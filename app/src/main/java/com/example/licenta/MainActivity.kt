package com.example.licenta

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log


//the punch through ultimate guide to bluetooth was immensely helpful


//end of scan result

class MainActivity : AppCompatActivity() {

    var globalGattReference : BluetoothGatt? = null
    private val listaRezultate = mutableListOf<ScanResult>()


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200 )

    }


    private val bluetoothAdapter: BluetoothAdapter by lazy{ //defining bt adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    private val bleScanner by lazy { //defining BLE scanner
        bluetoothAdapter.bluetoothLeScanner
    }

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()  //scan settings, which are necessary

    val scanCallBack = object : ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = listaRezultate.indexOfFirst {
                it.device.address == result.device.address
            }

            with(result.device){
                Log.i("ScanCallback", "Found Device! Name: ${name?: "Unnamed"}, Adress: $address")
            }



            if (result.device.address.toString() == "CE:45:BF:69:5A:7A" ){ //miband
                stopBleScan()
                var band = MiBand(result.device)
                band.authenticate()
                Log.i("scan callback", "conectat la mibadn")
            }


//            if (result.device.address.toString() == "2C:AB:33:C3:1A:EF" ){ //pulsoximetru
//                stopBleScan()
//                var oximetru = PulseOximeter(result.device)
//                oximetru.authenticate()
//                //Log.i("scan callback", "conectat la pulsoximetru")
//            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        promptEnableBluetooth()
        promptEnableLocation()
        startBleScan()

    }

    override fun onDestroy() {
        super.onDestroy() //adaugat disconnect
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableBluetooth(){ //prompt for enabling bluetooth
        if(!bluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableLocation(){

        val locManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locManager.isLocationEnabled){
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }

    }


    fun stopBleScan(){
        bleScanner.stopScan(scanCallBack)
    }


    fun startBleScan(){

        bleScanner.startScan(null, settings, scanCallBack)//call the bleScanner startScan function

    }






}

