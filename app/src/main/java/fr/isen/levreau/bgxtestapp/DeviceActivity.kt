package fr.isen.levreau.bgxtestapp

import android.bluetooth.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_device.*

class DeviceActivity : AppCompatActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG:String = "MyActivity"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        val device: BluetoothDevice = intent.getParcelableExtra("ble_device")
        device_name.text = device.name?: "Nom inconnu"
        bluetoothGatt = device.connectGatt(this, false, gattCallback)

    }

    private val gattCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : BluetoothGattCallback(){
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ){
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread{
                        device_statut.text = STATE_CONNECTED
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i(TAG, "Connected to GATT server.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        device_statut.text = STATE_DISCONNECTED
                    }
                    Log.i(TAG, "Disconnected from GATT server.")
                }
            }
        }

    }

    private fun byteArrayToHexString(array: ByteArray): String {
        val result = StringBuilder(array.size * 2)
        for ( byte in array ) {
            val toAppend = String.format("%X", byte) // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'
        return result.toString()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        bluetoothGatt?.discoverServices()
    }

    companion object{
        private const val STATE_CONNECTED = "Connected"
        private const val STATE_DISCONNECTED = "Disconnected"
    }

}
