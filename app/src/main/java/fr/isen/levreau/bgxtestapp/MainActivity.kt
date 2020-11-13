package fr.isen.levreau.bgxtestapp

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_device_listcell.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {

        private const val REQUEST_ENABLE_BT = 1
        private const val SCAN_PERIOD: Long = 10000
        private const val PERMISSIONS_REQUEST_LOCALISATION = 100
    }

    private lateinit var handler: Handler
    private var mScanning: Boolean = false
    private lateinit var adapter: BleAdapter


    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isBLEEnable: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLocalisation()

        image_start.setOnClickListener {

            when {
                isBLEEnable -> {
                    initBLEScan()
                }
                bluetoothAdapter != null -> {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    // bleFailedText.text = "nul"
                }
                else -> {
                    bleFailedText.visibility = View.VISIBLE
                }
            }
        }

        image_pause.setOnClickListener {
            pause()
        }
    }

        private fun scanLeDevice(enable: Boolean) {
            bluetoothAdapter?.bluetoothLeScanner?.apply {
            if (enable) {
                Log.w("MainActivity", "Scanning for devices")
                handler.postDelayed({
                    mScanning = false
                    stopScan(leScanCallBack)
                }, SCAN_PERIOD)
                mScanning = true
                startScan(leScanCallBack)
                adapter.clearResults()
                adapter.notifyDataSetChanged()
            } else {
                mScanning = false
                stopScan(leScanCallBack)
            }
        }
    }

    private val leScanCallBack = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.w("BLE", "${result.device}")
            runOnUiThread {
                adapter.addDeviceToList(result)
                adapter.notifyDataSetChanged()
                dividerBle.visibility = View.GONE
            }
        }
    }

    private fun initBLEScan() {

        progressBar.visibility = View.VISIBLE

        dividerBle.visibility = View.INVISIBLE
        image_pause.visibility = View.VISIBLE
        image_start.visibility = View.INVISIBLE
        ble_title.text = "Scan in process ... "

        adapter = BleAdapter(
            scanResults = arrayListOf(),
            deviceClickListener = ::onDeviceClicked
        )
        select_device_recycler.adapter = adapter
        select_device_recycler.layoutManager = LinearLayoutManager(this)
        select_device_recycler.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        handler = Handler()

        scanLeDevice(true)
    }

    fun pause() {
        super.onStop()

        scanLeDevice(false)
        image_pause.visibility = View.INVISIBLE
        image_start.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        dividerBle.visibility = View.VISIBLE
        ble_title.text = "Start scan"

        adapter.clearResults()

    }

   private fun onDeviceClicked(device: BluetoothDevice) {
       val intent = Intent(this,DeviceActivity::class.java)
       intent.putExtra("ble_device", device)
       startActivity(intent)
   }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (isBLEEnable) {
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun permissionLocalisation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        { requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_LOCALISATION)
            //callback onRequestPermissionsResult
        }
    }

}
