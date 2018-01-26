package pl.jachor.budzik.inject

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Component providing application context.
 */
@Module
class ApplicationModule(val context: Context) {

    @Provides
    @ForApplication
    fun applicationContext(): Context {
        return context
    }
}
