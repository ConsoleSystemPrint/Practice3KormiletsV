package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.practice3kormiletsv.R

private enum class AppTab(val route: String, val label: String, val icon: Int) {
    RECIPES("recipes", "Рецепты", R.drawable.ic_recipes),
    FAVORITES("favorites", "Избранное", R.drawable.ic_heart),
    ABOUT("about", "О приложении", R.drawable.ic_info)
}

private val MealsFactory = viewModelFactory {
    initializer { MealsViewModel(createSavedStateHandle()) }
}

@Composable
fun MealsApp(viewModel: MealsViewModel = viewModel(factory = MealsFactory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isDetail = route == "meal/{mealId}"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            if (!isDetail) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.RECIPES.route,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(AppTab.RECIPES.route) {
                MealListScreen(
                    state = state,
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setFilter,
                    filterOptions = viewModel::filterOptions,
                    onReset = viewModel::resetSearch,
                    onMealClick = { navController.navigate("meal/$it") },
                    onFavoriteClick = viewModel::toggleFavorite
                )
            }
            composable(AppTab.FAVORITES.route) {
                FavoritesScreen(
                    state = state,
                    onMealClick = { navController.navigate("meal/$it") },
                    onFavoriteClick = viewModel::toggleFavorite,
                    onBrowse = {
                        navController.navigate(AppTab.RECIPES.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppTab.ABOUT.route) { AboutScreen() }
            composable(
                route = "meal/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { detailEntry ->
                val id = detailEntry.arguments?.getString("mealId").orEmpty()
                MealDetailScreen(
                    meal = viewModel.getMeal(id),
                    isFavorite = id in state.favoriteIds,
                    onBack = { navController.popBackStack() },
                    onFavoriteClick = { viewModel.toggleFavorite(id) }
                )
            }
        }
    }
}
