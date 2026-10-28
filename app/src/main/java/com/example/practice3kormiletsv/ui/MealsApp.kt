package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import com.example.practice3kormiletsv.ui.profile.ProfileViewModel
import com.example.practice3kormiletsv.ui.profile.ProfileScreen
import com.example.practice3kormiletsv.ui.profile.EditProfileViewModel
import com.example.practice3kormiletsv.ui.profile.EditProfileRoute
import com.example.practice3kormiletsv.ui.profile.PdfViewerViewModel
import com.example.practice3kormiletsv.ui.profile.PdfViewerScreen
import androidx.compose.ui.platform.LocalContext
import com.example.practice3kormiletsv.MealsApplication
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
    PROFILE("profile", "Профиль", R.drawable.ic_profile),
    ABOUT("about", "О приложении", R.drawable.ic_info)
}

@Composable
fun MealsApp() {
    val context = LocalContext.current
    val container = (context.applicationContext as MealsApplication).container
    val factory = remember(container) {
        viewModelFactory {
            initializer {
                MealsViewModel(container.getMeals, container.observeSearchSettings, container.observeFavorites,
                    container.addFavorite, container.removeFavorite, container.settingsBadgeCache)
            }
        }
    }
    val viewModel: MealsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBottomBar = route in AppTab.entries.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
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
                    onSettingsClick = { navController.navigate("settings") { launchSingleTop = true } },
                    onRefresh = viewModel::refresh,
                    onMealClick = { navController.navigate("meal/$it") },
                    onFavoriteClick = viewModel::toggleFavorite
                )
            }
            composable(AppTab.FAVORITES.route) {
                FavoritesScreen(
                    state = state,
                    onMealClick = { navController.navigate("meal/$it") },
                    onFavoriteClick = viewModel::toggleFavorite,
                    onRetry = viewModel::retryFavorites,
                    onBrowse = {
                        navController.navigate(AppTab.RECIPES.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppTab.PROFILE.route) { profileEntry ->
                val profileFactory = remember(container) {
                    viewModelFactory { initializer { ProfileViewModel(container.observeProfile, container.downloadResume) } }
                }
                val profileViewModel: ProfileViewModel = viewModel(viewModelStoreOwner = profileEntry, factory = profileFactory)
                val profileState by profileViewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(profileViewModel) {
                    profileViewModel.documents.collect { document ->
                        val uri = Uri.parse(document.uri)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, document.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            clipData = ClipData.newRawUri(document.name, uri)
                        }
                        try { context.startActivity(intent) }
                        catch (_: ActivityNotFoundException) {
                            if (document.mimeType == "application/pdf") {
                                navController.navigate("profile/document?documentUri=${Uri.encode(document.uri)}&documentName=${Uri.encode(document.name)}")
                            } else snackbarHostState.showSnackbar("На устройстве нет просмотрщика для этого типа файла. Попробуйте ссылку на PDF.")
                        }
                        catch (_: SecurityException) {
                            snackbarHostState.showSnackbar("Не удалось открыть скачанный документ в просмотрщике.")
                        }
                    }
                }
                ProfileScreen(profileState, onEdit = { navController.navigate("profile/edit") { launchSingleTop = true } }, onResume = profileViewModel::openResume, onRetry = profileViewModel::load)
            }
            composable("profile/edit") { profileEntry ->
                val editFactory = remember(container) {
                    viewModelFactory {
                        initializer {
                            EditProfileViewModel(createSavedStateHandle(), container.observeProfile, container.saveProfile,
                                container.importAvatar, container.createCameraCapture, container.deleteDraftPhoto)
                        }
                    }
                }
                val editViewModel: EditProfileViewModel = viewModel(viewModelStoreOwner = profileEntry, factory = editFactory)
                EditProfileRoute(editViewModel, onExit = { navController.popBackStack() }, onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } })
            }
            composable(
                "profile/document?documentUri={documentUri}&documentName={documentName}",
                arguments = listOf(navArgument("documentUri") { type = NavType.StringType }, navArgument("documentName") { type = NavType.StringType })
            ) { documentEntry ->
                val pdfFactory = remember(context.applicationContext) {
                    viewModelFactory { initializer { PdfViewerViewModel(context.applicationContext as MealsApplication, createSavedStateHandle()) } }
                }
                val pdfViewModel: PdfViewerViewModel = viewModel(viewModelStoreOwner = documentEntry, factory = pdfFactory)
                val pdfState by pdfViewModel.state.collectAsStateWithLifecycle()
                PdfViewerScreen(pdfState, onBack = { navController.popBackStack() }, onRetry = pdfViewModel::retry, onPrevious = pdfViewModel::previous, onNext = pdfViewModel::next)
            }
            composable(AppTab.ABOUT.route) { AboutScreen() }
            composable("settings") { settingsEntry ->
                val settingsFactory = remember(container) {
                    viewModelFactory {
                        initializer {
                            SearchSettingsViewModel(createSavedStateHandle(), container.observeSearchSettings,
                                container.saveSearchSettings, container.getFilterOptions, container.settingsBadgeCache)
                        }
                    }
                }
                val settingsViewModel: SearchSettingsViewModel = viewModel(viewModelStoreOwner = settingsEntry, factory = settingsFactory)
                val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(settingsState.savedSettings) {
                    settingsState.savedSettings?.let { settings ->
                        viewModel.applySettings(settings)
                        navController.popBackStack()
                    }
                }
                SearchSettingsScreen(
                    state = settingsState,
                    onBack = { navController.popBackStack() },
                    onQueryChange = settingsViewModel::setQuery,
                    onTypeChange = settingsViewModel::setType,
                    onValueChange = settingsViewModel::setValue,
                    onRetry = settingsViewModel::loadSettings,
                    onRetryOptions = settingsViewModel::retryOptions,
                    onReset = settingsViewModel::reset,
                    onSave = settingsViewModel::save
                )
            }
            composable(
                route = "meal/{mealId}",
                arguments = listOf(navArgument("mealId") { type = NavType.StringType })
            ) { detailEntry ->
                val id = detailEntry.arguments?.getString("mealId").orEmpty()
                val detailFactory = remember(container) {
                    viewModelFactory { initializer { MealDetailViewModel(createSavedStateHandle(), container.getMeal) } }
                }
                val detailViewModel: MealDetailViewModel = viewModel(viewModelStoreOwner = detailEntry, factory = detailFactory)
                val detailState by detailViewModel.state.collectAsStateWithLifecycle()
                MealDetailScreen(
                    state = detailState,
                    onRetry = detailViewModel::retry,
                    isFavorite = id in state.favoriteIds,
                    onBack = { navController.popBackStack() },
                    onFavoriteClick = { viewModel.toggleFavorite(id) },
                    isSaving = id in state.pendingFavoriteIds,
                    localImagePath = state.favorites.firstOrNull { it.id == id }?.localImagePath
                )
            }
        }
    }
}
