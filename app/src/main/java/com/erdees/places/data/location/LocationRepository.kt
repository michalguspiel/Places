package com.erdees.places.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.erdees.places.domain.location.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import timber.log.Timber

class LocationRepositoryImpl(
    private val locationManager: LocationManager,
    private val context: Context
) : LocationRepository,
    KoinComponent {

    private val _locationPermissionState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val locationPermissionState: StateFlow<Boolean> = _locationPermissionState

    private val _location: MutableStateFlow<Location?> = MutableStateFlow(null)
    override val location: StateFlow<Location?> = _location


    override fun setPermissionState(granted: Boolean) {
        _locationPermissionState.value = granted
    }

    override fun startCollectingLocation() {
        setPermissionState(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getLocationApi31()
        } else {
            getLocation()
        }
    }

    /**
     * Overriding of onStatusChanged, onProviderEnabled, and onProviderDisabled
     * functions is very important, without overriding those and removing their .super
     * calls the app crashes instantly. More explanation [here](https://stackoverflow.com/a/67900719/19381734)
     * */
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Timber.i("onLocationChanged $location")
            _location.value = location
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("Timber.i(\"onStatusChanged\")", "timber.log.Timber")
        )
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Timber.i("onStatusChanged")
        }

        override fun onProviderEnabled(provider: String) {
            Timber.i("onProviderEnabled $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Timber.i("onProviderDisabled")
        }
    }

    /**
     * Request updates using [locationListener].
     *
     * Suppresses Missing Permission lint since permissions are already checked.
     *
     * */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val providers = locationManager.getProviders(true)
        val locationProvider: String = when {
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            providers.contains(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> return
        }
        locationManager.requestLocationUpdates(
            locationProvider,
            REFRESH_RATE,
            MIN_DISTANCE,
            locationListener,
        )
    }

    /**
     * Request updates using [locationListener].
     *
     * Used only in devices with API>=31
     *
     * Suppresses Missing Permission lint since permissions are already checked.
     * * */
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun getLocationApi31() {
        val request = LocationRequest.Builder(REFRESH_RATE)
            .setMinUpdateDistanceMeters(MIN_DISTANCE)
            .build()
        val providers = locationManager.getProviders(true)
        val locationProvider: String = when {
            providers.contains(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            providers.contains(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> return
        }
        locationManager.requestLocationUpdates(
            locationProvider,
            request,
            context.mainExecutor,
            locationListener,
        )
    }

    companion object {
        const val REFRESH_RATE = 2 * 1000L * 60 // 2 minutes
        const val MIN_DISTANCE = 250F
    }
}