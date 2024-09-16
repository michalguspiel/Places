package com.erdees.places.data.places

import kotlinx.serialization.Serializable

@Serializable
data class PlacesResponse(
    val meta: Meta? = null,
    val response: Response
)

@Serializable
data class Meta(
    val code: Int? = null,
    val requestId: String? = null
)

@Serializable
data class Response(
    val venues: List<Venue>
)

@Serializable
data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val categories: List<Category>? = null,
    val venuePage: VenuePage? = null
)

@Serializable
data class VenuePage(
    val id: String? = null
)

@Serializable
data class Icon(
    val prefix: String? = null,
    val suffix: String? = null
)

@Serializable
data class Category(
    val id: String? = null,
    val name: String? = null,
    val pluralName: String? = null,
    val shortName: String? = null,
    val icon: Icon? = null,
    val primary: Boolean? = null
)

@Serializable
data class LabeledLatLng(
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

@Serializable
data class Location(
    val address: String? = null,
    val crossStreet: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val labeledLatLngs: List<LabeledLatLng>? = null,
    val distance: Int,
    val postalCode: String? = null,
    val cc: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val formattedAddress: List<String>? = null
)