package pl.jachor.budzik.service

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * [BroadcastReceiver] for next alarm changes.
 */
class NextAlarmClockChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "Got broadcast: ${intent}")
        when (intent?.action) {
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED ->
                LightService.start(context)
        }
    }

    companion object {
        val TAG = "NextAlarmClockChange"
    }
}
