package com.erdees.places.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class PlacesScreen {

    @Serializable
    data object PlacesList : PlacesScreen()
}