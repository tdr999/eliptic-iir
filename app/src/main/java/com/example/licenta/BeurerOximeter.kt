package com.example.licenta
import android.bluetooth.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class BeurerOximeter(device: BluetoothDevice) {
    var dev = device
    var gatt : BluetoothGatt? = null

    var spo2 : Int  = 0
    var pi : Int    = 0
    var BPM : Int   = 0 //default values


    val gattCallBack = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {

            with(characteristic) {
                Log.i("din oxi","caracterista ${value}")
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

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            this@BeurerOximeter.gatt = gatt
            val serviciuDeConectare =
                gatt?.getService(UUID.fromString("0000ff12-0000-1000-8000-00805f9b34fb"))
            val caracteristicaTemp =
                serviciuDeConectare?.getCharacteristic(UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb"))
            val caracteristicaConfigurare =
                serviciuDeConectare?.getCharacteristic(UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb"))
            if (caracteristicaTemp == null) { //ptr notificcari
                Log.i("car temop", "e nula coaie \n\n")
            }
            Log.i("dupa sx", "${caracteristicaTemp?.value}")
            //gatt?.setCharacteristicNotification(caracteristicaTemp, true)
            gatt?.setCharacteristicNotification(caracteristicaTemp, true)
            val cccdUuid =
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //tre sa scriem la descriptor ptr ca sa subscribe
            val desc = caracteristicaTemp?.getDescriptor(cccdUuid)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)


            //cale oribila de a obtine time and date, ty kotlin

            var primiiBytesTrimisi: ByteArray = byteArrayOf( //vom folosi ca model
                131.toByte(),
                0x16,
                0x03,
                0x08,
                0x0d,
                0x38,
                0x05,
                0x05,
                0x05,
                0x78
            ) //gasiti cu wiresharek //bug compilator schimbat primu byte
            var date = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss SSS")
            var formatted = date.format(formatter)
            formatted = formatted.toString() //i wont bother reading documentatoo
            var lista = formatted.split(" ")
            var luna = lista[1].toInt().toByte()
            var zi = lista[2].toInt().toByte()
            var ora = lista[3].toInt().toByte()
            var minute = lista[4].toInt().toByte()
            var ultimul_numar = 0x2B + zi + ora + minute //luna inca nu e sigura
            Log.i("valoarea lui ult i", "cifra e ${ultimul_numar.toInt()}")
            primiiBytesTrimisi[2] = luna
            primiiBytesTrimisi[3] = zi
            primiiBytesTrimisi[4] = ora
            primiiBytesTrimisi[5] = minute
            primiiBytesTrimisi[9] = ultimul_numar.toByte()

            Log.i(
                "valoare byte aray",
                "${primiiBytesTrimisi.toHexString()}"
            )                               // 131 e 83 in hex
            //sugi pula android, cu tot respectul


            val aiDoileaBytesTrimisi = byteArrayOf(
                153.toByte(),
                0x00,
                0x19
            )                                            //nr asta se schimba

            val aiTreileaBytesTrimisi = byteArrayOf(153.toByte(), 0x01, 0x1a)
            caracteristicaConfigurare?.setValue(primiiBytesTrimisi)
            gatt?.writeCharacteristic(caracteristicaConfigurare)
            Log.i("dupa sx", "${caracteristicaTemp?.value}")
            caracteristicaConfigurare?.setValue(aiDoileaBytesTrimisi)
            gatt?.writeCharacteristic(caracteristicaConfigurare)

            caracteristicaConfigurare?.setValue(aiTreileaBytesTrimisi)
            gatt?.writeCharacteristic(caracteristicaConfigurare)

        }
    }

    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la pulsoximetru")
    }
}

