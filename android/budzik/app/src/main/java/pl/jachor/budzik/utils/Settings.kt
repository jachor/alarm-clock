package pl.jachor.budzik.utils

import android.content.Context
import android.content.SharedPreferences
import pl.jachor.budzik.inject.ApplicationScope
import pl.jachor.budzik.inject.ForApplication
import java.util.*
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Provides app settings.
 */
@ApplicationScope
class Settings @Inject constructor(@ForApplication context: Context) {
    private val settings = context.getSharedPreferences("my_prefs", 0)

    val onSharedPreferencesChange : MutableSet<() -> Unit> = HashSet()
    private val sharedPrefsCallback = SharedPreferences.OnSharedPreferenceChangeListener {
        _,_ -> onSharedPreferencesChange.forEach { it() }
    }

    init {
        settings.registerOnSharedPreferenceChangeListener(sharedPrefsCallback)
    }

    val clientId : String = getOrCreateClientId(settings)

    var deviceName by StringDelegate("device_name", "")
    var lastUnacknowledgedAlarmTimeMillis by LongDelegate("last_alarm_time_millis", 0L)
    var leadInTimeMinutes by IntDelegate("lead_in_time", 10)
    var leadOutTimeMinutes by IntDelegate("lead_in_time", 10)
    var isEnabled by BooleanDelegate("is_enabled", false)

    private class StringDelegate(val key : String, val defaultValue : String): ReadWriteProperty<Settings, String> {
        override fun getValue(thisRef: Settings, property: KProperty<*>): String {
            return thisRef.settings.getString(key, defaultValue)
        }

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: String) {
            thisRef.settings.edit().putString(key, value).apply()
        }
    }

    private class IntDelegate(val key : String, val defaultValue: Int): ReadWriteProperty<Settings, Int> {
        override fun getValue(thisRef: Settings, property: KProperty<*>): Int {
            return thisRef.settings.getInt(key, defaultValue)
        }

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: Int) {
            thisRef.settings.edit().putInt(key, value).apply()
        }
    }

    private class LongDelegate(val key : String, val defaultValue: Long): ReadWriteProperty<Settings, Long> {
        override fun getValue(thisRef: Settings, property: KProperty<*>): Long {
            return thisRef.settings.getLong(key, defaultValue)
        }

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: Long) {
            thisRef.settings.edit().putLong(key, value).apply()
        }
    }

    private class BooleanDelegate(val key : String, val defaultValue: Boolean): ReadWriteProperty<Settings, Boolean> {
        override fun getValue(thisRef: Settings, property: KProperty<*>): Boolean {
            return thisRef.settings.getBoolean(key, defaultValue)
        }

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: Boolean) {
            thisRef.settings.edit().putBoolean(key, value).apply()
        }
    }

    companion object {
        private fun getOrCreateClientId(settings : SharedPreferences) : String {
            var clientId = settings.getString("client_id", "")
            if (clientId.isEmpty()) {
                clientId = "and-${UUID.randomUUID().hashCode()}" // who cares...
                settings.edit().putString("client_id", clientId).apply()
            }
            return clientId
        }
    }
}
