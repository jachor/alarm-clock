package pl.jachor.budzik.utils

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import java.io.IOException
import javax.inject.Inject

/**
 * Scans network using mDNS.
 */
class NetworkScanner @Inject constructor(private val nsdManager : NsdManager, private val appExecutors: AppExecutors) {
    private var discoveryListener : NsdManager.DiscoveryListener? = null
    private val knownServices = HashSet<NsdServiceInfo>()
    val knownServicesChanged : MutableSet<() -> Unit> = HashSet()

    fun knownServices() : List<String> {
        return knownServices.map { it.serviceName }.sorted().toList()
    }

    fun startScan() {
        if (discoveryListener != null) {
            return
        }
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                appExecutors.uiExecutor.execute {
                    knownServices.add(serviceInfo)
                    notifyKnownServicesChanged()
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                appExecutors.uiExecutor.execute {
                    knownServices.remove(serviceInfo)
                    notifyKnownServicesChanged()
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {}
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "NsdManager.discoverServices failed: errorCode=${errorCode}")
                discoveryListener = null
            }
            override fun onDiscoveryStarted(serviceType: String?) {}
            override fun onDiscoveryStopped(serviceType: String?) {
                discoveryListener = null
            }
        }
        knownServices.clear()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopScan() {
        if (discoveryListener == null) {
            return
        }
        nsdManager.stopServiceDiscovery(discoveryListener)
        discoveryListener = null
    }

    fun resolve(name : String) : ListenableFuture<NsdServiceInfo> {
        if (name.isEmpty()) {
            return Futures.immediateFailedFuture(Exception("No service name given"))
        }
        val result = SettableFuture.create<NsdServiceInfo>()
        val serviceInfo = NsdServiceInfo()
        serviceInfo.serviceName = name
        serviceInfo.serviceType = SERVICE_TYPE
        val callback = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                result.setException(IOException("service resolution failed, errorCode=${errorCode}"))
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
                result.set(serviceInfo)
            }
        }
        nsdManager.resolveService(serviceInfo, callback)
        return result
    }

    private fun notifyKnownServicesChanged() {
        knownServicesChanged.forEach { it() }
    }

    companion object {
        val TAG = NetworkScanner::class.java.simpleName
        private val SERVICE_TYPE = "_budzik-v1._tcp"
    }
}