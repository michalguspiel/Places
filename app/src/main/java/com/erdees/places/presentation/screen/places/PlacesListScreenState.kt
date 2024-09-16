package com.erdees.places.presentation.screen.places

class LocationPermissionMissingError : kotlin.Error("Location permission is missing")

sealed class PlacesListScreenState {
    data object Idle : PlacesListScreenState()
    data object Loading : PlacesListScreenState()
    data class Error(val error: kotlin.Error) : PlacesListScreenState()
}