package com.example.MiBand

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object current_user {
    var user_id: Int? = null
    var username: String? = null
    var userpass: String? = null
    var device_mac: String? = null
    var device_type: String? = null
    var mb: MiBand? = null
}

fun sendMeasurementToRemoteDb(
    user_name: String?,
    pasi: Float?, distance: Float?,
    calories: Float?, dev_id: Int?, time_of_measurement: String
) {
    val urlString = "https://dev-perheart.eu/health/mi_band/"
    val url = URL(urlString)

    val conn = url.openConnection() as HttpURLConnection
    conn.doOutput = true
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json; utf-8")
    conn.setRequestProperty(
        "X-Api-Key",
        "d20b21f0-5f63-11ec-96b3-0242ac1c0002"
    )
    //vezi daca ar fi mai bine sa pui MAC ul in loc de device ID
    var values = JSONObject()
    values.put("username", user_name)
    values.put("pasi", pasi)
    values.put("distance", distance)
    values.put("calories", calories)
    values.put("device_id", dev_id)
    values.put("time_of_measurement", time_of_measurement)

    val os = DataOutputStream(conn.outputStream)
    os.writeBytes(values.toString())

    os.flush()
    os.close()

    var responseCode = conn.responseCode
    Log.i("Response DB", responseCode.toString())
    conn.disconnect()

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
}

object flagMondialTimeout{
    var neamConectat = 0
}

