package com.erdees.places.presentation

import com.erdees.places.domain.location.LocationRepository
import com.erdees.places.domain.places.PlacesRepository
import com.erdees.places.presentation.screen.places.LocationNotAvailable
import com.erdees.places.presentation.screen.places.LocationPermissionMissing
import com.erdees.places.presentation.screen.places.PlacesListScreenState
import com.erdees.places.presentation.screen.places.PlacesListViewModel
import com.erdees.places.testPlacesResponse
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesListViewModelTest {

    private val scheduler: TestCoroutineScheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(scheduler)

    private lateinit var cut: PlacesListViewModel

    private val placesRepository: PlacesRepository = mockk()
    private val locationRepository: LocationRepository = mockk()

    private val mockLocation: android.location.Location = mockk(relaxed = true)
    private val mockLatitude = 37.7749
    private val mockLongitude = -122.4194

    private val locationPermission = MutableStateFlow(false)
    private val location: MutableStateFlow<android.location.Location?> = MutableStateFlow(null)

    private val testModule = module {
        single<PlacesRepository> { placesRepository }
        single<LocationRepository> { locationRepository }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        startKoin { modules(testModule) }
        every { locationRepository.locationPermissionState } returns locationPermission
        every { locationRepository.location } returns location
        coEvery { placesRepository.getPlacesNearby(mockLocation) } coAnswers {
            Result.success(testPlacesResponse())
        }
        coEvery { placesRepository.getPlacesByKey(mockLocation, any<String>()) } coAnswers {
            Result.success(testPlacesResponse())
        }
        cut = PlacesListViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `GIVEN updateKeyWord was called, THEN keyWord is updated`() = runTest {
        val testKeyWord = "testKeyWord"
        cut.updateKeyWord(testKeyWord)

        advanceUntilIdle()
        cut.keyword.first() shouldBe testKeyWord
    }

    @Test
    fun `GIVEN locationPermission is false, THEN screenState is Error`() = runTest {
        locationPermission.value = false
        advanceUntilIdle()
        cut.screenState.first()::class shouldBe PlacesListScreenState.Error(
            LocationPermissionMissing()
        )::class
    }

    @Test
    fun `GIVEN locationPermission is false, WHEN turns to true, THEN screenState is Idle`() =
        runTest {
            locationPermission.value = false
            advanceUntilIdle()
            cut.screenState.first()::class shouldBe PlacesListScreenState.Error(
                LocationPermissionMissing()
            )::class
            locationPermission.value = true
            advanceUntilIdle()
            cut.screenState.first()::class shouldBe PlacesListScreenState.Idle::class
        }

    @Test
    fun `GIVEN keyword is null, WHEN location has updated, THEN getPlacesNearby was called`() =
        runTest {
            locationPermission.value = true
            location.value = mockLocation
            advanceUntilIdle()

            coVerify { placesRepository.getPlacesNearby(mockLocation) }
        }

    @Test
    fun `GIVEN keyword is null, WHEN location was initialized with null, THEN getPlacesNearby was not called, and ScreenState is Error`() =
        runTest {
            advanceUntilIdle()

            coVerify(exactly = 0) { placesRepository.getPlacesNearby(mockLocation) }
            cut.screenState.first()::class shouldBe PlacesListScreenState.Error(LocationNotAvailable())::class
        }

    @Test
    fun `GIVEN location is not null, WHEN keyWord gets updated, THEN getPlacesByKey is called`() =
        runTest {
            locationPermission.value = true
            location.value = mockLocation
            cut.updateKeyWord("test")
            advanceUntilIdle()
            coVerify { placesRepository.getPlacesByKey(mockLocation, "test") }
        }

    @Test
    fun `GIVEN location is null, WHEN keyWord gets updated, THEN getPlacesByKey is not called`() =
        runTest {
            locationPermission.value = true
            location.value = null
            cut.updateKeyWord("test")
            advanceUntilIdle()
            coVerify(exactly = 0) { placesRepository.getPlacesByKey(mockLocation, "test") }
            cut.screenState.first()::class shouldBe PlacesListScreenState.Error(LocationNotAvailable())::class
        }

    @Test
    fun `GIVEN Repository returns Failure, THEN screenState is Error`() = runTest {
        coEvery { placesRepository.getPlacesNearby(mockLocation) } coAnswers {
            Result.failure(Exception())
        }
        locationPermission.value = true
        location.value = mockLocation
        advanceUntilIdle()
        cut.screenState.first()::class shouldBe PlacesListScreenState.Error::class
    }
}