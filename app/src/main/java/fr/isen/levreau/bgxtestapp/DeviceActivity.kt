package fr.isen.levreau.bgxtestapp

import android.bluetooth.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.activity_device.device_name
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class DeviceActivity : AppCompatActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG:String = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        val device: BluetoothDevice = intent.getParcelableExtra("ble_device")
        device_name.text = device.name?: "Unnamed"
        bluetoothGatt = device.connectGatt(this, false, gattCallback)

        disconnect_button.setOnClickListener {
                BluetoothProfile.STATE_DISCONNECTED ;
                    runOnUiThread {
                        device_statut.text = STATE_DISCONNECTED
                    }
                    Log.i(TAG, "Disconnected from GATT server.")

        }

        read_button.setOnClickListener {
        }

        send_button.setOnClickListener {
        }
    }

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ){
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
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
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val value = characteristic.getStringValue(0)
            Log.e(
                "TAG",
                "onCharacteristicRead: " + value + " UUID " + characteristic.uuid.toString()
            )

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val value = characteristic.value
            Log.e(
                "TAG",
                "onCharacteristicWrite: " + value + " UUID " + characteristic.uuid.toString()
            )

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            val hex = characteristic.value.joinToString("") { byte -> "%02x".format(byte)}.toUpperCase(Locale.FRANCE)
            val value = "Valeur : $hex"

            Log.e(
                "TAG",
                "onCharacteristicChanged: " + value + " UUID " + characteristic.uuid.toString() + " x : "
            )
        }
    }

    override fun onStop() {
        super.onStop()
        bluetoothGatt?.discoverServices()
    }

    companion object{
        private const val STATE_CONNECTED = "Connected"
        private const val STATE_DISCONNECTED = "Disconnected"
        var m_myUUID = "a9da6040-0823-4995-94ec-9ce41ca28833"
        var m_myservice = "331a36f5-2459-45ea-9d95-6142f0c4b307"

    }

}
