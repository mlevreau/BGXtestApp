package fr.isen.levreau.bgxtestapp

import android.app.ProgressDialog
import android.bluetooth.*
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.activity_device.device_name
import kotlinx.android.synthetic.main.activity_device_listcell.*
import kotlinx.android.synthetic.main.activity_device_listcell.view.*
import java.io.IOException

class DeviceActivity : AppCompatActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG:String = "MyActivity"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

      //  m_address = intent.getStringExtra(device_listcell_layout.device_mac.toString())

      //  ConnectToDevice(this).execute()


        textView.visibility = View.INVISIBLE
        send_button.visibility = View.INVISIBLE
        textView.setBackgroundColor(Color.parseColor("#D3D3D3"))
        send_button.setOnClickListener{
            textView.text = "send"
                sendMessage("send")
        }

        val device: BluetoothDevice = intent.getParcelableExtra("ble_device")
        device_name.text = device.name?: "Unnamed"
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

                        textView.visibility= View.VISIBLE
                        send_button.visibility= View.VISIBLE
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        bluetoothGatt?.discoverServices()
    }

    companion object{
        private const val STATE_CONNECTED = "Connected"
        private const val STATE_DISCONNECTED = "Disconnected"
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    private fun sendMessage (input: String) {
       if (m_bluetoothSocket != null){
           try {
               m_bluetoothSocket!!.outputStream.write(input.toByteArray())
           }catch (e: IOException){
               e.printStackTrace()
           }
       }
    }

  /*  private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){

        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }*/
}
