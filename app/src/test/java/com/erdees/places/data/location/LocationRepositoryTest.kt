package com.erdees.places.data.location

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import com.erdees.places.data.location.LocationRepositoryImpl.Companion.MIN_DISTANCE
import com.erdees.places.data.location.LocationRepositoryImpl.Companion.REFRESH_RATE
import com.erdees.places.domain.location.LocationRepository
import com.erdees.places.util.BuildUtil
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryTest {

    private val locationManager: LocationManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var cut: LocationRepository

    @Before
    fun setup() {
        cut = LocationRepositoryImpl(
            locationManager = locationManager,
            context = context
        )
        every { locationManager.getProviders(true) } returns listOf(LocationManager.GPS_PROVIDER)
    }

    @Test
    fun `GIVEN startCollectingLocation was called, THEN permissionState changes to true`() =
        runTest {
            // GIVEN
            cut.startCollectingLocation()
            advanceUntilIdle()
            // THEN
            cut.locationPermissionState.first() shouldBe true
        }

    @Test
    fun `GIVEN SDK version is below 31, WHEN startCollectingLocation is called, THEN getLocation is called`() =
        runTest {
            // GIVEN
            mockkObject(BuildUtil)
            every { BuildUtil.getBuildVersion() } returns 29

            // WHEN
            cut.startCollectingLocation()
            advanceUntilIdle()

            // THEN
            verifyGetLocationApi31(false)
            verifyGetLocation(true)
        }

    @Test
    fun `GIVEN SDK version is above 31, WHEN startCollectingLocation is called, THEN getLocationApi31 is called`() =
        runTest {
            // GIVEN
            mockkObject(BuildUtil)
            every { BuildUtil.getBuildVersion() } returns 32
            mockkConstructor(LocationRequest.Builder::class)

            every { anyConstructed<LocationRequest.Builder>().setMinUpdateDistanceMeters(any()) } returns LocationRequest.Builder(
                REFRESH_RATE
            )
            every { anyConstructed<LocationRequest.Builder>().build() } returns mockk()

            // WHEN
            cut.startCollectingLocation()
            advanceUntilIdle()
            // THEN
            verifyGetLocation(false)
            verifyGetLocationApi31(true)
        }

    private fun verifyGetLocation(wasCalled: Boolean) {
        // getLocation() is private, therefore we can check it through requestLocationUpdates()
        verify(exactly = if (wasCalled) 1 else 0) {
            locationManager.requestLocationUpdates(
                any<String>(),
                REFRESH_RATE,
                MIN_DISTANCE,
                any<LocationListener>()
            )
        }
    }

    private fun verifyGetLocationApi31(wasCalled: Boolean) {
        // getLocationApi31() is private, therefore we can check it through requestLocationUpdates()
        verify(exactly = if (wasCalled) 1 else 0) {
            locationManager.requestLocationUpdates(
                any<String>(),
                any<LocationRequest>(),
                any<Executor>(),
                any<LocationListener>()
            )
        }
    }
}