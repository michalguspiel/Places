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

    private val _searchedPlaces = MutableStateFlow<List<Place>>(emptyList())
    val searchedPlaces: StateFlow<List<Place>> = _searchedPlaces

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

                if (keyword.isEmpty()) {
                    return@combine emptyList<Place>()
                }

                if (!locationPermission) {
                    return@combine emptyList<Place>()
                }

                if (location == null) {
                    _screenState.value =
                        PlacesListScreenState.Error(Error("Location not available"))
                }

                fetchPlacesByKey()
            }.collect {}
        }

        viewModelScope.launch {
            location.collect {
                if (it == null) {
                    _screenState.value = PlacesListScreenState.Error(LocationNotAvailable())
                } else {
                    fetchPlacesNearby()
                }
            }
        }

        viewModelScope.launch {
            locationPermission.collect { state ->
                Timber.i("Location permission state: $state")
                if (!state) {
                    _screenState.value =
                        PlacesListScreenState.Error(LocationPermissionMissing())
                } else {
                    resetScreenState()
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
            handleResult(result)?.let {
                _places.value = it
            }
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
            handleResult(result)?.let {
                _searchedPlaces.value = it
            }
        }
    }

    private fun handleResult(result: Result<PlacesResponse>): List<Place>? {
        if (result.isFailure) {
            _screenState.value =
                PlacesListScreenState.Error(
                    Error(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            return null
        }

        val resultPlaces = result.getOrNull()
        if (resultPlaces == null) {
            _screenState.value = PlacesListScreenState.Error(NoPlacesFound())
            return null
        }

        _screenState.value = PlacesListScreenState.Idle
        return resultPlaces.response.venues
            .map { it.toPlace() }
            .sortedBy { it.distance }
    }
}