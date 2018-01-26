package pl.jachor.budzik.service

import android.app.AlarmManager
import android.content.Context
import android.os.Handler
import android.support.v4.app.NotificationManagerCompat
import pl.jachor.budzik.LightIntensityPolicy
import pl.jachor.budzik.inject.ForApplication
import pl.jachor.budzik.utils.LightUpdater
import javax.inject.Inject

/**
 * Mostly logic part of [LightService].
 */
class LightServiceLogic private constructor(factory: Factory, private val service: LightService) {
    private val updateHandler = Handler(factory.context.mainLooper)
    private val sunIntensityCurve = factory.sunIntensityCurve
    private val lightUpdater = factory.lightUpdaterFactory.create("-s", UPDATE_INTERVAL_MS * 3)
    private val alarmManager = factory.alarmManager
    private val notificationManagerCompat = factory.notificationManagerCompat
    private val notificationHelper = factory.notificationHelper

    fun start() {
        service.startForeground(NOTIFICATION_ID, notificationHelper.createInitialNotification())
        update()
    }

    private fun update() {
        val result = sunIntensityCurve.run()

        notificationManagerCompat.notify(
                NOTIFICATION_ID, notificationHelper.createNotification(result))

        lightUpdater.update(result.intensity)
        if (result.continueRunning) {
            updateHandler.postDelayed(this@LightServiceLogic::update, UPDATE_INTERVAL_MS.toLong())
        } else {
            if (result.millisToNextAlarm != null) {
                scheduleNextUpdateIn(result.millisToNextAlarm)
            }
            requestStop()
        }
    }

    private fun scheduleNextUpdateIn(elapsedTimeMillis: Long) {
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedTimeMillis, LightService.createPendingIntent(service))
    }

    private fun requestStop() {
        updateHandler.removeCallbacksAndMessages(null)
        lightUpdater.stopTask()
        service.onLogicRequestedStop()
    }

    class Factory @Inject constructor(
            @ForApplication internal val context: Context,
            internal val sunIntensityCurve: LightIntensityPolicy,
            internal val lightUpdaterFactory: LightUpdater.Factory,
            internal val alarmManager: AlarmManager,
            internal val notificationManagerCompat: NotificationManagerCompat,
            internal val notificationHelper: LightServiceNotificationHelper
    ) {
        fun create(service: LightService): LightServiceLogic {
            return LightServiceLogic(this, service)
        }
    }

    companion object {
        const val UPDATE_INTERVAL_MS = 2000
        const val NOTIFICATION_ID = 1
    }
}