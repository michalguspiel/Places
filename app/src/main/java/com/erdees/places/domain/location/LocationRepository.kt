package com.erdees.places.domain.location

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val locationPermissionState : StateFlow<Boolean>

    fun setPermissionState(granted: Boolean)

    val location: StateFlow<Location?>

    fun startCollectingLocation()
}