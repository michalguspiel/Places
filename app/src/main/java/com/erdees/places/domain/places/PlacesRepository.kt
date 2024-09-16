package com.erdees.places.domain.places

import android.location.Location
import com.erdees.places.data.places.PlacesResponse

/**
 * Repository interface for fetching places data.
 */
interface PlacesRepository {
    /**
     * Fetches places nearby the given location.
     *
     * @param currentLocation The current location around which to retrieve place information.
     * @return A Result object containing Unit on success or an exception on failure.
     *
     * Note: The API does not provide an offset parameter, therefore there's no pagination.
     * Places are just assigned whenever the get happens.
     */
    suspend fun getPlacesNearby(
        currentLocation: Location
    ): Result<PlacesResponse>

    /**
     * Fetches places by a specific key.
     *
     * @param currentLocation The current location around which to retrieve place information.
     * @param key A string to be matched against all content for this place.
     * @return A Result object containing Unit on success or an exception on failure.
     *
     * Note: The API does not provide an offset parameter, therefore there's no pagination.
     * Places are just assigned whenever the get happens.
     */
    suspend fun getPlacesByKey(
        currentLocation: Location,
        key: String
    ): Result<PlacesResponse>
}