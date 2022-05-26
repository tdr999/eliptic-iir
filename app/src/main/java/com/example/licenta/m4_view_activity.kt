package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class m4_view_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_m4_view)
    }




    override fun onResume() {
        super.onResume()
        val received_device = intent.extras.getParcelable<BluetoothDevice>("bt_device") //primeste device
        val m4 = M4(received_device)
        m4.authenticate()

    }
}