package com.example.licenta

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.or
//add user info configuration using nightscout code as inspiration

class MiBand (device: BluetoothDevice) {

    val dev = device

    var ESTE_AUTHENTICAT = 0

    var gatt : BluetoothGatt? = null

    var authChar : BluetoothGattCharacteristic? = null
    var heart_rate : Int? = 0
    var steps : Int? = 0
    var distance : Float? = 0.0f
    var calories : Float? = 0.0f
    var baterie : Int? = null


//    var SECRET_KEY = byteArrayOf(
//        9.toByte(),
//        29.toByte(),
//        223.toByte(),
//        23.toByte(),
//        189.toByte(),
//        98.toByte(),
//        65.toByte(),
//        195.toByte(),
//        165.toByte(),
//        110.toByte(),
//        125.toByte(),
//        202.toByte(),
//        43.toByte(),
//        117.toByte(),
//        28.toByte(),
//        137.toByte(),
//    )


    var SECRET_KEY = byteArrayOf(
        1.toByte(),
        2.toByte(),
        3.toByte(),
        4.toByte(),
        5.toByte(),
        6.toByte(),
        7.toByte(),
        8.toByte(),
        9.toByte(),
        10.toByte(),
        11.toByte(),
        12.toByte(),
        13.toByte(),
        14.toByte(),
        15.toByte(),
        16.toByte(),
    )





    val gattCallback = object : BluetoothGattCallback() { //public callback so we get our variables
        //this callback is the core of our program honestly

        fun authenticateBand(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, valoareHex: List<String>){ //pentru authenticare

            with(characteristic){
                if (valoareHex[0] == "10" && valoareHex[1] == "01" && valoareHex[2] == "01"){
                    Log.i("din on chcarac changesd", "Da dom'le, ne am legat si acuma trimetem auth number")
                    val authNumber = byteArrayOf(0x02, 0x00, 0x02) //schimbat de la 0x02 0x08 la 0x02 0x00
                    characteristic.setValue(authNumber)
                    gatt.writeCharacteristic(authChar)
                }
                if (valoareHex[0] == "10" && valoareHex[1] == "02" && valoareHex[2] == "01"){
                    Log.i("din on chcarac changesd", "Da dom'le, ne am legat si acuma trimetem ENCRYPTEDKEYACUMA number")
                    var tempKey = valoareHex.takeLast(16) // keia primita
                    Log.i("ult16", "$tempKey\n")


                    //criptare aes cum vrea miband
                    var generatedSecretKey = SecretKeySpec(this@MiBand.SECRET_KEY, "AES")
                    val crypto : Cipher = Cipher.getInstance("AES/ECB/NoPadding")
                    crypto.init(Cipher.ENCRYPT_MODE,generatedSecretKey)

                    var finalKey = crypto.doFinal(value.takeLast(16).toByteArray()) //amperecherea


                    authChar?.setValue(byteArrayOf(0x03, 0x00) + finalKey ) //schimbat de la 0308 la 0300 ptr miband 4/miband 3 postupdate
                    gatt.writeCharacteristic(authChar)


                }
                if (valoareHex[0] == "10" && valoareHex[1] == "03" && valoareHex[2] == "01") {
                    Log.i("if4", "imperecheat succes\n")
                    ESTE_AUTHENTICAT = 1
//                    writeUserSettings() //NU UITA CA E ASTA AICI
                    //subscribeHeartRate()
//                    getSteps()
//                    getBattery()
//                    setDateTime()
//                    getActivityCharacteristic()
                    if (globalIsKnownDevice.isKnown == false){ //initial setup

                        setCaloriesDistanceMetric()
                    }
                    else{
                        ESTE_AUTHENTICAT = 1
                    }
//                    sendShortVibration()
//                    sendCustomMessage()

                }
                Log.i("carac post", "${valoareHex.take(3)}")

                if (valoareHex[0] == "10" && valoareHex[1] == "03" && valoareHex[2] == "04") {
                    Log.i("esec", "la criptare")

                }

                if (valoareHex[0] == "10" && valoareHex[1] == "01" && valoareHex[2] == "04") {
                    Log.i("primit 10 01 04", " bomba")
                    if (globalIsKnownDevice.isKnown == true) {
                        authChar?.value =
                            byteArrayOf(0x02, 0x00, 0x02) //comment this for first time pairing
                    }else{

                        authChar?.setValue(byteArrayOf(0x01, 0x00) + SECRET_KEY) //uncomment this for first time pairing
                    }

                    gatt.writeCharacteristic(authChar)
                }


            }
        }


        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            with(characteristic) {
                Log.i(
                    "bg call",
                    "wrote characteristic $uuid  value: ${value.toHexString()}"
                )
            }
        }

//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            super.onCharacteristicRead(gatt, characteristic, status)
//            with(characteristic) {
//                Log.i(
//                    "bg call",
//                    "read characteristic $uuid  value: ${value.toHexString()}"
//                )
//            }
//        }



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
            if(characteristic.uuid == UUID.fromString("00000007-0000-3512-2118-0009af100700") || characteristic.uuid == UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")) {
                if (characteristic.uuid == UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")) {
                    Log.i(
                        "din on char changed",
                        "${characteristic.value.toHexString().split(" ")[1].toInt(16)}"
                    )
                    this@MiBand.heart_rate =
                        characteristic.value.toHexString().split(" ")[1].toInt(16)
                }

                //mai jos e ptr steps characteristic
                if (characteristic.uuid == UUID.fromString("00000007-0000-3512-2118-0009af100700")) {
                    var byte_arr = characteristic?.value?.toHexString()?.split(" ")
                    var bitul_2 = byte_arr?.get(2)?.toInt(16)
                        ?.shl(8)//il shiftam asa si il adanum cu celalat si aia e
                    var bitul_1 = byte_arr?.get(1)?.toInt(16)
                    var bitul_5 = byte_arr?.get(5)?.toInt(16)
                    var bitul_6 = byte_arr?.get(6)?.toInt(16)
                    var bitul_9 = byte_arr?.get(9)?.toInt(16)
                    var bitul_10 = byte_arr?.get(10)?.toInt(16)
//            var bitul_3 = byte_arr?.get(3)?.toInt(16)
//            var bitul_3 = byte_arr?.get(3)?.toInt(16)
                    var steps_value = bitul_2?.let { bitul_1?.plus(it) }
                    var distance_value = bitul_6?.let { bitul_5?.plus(it) }
                    var calories = bitul_9?.let { bitul_10?.plus(it) }
                    this@MiBand.steps = steps_value
                    this@MiBand.calories = calories?.toFloat()
                    this@MiBand.distance = distance_value?.toFloat()?.div(100)

                }
                saveMeasurements()

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
            val descAuth =
                caracteristicaAuth.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))


            this@MiBand.gatt = referintaGatt
            this@MiBand.authChar = caracteristicaAuth
            //enable our stuff


