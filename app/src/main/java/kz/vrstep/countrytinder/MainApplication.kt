package kz.vrstep.countrytinder

import android.app.Application
import kz.vrstep.countrytinder.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG) // Use Level.INFO or Level.NONE in release
            androidContext(this@MainApplication)
            modules(appModule) // Your Koin modules
        }
    }
}