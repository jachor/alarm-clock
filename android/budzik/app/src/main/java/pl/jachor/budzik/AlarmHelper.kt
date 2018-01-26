package pl.jachor.budzik

import android.app.AlarmManager
import pl.jachor.budzik.utils.Settings
import javax.inject.Inject

/**
 * Created by jachor on 24.01.18.
 */
class AlarmHelper @Inject constructor(val settings: Settings, val alarmManager: AlarmManager) {
    class State(private val nextAlarmMillis : Long?, private val settings: Settings) {
        val millisToNextAlarm = if (nextAlarmMillis==null) null else nextAlarmMillis - System.currentTimeMillis();
        val millisToLastUnacknowledged: Long = settings.lastUnacknowledgedAlarmTimeMillis - System.currentTimeMillis()

        fun moveToNextAlarm() {
            if (nextAlarmMillis != null) {
                settings.lastUnacknowledgedAlarmTimeMillis = nextAlarmMillis
            }
        }
    }

    fun getState(): State {
        return State(alarmManager.nextAlarmClock?.triggerTime, settings)
    }

    fun acknowledgeLastAlarm() {
        settings.lastUnacknowledgedAlarmTimeMillis = 0
    }
}