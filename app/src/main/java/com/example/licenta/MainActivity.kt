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


    var global_db : database? = null
    var globalDevice : BluetoothDevice? = null




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200 )


        var gloabal_database = database(this, "Date.db", null, 1)
        global_db = gloabal_database
        gloabal_database.insertUser("mihai", "parola_de_test") // test

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

