package com.erdees.places.domain.places

import com.erdees.places.data.places.Venue

object PlaceMapper {
    fun Venue.toPlace(): Place {
        return Place(
            id = id,
            name = name,
            address = location.address,
            distance = location.distance
        )
    }
}