package com.example.licenta

import CustomAdapter
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
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast

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
        var db = database(this, "Date.db", null, 1)
        db.insertUser("adrian", "sexfut")



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

    private val bluetoothAdapter: BluetoothAdapter by lazy{ //defining bt adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    private val bleScanner by lazy { //defining BLE scanner
        bluetoothAdapter.bluetoothLeScanner
    }

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()  //scan settings, which are necessaryva

    val scanCallBack = object : ScanCallback(){

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
    fun stopBleScan(){
        bleScanner.stopScan(scanCallBack)
    }


    fun startBleScan(){

        bleScanner.startScan(null, settings, scanCallBack)//call the bleScanner startScan function

    }



    private fun promptEnableBluetooth(){ //prompt for enabling bluetooth
        if(!bluetoothAdapter.isEnabled){
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
        Toast.makeText(this@scanning_view_activity, "Item $position click", Toast.LENGTH_SHORT).show()
        val clickedItem = lista_scanare[position]
        adaptorRezultate.notifyItemChanged(position)
        //https://www.youtube.com/watch?v=wKFJsrdiGS8/
        if (clickedItem.device.name == "Mi Band 3"){
            stopBleScan()

            intent = Intent(this, miband_view_activity::class.java)//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)


        }

        else if (clickedItem.device.name == "B01H_M4"){
            stopBleScan()

            intent = Intent(this, m4_view_activity::class.java)//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)


        }
        else{
            //cod ximetr
                //cel mai simplu e de passuit bluetooth deviceul la alt activity si instantiat obiectul acolo
            stopBleScan()
            intent = Intent(this, oximeter_view_activity::class.java)//nu inteleg exact ce face scope res operatorul aici dar whatever
            intent.putExtra("bt_device", clickedItem.device)
            startActivity(intent)
        }
    }
}