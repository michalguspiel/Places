package com.erdees.places.data.places

import android.location.Location
import com.erdees.places.domain.places.PlacesRepository
import com.erdees.places.util.BuildUtil
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import timber.log.Timber

class PlacesRepositoryImpl(private val api: PlacesApiClient) : PlacesRepository {

    override suspend fun getPlacesNearby(
        currentLocation: Location
    ): Result<PlacesResponse> {
        Timber.i("Fetching places nearby")
        val response = api.getPlaces(
            clientId = BuildUtil.getClientID(),
            clientSecret = BuildUtil.getClientSecret(),
            ll = "${currentLocation.latitude},${currentLocation.longitude}",
            query = null,
            radius = DEFAULT_RADIUS,
            limit = DEFAULT_LIMIT
        )

        return handleApiResponse(response)
    }

    override suspend fun getPlacesByKey(
        currentLocation: Location, key: String
    ): Result<PlacesResponse> {
        Timber.i("Fetching places by key")
        val response = api.getPlaces(
            clientId = BuildUtil.getClientID(),
            clientSecret = BuildUtil.getClientSecret(),
            ll = "${currentLocation.latitude},${currentLocation.longitude}",
            query = key,
            radius = DEFAULT_RADIUS,
            limit = SHORT_LIMIT
        )
        return handleApiResponse(response)
    }

    private suspend fun handleApiResponse(response: HttpResponse): Result<PlacesResponse> {
        return try {
            val places = response.body<PlacesResponse>()
            Result.success(places)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_RADIUS = 10000
        const val DEFAULT_LIMIT = 50
        const val SHORT_LIMIT = 20
    }
}