//            val SEND_KEY =  byteArrayOf(0x01, 0x00) + this@MiBand.SECRET_KEY
            referintaGatt.setCharacteristicNotification(caracteristicaAuth, true) //enable phone to receive notifications
////
            descAuth.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            referintaGatt.writeDescriptor(descAuth) //configure characteristic on device to send notificaitons
//
//
//            caracteristicaAuth?.setValue(SEND_KEY)
//            caracteristicaAuth?.setValue(byteArrayOf(0x02, 0x00, 0x02))


            for (i in 1..2) {

                Handler(Looper.getMainLooper()).postDelayed({
                    caracteristicaAuth?.setValue(
                        byteArrayOf(
                            1.toByte(),
                            0.toByte()
                        )
                    ) //si comanda 01 face ceva
                    referintaGatt.writeCharacteristic(caracteristicaAuth)
                }, 1000)
            }
        }



    }
    fun saveMeasurements(){
        globalDatabase.db.insertMeasurement(current_user.user_id, heart_rate, 0, 0  ,"No pressure on Miband" , steps, distance, calories, current_user.current_device_id,
            SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(Date()).toString() )
    }


    fun sendCustomMessage(){


        //vom incerca o alerta de tipul 5

        val alert_sv_uuid = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")  // Immediate Alert on Mi Band 3
        val notif_sv_uuid = UUID.fromString("00001811-0000-1000-8000-00805F9B34FB")
        val alert_lv_char_uuid = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
        val send_alert_char_uuid = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")  // New Alert on Mi Band 3
        val miband_sv_uuid = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val config_char_uuid = UUID.fromString("00000003-0000-3512-2118-0009af100700")
        val special_uuid = UUID.fromString("00000002-0000-3512-2118-0009af100700")

        val alert_sv = gatt?.getService(notif_sv_uuid)
        val alert_char = alert_sv?.getCharacteristic(send_alert_char_uuid)

        val type = byteArrayOf(0x01) //mesaj type 0x01, tipul 0x02 e apel, merge pe alert sv uuid si alert lvl char uuid //mergea cu tipurile 1 si 5 bine
        val nr_alerts = byteArrayOf(0x01)    //alerta 250 e custom

//        var mesaj = "Fut bine si apasat la cioc".toByteArray()
        val mesaj = //prima linie titlul, in rest, lasa absolut totul asa, vezi care e faza cu 0a
            """
               Received Muie:                
                       /\)
                      / /
                     / /
                  (  Y  ) 
                  
            """.trimIndent()
        var titlu = ""
        var icon = 0.toByte()

        val command = type + nr_alerts + byteArrayOf(0x00) +  titlu.toByteArray() +  byteArrayOf(0x00)+ mesaj.toByteArray() + byteArrayOf(0x00)
//        val command = type + nr_alerts + byteArrayOf(0x00) +   mesaj.toByteArray() + byteArrayOf(0x00)

        Handler(Looper.getMainLooper()).postDelayed({
            alert_char?.setValue(command)
            gatt?.writeCharacteristic(alert_char)
        }, 2000)



    }



    fun sendShortVibration(){
        val alert_sv_uuid = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")  // Immediate Alert on Mi Band 3
        val notif_sv_uuid = UUID.fromString("00001811-0000-1000-8000-00805F9B34FB")
        val alert_lv_char_uuid = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")
        val send_alert_char_uuid = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")  // New Alert on Mi Band 3
        val miband_sv_uuid = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val config_char_uuid = UUID.fromString("00000003-0000-3512-2118-0009af100700")

        val alert_sv = gatt?.getService(alert_sv_uuid)
        val alert_char = alert_sv?.getCharacteristic(alert_lv_char_uuid)

        val short_vibration = intArrayOf(200, 200)
        val vibration = short_vibration[0]
        val pause = short_vibration[1]
        val repeat = 0x02.toByte()
        //val command =  -1.toByte()  + vibration.toByte() and 255 + vibration.toByte() shr 8 and 255 + pause.toByte() and 255 + pause.toByte() shr 8 and 255 + repeat
        var command = byteArrayOf(-1)
        command =  command + (vibration and 255).toByte() + ((vibration shr 8) and 255).toByte() + (pause and 255).toByte() + ((pause shr 8) and 255).toByte() + repeat

        Handler(Looper.getMainLooper()).postDelayed({
            alert_char?.setValue(command)
            gatt?.writeCharacteristic(alert_char)
        }, 1000)


    }

    fun setCaloriesDistanceMetric(){

        val miband_service_uuid = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val miband_config_char_uuid = UUID.fromString ("00000003-0000-3512-2118-0009AF100700")
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //we already know what this is

        val miband_service = gatt?.getService(miband_service_uuid)
        val miband_config_char = miband_service?.getCharacteristic(miband_config_char_uuid)



        val charac_3 = miband_service?.getCharacteristic(UUID.fromString("00000003-0000-3512-2118-0009AF100700"))
        val charac_4 = miband_service?.getCharacteristic(UUID.fromString("00000004-0000-3512-2118-0009AF100700"))
        val charac_5 = miband_service?.getCharacteristic(UUID.fromString("00000005-0000-3512-2118-0009AF100700"))
        val charac_6 = miband_service?.getCharacteristic(UUID.fromString("00000006-0000-3512-2118-0009AF100700"))
        val charac_7 = miband_service?.getCharacteristic(UUID.fromString("00000007-0000-3512-2118-0009AF100700"))
        val desc = miband_config_char?.getDescriptor(cccdUuid)

        val charac_8 = miband_service?.getCharacteristic(UUID.fromString("00000008-0000-3512-2118-0009AF100700"))
        val charac_20 = miband_service?.getCharacteristic(UUID.fromString("00000020-0000-3512-2118-0009AF100700"))


        gatt?.setCharacteristicNotification(miband_config_char, true)
        desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        gatt?.writeDescriptor(desc)


        //set 24 h format
//        var format = byteArrayOf(0x10, 0x41, 0, 0, 1, 2, 3, 4, 5) //41 in loc de 1 dupa for //orcum o scrie doar prima valoare
//        Handler(Looper.getMainLooper()).postDelayed({
//
//            miband_config_char?.value = byteArrayOf(0x0c)
//            gatt?.writeCharacteristic(miband_config_char)
//        }, 2000)
//
//
//        Handler(Looper.getMainLooper()).postDelayed({
//
//            miband_config_char?.value = byteArrayOf(0x11)
//            gatt?.writeCharacteristic(miband_config_char)
//        }, 4000)
//
//
//
//        Handler(Looper.getMainLooper()).postDelayed({
//
//            miband_config_char?.value = byteArrayOf(0x13)
//            gatt?.writeCharacteristic(miband_config_char)


//
//        Handler(Looper.getMainLooper()).postDelayed({
//            //aici practic o sa setam tot ce e nevoie pentru itemele de la more
//            //vezi linkul de mai jos si orice log de wireshark unde se scrie la care
//            //cteristica 03, o valoare care incepe cu 0a
////            miband_config_char?.value = byteArrayOf(0x0a, 255.toByte(), 0x30, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
////            gatt?.writeCharacteristic(miband_config_char)
//            //https://github.com/NightscoutFoundation/xDrip/blob/master/app/src/main/java/com/eveningoutpost/dexdrip/watch/miband/message/DisplayControllMessageMiband3_4.java
//            //si vezi si locul mesaj
//
////            miband_config_char?.value = byteArrayOf(0x0a, 255.toByte(), 0x30, 0x00, 0x05, 0x03, 0x04, 0x07, 0x01, 0x02, 0x06)
////            gatt?.writeCharacteristic(miband_config_char) //gasit in wireshark
//
//
//
//            miband_config_char?.value = byteArrayOf(0x0a, 255.toByte(), 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05)
//            gatt?.writeCharacteristic(miband_config_char) // comanda ptr miband 2
//
//
//        }, 2000)

        /*========================DE AICI INCEPE CODUL GENERAT DE GENERATUDOR=================*/
        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x0c)
            gatt?.writeCharacteristic(charac_3)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x11)
            gatt?.writeCharacteristic(charac_3)
        }, 2125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x13)
            gatt?.writeCharacteristic(charac_3)
        }, 2250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x17, 0x00, 0x65, 0x6e, 0x5f, 0x55, 0x53)
            gatt?.writeCharacteristic(charac_3)
        }, 2375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x11)
            gatt?.writeCharacteristic(charac_3)
        }, 2500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_6?.value = byteArrayOf(0x0f, 0x47, 0x00, 178.toByte(), 0x07, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 178.toByte(), 0x07, 0x01, 0x01, 0x00, 0x2c, 0x1d, 128.toByte(), 0x5c)
            gatt?.writeCharacteristic(charac_6)
        }, 2625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_7?.value = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_7)
        }, 2750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_6?.value = byteArrayOf(0x01, 0x46, 0x00)
            gatt?.writeCharacteristic(charac_6)
        }, 2875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_6?.value = byteArrayOf(0x0f, 0x46, 0x00, 178.toByte(), 0x07, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 178.toByte(), 0x07, 0x01, 0x01, 0x00, 0x2c, 0x1d, 128.toByte(), 0x5c)
            gatt?.writeCharacteristic(charac_6)
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_7?.value = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_7)
        }, 3125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x19, 0x00, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 3250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x01, 0x01, 230.toByte(), 0x07, 0x05, 0x0b, 0x16, 0x22, 0x07, 0x08)
            gatt?.writeCharacteristic(charac_4)
        }, 3375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x03)
            gatt?.writeCharacteristic(charac_4)
        }, 3500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_8?.value = byteArrayOf(0x4f, 0x00, 0x00, 206.toByte(), 0x07, 0x07, 0x01, 0x01, 170.toByte(), 0x00, 224.toByte(), 0x2e, 0x15, 0x77, 206.toByte(), 164.toByte())
            gatt?.writeCharacteristic(charac_8)
        }, 3625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x01, 0x02, 230.toByte(), 0x07, 0x05, 0x0b, 0x16, 0x22, 0x07, 0x08)
            gatt?.writeCharacteristic(charac_4)
        }, 3750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x03, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 3875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x03)
            gatt?.writeCharacteristic(charac_4)
        }, 4000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x08, 0x4f, 0x74, 0x6f, 0x70, 0x65, 0x6e, 0x69, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 4125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x02, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 4250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x04, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 255.toByte(), 255.toByte(), 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 4375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x01, 0x02, 230.toByte(), 0x07, 0x05, 0x0b, 0x16, 0x22, 0x07, 0x08)
            gatt?.writeCharacteristic(charac_4)
        }, 4500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x01, 0x00, 0x01, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 0x05, 0x00, 0x01, 0x19, 0x0b, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 4625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x41, 0x01, 0x00, 0x00, 0x1c, 0x0d, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00, 0x00, 0x03, 0x1e, 0x0e, 0x43, 0x6c, 0x65)
            gatt?.writeCharacteristic(charac_20)
        }, 4750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x0a, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 4875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x03)
            gatt?.writeCharacteristic(charac_4)
        }, 5000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x41, 0x02, 0x61, 0x72, 0x00, 0x03, 0x01, 0x1a, 0x0f, 0x53, 0x68, 0x6f, 0x77, 0x65, 0x72, 0x00, 0x04, 0x00, 0x18)
            gatt?.writeCharacteristic(charac_20)
        }, 5125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 129.toByte(), 0x03, 0x0e, 0x54, 0x68, 0x75, 0x6e, 0x64, 0x65, 0x72, 0x73, 0x74, 0x6f, 0x72, 0x6d, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 5250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x02, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 0x00, 0x18, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 5375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 128.toByte(), 0x07, 0x00, 0x00, 0x00, 0x00, 0x1f, 0x03)
            gatt?.writeCharacteristic(charac_20)
        }, 5500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x10, 0x00, 0x01, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 5625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x01, 0x05, 230.toByte(), 0x07, 0x05, 0x0b, 0x16, 0x21, 0x1d, 0x0c)
            gatt?.writeCharacteristic(charac_4)
        }, 5750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_4?.value = byteArrayOf(0x03)
            gatt?.writeCharacteristic(charac_4)
        }, 5875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x10, 0x00, 0x00, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 6000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x1e, 0x00, 0x4d, 0x4d, 0x2f, 0x64, 0x64, 0x2f, 0x79, 0x79, 0x79, 0x79, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 6125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x08, 0x4f, 0x74, 0x6f, 0x70, 0x65, 0x6e, 0x69, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 6250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x04, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 255.toByte(), 255.toByte(), 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 6375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x01, 0x00, 0x01, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 0x05, 0x00, 0x01, 0x19, 0x0b, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 6500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x41, 0x01, 0x00, 0x00, 0x1c, 0x0d, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00, 0x00, 0x03, 0x1e, 0x0e, 0x43, 0x6c, 0x65)
            gatt?.writeCharacteristic(charac_20)
        }, 6625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 0x41, 0x02, 0x61, 0x72, 0x00, 0x03, 0x01, 0x1a, 0x0f, 0x53, 0x68, 0x6f, 0x77, 0x65, 0x72, 0x00, 0x04, 0x00, 0x18)
            gatt?.writeCharacteristic(charac_20)
        }, 6750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 129.toByte(), 0x03, 0x0e, 0x54, 0x68, 0x75, 0x6e, 0x64, 0x65, 0x72, 0x73, 0x74, 0x6f, 0x72, 0x6d, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 6875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 0x02, 177.toByte(), 0x0f, 0x7c, 0x62, 0x0c, 0x00, 0x18, 0x43, 0x6c, 0x65, 0x61, 0x72, 0x00)
            gatt?.writeCharacteristic(charac_20)
        }, 7000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_20?.value = byteArrayOf(0x00, 193.toByte(), 0x00, 128.toByte(), 0x07, 0x00, 0x00, 0x00, 0x00, 0x1f, 0x03)
            gatt?.writeCharacteristic(charac_20)
        }, 7125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x50)
            gatt?.writeCharacteristic(charac_3)
        }, 7250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x06, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 7375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x00, 0x07, 0x00, 0x1f)
            gatt?.writeCharacteristic(charac_3)
        }, 7500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x01, 0x07, 0x00, 0x7f)
            gatt?.writeCharacteristic(charac_3)
        }, 7625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x02, 0x07, 0x00, 128.toByte())
            gatt?.writeCharacteristic(charac_3)
        }, 7750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x03, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 7875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x04, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x05, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x06, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x07, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x08, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x02, 0x09, 0x12, 0x2c, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x01, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 8750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_8?.value = byteArrayOf(0x4f, 0x00, 0x00, 206.toByte(), 0x07, 0x07, 0x01, 0x01, 170.toByte(), 0x00, 224.toByte(), 0x2e, 0x15, 0x77, 206.toByte(), 164.toByte())
            gatt?.writeCharacteristic(charac_8)
        }, 8875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x08, 0x00, 0x3c, 0x00, 0x08, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 9000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x09, 130.toByte())
            gatt?.writeCharacteristic(charac_3)
        }, 9125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x03, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 9250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x02, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 9375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x0a, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 9500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x10, 0x00, 0x01, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 9625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x10, 0x00, 0x00, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 9750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 9875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x07, 0x00, 155.toByte(), 0x2c, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x0d, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10125)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x0a, 255.toByte(), 0x30, 0x00, 0x05, 0x03, 0x04, 0x07, 0x01, 0x02, 0x06)
            gatt?.writeCharacteristic(charac_3)
        }, 10250)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x16, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10375)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x1e, 0x00, 0x4d, 0x4d, 0x2f, 0x64, 0x64, 0x2f, 0x79, 0x79, 0x79, 0x79, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10500)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x1d, 0x00, 0x01)
            gatt?.writeCharacteristic(charac_3)
        }, 10625)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x1a, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10750)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x1f, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 10875)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_3?.value = byteArrayOf(0x06, 0x20, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_3)
        }, 11000)

        Handler(Looper.getMainLooper()).postDelayed({
            charac_7?.value = byteArrayOf(0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            gatt?.writeCharacteristic(charac_7)
            ESTE_AUTHENTICAT = 1
        }, 11125)

        setDateTime()


        /*===============Aici Se Termina==========================*/




    }



    fun setDateTime(){

        val miband_service_uuid = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val time_characteristic_uuid  = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
//        val time_characteristic_uuid  = UUID.fromString("00000004-0000-3512-2118-0009AF100700")
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //we already know what this is

        val miband_service = gatt?.getService(miband_service_uuid)
        val time_characteristic = miband_service?.getCharacteristic(time_characteristic_uuid)
        val desc_time_char = time_characteristic?.getDescriptor(cccdUuid)


        //setting a random date for testing purposes
        //year is transmitted in little endian, therefore 2022 is not sent as 7e6 but as 6e07
        //we will try to set the year 2023, 1/1
//        var year = byteArrayOf(230.toByte(), 0x07)
//        //e7 written in int.
//        var day = 0x02.toByte()
//        var month = 0x05.toByte()
//        var hours = 0x02.toByte()
//        var minutes = 0x02.toByte()
//        var seconds = 0x02.toByte()
//        var fractions = 0x00.toByte()
//        var adjust_reason = 0x08.toByte()
//        var caracter_terminal = 0x0c.toByte()

//        gatt?.setCharacteristicNotification(time_characteristic, true)
//
//       Handler(Looper.getMainLooper()).postDelayed({
//           desc_time_char?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//           gatt?.writeDescriptor(desc_time_char)
//       }, 750)

        Handler(Looper.getMainLooper()).postDelayed({
            gatt?.readCharacteristic(time_characteristic)
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
//            var numarul_care_trebe_scris = year + month + day + hours + minutes + seconds + fractions + adjust_reason  //+ caracter_terminal
            var numarul_care_trebe_scris = byteArrayOf(226.toByte(), 0x07,0x01,0x1e,0x00,0x00,0x00,0x00,0x00,0x00,0x16)//mergeeeeeeeeee sa mi bag toata pula merge in sfarsit

//            Log.i("curr time", "${time_characteristic?.value?.toHexString()}")
            time_characteristic?.value = numarul_care_trebe_scris
            gatt?.writeCharacteristic(time_characteristic)
        }, 2500)

    }


    fun subscribeHeartRate(){

        //  https://github.com/MalveiraAlexander/Mi-Band-3-SDK/blob/master/MiBand3SDK/Components/HeartRate.cs
        val HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_CHARACTERISTIC  = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_CONTROLPOINT_CHARACTERISTIC = UUID.fromString ("00002a39-0000-1000-8000-00805f9b34fb")
        val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //we already know what this is


        val serviciuHeart = gatt?.getService(HEART_RATE_SERVICE)
        val measHeart = serviciuHeart?.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC)
        val controlHeart = serviciuHeart?.getCharacteristic(HEART_RATE_CONTROLPOINT_CHARACTERISTIC)
        val descMeasHeart = measHeart?.getDescriptor(cccdUuid)



        val sensor_char = serviciuHeart?.getCharacteristic(UUID.fromString("00000001-0000-3512-2118-0009af100700"))



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

