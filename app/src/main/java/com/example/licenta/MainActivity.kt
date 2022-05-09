package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity


//the punch through ultimate guide to bluetooth was immensely helpful


//end of scan result

class MainActivity : AppCompatActivity() {


    var globalDevice : BluetoothDevice? = null




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200 )


    }




    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()

        //incepe scanning activity
        intent = Intent(this, scanning_view_activity::class.java)//nu inteleg exact ce face scope res operatorul aici dar whatever
        startActivity(intent)


    }

    override fun onDestroy() {
        super.onDestroy() //adaugat disconnect
    }

}

