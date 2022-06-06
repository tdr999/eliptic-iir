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


    fun updateLoop() {
        Thread{
            while(true){
                runOnUiThread{

                    findViewById<TextView>(R.id.text_steps).text = miband_global?.steps.toString() + " Steps"
                    findViewById<TextView>(R.id.text_calories).text = miband_global?.calories.toString() + " kCal"
                    findViewById<TextView>(R.id.text_distance).text = miband_global?.distance.toString() + " m"
                    findViewById<TextView>(R.id.text_heart_rate).text = miband_global?.heart_rate.toString() + " BPM"
                }

                Thread.sleep(125)
            }



        }.start()
    }



    override fun onResume() {

        super.onResume()
        val received_device = intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val miband = MiBand(received_device)
        miband.connect() //asta dureaza cam 2 secunde
        miband_global = miband
        updateLoop()

    }

    fun getSteps(view: View){ //functie apelata initial ca sa citeasca cei mai recenti pasi de la bratara
        miband_global?.getSteps()
    }

    fun getBattery(view: View){
        miband_global?.getBattery()
        Handler(Looper.getMainLooper()).postDelayed({
            var baterie = miband_global?.baterie
            findViewById<TextView>(R.id.text_baterie).text = baterie.toString() + "%"
        }, 2100)
    }

    fun getHeart(view : View){
        miband_global?.subscribeHeartRate()
    }

}


