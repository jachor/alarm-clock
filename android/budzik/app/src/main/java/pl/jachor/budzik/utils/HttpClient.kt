package pl.jachor.budzik.utils

import android.net.Uri
import com.google.common.io.CharStreams
import com.google.common.util.concurrent.ListenableFuture
import pl.jachor.budzik.utils.AppExecutors
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import javax.inject.Inject

/**
 * HTTP client.
 */
class HttpClient @Inject constructor(val appExecutors: AppExecutors) {
    fun textGet(uri : Uri) : ListenableFuture<String> {
        return appExecutors.bgExecutor.submit(Callable<String> {
            URL(uri.toString()).openStream().reader(Charset.forName("UTF-8")).use(InputStreamReader::readText)
        })
    }
}