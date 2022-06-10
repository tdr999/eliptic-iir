package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import org.w3c.dom.Text


//the punch through ultimate guide to bluetooth was immensely helpful


//end of scan result

class newalertactivty : AppCompatActivity() {




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newalertactivitylayout)


        var time = findViewById<TimePicker>(R.id.time_picker)
        time.setIs24HourView(true)

    }




    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()



    }


    override fun onDestroy() {
        super.onDestroy() //adaugat disconnect
    }



    @androidx.annotation.RequiresApi(Build.VERSION_CODES.M)
    fun savealert(view: View){

        var time = findViewById<TimePicker>(R.id.time_picker)
        var timp_db = time.hour.toString()+":"+time.minute.toString()+":"+"00"
        var descriere = findViewById<TextView>(R.id.editTextTextPersonName2).text.toString()
        globalDatabase.db.insertAlert(timp_db, descriere)

        var intent = Intent(this, alerts_view_activity::class.java)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

//        intent = Intent(
//            this,
//            new_user_activity::class.java
//        )//nu inteleg exact ce face scope res operatorul aici dar whatever
//        startActivity(intent)

    }

}

