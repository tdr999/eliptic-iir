package com.example.licenta
//netestat
import android.bluetooth.*
import android.util.Log
import java.nio.charset.Charset
import java.util.*

class PulseOximeter(device: BluetoothDevice) {
    var dev = device
    var gatt : BluetoothGatt? = null
    var caracteristicaAuth : BluetoothGattCharacteristic? = null


    val gattCallBack = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic){
                Log.i("schimbat caracteristca", "$value\n")
            }
        }


        private fun BluetoothGatt.printGattTable() { //de sters dupa ce o terminam de folosit
            //this prints gatt service numbers

            if (services.isEmpty()) {
                Log.i("gattTable", "nu merge dom le, e gol tabelu")
                return
            } else {
                var tempStr = "Servicii: \n"
                services.forEach { service ->
                    val table = service.characteristics.joinToString(
                        "\n|--",
                        "|--"
                    ) {
                        it.uuid.toString()
                    }
                    Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$table")
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            // some logging stuff for when connection changes, ie it's either connected or disconnected

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //    gatt.requestMtu(20) //ii zicem sa ne dea 512bytes odata
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    gatt.discoverServices() //find services

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }





        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.printGattTable()
            this@PulseOximeter.gatt = gatt
            val serviciuDeConectare =
               gatt?.getService(UUID.fromString("cdeacb80-5235-4c07-8846-93a37ee6b86d"))
            this@PulseOximeter.caracteristicaAuth =
                serviciuDeConectare?.getCharacteristic(UUID.fromString("cdeacb81-5235-4c07-8846-93a37ee6b86d"))
            gatt?.setCharacteristicNotification(this@PulseOximeter.caracteristicaAuth, true)
        }


    }







    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la pulsoximetru")
    }
}



