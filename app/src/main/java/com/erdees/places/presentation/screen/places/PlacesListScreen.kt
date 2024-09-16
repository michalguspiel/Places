package com.erdees.places.presentation.screen.places


import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erdees.places.domain.places.Place
import com.erdees.places.presentation.composables.FullScreenLoadingOverlay
import timber.log.Timber

@Composable
fun PlacesListScreen() {

    val viewModel: PlacesListViewModel = viewModel()
    val places by viewModel.places.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val keyWord by viewModel.keyword.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
    ) {
        when (screenState) {
            is PlacesListScreenState.Error -> {
                val error = (screenState as PlacesListScreenState.Error).error
                if (error is LocationPermissionMissingError) {
                    GuideToPermissions()
                }
            }

            else -> {
                PlacesListScreenContent(
                    screenState,
                    keyWord,
                    places,
                    Modifier,
                    viewModel::updateKeyWord
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesListScreenContent(
    screenState: PlacesListScreenState,
    keyWord: String,
    places: List<Place>,
    modifier: Modifier = Modifier,
    onKeyWordUpdate: (String) -> Unit
) {
    Timber.i("PlacesListScreenContent")
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = keyWord,
                    onQueryChange = { onKeyWordUpdate(it) },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Find something specific") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(onClick = {
                                expanded = false
                                onKeyWordUpdate("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                places.forEach {
                    PlaceCard(place = it, modifier = Modifier.clickable { expanded = false })
                }
            }
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .semantics { traversalIndex = 1f },
            contentPadding = PaddingValues(
                top = 72.dp, bottom = 16.dp
            ),
        ) {
            items(places.size) { i ->
                val place = places[i]
                PlaceCard(place = place)
            }
        }

        when (screenState) {
            is PlacesListScreenState.Loading -> FullScreenLoadingOverlay()
            else -> {}
        }
    }
}


@Composable
fun GuideToPermissions(modifier: Modifier = Modifier) {
    Timber.i("GuideToPermissions")
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Location permissions are missing", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "App requires location permissions to show nearby places",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = {
                    Timber.i("Click")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
            ) {
                Text(text = "Grant Permissions")
            }
        }
    }
}

@Composable
fun PlaceCard(place: Place, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier,
        headlineContent = { Text(text = place.name ?: "Unidentified") },
        supportingContent = { Text(text = place.address ?: "Unidentified address") },
        trailingContent = { Text(text = place.distance.toString() + "m") },
    )
}