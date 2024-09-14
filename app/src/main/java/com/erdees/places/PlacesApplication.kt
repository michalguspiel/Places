package com.erdees.places

import android.app.Application
import com.erdees.places.data.location.locationModule
import com.erdees.places.domain.permissions.permissionsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class PlacesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@PlacesApplication)
            modules(
                listOf(locationModule, permissionsModule)
            )
        }
    }
}
