package com.erdees.places.data.places

import android.location.Location
import com.erdees.places.data.places.PlacesRepositoryImpl.Companion.DEFAULT_LIMIT
import com.erdees.places.data.places.PlacesRepositoryImpl.Companion.DEFAULT_RADIUS
import com.erdees.places.data.places.PlacesRepositoryImpl.Companion.SHORT_LIMIT
import com.erdees.places.domain.places.PlacesRepository
import com.erdees.places.testPlacesResponse
import com.erdees.places.util.BuildUtil
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesRepositoryTest {

    private val scheduler: TestCoroutineScheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(scheduler)

    private lateinit var cut: PlacesRepository

    private val placesApiClient: PlacesApiClient = mockk(relaxed = true)

    private val testClientSecret = "mockClientSecret"
    private val testClientId = "mockClientID"
    private val mockLocation: Location = mockk(relaxed = true)
    private val mockLatitude = 37.7749
    private val mockLongitude = -122.4194
    private val httpResponse: HttpResponse = mockk(relaxed = true)


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        cut = PlacesRepositoryImpl(placesApiClient)
        mockkObject(BuildUtil)
        every { BuildUtil.getClientID() } returns testClientId
        every { BuildUtil.getClientSecret() } returns testClientSecret
        every { mockLocation.latitude } returns mockLatitude
        every { mockLocation.longitude } returns mockLongitude

    }

    @Test
    fun `GIVEN getPlacesNearby returns HTTPSResponse OK ,THEN returns Result Success with places`() =
        runTest {
            val placesResponse = testPlacesResponse()
            setupMocks(io.ktor.http.HttpStatusCode.OK, placesResponse)
            testGetPlaces(null, DEFAULT_RADIUS, DEFAULT_LIMIT, true, placesResponse)
        }

    @Test
    fun `GIVEN getPlacesNearby returns HTTPSResponse NotFound ,THEN returns Result Failure`() =
        runTest {
            setupMocks(io.ktor.http.HttpStatusCode.NotFound, null)
            testGetPlaces(null, DEFAULT_RADIUS, DEFAULT_LIMIT, false, null)
        }

    @Test
    fun `GIVEN getPlacesByKey returns HTTPSResponse OK ,THEN returns Result Success with places`() =
        runTest {
            val placesResponse = testPlacesResponse()
            setupMocks(io.ktor.http.HttpStatusCode.OK, placesResponse)
            testGetPlaces("test", DEFAULT_RADIUS, SHORT_LIMIT, true, placesResponse)
        }

    @Test
    fun `GIVEN getPlacesByKey returns HTTPSResponse InternalServerError ,THEN returns Result Failure`() =
        runTest {
            setupMocks(io.ktor.http.HttpStatusCode.InternalServerError)
            testGetPlaces("test", DEFAULT_RADIUS, SHORT_LIMIT, false, null)
        }

    private fun setupMocks(status: io.ktor.http.HttpStatusCode, response: PlacesResponse? = null) {
        every { httpResponse.status } returns status
        response?.let {
            coEvery { httpResponse.body<PlacesResponse>() } returns response
        }
    }

    private suspend fun TestScope.testGetPlaces(
        query: String?,
        radius: Int,
        limit: Int,
        expectedSuccess: Boolean,
        expectedResponse: PlacesResponse?
    ) {
        val locationSlot = slot<String>()
        coEvery {
            placesApiClient.getPlaces(
                clientId = testClientId,
                clientSecret = testClientSecret,
                ll = capture(locationSlot),
                query = query,
                radius = radius,
                limit = limit
            )
        } returns httpResponse

        val result = if (query == null) {
            cut.getPlacesNearby(mockLocation)
        } else {
            cut.getPlacesByKey(mockLocation, query)
        }

        advanceUntilIdle()

        result.isSuccess shouldBe expectedSuccess
        result.getOrNull() shouldBe expectedResponse
        locationSlot.captured shouldBe "$mockLatitude,$mockLongitude"
    }
}