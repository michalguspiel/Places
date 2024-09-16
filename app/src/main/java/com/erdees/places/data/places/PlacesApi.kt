package com.erdees.places.data.places

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber


interface PlacesApiClient {
    suspend fun getPlaces(
        clientId: String,
        clientSecret: String,
        query: String?,
        ll: String?,
        radius: Int?,
        limit: Int?
    ): HttpResponse
}

class PlacesApiClientImpl(private val client: HttpClient) : PlacesApiClient {

    /**
     * Fetches places from the Foursquare API.
     *
     * @param clientId The client ID for authentication.
     * @param clientSecret The client secret for authentication.
     * @param query A string to be matched against all content for this place, including but not limited to venue name, category, telephone number, taste, and tips.
     * @param ll The latitude/longitude around which to retrieve place information. This must be specified as latitude,longitude (e.g., ll=41.8781,-87.6298).
     * @param radius The radius in meters in which to retrieve place information. The maximum supported radius is 100,000 meters.
     * @param limit The number of results to return, up to 50. Defaults to 10.
     * @return The HTTP response from the Foursquare API.
     */
    override suspend fun getPlaces(
        clientId: String,
        clientSecret: String,
        query: String?,
        ll: String?,
        radius: Int?,
        limit: Int?
    ): HttpResponse {
        Timber.i("Fetching places, query: $query, ll: $ll, radius: $radius, limit: $limit")
        val url = "https://api.foursquare.com/v2/venues/search"
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            parameter("client_id", clientId)
            parameter("client_secret", clientSecret)
            parameter("v","20240909")
            query?.let { parameter("query", it) }
            ll?.let { parameter("ll", it) }
            radius?.let { parameter("radius", it) }
            limit?.let { parameter("limit", it) }
        }
        return response
    }
}