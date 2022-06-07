package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.w3c.dom.Text


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
//        gloabal_database.insertUser("mihai", "parola_de_test") // test
        globalContext.setGlobalContext(this.applicationContext)

    }




    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()



    }


    override fun onDestroy() {
        super.onDestroy() //adaugat disconnect
    }


    fun login(view: View) {

        if (global_db?.checkIfUserExists(
                findViewById<EditText>(R.id.username_in).text.toString(),
                findViewById<EditText>(R.id.pass_in).text.toString()
            ) == true
        ) {
            var temp_id = global_db?.getUserId(findViewById<EditText>(R.id.username_in).text.toString(),
                findViewById<EditText>(R.id.pass_in).text.toString())
            current_user.setUserPass(temp_id,findViewById<EditText>(R.id.username_in).text.toString(),
                findViewById<EditText>(R.id.pass_in).text.toString())

            intent = Intent(
                this,
                scanning_view_activity::class.java
            )//nu inteleg exact ce face scope res operatorul aici dar whatever
            startActivity(intent)
        }
        else    {
            Toast.makeText(this, "User doesn't exist", Toast.LENGTH_LONG).show()
        }
    }

    fun newUser(view: View){
        intent = Intent(
            this,
            new_user_activity::class.java
        )//nu inteleg exact ce face scope res operatorul aici dar whatever
        startActivity(intent)

    }

}

