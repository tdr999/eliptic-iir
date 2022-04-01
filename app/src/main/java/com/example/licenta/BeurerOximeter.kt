package com.example.licenta
import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


//aceasta mizerie de oximetru este atat de cretina incat e nevoie de o coada ca sa implementam operatiile intr o ordine
//suficient de inceata ca sa inteleaga oximetrul ce se petrece







class BeurerOximeter(device: BluetoothDevice) {
    var dev = device
    var referintaGatt : BluetoothGatt? = null

    var spo2 : Int  = 0
    var pi : Int    = 0
    var BPM : Int   = 0 //default values




    val gattCallBack = object : BluetoothGattCallback() {

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i("onCharWrite", "${status}")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {

            with(characteristic) {
                Log.i("din oxi","caracterista ${value.toHexString()}")
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
        private fun BluetoothGatt.printGattTable() { //de sters dupa ce o terminam de folosit
            //this prints gatt service numbers

            if (services.isEmpty()) {
                Log.i("gattTable", "nu merge dom le, e gol tabelu")
                //val mesaj =
                //Toast.makeText(, "Eroare la servicii", Toast.LENGTH_SHORT)
                //mesaj.show()
                //daca tot avem eroarea, si deconectam
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
        @SuppressLint("NewApi")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.printGattTable()
            val serviciuDeConectare =
                gatt?.getService(UUID.fromString("0000ff12-0000-1000-8000-00805f9b34fb"))
            if (serviciuDeConectare != null){
                Log.i("on service discovere", "hei, nu e null serviciul de conectare")
                this@BeurerOximeter.referintaGatt = gatt
            }
            val caracteristicaTemp =
                serviciuDeConectare?.getCharacteristic(UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb"))
            val caracteristicaConfigurare =
                serviciuDeConectare?.getCharacteristic(UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb"))
            caracteristicaConfigurare?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE    //ca sa trimita comenz . Request e write cu response, comanda e write fara
            if (caracteristicaTemp == null) { //ptr notificcari
                Log.i("car temop", "e nula coaie \n\n")
            }

            if (caracteristicaConfigurare == null) { //ptr notificcari
                Log.i("car temop", "e nula coaie si aia de configurare \n\n")
            }



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




            Log.i(
                "valoare byte aray",
                "${primiiBytesTrimisi.toHexString()} , ${primiiBytesTrimisi}"
            )                               // 131 e 83 in hex
            //sugi pula android, cu tot respectul


            val aiDoileaBytesTrimisi = byteArrayOf(
                153.toByte(),
                0x00,
                0x19
            )                                            //nr asta se schimba

            val aiTreileaBytesTrimisi = byteArrayOf(153.toByte(), 0x01, 0x1a)


            //gatt?.setCharacteristicNotification(caracteristicaTemp, true)

            val cccdUuid =
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //tre sa scriem la descriptor ptr ca sa subscribe
            val desc = caracteristicaTemp?.getDescriptor(cccdUuid)

            gatt?.setCharacteristicNotification(caracteristicaTemp, true)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            var boolean = gatt?.writeDescriptor(desc)




            if (boolean == true){

                var date = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss SSS")
                var formatted = date.format(formatter)
                formatted = formatted.toString() //i wont bother reading documentatoo
                formatted = formatted.lowercase(Locale.getDefault())
                var lista = formatted.split(" ")
                var luna = lista[1].toInt().toByte()
                var zi = lista[2].toInt().toByte()
                var ora = lista[3].toInt().toByte()
                var minute = lista[4].toInt().toByte()
                var ultimul_numar = 0x2B + zi + ora + minute //luna inca nu e sigura
                primiiBytesTrimisi[2] = luna
                primiiBytesTrimisi[3] = zi
                primiiBytesTrimisi[4] = ora
                primiiBytesTrimisi[5] = minute
                primiiBytesTrimisi[9] = ultimul_numar.toByte()
                caracteristicaConfigurare?.value = primiiBytesTrimisi
                boolean = this@BeurerOximeter.referintaGatt?.writeCharacteristic(caracteristicaConfigurare)
                Log.i("dupaScriereristica", "${boolean}")
            }



//
////            Log.i("dupa sx", "${caracteristicaTemp?.value}")
//            caracteristicaConfigurare?.setValue(aiDoileaBytesTrimisi)
//            boolean = gatt?.writeCharacteristic(caracteristicaConfigurare)
//            Log.i("dupaScriereristica2", "${boolean}")

//            caracteristicaConfigurare?.setValue(aiTreileaBytesTrimisi)
//            gatt?.writeCharacteristic(caracteristicaConfigurare)

        }
    }

    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la pulsoximetru")
    }
}

