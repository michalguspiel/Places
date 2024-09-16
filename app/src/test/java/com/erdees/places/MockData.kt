package com.erdees.places

import com.erdees.places.data.places.Location
import com.erdees.places.data.places.PlacesResponse
import com.erdees.places.data.places.Response
import com.erdees.places.data.places.Venue

val mockLocation = Location(
    address = "123 Main St",
    crossStreet = "1st Ave",
    lat = 37.7749,
    lng = -122.4194,
    labeledLatLngs = listOf(),
    distance = 100,
    postalCode = "94103",
    cc = "US",
    city = "San Francisco",
    state = "CA",
    country = "USA",
    formattedAddress = listOf("123 Main St", "San Francisco, CA 94103")
)

val mockVenue = Venue(
    id = "1",
    name = "Mock Venue",
    location = mockLocation,
    categories = listOf(),
    venuePage = null
)

val mockResponse = Response(
    venues = listOf(mockVenue)
)


fun testPlacesResponse(): PlacesResponse = PlacesResponse(
    meta = null,
    response = mockResponse
)