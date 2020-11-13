package fr.isen.levreau.bgxtestapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_device_listcell.view.*

class BleAdapter(
    private val scanResults: ArrayList<ScanResult>,
    private val deviceClickListener: (BluetoothDevice) -> Unit
) :

    RecyclerView.Adapter<BleAdapter.DevicesViewHolder>() {

    class DevicesViewHolder(devicesView: View) : RecyclerView.ViewHolder(devicesView){
        val layout = devicesView.device_listcell_layout
        val deviceName: TextView = devicesView.device_name
        val deviceMac: TextView = devicesView.device_mac

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_device_listcell, parent, false)

        return DevicesViewHolder(view)
    }

    override fun getItemCount(): Int = scanResults.size

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.deviceName.text = scanResults[position].device.name?: "Unnamed"
        holder.deviceMac.text = scanResults[position].device.address
        holder.layout.setOnClickListener{
            deviceClickListener.invoke(scanResults[position].device)
        }
    }

    fun addDeviceToList(result: ScanResult){
        val index = scanResults.indexOfFirst { it.device.address == result.device.address }
        if (index != -1) {
            scanResults[index] = result
        }else {
            scanResults.add(result)
        }
    }

    fun clearResults() {
        scanResults.clear()
    }

}