package pl.jachor.budzik.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import pl.jachor.budzik.LightIntensityPolicy
import pl.jachor.budzik.LightIntensityPolicy.Companion.MILLIS_IN_MINUTE
import pl.jachor.budzik.R
import pl.jachor.budzik.inject.ForApplication
import pl.jachor.budzik.ui.MainActivity
import pl.jachor.budzik.utils.LightClient
import javax.inject.Inject

/**
 * Helper class to create notifications.
 */
class LightServiceNotificationHelper @Inject constructor(@ForApplication private val context: Context) {
    val acknowledgePendingIntent = NotificationIntentService.pendingIntentFor(
            context, NotificationIntentService.ACTION_ACKNOWLEDGE_ALARM)
    val startMainActivityPendingIntent = createMainActivityIntent(context)

    private fun startBuildingNotification(description: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Budzik")
                .setContentText(description)
                .setContentIntent(startMainActivityPendingIntent)
    }

    private fun formatIntensity(intensity: Int): String =
            "%.1f%%".format(intensity.toFloat() / LightClient.MAX_BRIGHTNESS)

    private fun describeResult(result: LightIntensityPolicy.Result) =
            "Set to ${formatIntensity(result.intensity)}"

    fun createInitialNotification(): Notification =
            startBuildingNotification("started").build()

    fun createNotification(result: LightIntensityPolicy.Result): Notification {
        val builder = startBuildingNotification(describeResult(result))
        if (result.continueRunning) {
            builder.addAction(R.drawable.ic_check, "Acknowledge", acknowledgePendingIntent)
        }
        return builder.build()
    }

    companion object {
        fun createMainActivityIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}