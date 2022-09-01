package com.example.MiBand

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.json.JSONObject
import java.io.DataOutput
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class user_device(var dev_id: Int, var user_Id: Int, var dev_type: String, var mac: String) {
    var device_id = dev_id
    var user_id = user_Id
    var device_type = dev_type
    var mac_number = mac
}

object current_user {
    var user_id: Int? = null
    var username: String? = null
    var userpass: String? = null
    var current_device_id: Int? = null
    var current_device_mac: String? = null
    var device_type: String? = null
    var mb: MiBand? = null

    fun setUserPass(userid: Int?, user_name: String, user_password: String) {
        username = user_name
        userpass = user_password
        user_id = userid
    }

    fun setDevice(cur_id: Int?, cur_mac: String) {
        current_device_id = cur_id
        current_device_mac = cur_mac
    }

    fun setDeviceType(type: String) {
        device_type = type
    }

    fun setMiband(mib: MiBand) {
        mb = mib
    }

}

data class alert(var alert_id: Int, var user_Id: Int, var time: String, var descriere: String) {
    var alert_ID = alert_id
    var user_id = user_Id
    var timp = time
    var description = descriere
}

data class measurement(
    var meas_id: Int,
    var user_Id: Int,
    var bpm: Int,
    var Spo2: Int,
    var press: String,
    var pasi: Int,
    var distance: Float,
    var cal: Float,
    var dev_id: Int
) {
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

        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS measurements ( " +
                    "measurement_id INTEGER PRIMARY KEY,  " +
                    "user_id INT,  " +
                    "bpm INT DEFAULT 0,  " +
                    "spo2 INT DEFAULT 0,  " +
                    "pen_index INT DEFAULT 0," +
                    "press TEXT DEFAULT \"nimic\",  " +
                    "pasi FLOAT DEFAULT 0, " +
                    "distance FLOAT DEFAULT 0, " +
                    "calories FLOAT DEFAULT 0, " +
                    "device_id INT,  " +
                    "time_of_measurement DATETIME,  " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id),  " +
                    "FOREIGN KEY (device_id) REFERENCES devices(device_id) " +
                    "); "
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS users( " +
                    "user_id INTEGER PRIMARY KEY,  " +
                    "user_name TEXT, " +
                    "user_pass TEXT " +
                    "); "
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS devices( " +
                    "device_id INTEGER PRIMARY KEY,  " +
                    "device_type TEXT,  " +
                    "user_id INT,  " +
                    "mac TEXT UNIQUE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) " +
                    "); "
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS alerts( " +
                    "alert_id INTEGER PRIMARY KEY,  " +
                    "timp TIME, " +
                    "descriere TEXT, " +
                    "user_id INT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) " +
                    "); "
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        db?.execSQL(
            "DROP TABLE IF EXISTS measurements;" +
                    "DROP TABLE IF EXISTS users;" +
                    "DROP TABLE IF EXISTS devices;" +
                    "DROP TABLE IF EXISTS alerts;"
        )

    }

    fun insertDevice(device_type: String, user_id: Int?, mac_address: String) {
        var db = this.writableDatabase
        db?.execSQL(
            "INSERT INTO devices(device_type, user_id, mac) VALUES(\'" +
                    device_type + "\'," + user_id.toString() + ",\'" + mac_address + "\')"
        )
    }

    fun insertMeasurement(
        user_Id: Int?, bpm: Int?, spo2: Int?, pen_index: Int?,
        press: String, pasi: Float?, distance: Float?,
        calories: Float?, dev_id: Int?, time_of_measurement: String
    ) {

        var db = this.writableDatabase
        var sql_string = "INSERT INTO measurements(user_id, bpm, spo2, press, pasi, " +
                "distance, calories, device_id, time_of_measurement) VALUES(" +
                user_Id.toString() + ", " + bpm.toString() + ", " + spo2.toString() +
                ", " + press + ", " + pasi.toString() + ", " + distance.toString() + ", " +
                calories.toString() + ", " + dev_id.toString() + ", " + time_of_measurement + ")"
        Log.i("Valoare sql to be", "${sql_string}")
        var values = ContentValues()
        values.put("user_id", current_user.user_id)
        values.put("bpm", bpm)
        values.put("spo2", spo2)
        values.put("pen_index", pen_index)
        values.put("press", press)
        values.put("pasi", pasi)
        values.put("distance", distance)
        values.put("calories", calories)
        values.put("device_id", current_user.current_device_id)
        values.put("time_of_measurement", time_of_measurement)
        var success = db.insert("measurements", null, values)
        Log.i("rezultat inset", "${success}")

    }

    fun insertUser(user_name: String, pass: String) {
        var db = this.writableDatabase
        if (checkIfUserExists(user_name, pass) == false) {
            db?.execSQL("INSERT INTO users(user_name, user_pass) VALUES(\'" + user_name + "\', \'" + pass + "\')")
        }
    }

    fun removeAlert(alert_id: Int?) {
        var db = this.writableDatabase
        db.delete("alerts", "alert_id=?", arrayOf(alert_id.toString()))
    }

    fun insertAlert(data: String, descriere: String) {

        var db = this.writableDatabase
        var values = ContentValues()

        values.put("timp", data.toString())
        values.put("descriere", descriere.toString())
        values.put("user_id", current_user.user_id.toString())
        var success = db.insert("alerts", null, values)
        Log.i("rezultat inset", "${success}")
    }

    fun fetchAlerts(): Cursor? {
        var db = this.readableDatabase
        var cursor = db?.rawQuery(
            "Select * FROM alerts WHERE user_id = " + current_user.user_id.toString(),
            null
        )
        return cursor

    }

    fun checkIfUserHasDevice(mac_address: String): Boolean { //vedem daca userul curent are deviceul aparut
        var db = this.readableDatabase
        var current_user_id =
            current_user.user_id //ne trebe sa vedem daca userul curent stie deviceul
        var cursor = db?.rawQuery(
            "SELECT * FROM devices WHERE (user_id = " + current_user_id.toString() + " AND mac = \'" + mac_address + "\')",
            null
        )
        if (cursor?.count == 1) {
            return true
        }
        return false
    }

    fun checkIfUserExists(user_name: String, user_password: String): Boolean {
        var db = this.readableDatabase
        var cursor = db?.rawQuery(
            "SELECT * FROM users WHERE (user_name = \'" + user_name + "\' AND user_pass = \'" + user_password + "\')",
            null
        )
        if (cursor?.count == 1) {
            return true
        }
        return false

    }

    fun getUserId(user_name: String, user_password: String): Int? {
        var db = this.readableDatabase
        var cursor =
            db?.rawQuery("SELECT user_id FROM users WHERE user_name = \'" + user_name + "\'", null)
        cursor?.moveToFirst()
        var col_index = cursor?.getColumnIndex("user_id")
        return col_index?.let { cursor?.getInt(it) }

    }

    fun getDeviceId(mac: String): Int? {
        var db = this.readableDatabase
        var cursor = db?.rawQuery("SELECT device_id FROM devices WHERE mac = \'" + mac + "\'", null)
        cursor?.moveToFirst()
        var col_index = cursor?.getColumnIndex("device_id")
        return col_index?.let { cursor?.getInt(it) }

    }





    fun sendMeasurementToRemoteDb(
        user_name: String?,
        pasi: Float?, distance: Float?,
        calories: Float?, dev_id: Int?, time_of_measurement: String
    ) {
        val urlString = "https://dev-perheart.eu/health/mi_band/";
        val url = URL(urlString);

        val conn = url.openConnection() as HttpURLConnection
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty(
            "X-Api-Key",
            "d20b21f0-5f63-11ec-96b3-0242ac1c0002"
        )

        var values = JSONObject()
        values.put("username", user_name)
        values.put("pasi", pasi)
        values.put("distance", distance)
        values.put("calories", calories)
        values.put("device_id", current_user.current_device_id)
        values.put("time_of_measurement", time_of_measurement)

        val os = DataOutputStream(conn.outputStream)
        os.writeBytes(values.toString())

        os.flush()
        os.close()

        var responseCode = conn.responseCode
        Log.i("Response DB", responseCode.toString())
        conn.disconnect()

    }

}

@SuppressLint("StaticFieldLeak")
object globalContext {
    var context: Context? = null
    fun setGlobalContext(contex: Context?) {
        context = contex

    }

}

object globalIsKnownDevice { //obiect global sa salvam stdiul unui device la imperechere
    var isKnown: Boolean = false

    fun checkIsKnown(state: String) {
        Log.i("fun checkIsKnown", "primit ${state}")
        if (state == "Unknown Device") {
            globalIsKnownDevice.isKnown = false
        } else if (state == "Known device") {
            globalIsKnownDevice.isKnown = true
        }
    }
}

object globalDatabase { //instanta globala a helperului de baza de date ca sa nu mai instantiem peste tot alte instante
    var db = database(globalContext.context, "Date.db", null, 1)
}







