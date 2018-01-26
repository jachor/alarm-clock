package pl.jachor.budzik.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import pl.jachor.budzik.inject.ApplicationLevelInjectors
import javax.inject.Inject

/**
 * Service running periodic light state updates.
 */
class LightService : Service() {
    @Inject
    internal lateinit var logicFactory: LightServiceLogic.Factory

    var logic: LightServiceLogic? = null
    var lastStartId = 0

    override fun onCreate() {
        super.onCreate()
        ApplicationLevelInjectors.get(this).inject(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId

        if (logic == null) {
            logic = logicFactory.create(this)
            logic?.start()
        }
        return Service.START_STICKY
    }

    fun onLogicRequestedStop() {
        stopSelfResult(lastStartId)
        logic = null
    }

    companion object {
        private fun getIntent(context: Context): Intent {
            return Intent(context, LightService::class.java)
        }

        fun start(context: Context) {
            context.startService(getIntent(context))
        }

        fun createPendingIntent(context: Context): PendingIntent {
            return PendingIntent.getService(context, 1, getIntent(context), PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
