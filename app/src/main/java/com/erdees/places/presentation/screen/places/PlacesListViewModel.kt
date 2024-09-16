package com.erdees.places.presentation.screen.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.places.data.places.PlacesResponse
import com.erdees.places.domain.location.LocationRepository
import com.erdees.places.domain.places.Place
import com.erdees.places.domain.places.PlaceMapper.toPlace
import com.erdees.places.domain.places.PlacesRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber


@OptIn(FlowPreview::class)
class PlacesListViewModel : ViewModel(), KoinComponent {

    private val placesRepository: PlacesRepository by inject()
    private val locationRepository: LocationRepository by inject()

    private val locationPermission = locationRepository.locationPermissionState
    val location = locationRepository.location

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    private val _screenState = MutableStateFlow<PlacesListScreenState>(PlacesListScreenState.Idle)
    val screenState: StateFlow<PlacesListScreenState> = _screenState

    private val _keyWord = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyWord

    fun updateKeyWord(name: String) {
        _keyWord.value = name
    }

    private fun resetScreenState() {
        _screenState.value = PlacesListScreenState.Idle
    }

    init {
        viewModelScope.launch {
            combine(
                location,
                keyword.debounce(200).onStart { emit("") },
                locationPermission
            ) { location, keyword, locationPermission ->

                if (!locationPermission) {
                    return@combine emptyList<Place>()
                }

                if (location == null) {
                    _screenState.value =
                        PlacesListScreenState.Error(Error("Location not available"))
                    return@combine emptyList<Place>()
                }
                if (keyword.isNotEmpty()) {
                    fetchPlacesByKey()
                } else {
                    fetchPlacesNearby()
                }
            }.collect {}
        }

        viewModelScope.launch {
            locationPermission.collect { state ->
                Timber.i("Location permission state: $state")
                // If we receive true state when screen state is error, reset it
                if (
                    state &&
                    _screenState.value::class == PlacesListScreenState.Error(
                        LocationPermissionMissing()
                    )::class
                ) {
                    resetScreenState()
                    return@collect
                }
                // If we receive false state then set screen state to error
                if (!state) {
                    _screenState.value =
                        PlacesListScreenState.Error(LocationPermissionMissing())
                    return@collect
                }
            }
        }
    }

    private fun fetchPlacesNearby() {
        _screenState.value = PlacesListScreenState.Loading
        val location = location.value
        if (location == null) {
            _screenState.value = PlacesListScreenState.Error(LocationNotAvailable())
            return
        }
        viewModelScope.launch {
            val result = placesRepository.getPlacesNearby(location)
            handleResult(result)
        }
    }

    private fun fetchPlacesByKey() {
        _screenState.value = PlacesListScreenState.Loading
        val location = location.value
        if (location == null) {
            _screenState.value = PlacesListScreenState.Error(LocationNotAvailable())
            return
        }
        viewModelScope.launch {
            val result = placesRepository.getPlacesByKey(location, keyword.value)
            handleResult(result)
        }
    }

    private fun handleResult(result: Result<PlacesResponse>) {
        if (result.isFailure) {
            _screenState.value =
                PlacesListScreenState.Error(
                    Error(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            return
        }

        val resultPlaces = result.getOrNull()
        if (resultPlaces == null) {
            _screenState.value = PlacesListScreenState.Error(NoPlacesFound())
            return
        }

        _places.value = resultPlaces.response.venues
            .map { it.toPlace() }
            .sortedBy { it.distance }
        _screenState.value = PlacesListScreenState.Idle
    }
}