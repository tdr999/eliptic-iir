package com.example.licenta
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class M4(device: BluetoothDevice)  {
    var dev = device
    var gatt : BluetoothGatt? = null
    var car_com : BluetoothGattCharacteristic? = null
    var bpm : Int? = 0
    var pressure1 : Int? = 0
    var pressure2 : Int? = 0
    var saturation : Int? = 0
    var steps : Int? = 0
    var calories : Float? = 0.0f
    var distance : Float? = 0.0f
    var flagStep = 0
    var flagBlood = 0
    var flagSaturation = 0
    var flagBPM = 0
    var temp = ""

    val gattCallBack = object : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {

            with(characteristic) {
                Log.i(
                    "BluetoothGattCallback",
                    "Characteristic $uuid changed | value: ${value.toHexString()}"
                )
                var valoare = value.toHexString().chunked(3) //split the string in chunks of 2 //also de ce mortii lui face o lista de liste, cine se crede
                Log.i("lungimea valoare", "${valoare.size}")
                if ( valoare.size == 20) {
                    if (valoare[6] == "04 ") {
                        flagBPM = 1
                        Log.i("received", "BPM measurement")
                    }

                    if (valoare[6] == "05 ") {
                        flagBlood = 1
                        Log.i("received", "Blood Pressure measurement")
                        pressure2 = valoare[19].trim().toInt(16)
                    }


                    if (valoare[6] == "0E ") {
                        flagSaturation = 1
                        Log.i("received", "Saturation measurement")
                    }


                    if (valoare[6] == "0C ") {
                        flagStep = 1
                        temp = valoare[17].trim() + valoare[18].trim() + valoare[19].trim()
                        distance = (valoare[13].trim() + valoare[14].trim() + valoare[15].trim() + valoare[16].trim()).toInt(16).toFloat() / 1000
                        steps = (valoare[9].trim() + valoare[10].trim() + valoare[11].trim() + valoare[12].trim()).toInt(16)
                        Log.i("received", "Steps & Distance & Calories measurement")
                    }

                }
                if (valoare.size == 1){
                    if (flagBPM == 1){
                        flagBPM = 0
                        bpm = valoare[0].toInt(16)
                        Log.i("valoare BPM", "${bpm}")
                    }

                    if (flagSaturation == 1){
                        flagSaturation = 0
                        saturation = valoare[0].trim().toInt(16)
                    }

                    if (flagBlood == 1){
                        flagBlood = 0
                        pressure1 = valoare[0].trim().toInt(16)
                    }

                    if (flagStep == 1){
                        temp = temp + valoare[0].trim()
                        calories = temp.toInt(16).toFloat() / 1000

                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        saveMeasurements()
                    },125) //add a small delay for all parameters to update

                }

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

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            //super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i("written to charac", "${characteristic?.uuid}, ${characteristic?.value?.toHexString()}")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            this@M4.gatt = gatt
            val serviciuDeConectare =
                gatt?.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9f"))
            val caracteristicaNotificari = serviciuDeConectare?.getCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9f"))
            val caracteristicaComenzi = serviciuDeConectare?.getCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9f"))
            this@M4.car_com = caracteristicaComenzi
            //comenzile le trimite pe 02 si primeste pe 03
            if (caracteristicaNotificari == null){
                Log.i("car temop", "e nula coaie \n\n")
            }
            //gatt?.setCharacteristicNotification(caracteristicaTemp, true)
            val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //tre sa scriem la descriptor ptr ca sa subscribe
            val desc = caracteristicaNotificari?.getDescriptor(cccdUuid)


            gatt?.requestMtu(512)


            Handler(Looper.getMainLooper()).postDelayed({
                gatt?.setCharacteristicNotification(caracteristicaNotificari, true)
                desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gatt?.writeDescriptor(desc) //setam notificaari si tot
            }, 1200)

            caracteristicaComenzi?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x0d, 214.toByte(), 0x03, 0x10, 0x00, 0x00, 0x08, 0x61, 0x37, 0x31, 0x33, 0x38, 0x33, 0x38, 0x30)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x17, 0x0c, 0x08, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x04, 0x02, 0x10, 0x0a, 0x00, 0x01, 0x02)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2250)
