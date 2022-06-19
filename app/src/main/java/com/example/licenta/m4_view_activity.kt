package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class m4_view_activity : AppCompatActivity() {

    var m4_global: M4? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_m4_view)

        val received_device =
            intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val m4 = M4(received_device)
        m4_global = m4
        m4.authenticate()

    }

    fun updateLoop() {
        Thread {
            while (true) {
                runOnUiThread {

                    findViewById<TextView>(R.id.text_steps).text =
                        m4_global?.steps.toString() + " Steps"
                    findViewById<TextView>(R.id.text_calories).text =
                        m4_global?.calories.toString() + " kCal"
                    findViewById<TextView>(R.id.text_distance).text =
                        m4_global?.distance.toString() + " km"

                    findViewById<TextView>(R.id.text_heart_rate).text =
                        m4_global?.bpm.toString() + " BPM"
                    findViewById<TextView>(R.id.text_saturation).text =
                        m4_global?.saturation.toString() + "% SpO2"
                    findViewById<TextView>(R.id.text_blood).text =
                        m4_global?.pressure2.toString() + "/" + m4_global?.pressure1.toString() + " mmHg"

                }

                Thread.sleep(125)
            }

        }.start()
    }

    override fun onResume() {
        super.onResume()

        updateLoop()
    }

    fun getSteps(view: View) {
        m4_global?.getSteps()
//        findViewById<TextView>(R.id.text_steps).text = m4_global?.steps.toString() + " Steps"
//        findViewById<TextView>(R.id.text_calories).text = m4_global?.calories.toString() + " kCal"
//        findViewById<TextView>(R.id.text_distance).text = m4_global?.distance.toString() + " km"
    }

    fun getBlood(view: View) {
        m4_global?.getBlood()
//        findViewById<TextView>(R.id.text_blood).text = m4_global?.pressure2.toString() + "/" + m4_global?.pressure1.toString()+" mmHg"
    }

    fun getSaturation(view: View) {
        m4_global?.getSaturation()
//        findViewById<TextView>(R.id.text_saturation).text = m4_global?.saturation.toString() + "% SpO2"
    }

    fun getHeart(view: View) {
        m4_global?.getHeart()
//        findViewById<TextView>(R.id.text_heart_rate).text = m4_global?.bpm.toString() + " BPM"
    }


    fun goToAlert(view: View) {
        intent = Intent(
            this,
            alerts_view_activity::class.java
        )//nu inteleg exact ce face scope res operatorul aici dar whatever
        startActivity(intent)
    }


}