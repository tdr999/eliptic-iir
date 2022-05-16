package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast

class miband_view_activity : AppCompatActivity() {

    var miband_global : MiBand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_miband_view)
    }


    override fun onResume() {

        super.onResume()
        val received_device = intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val miband = MiBand(received_device)
        miband.connect() //asta dureaza cam 2 secunde
        miband_global = miband


//        Handler(Looper.getMainLooper()).postDelayed({
//
//            getBattery(findViewById(R.id.text_baterie)) //astea trebuie rulate in paralel, aceste comenzi
//
//        }, 8000)
//
//        Handler(Looper.getMainLooper()).postDelayed({
//
//            getSteps(findViewById(R.id.text_steps)) //ca sa ruleze in paralel comenzile
//        }, 7900)
//
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            miband.sendShortVibration()
//        }, 7800)


    }

    fun getSteps(view: View){ //functie apelata initial ca sa citeasca cei mai recenti pasi de la bratara
        miband_global?.getSteps()
        Handler(Looper.getMainLooper()).postDelayed({ //trebuie 2.1 secunde intarziere ptr ca dureaza 2 secunde sa getSteps
            var steps = miband_global?.steps
            findViewById<TextView>(R.id.text_steps).text = steps.toString()
        }, 2100)
    }

    fun getBattery(view: View){
        miband_global?.getBattery()
        Handler(Looper.getMainLooper()).postDelayed({
            var baterie = miband_global?.baterie
            findViewById<TextView>(R.id.text_baterie).text = baterie.toString() + "%"
        }, 2100)
    }

}


