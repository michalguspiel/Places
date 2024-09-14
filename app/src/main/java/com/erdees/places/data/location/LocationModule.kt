package com.erdees.places.data.location

import android.content.Context
import android.location.LocationManager
import com.erdees.places.domain.location.LocationRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val locationModule = module {
    single<LocationManager> { androidApplication().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    single<LocationRepository> { LocationRepositoryImpl(get(),get()) }
}