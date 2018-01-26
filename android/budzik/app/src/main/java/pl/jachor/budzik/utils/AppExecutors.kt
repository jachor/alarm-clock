package pl.jachor.budzik.utils;

import android.content.Context
import android.os.Handler
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.ThreadFactoryBuilder
import pl.jachor.budzik.inject.ApplicationScope
import pl.jachor.budzik.inject.ForApplication
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

/**
 * Provides UI executor and background thread pool.
 */
@ApplicationScope
class AppExecutors @Inject constructor(@ForApplication context : Context) {
    private val uiHandler = Handler(context.mainLooper)
    private val bgThreadFactory = ThreadFactoryBuilder()
            .setNameFormat("bgthread-%d")
            .build()
    val bgExecutor = MoreExecutors.listeningDecorator(
            Executors.newScheduledThreadPool(4, bgThreadFactory))
    val uiExecutor = Executor { uiHandler.post(it) }
}
