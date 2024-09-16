package com.erdees.places.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.erdees.places.presentation.screen.places.PlacesListScreen

@Composable
fun PlacesNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = PlacesScreen.PlacesList,
        modifier = modifier
    ) {
        composable<PlacesScreen.PlacesList> {
            PlacesListScreen()
        }
    }
}