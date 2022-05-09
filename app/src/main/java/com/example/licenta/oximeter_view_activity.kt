package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class oximeter_view_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oximeter_view)
    }


    fun makeUpdateLoope(oxi: PulseOximeter) {
        Thread{
                while(true) {
                    runOnUiThread {
                        var a = findViewById<TextView>(R.id.bpm_dinamic)
                        a.text = oxi.BPM.toString()
                        findViewById<TextView>(R.id.spo2_dinamic).setText( oxi.spo2.toString())
                        findViewById<TextView>(R.id.pi_dinamic).setText( oxi.pi.toString())
                        Log.i("din ui thread", "BPM ${oxi.BPM}")

                    }

                    Thread.sleep(100)
                }
        }.start()
//        Thread {
//            while (true) {
////                findViewById<TextView>(R.id.bpm_dinamic).text = oxi.BPM.toString()
////                findViewById<TextView>(R.id.spo2_dinamic).text = oxi.spo2.toString()
////                findViewById<TextView>(R.id.pi_dinamic).text = oxi.pi.toString()
//                Thread.sleep(500)
//            }
//        }.start()
    }


    override fun onResume() {
        super.onResume()
        val received_device = intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val oximetru = PulseOximeter(received_device)
        oximetru.authenticate()
        makeUpdateLoope(oximetru)

    }
}