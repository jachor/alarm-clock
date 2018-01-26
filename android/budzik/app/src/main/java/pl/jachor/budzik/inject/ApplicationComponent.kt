package pl.jachor.budzik.inject

import dagger.Component

/**
 * Component having lifecycle of Android application.
 */
@ApplicationScope
@Component(modules = arrayOf(ApplicationModule::class, AndroidServicesModule::class))
abstract class ApplicationComponent : ApplicationLevelInjectors
