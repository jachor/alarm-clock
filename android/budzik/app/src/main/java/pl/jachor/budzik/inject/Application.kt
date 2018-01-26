package pl.jachor.budzik.inject

import pl.jachor.budzik.inject.ApplicationComponent
import pl.jachor.budzik.inject.ApplicationModule
import pl.jachor.budzik.inject.DaggerApplicationComponent

/**
 * Custom application class providing [ApplicationComponent].
 */
class Application : android.app.Application() {
    private var component: ApplicationComponent? = null

    override fun onCreate() {
        super.onCreate()
        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    fun component() : ApplicationComponent {
        return component!!
    }
}