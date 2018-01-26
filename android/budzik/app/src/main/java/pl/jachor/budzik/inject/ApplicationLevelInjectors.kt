package pl.jachor.budzik.inject

import android.content.Context
import android.support.v4.app.Fragment
import pl.jachor.budzik.service.LightService
import pl.jachor.budzik.service.NotificationIntentService
import pl.jachor.budzik.ui.CurrentStateFragment
import pl.jachor.budzik.ui.MainActivity
import pl.jachor.budzik.ui.SelectDeviceFragment
import pl.jachor.budzik.ui.SettingsFragment

/**
 * Interface providing [inject] method for application scoped components.
 */
interface ApplicationLevelInjectors {
    fun inject(activity: MainActivity)
    fun inject(fragment: SelectDeviceFragment)
    fun inject(service: LightService)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: CurrentStateFragment)
    fun inject(service: NotificationIntentService)

    companion object {
        fun get(context: Context): ApplicationLevelInjectors {
            return (context.applicationContext as Application).component()
        }

        fun get(fragment: Fragment): ApplicationLevelInjectors {
            return get(fragment.context)
        }
    }
}