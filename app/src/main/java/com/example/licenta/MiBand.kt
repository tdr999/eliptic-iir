package com.example.licenta

import android.bluetooth.*
import android.util.Log
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec



class MiBand (device: BluetoothDevice) {

    val dev = device

    var ESTE_AUTHENTICAT = 0

    var gatt : BluetoothGatt? = null

    var authChar : BluetoothGattCharacteristic? = null


    val gattCallback = object : BluetoothGattCallback() { //public callback so we get our variables
        //this callback is the core of our program honestly

        fun authenticateBand(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, valoareHex: List<String>){ //pentru authenticare

            with(characteristic){
                if (valoareHex[0] == "10" && valoareHex[1] == "01" && valoareHex[2] == "01"){
                    Log.i("din on chcarac changesd", "Da dom'le, ne am legat si acuma trimetem auth number")
                    val authNumber = byteArrayOf(0x02, 0x08)
                    characteristic.setValue(authNumber)
                    gatt.writeCharacteristic(authChar)
                }
                if (valoareHex[0] == "10" && valoareHex[1] == "02" && valoareHex[2] == "01"){
                    Log.i("din on chcarac changesd", "Da dom'le, ne am legat si acuma trimetem ENCRYPTEDKEYACUMA number")

                    var tempKey = valoareHex.takeLast(16) // keia primita
                    Log.i("ult16", "$tempKey\n")
                    var SECRET_KEY : ByteArray = byteArrayOf(
                        0x30,
                        0x31,
                        0x32,
                        0x33,
                        0x34,
                        0x35,
                        0x36,
                        0x37,
                        0x38,
                        0x39,
                        0x40,
                        0x41,
                        0x42,
                        0x43,
                        0x44,
                        0x45
                    ) //de tudor

                    //criptare aes cum vrea miband
                    var generatedSecretKey = SecretKeySpec(SECRET_KEY, "AES")
                    val crypto : Cipher = Cipher.getInstance("AES/ECB/NoPadding")
                    crypto.init(Cipher.ENCRYPT_MODE,generatedSecretKey)

                    var finalKey = crypto.doFinal(value.takeLast(16).toByteArray()) //amperecherea


                    authChar?.setValue(byteArrayOf(0x03, 0x08) + finalKey )
                    gatt.writeCharacteristic(authChar)


                }
                if (valoareHex[0] == "10" && valoareHex[1] == "03" && valoareHex[2] == "01") {
                    Log.i("if4", "imperecheat succes\n")
                    ESTE_AUTHENTICAT = 1

                }
                Log.i("carac post", "${valoareHex.take(3)}")
                subscribeHeartRate()



            }
        }


        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //this is what happens when the characteristic value is changed

            with(characteristic) {
                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic $uuid changed | value: ${value.toHexString()}"
                )

                var valoareHex = (value.toHexString()).split(" ")
                if (this@MiBand.ESTE_AUTHENTICAT == 0) { //daca nu e authenticat
                    authenticateBand(gatt, characteristic, valoareHex)
                }
            }
            if (characteristic.uuid == UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")){
               Log.i("din on char changed", "${characteristic.value.toHexString().split(" ")[1].toInt(16)}")
            }

        }


        fun ByteArray.toHexString(): String =
            joinToString(separator = " ", prefix = "") { String.format("%02X", it) }



        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
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

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) { //aici gasim ce e important la logare

            gatt.printGattTable()//aici e clar conectat deja
            val referintaGatt = gatt
            val serviciuDeConectare = //fa clauza separata pentru authenticare
                referintaGatt.getService(UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb"))
            val caracteristicaAuth =
                serviciuDeConectare.getCharacteristic(UUID.fromString("00000009-0000-3512-2118-0009af100700"))


            this@MiBand.gatt = referintaGatt
            this@MiBand.authChar = caracteristicaAuth
            //enable our stuff
            val SECRET_KEY = byteArrayOf(
                0x30,
                0x31,
                0x32,
                0x33,
                0x34,
                0x35,
                0x36,
                0x37,
                0x38,
                0x39,
                0x40,
                0x41,
                0x42,
                0x43,
                0x44,
                0x45
            ) //de tudor


            val SEND_KEY =  byteArrayOf(0x01, 0x08) + SECRET_KEY
            referintaGatt?.setCharacteristicNotification(caracteristicaAuth, true)
            caracteristicaAuth?.setValue(SEND_KEY)
            referintaGatt?.writeCharacteristic(caracteristicaAuth)
        }



    }


    fun subscribeHeartRate(){

        //  https://github.com/MalveiraAlexander/Mi-Band-3-SDK/blob/master/MiBand3SDK/Components/HeartRate.cs
        val HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_CHARACTERISTIC  = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_CONTROLPOINT_CHARACTERISTIC = UUID.fromString ("00002a39-0000-1000-8000-00805f9b34fb")
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //we already know what this is
        val HEART_RATE_START_COMMAND = byteArrayOf(21, 2, 1)

        val serviciuHeart = gatt?.getService(HEART_RATE_SERVICE)
        val measHeart = serviciuHeart?.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC)
        val controlHeart = serviciuHeart?.getCharacteristic(HEART_RATE_CONTROLPOINT_CHARACTERISTIC)
        val descMeasHeart = measHeart?.getDescriptor(cccdUuid)

        /*
        1. Scrie la descriptoru de control bytii de enable notify
        2. Alege felul de measurememnt
        3. Scrie bytii de comandaManual apoi de comandaContinua la caracteristica de control
        4. Citeste valoarea la caracteristica de masurare
         */


        gatt?.setCharacteristicNotification(measHeart, true) // enable recv notif
        descMeasHeart?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)//config carac de mas sa trimita notif
        gatt?.writeDescriptor(descMeasHeart) //1


        //comenzile pentru diverse feluri de citire

        //https://dzone.com/articles/miband-3-and-react-native-partnbsp1 inspirat de aici parca

        val manualCmd = byteArrayOf(0x15, 0x02, 0x00 ) //pentru oprit prima e cu 1 la final a doua cu 0
        controlHeart?.setValue(byteArrayOf(0x15, 0x02, 0x00))
        gatt?.writeCharacteristic(controlHeart)
        val continuousCmd = byteArrayOf( 0x15, 0x01, 0x01) //2
        controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x01))
        gatt?.writeCharacteristic(controlHeart)



        controlHeart?.setValue(byteArrayOf( 0x01, 0x00))
        gatt?.writeCharacteristic(controlHeart)


        controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x01))
        gatt?.writeCharacteristic(controlHeart)



        gatt?.readCharacteristic(measHeart)

        Log.i("din heart rate", "valoare ${measHeart?.value?.toHexString()?.split(" ")?.get(1)?.toInt(16)}")//bytes primit in int



        controlHeart?.value = HEART_RATE_START_COMMAND
        gatt?.writeCharacteristic(controlHeart) //folosit sa anuntam ca incepem masuratorile

    }



    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "") { String.format("%02X", it) }

    fun connect(){
        dev.connectGatt(null, false, gattCallback) //schimba la true sa se conecteze automat
    }
    fun disconnect(){
        gatt?.disconnect()
    }


}