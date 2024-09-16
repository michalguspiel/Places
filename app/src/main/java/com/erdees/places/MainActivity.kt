package com.erdees.places

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.erdees.places.domain.location.LocationRepository
import com.erdees.places.domain.permissions.PermissionChecker
import com.erdees.places.domain.permissions.PermissionHandler
import com.erdees.places.presentation.navigation.PlacesNavigation
import com.erdees.places.presentation.theme.PlacesTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val permissionChecker: PermissionChecker by inject()
    private val locationRepository: LocationRepository by inject()
    private val permissionManager = PermissionHandler(this, permissionChecker) { granted ->
        if (granted) {
            locationRepository.startCollectingLocation()
        } else {
            locationRepository.setPermissionState(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        permissionManager.askForLocationPermission()
        setContent {
            PlacesTheme {
                PlacesNavigation()
            }
        }
    }
}