package pl.jachor.budzik.utils

import android.net.Uri
import android.util.Log
import com.google.common.util.concurrent.AsyncFunction
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Communicates with device.
 */
class LightClient @Inject constructor(private val networkScanner: NetworkScanner, private val httpClient: HttpClient, private val appExecutors: AppExecutors) {
    class LightWrite(val value: Int, val clientId: String, val validityMs: Int)

    fun set(name: String, value: LightWrite): ListenableFuture<Unit> =
            retry(3, {setAttempt(name, value)})

    private fun <I> retry(times: Int, attempt: ()->ListenableFuture<I>): ListenableFuture<I> {
        if (times <= 0) {
            return Futures.immediateFailedFuture(Exception("Number of attempts exhausted"))
        } else {
            return Futures.catchingAsync(attempt(), Exception::class.java, AsyncFunction<Exception, I> { e ->
                Log.w(TAG, "Failed, ${times} attempts failed", e);
                attempt();
            }, MoreExecutors.directExecutor())
        }
    }

    private fun setAttempt(name: String, value: LightWrite): ListenableFuture<Unit> {
        val resolvedFuture = networkScanner.resolve(name)
        val baseUriFuture = resolvedFuture.transform { Uri.parse("http://${it.host}:${it.port}/set") }
        val httpGetCompleteFuture = baseUriFuture.transformAsync {
            httpClient.textGet(it.buildUpon()
                    .appendQueryParameter("v", value.value.toString())
                    .appendQueryParameter("client", value.clientId)
                    .appendQueryParameter("slot_ms", value.validityMs.toString())
                    .build())
        }
        return Futures.withTimeout(httpGetCompleteFuture.transform {}, 1, TimeUnit.SECONDS, appExecutors.bgExecutor)
    }

    companion object {
        const val TAG = "LightClient"
        const val MAX_BRIGHTNESS = 1000
    }
}