//        val manualCmd = byteArrayOf(0x15, 0x02, 0x00 ) //pentru oprit prima e cu 1 la final a doua cu 0
//        controlHeart?.setValue(byteArrayOf(0x15, 0x02, 0x00))
//        gatt?.writeCharacteristic(controlHeart)
//        val continuousCmd = byteArrayOf( 0x15, 0x01, 0x01) //2
//        controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x01))
//        gatt?.writeCharacteristic(controlHeart)

////de aici incepe cod de citire a pulsului remote
//        Handler(Looper.getMainLooper()).postDelayed({
//            controlHeart?.setValue(byteArrayOf(0x15, 0x02, 0x00)) //stop manual
//            gatt?.writeCharacteristic(controlHeart)
//        }, 125)



        Handler(Looper.getMainLooper()).postDelayed({
            controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x00)) //stop continuous
            gatt?.writeCharacteristic(controlHeart)
        }, 250)



        Handler(Looper.getMainLooper()).postDelayed({
            controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x01)) //start continuous
            gatt?.writeCharacteristic(controlHeart)
        }, 500)




//        Handler(Looper.getMainLooper()).postDelayed({
//            controlHeart?.setValue(byteArrayOf(0x15, 0x01, 0x00)) //stop continuous
//            gatt?.writeCharacteristic(controlHeart)
//        }, 625)