//
//            Handler(Looper.getMainLooper()).postDelayed({                                               //ora?
//                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x09, 153.toByte(), 0x02, 0x10, 0x01, 0x00, 0x04, 0x59, 0x77, 0x05, 197.toByte())
//                gatt?.writeCharacteristic(caracteristicaComenzi)
//            }, 2375) //dupa multe teste, am ajuns la conculzia ca aceasta linie schimba  macar ora , dar nu stim cum asa ca vom seta cu aplicati ainitiala

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x09, 165.toByte(), 0x02, 0x10, 0x04, 0x00, 0x04, 151.toByte(), 0x61, 139.toByte(), 0x20)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2500)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x09, 156.toByte(), 0x02, 0x10, 0x03, 0x00, 0x04, 0x00, 0x00, 0x13, 136.toByte())
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2625)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x05, 253.toByte(), 0x09, 0x10, 0x00, 0x00, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2750)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x15, 0x09, 0x09, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2875)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x05, 0x04, 0x0f, 0x10, 0x01, 0x00, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x1b, 0x0f, 0x09, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x05, 0x07, 0x0f, 0x10, 0x04, 0x00, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3250) //toate comenzile astea sunt standarde de configuratie

//            sendMessage()



        }




    }

    fun getSteps(){

        Handler(Looper.getMainLooper()).postDelayed({
            car_com?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x17, 0x0f, 0x05, 0x00, 0x00, 0x01)
            gatt?.writeCharacteristic(car_com)
        }, 125)

        Handler(Looper.getMainLooper()).postDelayed({
            car_com?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x02, 0x05, 0x10, 0x06, 0x00, 0x01, 0x01)
            gatt?.writeCharacteristic(car_com)
        }, 175)  //aceasta cmanda si cea de deasupra banuiesc ca sunt ce trebe
    }

    fun getHeart(){


        var bytes_to_write_hrt = byteArrayOf( //comanda ptr masurare puls, log hrt tarziu
            223.toByte(),
            0.toByte(),
            6.toByte(),
            6.toByte(),
            2.toByte(),
            16.toByte(),
            13.toByte(),
            0.toByte(),
            1.toByte(),
            1.toByte(),
        )


        Handler(Looper.getMainLooper()).postDelayed({
            car_com?.value = bytes_to_write_hrt
            gatt?.writeCharacteristic(car_com)
        }, 125)

    }


    fun sendMessage(){

        var serviciu_mesaj = gatt?.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9f"))
        var caracter_mesaj = serviciu_mesaj?.getCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9f"))
        var mesaj = "acesta este un mesaj de test si atata"
        var mesaj_bytes =  mesaj.toByteArray(Charset.defaultCharset())
        var bytes_to_write = byteArrayOf(
            223.toByte(),
            0.toByte(),
//            44.toByte(),
//            87.toByte(),
//            2.toByte(),
//            16.toByte(),
//            18.toByte(),
//            0.toByte(),
//            8.toByte(), //byte lungime?
//            10.toByte(),
//            0.toByte(),
//            0.toByte(),
//            84.toByte(),
//            117.toByte(),
//            100.toByte(),
//            111.toByte(),
//            114.toByte(),
//            32.toByte(),
//            80.toByte(),
//            114.toByte(),
        )
        bytes_to_write = bytes_to_write

        Handler(Looper.getMainLooper()).postDelayed(
            {
                caracter_mesaj?.setValue(bytes_to_write)
                gatt?.writeCharacteristic(caracter_mesaj)

            }, 4500)



        Handler(Looper.getMainLooper()).postDelayed(
            {
                caracter_mesaj?.setValue(mesaj_bytes)
                gatt?.writeCharacteristic(caracter_mesaj)

            }, 5000)

    }

    fun getBlood(){


        var bytes_to_write = byteArrayOf( //comanda tensiune ultimii 2 bytes
            223.toByte(),
            0.toByte(),
            6.toByte(),
            7.toByte(),
            2.toByte(),
            16.toByte(),
            14.toByte(),
            0.toByte(),
            1.toByte(),
            1.toByte(),
        )


        Handler(Looper.getMainLooper()).postDelayed({
            car_com?.value = bytes_to_write
            gatt?.writeCharacteristic(car_com)
        }, 125)

    }

    fun getSaturation(){


        var bytes_to_write = byteArrayOf( // comanda saturatie
            223.toByte(),
            0.toByte(),
            6.toByte(),
            21.toByte(),
            2.toByte(),
            16.toByte(),
            28.toByte(),
            0.toByte(),
            1.toByte(),
            1.toByte(),
        )

        Handler(Looper.getMainLooper()).postDelayed({
            car_com?.value = bytes_to_write
            gatt?.writeCharacteristic(car_com)
        }, 125)

    }

    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la m4")
    }

    fun saveMeasurements(){
        globalDatabase.db.insertMeasurement(current_user.user_id, bpm, saturation  , (pressure2.toString() + "/" + pressure1.toString()), steps, distance, calories, current_user.current_device_id,
            SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(Date()).toString() )
    }




}
