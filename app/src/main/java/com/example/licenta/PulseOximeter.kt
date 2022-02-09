package com.example.licenta
import android.bluetooth.*
import android.util.Log
import java.util.*

class PulseOximeter(device: BluetoothDevice) {
    var dev = device
    var gatt : BluetoothGatt? = null

    val gattCallBack = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {

            with(characteristic) {
                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic $uuid changed | value: ${value.toHexString().toString()}"
                )
            }
        }

        fun ByteArray.toHexString(): String =
            joinToString(separator = " ", prefix = "") { String.format("%02X", it) }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("BluetoothGattCallback", "Successfully connected to $deviceAddress")
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
            this@PulseOximeter.gatt = gatt
            val serviciuDeConectare =
                gatt?.getService(UUID.fromString("cdeacb80-5235-4c07-8846-93a37ee6b86d"))
            val caracteristicaTemp = serviciuDeConectare?.getCharacteristic(UUID.fromString("CDEACB81-5235-4C07-8846-93A37EE6B86D"))
           if (caracteristicaTemp == null){
               Log.i("car temop", "e nula coaie \n\n")
           }
           Log.i("dupa sx", "${caracteristicaTemp?.value}")
            //gatt?.setCharacteristicNotification(caracteristicaTemp, true)
           gatt?.setCharacteristicNotification(caracteristicaTemp, true)
           val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //tre sa scriem la descriptor ptr ca sa subscribe
           val desc = caracteristicaTemp?.getDescriptor(cccdUuid)
           desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
           gatt?.writeDescriptor(desc)


        }
    }

    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la pulsoximetru")
    }
}
