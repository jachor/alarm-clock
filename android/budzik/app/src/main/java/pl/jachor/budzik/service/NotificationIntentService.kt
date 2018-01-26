package pl.jachor.budzik.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import pl.jachor.budzik.AlarmHelper
import pl.jachor.budzik.inject.ApplicationLevelInjectors
import javax.inject.Inject

/**
 * Handles non-ui actions from notification.
 */
class NotificationIntentService : Service() {
    @Inject
    internal lateinit var alarmHelper: AlarmHelper

    override fun onCreate() {
        ApplicationLevelInjectors.get(this).inject(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ACKNOWLEDGE_ALARM -> {
                alarmHelper.acknowledgeLastAlarm()
                LightService.start(this)
            }
        }

        return START_NOT_STICKY
    }

    companion object {
        const val ACTION_ACKNOWLEDGE_ALARM = "pl.jachor.budzik.ACKNOWLEDGE_ALARM"

        fun pendingIntentFor(context: Context, action: String): PendingIntent =
                PendingIntent.getService(
                        context,
                        0,
                        intentFor(context, action),
                        PendingIntent.FLAG_UPDATE_CURRENT)

        private fun intentFor(context: Context, action: String): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = action
            return intent
        }
    }
}