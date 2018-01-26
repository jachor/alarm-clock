package pl.jachor.budzik

import pl.jachor.budzik.utils.LightClient
import pl.jachor.budzik.utils.Settings
import javax.inject.Inject
import kotlin.math.cos

/**
 * Decides what light intensity light should be set to.
 */
class LightIntensityPolicy @Inject constructor(private val settings: Settings, private val alarmHelper: AlarmHelper) {
    class Result(val intensity: Int, val continueRunning : Boolean, val millisToNextAlarm: Long?) {
        override fun toString(): String {
            return "set to ${intensity}" + if(millisToNextAlarm != null) ", next wake up in ${millisToNextAlarm}" else ""
        }

        companion object {
            fun updateSoon(intensity: Int): Result {
                return Result(intensity, true, null)
            }
            fun waitForAlarm(millisToNextAlarm: Long?): Result {
                return Result(0, false, millisToNextAlarm)
            }
            fun doNothing(): Result {
                return Result(0, false, null)
            }
        }
    }

    private fun curvePart(pos: Long, max: Int): Int {
        val floatPos = pos.toFloat() / max
        if (floatPos < 0.0) {
            return LightClient.MAX_BRIGHTNESS
        } else if (floatPos > 1.0) {
            return 0
        } else {
            return (cos(floatPos * Math.PI / 2) * LightClient.MAX_BRIGHTNESS).toInt()
        }
    }

    private fun computeIntensityFor(millisLeft: Long): Int {
        if (millisLeft < 0) {
            return curvePart(-millisLeft, settings.leadInTimeMinutes * MILLIS_IN_MINUTE)
        } else {
            return curvePart(millisLeft, settings.leadOutTimeMinutes * MILLIS_IN_MINUTE)
        }
    }

    fun run() : Result {
        if (!settings.isEnabled) {
            return Result.doNothing()
        }

        val alarmState = alarmHelper.getState()
        val nextAlarmIntensity = if (alarmState.millisToNextAlarm == null) 0 else computeIntensityFor(alarmState.millisToNextAlarm)
        val unacknowledgedAlarmIntensity = computeIntensityFor(alarmState.millisToLastUnacknowledged)
        if (nextAlarmIntensity > 0) {
            alarmState.moveToNextAlarm() // already drop last unacknowledged
            return Result.updateSoon(nextAlarmIntensity)
        } else if (unacknowledgedAlarmIntensity > 0) {
            return Result.updateSoon(unacknowledgedAlarmIntensity)
        } else if (alarmState.millisToNextAlarm != null) {
            return Result.waitForAlarm(alarmState.millisToNextAlarm - settings.leadInTimeMinutes * MILLIS_IN_MINUTE)
        } else {
            return Result.doNothing()
        }
    }

    companion object {
        val MILLIS_IN_MINUTE = 60 * 1000
    }
}