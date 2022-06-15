package com.example.licenta

import CustomAdapter
import alerta
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import java.util.*

class scanning_view_activity : AppCompatActivity(), CustomAdapter.OnItemClickListener {

    private val lista_scanare = mutableListOf<ScanResult>()
    var adaptorRezultate = CustomAdapter(lista_scanare, this)

    private val lista_adrese = mutableListOf<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanning_activity_layout)

        var rec_view = findViewById<RecyclerView>(R.id.recycler_view_scan)
        rec_view.layoutManager = LinearLayoutManager(this)
        rec_view.adapter = adaptorRezultate
//        var db = database(this, "Date.db", null, 1)
//        db.insertUser("adrian", "sexfut")

        var lista_alerte = mutableListOf<alerta>()
        lista_alerte.clear()
        var cursor = globalDatabase.db.fetchAlerts()
        while (cursor?.moveToNext() == true) {
            var index_alert_id = cursor.getColumnIndex("alert_id")
            var index_user_id = cursor.getColumnIndex("user_id")
            var index_date_time = cursor.getColumnIndex("timp")
            var index_descriere = cursor.getColumnIndex("descriere")
            var temp = alerta(
                alert_id = cursor.getInt(index_alert_id),
                user_id = cursor.getInt(index_user_id),
                descriere = cursor.getString(index_descriere),
                calendar = cursor.getString(index_date_time)
            )

            lista_alerte.add(temp)
        }
        if (cursor?.moveToFirst() != false) {
            lista_alerte.sortBy {
                (it.calendar?.split(":")?.get(1)?.let { it1 ->
                    it.calendar.split(":").get(0).toInt().times(100).plus(
                        it1.toInt()
                    ) //puteam sa fi facut o functie
                })
            } //sunt un zeu printre muritori
            globalSortedAlerts.updateList(lista_alerte)
//            if(cursor?.moveToFirst() != true) {
            globalSortedAlerts.getNextAlert()
//            }
        }
        Log.i(
            "next alert", "${
                globalSortedAlerts.next_alert_index?.let {
                    globalSortedAlerts.alerte_sortate?.get(
                        it
                    )?.calendar
                }
            }"
        )

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        promptEnableBluetooth()
        promptEnableLocation()
        startBleScan()

    }

    override fun onStop() {
        super.onStop()
        stopBleScan()
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy { //defining bt adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy { //defining BLE scanner
        bluetoothAdapter.bluetoothLeScanner
    }

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()  //scan settings, which are necessaryva

    val scanCallBack = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {

            if (result.device.name == "My Oximeter" || result.device.name == "Mi Band 3" || result.device.name == "B01H_M4") {
                if (result.device in lista_adrese == false) {//tinem doua liste in paralel, una cu adrese, alta cu results
                    lista_scanare.add(result)
                    lista_adrese.add(result.device)
                    adaptorRezultate.notifyItemInserted(lista_scanare.indexOf(lista_scanare.last()))
                }

                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found Device! Name: ${name ?: "Unnamed"}, Adress: $address"
                    )
                }
            }

        }
    }

    fun stopBleScan() {
        bleScanner.stopScan(scanCallBack)
    }

    fun startBleScan() {

        bleScanner.startScan(null, settings, scanCallBack)//call the bleScanner startScan function

    }

    fun goToAlert(view: View) {
        intent = Intent(
            this,
            alerts_view_activity::class.java
        )//nu inteleg exact ce face scope res operatorul aici dar whatever
        startActivity(intent)
    }

    fun insert_test_alert(view: View) {
        globalDatabase.db.insertAlert("2023-05-20 20:00:00", "viagra")
    }

    private fun promptEnableBluetooth() { //prompt for enabling bluetooth
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableLocation() {

        val locManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locManager.isLocationEnabled) {
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }
    }

    override fun onItemClick(position: Int) { //functia asta e rulata cand dai click pe un item din lista
        Toast.makeText(this@scanning_view_activity, "Item $position click", Toast.LENGTH_SHORT)
            .show()
        val clickedItem = lista_scanare[position]
        adaptorRezultate.notifyItemChanged(position)
        //https://www.youtube.com/watch?v=wKFJsrdiGS8/
        if (clickedItem.device.name == "Mi Band 3") {
            stopBleScan()


            if (globalDatabase.db.checkIfUserHasDevice(clickedItem.device.address) == false) {
                globalDatabase.db.insertDevice(
                    "Mi Band 3",
                    current_user.user_id,
                    clickedItem.device.address
                )
            } else {
                globalIsKnownDevice.isKnown = true //doar ptr miband
            }

//            var state= findViewById<TextView>(R.id.textView_statut).text.toString().trim() //asta nu crek merge ca nu se specifica pozitia in viewholder

//            globalIsKnownDevice.checkIsKnown(state) //folosim asta pentru a tine minte daca mibandul e conectat ptr prima data

            current_user.setDeviceType("Mi Band 3")
            current_user.setDevice(
                globalDatabase.db.getDeviceId(clickedItem.device.address),
                clickedItem.device.address
            )
            intent = Intent(
                this,
                miband_view_activity::class.java
            )//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)

        } else if (clickedItem.device.name == "B01H_M4") {
            stopBleScan()

            var state = findViewById<TextView>(R.id.textView_statut).text.toString()
            globalIsKnownDevice.checkIsKnown(state)

            if (globalDatabase.db.checkIfUserHasDevice(clickedItem.device.address) == false) {
                globalDatabase.db.insertDevice(
                    "M4SmartBand",
                    current_user.user_id,
                    clickedItem.device.address
                )
                Log.i("intrat in device know", "Inserteed dev")
            }

            current_user.setDeviceType("M4SmartBand")
            current_user.setDevice(
                globalDatabase.db.getDeviceId(clickedItem.device.address),
                clickedItem.device.address
            )

            intent = Intent(
                this,
                m4_view_activity::class.java
            )//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)

        } else {
            //cod ximetr
            //cel mai simplu e de passuit bluetooth deviceul la alt activity si instantiat obiectul acolo
            stopBleScan()

            if (globalDatabase.db.checkIfUserHasDevice(clickedItem.device.address) == false) {
                globalDatabase.db.insertDevice(
                    "JPD 500",
                    current_user.user_id,
                    clickedItem.device.address
                )
                Log.i("intrat in device know", "Inserteed dev")
            }

            current_user.setDeviceType("JPD 500")
            current_user.setDevice(
                globalDatabase.db.getDeviceId(clickedItem.device.address),
                clickedItem.device.address
            )
            var state = findViewById<TextView>(R.id.textView_statut).text.toString()
            globalIsKnownDevice.checkIsKnown(state)

            if (globalDatabase.db.checkIfUserHasDevice(clickedItem.device.address) == false) {
                globalDatabase.db.insertDevice(
                    "Jumper Pulseoximeter",
                    current_user.user_id,
                    clickedItem.device.address
                )
            }

            intent = Intent(
                this,
                oximeter_view_activity::class.java
            )//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)
        }
    }
}