//        Log.i("din heart rate", "valoare ${measHeart?.value?.toHexString()?.split(" ")?.get(1)?.toInt(16)}")//bytes primit in int


//
//        controlHeart?.value = HEART_RATE_START_COMMAND
//        gatt?.writeCharacteristic(controlHeart) //folosit sa anuntam ca incepem masuratorile

    }

    fun getBattery() {

        val miband_service_uuid = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val battery_info_characteristic_uuid = UUID.fromString("00000006-0000-3512-2118-0009af100700")
        val miband_service = gatt?.getService(miband_service_uuid)
        val battery_characteristic = miband_service?.getCharacteristic(battery_info_characteristic_uuid)

        val descAuth =
            battery_characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))


        var valoare_citita = gatt?.readCharacteristic(battery_characteristic)
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i("din get battery", "valoarea citita ${valoare_citita}")
            Log.i(
                "din get battery",
                "valoarea battery ${battery_characteristic?.value?.toHexString()}"
            )
            //var byte_arr = battery_characteristic?.value?.toHexString()?.split(" ")?.get(1)?.toInt(16) //asta chiar ia valaorea baterieie
            var byte_arr = battery_characteristic?.value?.toHexString()?.split(" ")
            var charge_value = byte_arr?.get(1)?.toInt(16)
            Log.i("valoare baterie", "${charge_value}")
            this@MiBand.baterie = charge_value
        }, 2000)
        gatt?.setCharacteristicNotification(battery_characteristic, true)
        descAuth?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        gatt?.writeDescriptor(descAuth)

    }

    fun getActivityCharacteristic(){

        val miband_service_uuid  = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val miband_confg_char_uuid = UUID.fromString ("00000005-0000-3512-2118-0009af100700") //caracteristica de user settings
        val miband_service = gatt?.getService(miband_service_uuid)
        val config_char = miband_service?.getCharacteristic(miband_confg_char_uuid)
        Handler(Looper.getMainLooper()).postDelayed({
            var valoare = config_char?.value

            Log.i("valoare char 5", "${valoare?.toHexString()}")
        }, 3000)
    }



    fun writeUserSettings(){
        //cred ca trebuie sa scriem niste user settings pe o characteristica ca sa apara distanta si caloriile

        val miband_service_uuid  = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val user_settings_uuid = UUID.fromString ("00000008-0000-3512-2118-0009af100700") //caracteristica de user settings
        val miband_service = gatt?.getService(miband_service_uuid)
        val user_settings_characteristic = miband_service?.getCharacteristic(user_settings_uuid)

        //https://github.com/MalveiraAlexander/Mi-Band-3-SDK/blob/master/MiBand3SDK/Components/Identity.cs


        //pentru inceput o sa hardcodam niste date
        var set_user_info_command = 79

        var birth_year = 1999
        var birth_month = 11
        var birth_day = 8
        var gender = 0 //1 female 0 male
        var age = 22
        var height = 195 //centimetrii
        var weight = 90  //kg
        var user_id = 1
        //(set_user_info_command, 0, 0)
        var data = byteArrayOf(0x79, 0x00, 0x00)
        data += (birth_year and 255).toByte()
        data += (birth_year.shr(8) and 255).toByte()
        data += birth_month.toByte()
        data += birth_day.toByte()
        data += gender.toByte()
        data += (height.shr(8) and 255).toByte()
        data += ((weight * 200) and 255).toByte()
        data += ((weight * 200).shr(8) and 255).toByte()
        data += (user_id and 255).toByte()
        data += (user_id.shr(8) and 255).toByte()
        data += (user_id.shr(16) and 255).toByte()
        data += (user_id.shr(24) and 255).toByte()
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i("user info to be written", "${data.toHexString()}")
            user_settings_characteristic?.value = data
            gatt?.writeCharacteristic(user_settings_characteristic)
        }, 1000)



    }


    fun setUserInfo(){

    }

    //de dat seama
    fun getSteps(){

        val miband_service_uuid  = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        val steps_info_characteristic_uuid = UUID.fromString ("00000007-0000-3512-2118-0009af100700")
        val miband_service = gatt?.getService(miband_service_uuid)
        val steps_characteristic = miband_service?.getCharacteristic(steps_info_characteristic_uuid)

        val descAuth =
            steps_characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))

        gatt?.readCharacteristic(steps_characteristic)

        Handler(Looper.getMainLooper()).postDelayed({//adding a delay of 1s
            Log.i("din getSteps", "valoarea charactertistici ${steps_characteristic?.value?.toHexString()}")
            var byte_arr = steps_characteristic?.value?.toHexString()?.split(" ")
            var bitul_2 = byte_arr?.get(2)?.toInt(16)
                ?.shl(8)//il shiftam asa si il adanum cu celalat si aia e
            var bitul_1 = byte_arr?.get(1)?.toInt(16)
            var bitul_5 = byte_arr?.get(5)?.toInt(16)
            var bitul_6 = byte_arr?.get(6)?.toInt(16)
            var bitul_9 = byte_arr?.get(9)?.toInt(16)
            var bitul_10 = byte_arr?.get(10)?.toInt(16)
//            var bitul_3 = byte_arr?.get(3)?.toInt(16)
//            var bitul_3 = byte_arr?.get(3)?.toInt(16)
            var steps_value = bitul_2?.let { bitul_1?.plus(it) }
            var distance_value = bitul_6?.let{bitul_5?.plus(it)}
            var calories = bitul_9?.let{bitul_10?.plus(it)}
            this@MiBand.steps = steps_value
            this@MiBand.calories = calories?.toFloat()
            this@MiBand.distance = distance_value?.toFloat()?.div(100)

            //practic hexii nu bitii

            Log.i("valoare steps", "${steps_value}")
            Log.i("valoare distance", "${distance_value}")
            Log.i("valoare calories", "${calories}")
            //aici luam pasii continuu
            gatt?.setCharacteristicNotification(steps_characteristic, true)
            descAuth?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(descAuth)
        }, 1000)


//        Log.i("din get steps", "valoarea steps ${steps_characteristic?.value}")


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