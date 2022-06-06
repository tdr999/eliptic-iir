package com.example.licenta

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


data class user_device(var dev_id: Int, var user_Id: Int, var dev_type: String, var mac: String){
    var device_id = dev_id
    var user_id = user_Id
    var device_type = dev_type
    var mac_number = mac
}

data class user(var user_Id:Int, var user_name:String, var user_password:String){
   var user_id = user_Id
   var user_Name = user_name
   var user_pass = user_password
}

data class alert(var alert_id:Int, var user_Id: Int, var time : String, var descriere : String){
    var alert_ID = alert_id
    var user_id = user_Id
    var timp = time
    var description = descriere
}


data class measurement(var meas_id:Int, var user_Id: Int, var bpm : Int, var Spo2:Int, var press : String, var pasi : Int, var distance: Float, var cal : Float, var dev_id: Int){
    var measurement_id = meas_id
    var user_id = user_Id
    var BPM = bpm
    var SPO2 = Spo2
    var blood_pressure = press
    var steps = pasi
    var distanta = distance
    var calories = cal
    var device_id = dev_id
}


class database(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {






    override fun onCreate(db: SQLiteDatabase?) {
        Log.i("suntem in on create", "executam creearea de taele")

        db?.execSQL("CREATE TABLE IF NOT EXISTS measurements ( " +
                "measurement_id INT PRIMARY KEY,  " +
                "user_id INT,  " +
                "bpm INT DEFAULT 0,  " +
                "spo2 INT DEFAULT 0,  " +
                "press TEXT DEFAULT \"nimic\",  " +
                "pasi INT DEFAULT 0, " +
                "distance FLOAT DEFAULT 0, " +
                "calories FLOAT DEFAULT 0, " +
                "device_id INT,  " +
                "time_of_measurement DATETIME,  " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id),  " +
                "FOREIGN KEY (device_id) REFERENCES devices(device_id) " +
                "); ")
        db?.execSQL(
                "CREATE TABLE IF NOT EXISTS users( " +
                "user_id INTEGER PRIMARY KEY,  " +
                "user_name TEXT UNIQUE, " +
                "user_pass TEXT " +
                "); ")
        db?.execSQL(
                "CREATE TABLE IF NOT EXISTS devices( " +
                "device_id INTEGER PRIMARY KEY,  " +
                "device_type TEXT,  " +
                "user_id INT,  " +
                "mac TEXT UNIQUE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) " +
                "); " )
        db?.execSQL(
                "CREATE TABLE IF NOT EXISTS alerts( " +
                "alert_id INTEGER PRIMARY KEY,  " +
                "timp TEXT, " +
                "descriere TEXT, " +
                "user_id INT, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) " +
                "); ")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        db?.execSQL("DROP TABLE IF EXISTS measurements;" +
               "DROP TABLE IF EXISTS users;" +
               "DROP TABLE IF EXISTS devices;" +
               "DROP TABLE IF EXISTS alerts;")

    }

    fun insertDevice(device_type : String, user_id : Int, mac_address : String){
        var db = this.writableDatabase
        db?.execSQL("INSERT INTO devices(device_type, user_id, mac) VALUES(" +
                device_type + "," + user_id.toString() + ","+ mac_address + ")")
    }


    fun insertMeasurement(user_Id: Int, bpm: Int, spo2 : Int,
                          press: String, pasi: Int, distance: Float,
                          calories: Float, dev_id: Int, time_of_measurement : String){

        var db = this.writableDatabase
        db?.execSQL("INSERT INTO measurements(user_id, bpm, spo2, press, pasi, " +
                "distance, calories, device_id, time_of_measurement) VALUES(" +
                user_Id.toString() + ", " + bpm.toString() +", " + spo2.toString()
                + ", " + press + ", " + pasi.toString() + ", " + distance.toString() + ", " +
                calories.toString() + ", " + dev_id.toString() + ", " + time_of_measurement + ")")

    }
    fun insertUser(user_name: String, pass : String){
        var db = this.writableDatabase
        db?.execSQL("INSERT INTO users(user_name, user_pass) VALUES(\'" + user_name + "\', \'" + pass + "\')")

    }


    fun insertAlert(){

    }
    fun fetchAlert(){

    }

}