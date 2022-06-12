package com.example.licenta

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.w3c.dom.Text


//the punch through ultimate guide to bluetooth was immensely helpful


//end of scan result

class new_user_activity : AppCompatActivity() {


    var global_db : database? = null



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_user_activity)


    }




    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()

        global_db = database(this, "Date.db", null, 1)


    }


    override fun onDestroy() {
        super.onDestroy() //adaugat disconnect
    }


    fun saveUser(view : View){
        var pas1 = findViewById<EditText>(R.id.editTextTextPassword).text.toString()
        var pas2 = findViewById<EditText>(R.id.editTextTextPassword2).text.toString()
        var username = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()

        if (pas1 == pas2){
            if (global_db?.checkIfUserExists(username, pas1)==true){
                Toast.makeText(this, "User already exists", Toast.LENGTH_LONG).show()
            }else{
                global_db?.insertUser(username, pas1)
                Handler(Looper.getMainLooper()).postDelayed({
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                },750)
            }
        }else{
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show()
        }

    }



}

