package com.erdees.places.presentation.screen.places

class LocationPermissionMissing : Error("Location permission is missing")
class LocationNotAvailable : Error("Location not available")
class NoPlacesFound : Error("No places found")

sealed class PlacesListScreenState {
    data object Idle : PlacesListScreenState()
    data object Loading : PlacesListScreenState()
    data class Error(val error: kotlin.Error) : PlacesListScreenState()
}