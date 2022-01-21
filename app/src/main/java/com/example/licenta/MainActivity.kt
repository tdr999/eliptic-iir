package com.example.licenta

import android.R.attr.*
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import java.nio.charset.Charset
import java.security.Key
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec


//the punch through ultimate guide to bluetooth was immensely helpful


//end of scan result

class MainActivity : AppCompatActivity() {

    var globalRecievedCharacteristicReference = ""
    var globalCharacteristicReference : BluetoothGattCharacteristic? = null
    var globalGattReference : BluetoothGatt? = null
    var globalDescriptorReference : BluetoothGattDescriptor? = null


    val gattCallback = object : BluetoothGattCallback() { //public callback so we get our variables
        //this callback is the core of our program honestly

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
                this@MainActivity.globalRecievedCharacteristicReference += value.toString(Charsets.UTF_8)//we basically add the chcaracteristic

                var valoareHex = (value.toHexString()).split(" ")
                if (valoareHex[0] == "10" && valoareHex[1] == "01" && valoareHex[2] == "01"){
                    Log.i("din on chcarac changesd", "Da dom'le, ne am legat si acuma trimetem auth number")
                    val authNumber = byteArrayOf(0x02, 0x08)
                    characteristic.setValue(authNumber)
                    globalGattReference?.writeCharacteristic(globalCharacteristicReference)
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
                    var generatedSecretKey = SecretKeySpec(SECRET_KEY, "AES")
                    val crypto : Cipher = Cipher.getInstance("AES/ECB/NoPadding")


                    crypto.init(Cipher.ENCRYPT_MODE,generatedSecretKey)

                    var temp = byteArrayOf()

                    for (i in tempKey){
                        temp = temp + i.toByteArray()
                        Log.i("din  for", "byte array ${i.toByteArray().toHexString()}")
                    }


                    var finalKey = crypto.doFinal(value.takeLast(16).toByteArray()) //amperecherea

                    Log.i("sex", "${finalKey} \n ")
                    Log.i("sex", "${(byteArrayOf(0x03, 0x08) + finalKey).toHexString()}")


                    globalCharacteristicReference?.setValue(byteArrayOf(0x03, 0x08) + finalKey )
                    globalGattReference?.writeCharacteristic(globalCharacteristicReference)


                }
                if (valoareHex[0] == "10" && valoareHex[1] == "03" && valoareHex[2] == "01") {
                    Log.i("if4", "imperecheat succes\n")

                }
                Log.i("carac post", "${valoareHex.take(3)}")

            }

        }


        fun ByteArray.toHexString(): String =
            joinToString(separator = " ", prefix = "") { String.format("%02X", it) }


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


        private fun BluetoothGatt.printGattTable() {
            //this prints gatt service numbers

            if (services.isEmpty()) {
                Log.i("gattTable", "nu merge dom le, e gol tabelu")
                val mesaj =
                    Toast.makeText(this@MainActivity, "Eroare la servicii", Toast.LENGTH_SHORT)
                mesaj.show()
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


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {


            gatt.printGattTable()//aici e clar conectat deja
            val referintaGatt = gatt
            val serviciuDeConectare =
                referintaGatt.getService(UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb"))
            val caracteristicaAuth =
                serviciuDeConectare.getCharacteristic(UUID.fromString("00000009-0000-3512-2118-0009af100700"))

            val descriptorConectare : BluetoothGattDescriptor = caracteristicaAuth.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))


            this@MainActivity.globalGattReference = referintaGatt
            this@MainActivity.globalCharacteristicReference = caracteristicaAuth
            this@MainActivity.globalDescriptorReference = descriptorConectare
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

            val AUTH_REQUEST_RANDOM_AUTH_NUMBER: Byte = 0x02
            val AUTH_BYTE: Byte = 0x8
            val AUTH_SEND_ENCRYPTED_AUTH_NUMBER: Byte = 0x03
            val AUTH_FAIL: Byte = 0x04
            val HEAD = byteArrayOf(0x01, 0x08)
            val HEAD2 = byteArrayOf(0x01, 0x00)
            val SEND_KEY = HEAD + SECRET_KEY



            globalGattReference?.setCharacteristicNotification(globalCharacteristicReference, true)
//            globalDescriptorReference?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//            globalGattReference?.writeDescriptor(globalDescriptorReference) //pornit notificari


            globalCharacteristicReference?.setValue(SEND_KEY)
            globalGattReference?.writeCharacteristic(globalCharacteristicReference)




        }
    }




    private val listaRezultate = mutableListOf<ScanResult>()


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) { //MAIN FUNCTION DONT TOUCH
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200 )
        //this is a horrible way to request location, but it is necessary and it works well

    }


    private val bluetoothAdapter: BluetoothAdapter by lazy{ //defining bt adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    private val bleScanner by lazy { //defining BLE scanner
        bluetoothAdapter.bluetoothLeScanner
    }

    private val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()  //scan settings, which are necessary
    //later when scanning

    val scanCallBack = object : ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = listaRezultate.indexOfFirst {
                it.device.address == result.device.address
            }
            //this "function" is called whenever we find something
            //after finding it, we add it to a list and then to
            //the recycle viewer

            with(result.device){
                Log.i("ScanCallback", "Found Device! Name: ${name?: "Unnamed"}, Adress: $address")
            }

            //hardcode connection to miband

            val miBand = "CE:45:BF:69:5A:7A"
            if (result.device.address.toString() == miBand ){
                stopBleScan()
                result.device.connectGatt(this@MainActivity, false, gattCallback)
                Log.i("scan callback", "conectat la mibadn")
            }



        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        this@MainActivity.disconnect() //ne asiguram ca deconectaa
        super.onResume()
        promptEnableBluetooth()
        promptEnableLocation()
        startBleScan()

    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableBluetooth(){ //prompt for enabling bluetooth
        if(!bluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun promptEnableLocation(){
        //prompt for enabling location which is required for BLE
        //also, I know this gets you into the settings manager
        //but I didnt want to bother with 50 lines of extra code
        //to make it a prompt
        val locManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locManager.isLocationEnabled){
            val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(enableLocationIntent)
        }

    }


    fun stopBleScan(){
        bleScanner.stopScan(scanCallBack)
    }


    fun startBleScan(){
        //if you start scanning, clear previous results
//        listaRezultate.clear() //CURRENTLY MAKE BUG BECAUSE RECYCLE VIEWER RETARDED


        bleScanner.startScan(null, settings, scanCallBack)//call the bleScanner startScan function
        //the bluetooth Callback is crucial from now on


        //bleScanner.stopScan(scanCallBack) //oprim scanarea cand e rosu

    }


    @SuppressLint("NewApi")
    fun disconnect(){
        globalGattReference?.disconnect() //the global gatt reference we got earlier
        if(BluetoothProfile.STATE_DISCONNECTED == 0) {

            var msj = Toast.makeText(this@MainActivity, "Deconectat", Toast.LENGTH_SHORT)
            msj.show() //toast doesnt work if I do it directly, si I did it in two lines
        }
    }




}

