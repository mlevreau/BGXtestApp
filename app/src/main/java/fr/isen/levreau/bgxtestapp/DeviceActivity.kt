package fr.isen.levreau.bgxtestapp

import android.app.AlertDialog
import android.bluetooth.*
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.activity_device.device_name
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_ble.view.*
import java.util.*

class DeviceActivity : AppCompatActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG:String = "MyActivity"
    var notifier = false
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        val device: BluetoothDevice = intent.getParcelableExtra("ble_device")
        device_name.text = device.name?: "Unnamed"
        bluetoothGatt = device.connectGatt(this, false, gattCallback)

        disconnect_button.setOnClickListener {
            BluetoothProfile.STATE_DISCONNECTED ;
            device_statut.text = STATE_DISCONNECTED
            bluetoothGatt?.close()
            Log.i(TAG, "Disconnected from GATT server.")

        }


        read_button.setOnClickListener {
            if(!notifier){
                notifier=true
                setCharacteristicNotificationInternal(bluetoothGatt , bluetoothGatt?.services?.get(3)?.characteristics?.get(1), true)
                text_view.append("<")
            }else{
                notifier=false
                setCharacteristicNotificationInternal(bluetoothGatt , bluetoothGatt?.services?.get(3)?.characteristics?.get(1), false)
            }
        }

        send_button.setOnClickListener {
            val dialog = AlertDialog.Builder(this)

            val editView = View.inflate(this, R.layout.dialog_ble, null)

            dialog.setView(editView)
            dialog.setNegativeButton("Annuler", DialogInterface.OnClickListener { dialog, which ->  })
            dialog.setPositiveButton("Valider", DialogInterface.OnClickListener {
                    _, _ ->
                val text = editView.edit_id.text.toString()
                bluetoothGatt?.services?.get(3)?.characteristics?.get(0)?.setValue(text)
                bluetoothGatt?.writeCharacteristic(bluetoothGatt?.services?.get(3)?.characteristics?.get(0))
            })
            dialog.show()
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

            runOnUiThread {
                text_view.append("$value\n")
            }

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
            val value = characteristic.getStringValue(0)
            runOnUiThread {
                text_view.append(">$value\n")
            }
            Log.e(
                "TAG",
                "onCharacteristicWrite: " + value + " UUID " + characteristic.uuid.toString()
            )

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.getStringValue(0)

            runOnUiThread {
                if (value == "\n"){
                    text_view.append("\n<")
                }else {
                    text_view.append("$value")
                }
            }
            Log.e(
                "TAG",
                "onCharacteristicChanged: " + value + " UUID " + characteristic.uuid.toString() + " x : "
            )
        }
    }

    private fun setCharacteristicNotificationInternal(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, enabled: Boolean){
        gatt?.setCharacteristicNotification(characteristic, enabled)

        if (characteristic != null) {
            if (characteristic.descriptors.size  > 0) {

                val descriptors = characteristic.descriptors
                for (descriptor in descriptors) {

                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                        descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    } else if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                        descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    }
                    gatt?.writeDescriptor(descriptor)
                }
            }
        }
    }

    private fun toggleNotificationOnCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        characteristic.descriptors.forEach {
            it.value =
                if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
        }
        gatt.setCharacteristicNotification(characteristic, enable)
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                gatt.readCharacteristic(characteristic)
            }
        }, 0, 1000)
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
        var uuid_test = "466c5002-f593-11e8-8eb2-f2801f1b9fd1"

    }
}
