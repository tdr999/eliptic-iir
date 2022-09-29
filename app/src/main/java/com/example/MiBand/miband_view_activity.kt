package com.example.MiBand

import MiBand.R
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView

class miband_view_activity : AppCompatActivity() {

    var miband_global: MiBand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val received_device =
            intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val miband = MiBand(received_device)
        miband_global = miband
        miband.connect() //conectam

        if (globalIsKnownDevice.isKnown == false) {
            Handler(Looper.getMainLooper()).postDelayed({
                setContentView(R.layout.activity_miband_view)
            }, 15000) //asteptam dupa caz pana sa incarcam uiul
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                setContentView(R.layout.activity_miband_view)
            }, 8000)
        }

    }

    fun updateLoop() {
        Thread {
            while (true) {
                runOnUiThread {

                    findViewById<TextView>(R.id.text_steps).text =
                        miband_global?.steps.toString() + " Steps"
//                    findViewById<TextView>(R.id.text_calories).text =
//                        miband_global?.calories.toString() + " kCal"
                    findViewById<TextView>(R.id.text_distance).text =
                        miband_global?.distance.toString() + " km"
//                    findViewById<TextView>(R.id.text_heart_rate).text =
//                        miband_global?.heart_rate.toString() + " BPM"
                }

                Thread.sleep(125)
            }

        }.start()
    }

    override fun onResume() {

        super.onResume()


        if (globalIsKnownDevice.isKnown == false) {
            Handler(Looper.getMainLooper()).postDelayed({

                updateLoop()
            }, 15250)
            Handler(Looper.getMainLooper()).postDelayed({

                getBattery() //get data
            }, 15500)
            Handler(Looper.getMainLooper()).postDelayed({

                getSteps()
            }, 16000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({

                updateLoop()
            }, 8250)
            Handler(Looper.getMainLooper()).postDelayed({

                getBattery() //get data
            }, 8500)
            Handler(Looper.getMainLooper()).postDelayed({

                getSteps()
            }, 9000)
        }

    }

    fun getSteps() { //functie apelata initial ca sa citeasca cei mai recenti pasi de la bratara
        miband_global?.getSteps()
    }

    fun getBattery() {
        miband_global?.getBattery()
        Handler(Looper.getMainLooper()).postDelayed({
            var baterie = miband_global?.baterie
            findViewById<TextView>(R.id.text_baterie).text = baterie.toString() + "%"
        }, 2100)
    }

    fun getHeart() {
        miband_global?.subscribeHeartRate()
    }

    override fun onStop() {
        super.onStop()
        miband_global?.gatt?.disconnect() //sa fie asta ce am nevoie ptr deconectare?
    }


    //on back pressed si butonu de back fac acelasi lucru
    fun closeApp(view: View) {
        this.finishAffinity() //excelent aceasta functie
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAffinity() //excelent aceasta functie
    }


}


