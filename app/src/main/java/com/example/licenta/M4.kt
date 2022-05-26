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
            Log.i("written to charac", "${characteristic}, ${characteristic?.value?.toHexString()}")
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
            var bytes_to_write = byteArrayOf(
                223.toByte(),
                0.toByte(),
                13.toByte(),
                54.toByte(),
                3.toByte(),
                16.toByte(),
                0.toByte(),
                0.toByte(),
                8.toByte(),
                100.toByte(),
                100.toByte(),
                55.toByte(),
                53.toByte(),
                101.toByte(),
                49.toByte(),
                51.toByte(),
                50.toByte(),
            )

//            var bytes_to_write = byteArrayOf(
//                253.toByte(),
//                0.toByte(),
//                5.toByte(),
//                20.toByte(),
//                5.toByte(),
//                12.toByte(),
//                0.toByte(),
//                0.toByte(),
//                1.toByte(),
//            )

            caracteristicaComenzi?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
//test

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x0d, 0x36, 0x03, 0x10, 0x00, 0x00, 0x08, 0x64, 0x64, 0x37, 0x35, 0x65, 0x31, 0x33, 0x32)
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

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x09, 0x20, 0x02, 0x10, 0x01, 0x00, 0x04, 0x59, 0x74, 211.toByte(), 129.toByte())
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 2375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x09, 0x64, 0x02, 0x10, 0x04, 0x00, 0x04, 151.toByte(), 0x61, 138.toByte(), 224.toByte())
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

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x17, 0x0f, 0x05, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x02, 0x05, 0x10, 0x06, 0x00, 0x01, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3500)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x05, 250.toByte(), 0x05, 0x10, 0x01, 0x00, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3625)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3750)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0f, 0x05, 0x07, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 3875)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0a, 0x05, 0x02, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x10, 0x05, 0x08, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x16, 0x79, 0x02, 0x10, 0x1d, 0x00, 0x11, 0x07, 230.toByte(), 0x05, 0x1a, 0x00, 0x00, 0x1a, 0x1e, 0x07, 0x4f, 0x74)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4250)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x6f, 0x70, 0x65, 0x6e, 0x69, 0x1b)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x29, 182.toByte(), 0x02, 0x10, 0x12, 0x00, 0x24, 0x0a, 0x00, 0x00, 0x54, 0x75, 0x64, 0x6f, 0x72, 0x20, 0x50, 0x72)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4500)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x65, 0x64, 0x75, 0x6e, 196.toByte(), 131.toByte(), 0x3a, 0x74, 0x75, 0x64, 0x6f, 0x72, 0x72, 0x61, 0x66, 0x6f, 0x6e, 0x75, 0x6c, 0x3a)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4625)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x20, 0x74, 0x65, 0x73, 0x74)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4750)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x2c, 0x57, 0x02, 0x10, 0x12, 0x00, 0x27, 0x0a, 0x00, 0x00, 0x54, 0x75, 0x64, 0x6f, 0x72, 0x20, 0x50, 0x72)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 4875)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x65, 0x64, 0x75, 0x6e, 196.toByte(), 131.toByte(), 0x3a, 0x74, 0x75, 0x64, 0x6f, 0x72, 0x72, 0x61, 0x66, 0x6f, 0x6e, 0x75, 0x6c, 0x3a)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x20, 0x43, 0x65, 0x20, 0x66, 0x61, 0x63, 0x69)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x3f, 0x43, 0x02, 0x10, 0x12, 0x00, 0x3a, 0x0a, 0x00, 0x00, 0x54, 0x75, 0x64, 0x6f, 0x72, 0x20, 0x50, 0x72)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5250)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x65, 0x64, 0x75, 0x6e, 196.toByte(), 131.toByte(), 0x3a, 0x74, 0x75, 0x64, 0x6f, 0x72, 0x72, 0x61, 0x66, 0x6f, 0x6e, 0x75, 0x6c, 0x3a)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x20, 0x56, 0x72, 0x65, 0x61, 0x75, 0x20, 0x73, 0x61, 0x20, 0x76, 0x61, 0x64, 0x20, 0x63, 0x61, 0x72, 0x65, 0x20, 0x65)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5500)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(0x20, 0x74, 0x72, 0x65, 0x61, 0x62, 0x61)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5625)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x01, 0x05, 0x10, 0x06, 0x00, 0x01, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5750)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x02, 0x05, 0x10, 0x06, 0x00, 0x01, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 5875)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x05, 250.toByte(), 0x05, 0x10, 0x01, 0x00, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0f, 0x05, 0x07, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6250)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0a, 0x05, 0x02, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x10, 0x05, 0x08, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6500)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6625)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6750)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 6875)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x14, 0x05, 0x0c, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 7000)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0c, 0x05, 0x04, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 7125)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x16, 0x05, 0x0e, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 7250)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(253.toByte(), 0x00, 0x05, 0x0d, 0x05, 0x05, 0x00, 0x00, 0x01)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 7375)

            Handler(Looper.getMainLooper()).postDelayed({
                caracteristicaComenzi?.value = byteArrayOf(223.toByte(), 0x00, 0x06, 0x01, 0x05, 0x10, 0x06, 0x00, 0x01, 0x00)
                gatt?.writeCharacteristic(caracteristicaComenzi)
            }, 7500)



        }


    }



    fun authenticate(){
        dev.connectGatt(null, false, gattCallBack) //fa tru falseul sa se faca automatt
        Log.i("din auth", "conectat la m4")
    }




}
