package com.example.MiBand

import MiBand.R
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.system.exitProcess

class miband_view_activity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_miband_view)




    }
    fun updateLoop() {
        Thread {
            while (true) {
                runOnUiThread {
                    findViewById<TextView>(R.id.text_steps).text =
                        miband_global?.steps.toString()+" " + getString(R.string.steps_miband)
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



        Thread{ //salveaza la fiecaerr 5 s
            while (true) {
                miband_global?.saveMeasurements()
                Thread.sleep(5000)
            }
        }.start()
    }

    override fun onResume() {

        super.onResume()


        Handler(Looper.getMainLooper()).postDelayed({

            updateLoop()
        }, 1250)
        Handler(Looper.getMainLooper()).postDelayed({

            getBattery() //get data
        }, 1500)
        Handler(Looper.getMainLooper()).postDelayed({

            getSteps()
        }, 2000)

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

    //INNEBUNESC, mori
    override fun onStop() {
        super.onStop()

        miband_global?.gatt?.disconnect() //sa fie asta ce am nevoie ptr deconectare?
        finishAffinity()
        System.exit(0)

    }



    //on back pressed si butonu de back fac acelasi lucru
    fun closeApp(view: View) {
        this.finishAffinity() //excelent aceasta functie
        System.exit(0)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        this.finishAffinity() //excelent aceasta functie
        System.exit(0)
    }

    override fun onPause() {
        super.onPause()

        this.finishAffinity() //excelent aceasta functie
        System.exit(0)
    }

    override fun onDestroy() {
        super.onDestroy()

        this.finishAffinity() //excelent aceasta functie
        System.exit(0)
    }

}


