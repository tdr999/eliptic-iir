package com.example.licenta

import alerta
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


data class user_device(var dev_id: Int, var user_Id: Int, var dev_type: String, var mac: String){
    var device_id = dev_id
    var user_id = user_Id
    var device_type = dev_type
    var mac_number = mac
}

object current_user{
    var user_id : Int? = null
    var username : String? = null
    var userpass : String? = null
    var current_device_id : Int? = null
    var current_device_mac : String? = null
    var device_type : String? = null
    var mb : MiBand? = null

    fun setUserPass(userid : Int?, user_name: String, user_password: String){
        username = user_name
        userpass = user_password
        user_id = userid
    }

    fun setDevice(cur_id : Int?, cur_mac : String){
       current_device_id = cur_id
       current_device_mac = cur_mac
    }
    fun setDeviceType(type : String){
        device_type = type
    }
    fun setMiband(mib : MiBand){
        mb = mib
    }

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
                "measurement_id INTEGER PRIMARY KEY,  " +
                "user_id INT,  " +
                "bpm INT DEFAULT 0,  " +
                "spo2 INT DEFAULT 0,  " +
                "pen_index INT DEFAULT 0," +
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
                "user_name TEXT, " +
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
                "timp TIME, " +
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

    fun insertDevice(device_type: String, user_id: Int?, mac_address: String){
        var db = this.writableDatabase
        db?.execSQL("INSERT INTO devices(device_type, user_id, mac) VALUES(\'" +
                device_type + "\'," + user_id.toString() + ",\'"+ mac_address + "\')")
    }


    fun insertMeasurement(
        user_Id: Int?, bpm: Int?, spo2: Int?, pen_index : Int?,
        press: String, pasi: Int?, distance: Float?,
        calories: Float?, dev_id: Int?, time_of_measurement: String){

        var db = this.writableDatabase
        var sql_string = "INSERT INTO measurements(user_id, bpm, spo2, press, pasi, " +
                "distance, calories, device_id, time_of_measurement) VALUES(" +
                user_Id.toString() + ", " + bpm.toString() +", " + spo2.toString() +
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
        Log.i("rezultat inset","${success}" )


    }
    fun insertUser(user_name: String, pass : String){
        var db = this.writableDatabase
        if (checkIfUserExists(user_name, pass) == false) {
            db?.execSQL("INSERT INTO users(user_name, user_pass) VALUES(\'" + user_name + "\', \'" + pass + "\')")
        }
    }


    fun removeAlert(alert_id: Int?){
        var db = this.writableDatabase
        db.delete("alerts", "alert_id=?", arrayOf(alert_id.toString()))
    }

    fun insertAlert(data : String, descriere : String){

        var db = this.writableDatabase
        var values = ContentValues()

        values.put("timp", data.toString())
        values.put("descriere", descriere.toString())
        values.put("user_id", current_user.user_id.toString())
        var success = db.insert("alerts", null, values)
        Log.i("rezultat inset","${success}" )
    }
    fun fetchAlerts(): Cursor?{
        var db = this.readableDatabase
        var cursor = db?.rawQuery("Select * FROM alerts WHERE user_id = " + current_user.user_id.toString(), null)
        return cursor

    }





    fun checkIfUserHasDevice(mac_address: String): Boolean { //vedem daca userul curent are deviceul aparut
        var db = this.readableDatabase
        var current_user_id = current_user.user_id //ne trebe sa vedem daca userul curent stie deviceul
        var cursor = db?.rawQuery("SELECT * FROM devices WHERE (user_id = "+ current_user_id.toString() + " AND mac = \'"+mac_address+"\')", null)
        if (cursor?.count == 1 ){
            return true
        }
        return false
    }




    fun checkIfUserExists(user_name: String, user_password: String): Boolean {
        var db = this.readableDatabase
        var cursor = db?.rawQuery("SELECT * FROM users WHERE (user_name = \'"+ user_name + "\' AND user_pass = \'"+user_password+"\')", null)
        if (cursor?.count == 1 ){
            return true
        }
        return false

    }



    fun getUserId(user_name: String, user_password: String): Int? {
        var db = this.readableDatabase
        var cursor = db?.rawQuery("SELECT user_id FROM users WHERE user_name = \'"+ user_name + "\'", null)
        cursor?.moveToFirst()
        var col_index = cursor?.getColumnIndex("user_id")
        return col_index?.let { cursor?.getInt(it) }

    }


    fun getDeviceId(mac : String): Int? {
        var db = this.readableDatabase
        var cursor = db?.rawQuery("SELECT device_id FROM devices WHERE mac = \'"+ mac + "\'" , null)
        cursor?.moveToFirst()
        var col_index = cursor?.getColumnIndex("device_id")
        return col_index?.let { cursor?.getInt(it) }

    }

}


@SuppressLint("StaticFieldLeak")
object globalContext{
    var context : Context? = null
    fun setGlobalContext(contex: Context?){
        context = contex

    }

}

object globalIsKnownDevice{ //obiect global sa salvam stdiul unui device la imperechere
    var isKnown : Boolean = false

    fun checkIsKnown(state : String){
        Log.i("fun checkIsKnown", "primit ${state}")
        if (state == "Unknown Device"){
            globalIsKnownDevice.isKnown = false
        }

        else if (state == "Known device"){
            globalIsKnownDevice.isKnown = true
        }
    }
}


object globalDatabase{ //instanta globala a helperului de baza de date ca sa nu mai instantiem peste tot alte instante
    var db = database(globalContext.context, "Date.db", null, 1)
}

