package pl.jachor.budzik.utils

import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.CancellationException
import javax.inject.Inject

/**
 * Allows to update light state having at most one task running.
 */
class LightUpdater private constructor(private val clientIdSuffix: String, private val validityMs : Int, factory: Factory) {
    private val lightClient = factory.lightClient
    private val settings = factory.settings
    private val appExecutors = factory.appExecutors
    private var pendingBrightnessUpdate: ListenableFuture<Unit>? = null
    private var valueToWriteNext: Int? = null

    private fun startUpdateTask(value: Int): ListenableFuture<Unit> {
        return lightClient.set(settings.deviceName, LightClient.LightWrite(
                value,
                settings.clientId + clientIdSuffix,
                validityMs
        ))
    }

    fun update(value: Int) {
        if (pendingBrightnessUpdate != null) {
            valueToWriteNext = value
            return
        }

        valueToWriteNext = null
        val updateTask = startUpdateTask(value)
        pendingBrightnessUpdate = updateTask
        fun startNewTaskIfNeeded() {
            pendingBrightnessUpdate = null
            val v = valueToWriteNext
            if (v != null) {
                update(v)
            }
        }
        updateTask.listen({
            startNewTaskIfNeeded()
        }, { e ->
            Log.w(TAG, "set brightness task failed", e)
            if (e is CancellationException) {
                // ignore
            } else {
                startNewTaskIfNeeded()
            }
        }, appExecutors.uiExecutor)
    }

    fun stopTask() {
        valueToWriteNext = null
        pendingBrightnessUpdate?.cancel(false)
        pendingBrightnessUpdate = null
    }

    class Factory @Inject constructor(
            internal val settings: Settings,
            internal val appExecutors: AppExecutors,
            internal val lightClient: LightClient
    ) {
        fun create(clientIdSuffix: String, validityMs : Int): LightUpdater {
            return LightUpdater(clientIdSuffix, validityMs, this)
        }
    }

    companion object {
        const val TAG = "LightUpdater"
    }
}
