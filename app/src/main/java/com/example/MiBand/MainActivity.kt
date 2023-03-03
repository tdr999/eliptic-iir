package com.example.MiBand

import MiBand.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.util.*

//the punch through ultimate guide to bluetooth was immensely helpful

//end of scan result

lateinit var anim: AnimationDrawable
lateinit var animatie: ImageView

class MainActivity : AppCompatActivity() {

    var globalDevice: BluetoothDevice? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)

        ////

        val intent = intent
        // AICI SA TE UITI ANDREI
        current_user.username = intent.getStringExtra("username")
        current_user.device_mac = intent.getStringExtra("mac")
        globalIsKnownDevice.isKnown = intent.getStringExtra("previousConnected").toBoolean()
        Log.i("primit prevConn", "${globalIsKnownDevice.isKnown}")
//        current_user.device_mac = "E5:4C:5D:74:BE:7B"
//        current_user.username = "tudor_en"
        var lang = current_user.username
        lang = lang!!.split("_")[1]

        val config = resources.configuration //ty stack overflow
        val locale = Locale(lang)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            config.setLocale(locale)
        else
            config.locale = locale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        ///

        setContentView(R.layout.activity_main)
        window.decorView.setBackgroundColor(Color.WHITE)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)

        globalContext.setGlobalContext(this.applicationContext)
        // initializam tot ce avem nevoie pentru alerte
        findViewById<TextView>(R.id.usernameID).text = current_user.username


        animatie = findViewById<ImageView>(R.id.anim).apply {
            setBackgroundResource(R.drawable.minge)
            anim = background as AnimationDrawable
        }
        animatie.visibility = View.INVISIBLE //facem invis

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

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()  //scan settings, which are necessaryva

    val scanCallBack = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {

            Log.i("mac ", "${result.device.address}")
            if (result.device.address == current_user.device_mac) {
                stopBleScan() //adauga cod care verifica daca a mai fost conectat
                flagMondialTimeout.neamConectat = 1 //modificam flagul mondial ptr timeut

                miband_global = MiBand(result.device) //mutat conectarea naine de launch

                intent = Intent(
                    globalContext.context, //mizerie de context
                    miband_view_activity::class.java
                ) //nu inteleg exact ce face scope res operatorul aici dar whatever

                miband_global?.intent = intent


                miband_global?.connect()

                //acum aratam ca connecting

                findViewById<TextView>(R.id.loginId).text = getString(R.string.connecting)
                findViewById<TextView>(R.id.loginId).setTextColor(Color.rgb(0, 153, 51))
                findViewById<TextView>(R.id.usernameID).setTextColor(Color.rgb(0, 153, 51))
                //anim e animatia iar animatie e imageviewul corespunzator
                animatie.visibility = View.VISIBLE //vizibila
                anim.start()

                //timeout la conexiune in caz de eroarea 133
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (flagMondialTimeout.neamConectat == 0) {
                            stopBleScan() //oprim scanarea daca in 10 sec nu am gasit nimic

                            findViewById<TextView>(R.id.loginId).text = getString(R.string.try_again)
                            findViewById<TextView>(R.id.loginId).setTextColor(Color.RED)
                            findViewById<TextView>(R.id.usernameID).text = getString(R.string.device_not_found)
                            findViewById<TextView>(R.id.usernameID).setTextColor(Color.RED)
                            //                    Toast.makeText(this, "Failed to find device. Try Again!", Toast.LENGTH_LONG).show() //anuntam useru
                            Handler(Looper.getMainLooper()).postDelayed({

                                finishAffinity() //inchidem app dupa ce vede useru mesaju destul
                                System.exit(0)
                            }, 4000)
                        }

                    }, 20000
                )

            }
        }
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallBack)
    }

    fun startBleScan() {
        findViewById<TextView>(R.id.loginId).text = getString(R.string.scanning)
        findViewById<TextView>(R.id.usernameID).text = current_user.device_mac.toString()

        bleScanner.startScan(null, settings, scanCallBack)
        //facem mare inginerie pentru timeout
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (flagMondialTimeout.neamConectat == 0) {
                    stopBleScan() //oprim scanarea daca in 10 sec nu am gasit nimic

                    findViewById<TextView>(R.id.loginId).text = getString(R.string.try_again)
                    findViewById<TextView>(R.id.loginId).setTextColor(Color.RED)
                    findViewById<TextView>(R.id.usernameID).text = getString(R.string.device_not_found)
                    findViewById<TextView>(R.id.usernameID).setTextColor(Color.RED)
                    //                    Toast.makeText(this, "Failed to find device. Try Again!", Toast.LENGTH_LONG).show() //anuntam useru
                    Handler(Looper.getMainLooper()).postDelayed({

                        finishAffinity() //inchidem app dupa ce vede useru mesaju destul
                        System.exit(0)
                    }, 4000)
                }

            }, 20000
        )
        //call the bleScanner startScan function
    }

    private fun promptEnableBluetooth() { //prompt for enabling bluetooth
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

    }

//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun promptEnableLocation() {
//
//        val locManager: LocationManager =
//            getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if (!locManager.isLocationEnabled) {
//            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(enableLocationIntent)
//        }
//    }
//

    private fun promptEnableLocation() {
        val locManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }
    }
}