object globalSortedAlerts{
    var alerte_sortate : MutableList<alerta>? = null
    var next_alert_id : String? = "No next"
    var next_alert_index : Int? = 0
    fun updateList(mutableList: MutableList<alerta>){
        alerte_sortate = mutableList

    }

    fun getList() : MutableList<alerta>?{
        return alerte_sortate
    }

    fun getNextAlert(){
        var hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
        var minute = Calendar.getInstance().get(Calendar.MINUTE).toString()
        if (minute.length == 1)
        {
           minute = "0$minute"
        }
        var time_for_sort = "1" + hour + minute
        var intrat_in_for = 0
        Log.i("current_time", "for sorting ${time_for_sort}")

//        Log.i("time", "${time_for_sort}")
        if (alerte_sortate?.size!! > 0) {
            var sz = alerte_sortate?.size!! - 1
            for (i  in 0..sz!!) {
                var timp_din_alerta = "nothing"
                if (alerte_sortate!![i].calendar?.split(":")?.get(1)?.length  == 1){

                    timp_din_alerta = "1" + alerte_sortate!![i].calendar?.split(":")
                        ?.get(0) +"0" + alerte_sortate!![i].calendar?.split(":")?.get(1)
                }
                else {
                    timp_din_alerta = "1" + alerte_sortate!![i].calendar?.split(":")
                        ?.get(0) + alerte_sortate!![i].calendar?.split(":")?.get(1)
                }
//                Log.i("timp din alerta", "${timp_din_alerta}")
                if (timp_din_alerta.toInt() > time_for_sort.toInt()) {
                    next_alert_id = alerte_sortate!![i].alert_id.toString()
                    next_alert_index = i
                    intrat_in_for = 1
                    break

                }





            }
            if (intrat_in_for == 1) {
                Log.i(
                    "Next alert ID",
                    " ${alerte_sortate!![next_alert_index!!].calendar}"
                )

            }
            else{

                next_alert_id = "No next"
                next_alert_index = 0
            }
        }else{
            Log.i("no", "no next alert")
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun setAlarm(time : String){
    val temp = time.split(":")
    var time_milis = 0
    time_milis = temp[0].toInt() * 3600 * 1000 + temp[1].toInt() * 60 * 1000
    val sdf = SimpleDateFormat("yyyy:MM:dd:HH:mm:ss")
    val date = sdf.format(Date())
    val timp_cur = date.split(":")[3].toInt() * 3600 * 1000 + date.split(":")[4].toInt() * 60 * 1000
    val timp = (time_milis - timp_cur).toLong()

//    val timp = time_milis.toLong()
    Log.i("set_alarm", "at ${timp} timp cur timp milis ${timp_cur}, ${time_milis}")
    Log.i("will fire at", "${System.currentTimeMillis() + timp}")
    val alarm_manager = globalContext.context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val inten = Intent(globalContext.context, AlarmReceiver::class.java)
    val pendingInt = PendingIntent.getBroadcast(globalContext.context, 0, inten, PendingIntent.FLAG_CANCEL_CURRENT )

//    alarm_manager.cancel(pendingInt) //cancel last alarm
//    alarm_manager.setRepeating(AlarmManager.RTC,
//        timp.toLong(), AlarmManager.INTERVAL_DAY, pendingInt)
    android.support.v4.app.AlarmManagerCompat.setExact(alarm_manager, AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +  timp, pendingInt)
//    alarm_manager.setExact(AlarmManager.RTC_WAKEUP, timp, pendingInt)

//    alarm_manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timp, pendingInt)
//    alarm_manager.setAlarmClock(AlarmManager.AlarmClockInfo(timp, pendingInt), pendingInt)
}


class AlarmReceiver : BroadcastReceiver(){ //fa notificari si noptificari la miband
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("received_alarm", "yes received alarm")
        if (current_user.device_type == "Mi Band 3" && globalSortedAlerts.next_alert_id != "No next"){
            globalSortedAlerts.next_alert_index?.let {
                globalSortedAlerts.alerte_sortate?.get(it)?.let {
                    it.descriere?.let { it1 ->
                        current_user.mb?.sendCustomMessage(
                            it1
                        )
                    }
                }
            }


            globalSortedAlerts.next_alert_index?.let { globalSortedAlerts.alerte_sortate?.get(it)?.let { it.descriere?.let { it1 ->
                globalAlertManager.sendNotif(
                    it1
                )
            } } }


        }else if (globalSortedAlerts.next_alert_id != "No next"){
            globalSortedAlerts.next_alert_index?.let { globalSortedAlerts.alerte_sortate?.get(it)?.let { it.descriere?.let { it1 ->
                globalAlertManager.sendNotif(
                    it1
                )
            } } }
        }

    }

}


object globalAlertManager{
    val notificationManager: NotificationManager =
        globalContext.context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotif(descriere: String){

        var channel = NotificationChannel("1", "mihai", NotificationManager.IMPORTANCE_DEFAULT ).apply { description = descriere }

        notificationManager.createNotificationChannel(channel)
        var notificatudor = Notification.Builder(globalContext.context, "1" )
            .setContentTitle("Take medication")
            .setContentText(descriere)
            .setSmallIcon(R.drawable.etti)
            .setAutoCancel(true)


        try{ notificationManager.notify(1, notificatudor.build())}
        catch (e : Exception){
            Log.i("primit", "exception ${e}")
        }


    }

}