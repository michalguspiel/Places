package com.erdees.places.data.places

import android.location.Location
import com.erdees.places.BuildConfig
import com.erdees.places.domain.places.PlacesRepository
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import timber.log.Timber

class PlacesRepositoryImpl(private val api: PlacesApiClient) : PlacesRepository {

    override suspend fun getPlacesNearby(
        currentLocation: Location
    ): Result<PlacesResponse> {
        Timber.i("Fetching places nearby")
        val response = api.getPlaces(
            clientId = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET,
            ll = "${currentLocation.latitude},${currentLocation.longitude}",
            query = null,
            radius = 10000,
            limit = 50
        )

        return handleApiResponse(response)
    }

    override suspend fun getPlacesByKey(
        currentLocation: Location, key: String
    ): Result<PlacesResponse> {
        val response = api.getPlaces(
            clientId = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET,
            ll = "${currentLocation.latitude},${currentLocation.longitude}",
            query = key,
            radius = 10000,
            limit = 15
        )
        return handleApiResponse(response)
    }

    private suspend fun handleApiResponse(response: HttpResponse): Result<PlacesResponse> {
        Timber.i("Handling response ${response.bodyAsText()}}")
        return try {
            val places = response.body<PlacesResponse>()
            Result.success(places)
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }
    }
}