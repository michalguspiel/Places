package com.erdees.places.domain.places

/**
 * Data class representing a Place.
 *
 * @property id Unique identifier for the place.
 * @property name Name of the place.
 * @property address Address of the place.
 * @property distance Distance to the place in meters.
 */
data class Place(
    val id: String,
    val name: String?,
    val address: String?,
    val distance: Int
)