package com.erdees.places.domain.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import com.erdees.places.MainActivity
import org.koin.core.component.KoinComponent
import timber.log.Timber

class PermissionHandler(
    private val activity: MainActivity,
    private val permissionChecker: PermissionChecker,
    private val onPermissionResult: (granted: Boolean) -> Unit,
) : KoinComponent {

    fun askForLocationPermission() {
        if (permissionChecker.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            onPermissionResult(true)
        }
    }

    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Timber.i("Permission is granted")
                onPermissionResult(true)
            } else {
                Timber.i("Permission is denied")
                onPermissionResult(false)
            }
        }
}
