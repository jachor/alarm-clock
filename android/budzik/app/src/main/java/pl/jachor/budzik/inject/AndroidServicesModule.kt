package pl.jachor.budzik.inject

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.net.nsd.NsdManager
import android.support.v4.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides

/**
 * [Module] providing core Android services.
 */
@Module
@ApplicationScope
class AndroidServicesModule {
    @Provides
    fun nsdManager(@ForApplication context: Context): NsdManager {
        return context.getSystemService(Context.NSD_SERVICE) as NsdManager;
    }

    @Provides
    fun alarmManager(@ForApplication context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    fun notificationManager(@ForApplication context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun notificationManagerCompat(@ForApplication context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }
}