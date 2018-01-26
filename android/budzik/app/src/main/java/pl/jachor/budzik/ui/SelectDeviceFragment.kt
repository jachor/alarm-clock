package pl.jachor.budzik.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_select_device.*
import pl.jachor.budzik.R
import pl.jachor.budzik.inject.ApplicationLevelInjectors
import pl.jachor.budzik.utils.NetworkScanner
import pl.jachor.budzik.utils.Settings
import javax.inject.Inject

/**
 * Dialog allowing user to choose device from one of detected on local network.
 */
class SelectDeviceFragment : DialogFragment() {
    @Inject
    internal lateinit var networkScanner: NetworkScanner
    @Inject
    internal lateinit var settings: Settings
    private lateinit var adapter: ArrayAdapter<DeviceInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationLevelInjectors.get(this).inject(this)
        super.onCreate(savedInstanceState)
        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_single_choice)
        networkScanner.knownServicesChanged.add(::onKnownServicesChanged)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_device, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        found_device_list.adapter = adapter
        found_device_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedDevice = adapter.getItem(position)!!
            onDeviceSelected(selectedDevice.name)
        }
        found_device_list.emptyView = found_device_list_empty
    }

    override fun onResume() {
        super.onResume()
        networkScanner.startScan()
    }

    override fun onPause() {
        networkScanner.stopScan()
        super.onPause()
    }

    private fun onKnownServicesChanged() {
        val foundServices = networkScanner.knownServices()
        val deviceList = networkScanner.knownServices().map { DeviceInfo(it, true) }.toMutableList()
        val selectedDeviceName = settings.deviceName
        if (!selectedDeviceName.isEmpty() && !foundServices.contains(selectedDeviceName)) {
            deviceList.add(DeviceInfo(selectedDeviceName, false))
        }
        adapter.clear()
        adapter.addAll(deviceList)
        if (!selectedDeviceName.isEmpty()) {
            found_device_list.setItemChecked(deviceList.indexOfFirst { it.name == selectedDeviceName }, true)
        }
    }

    private fun onDeviceSelected(deviceName: String) {
        settings.deviceName = deviceName
        dismiss()
    }

    private class DeviceInfo(val name: String, val found: Boolean) {
        override fun toString(): String {
            return name + (if (found) "" else "(not available now)")
        }
    }
}