package com.example.licenta
import android.bluetooth.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

class M4(device: BluetoothDevice)  {
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
                    "Characteristic $uuid changed | value: ${value.toHexString()}"
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
//            var bytes_to_write = byteArrayOf(
//                223.toByte(),
//                0.toByte(),
//                13.toByte(),
//                54.toByte(),
//                3.toByte(),
//                16.toByte(),
//                0.toByte(),
//                0.toByte(),
//                8.toByte(),
//                100.toByte(),
//                100.toByte(),
//                55.toByte(),
//                53.toByte(),
//                101.toByte(),
//                49.toByte(),
//                51.toByte(),
//                50.toByte(),
//            )

            var bytes_to_write = byteArrayOf(
                253.toByte(),
                0.toByte(),
                5.toByte(),
                20.toByte(),
                5.toByte(),
                12.toByte(),
                0.toByte(),
                0.toByte(),
                1.toByte(),
            )

            caracteristicaComenzi?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
//test

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
            }, 3250)

//            Handler(Looper.getMainLooper()).postDelayed({
//                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x17, 0x0f, 0x05, 0x00, 0x00, 0x01)
//                gatt?.writeCharacteristic(caracteristicaComenzi)
//            }, 3375)
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x02, 0x05, 0x10, 0x06, 0x00, 0x01, 0x01)
//                gatt?.writeCharacteristic(caracteristicaComenzi)
//            }, 3500)  //aceasta cmanda si cea de deasupra banuiesc ca sunt ce trebe

            //teste heart rate


//            var bytes_to_write_hrt = byteArrayOf( //comanda ptr masurare puls, log hrt tarziu
//                223.toByte(),
//                0.toByte(),
//                6.toByte(),
//                6.toByte(),
//                2.toByte(),
//                16.toByte(),
//                13.toByte(),
//                0.toByte(),
//                1.toByte(),
//                1.toByte(),
//            )
//            var bytes_to_write_hrt = byteArrayOf( //comanda tensiune ultimii 2 bytes
//                223.toByte(),
//                0.toByte(),
//                6.toByte(),
//                7.toByte(),
//                2.toByte(),
//                16.toByte(),
//                14.toByte(),
//                0.toByte(),
//                1.toByte(),
//                1.toByte(),
//            )

            var bytes_to_write_hrt = byteArrayOf( // comanda saturatie
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
                caracteristicaComenzi?.value = bytes_to_write_hrt
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3625)

        }


    }


    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la m4")
    }




}
