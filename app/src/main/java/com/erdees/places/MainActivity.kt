package com.erdees.places

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.erdees.places.domain.location.LocationRepository
import com.erdees.places.domain.permissions.PermissionChecker
import com.erdees.places.domain.permissions.PermissionHandler
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PlacesTheme {
        Greeting("Android")
    }
}
