package com.example.practice3kormiletsv.feature.profile

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.practice3kormiletsv.feature.profile.ui.EditProfileRoute
import com.example.practice3kormiletsv.feature.profile.ui.EditProfileViewModel
import com.example.practice3kormiletsv.feature.profile.ui.PdfViewerScreen
import com.example.practice3kormiletsv.feature.profile.ui.PdfViewerViewModel
import com.example.practice3kormiletsv.feature.profile.ui.ProfileScreen
import com.example.practice3kormiletsv.feature.profile.ui.ProfileViewModel

object ProfileDestination {
    const val route = "profile"
}

private const val EDIT_ROUTE = "profile/edit"
private const val DOCUMENT_ROUTE = "profile/document?documentUri={documentUri}&documentName={documentName}"

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    feature: ProfileFeature,
    onMessage: (String) -> Unit
) {
    composable(ProfileDestination.route) { profileEntry ->
        val context = LocalContext.current
        val factory = remember(feature) {
            viewModelFactory { initializer { ProfileViewModel(feature.observeProfile, feature.downloadResume) } }
        }
        val viewModel: ProfileViewModel = viewModel(viewModelStoreOwner = profileEntry, factory = factory)
        val state by viewModel.state.collectAsStateWithLifecycle()
        LaunchedEffect(viewModel) {
            viewModel.documents.collect { document ->
                val uri = Uri.parse(document.uri)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, document.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newRawUri(document.name, uri)
                }
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    if (document.mimeType == "application/pdf") {
                        navController.navigate("profile/document?documentUri=${Uri.encode(document.uri)}&documentName=${Uri.encode(document.name)}")
                    } else {
                        onMessage("На устройстве нет просмотрщика для этого типа файла. Попробуйте ссылку на PDF.")
                    }
                } catch (_: SecurityException) {
                    onMessage("Не удалось открыть скачанный документ в просмотрщике.")
                }
            }
        }
        ProfileScreen(
            state,
            onEdit = { navController.navigate(EDIT_ROUTE) { launchSingleTop = true } },
            onResume = viewModel::openResume,
            onRetry = viewModel::load
        )
    }
    composable(EDIT_ROUTE) { editEntry ->
        val factory = remember(feature) {
            viewModelFactory {
                initializer {
                    EditProfileViewModel(
                        createSavedStateHandle(), feature.observeProfile, feature.saveProfile,
                        feature.importAvatar, feature.createCameraCapture, feature.deleteDraftPhoto
                    )
                }
            }
        }
        val viewModel: EditProfileViewModel = viewModel(viewModelStoreOwner = editEntry, factory = factory)
        EditProfileRoute(viewModel, onExit = { navController.popBackStack() }, onMessage = onMessage)
    }
    composable(
        DOCUMENT_ROUTE,
        arguments = listOf(
            navArgument("documentUri") { type = NavType.StringType },
            navArgument("documentName") { type = NavType.StringType }
        )
    ) { documentEntry ->
        val application = LocalContext.current.applicationContext as Application
        val factory = remember(application) {
            viewModelFactory { initializer { PdfViewerViewModel(application, createSavedStateHandle()) } }
        }
        val viewModel: PdfViewerViewModel = viewModel(viewModelStoreOwner = documentEntry, factory = factory)
        val state by viewModel.state.collectAsStateWithLifecycle()
        PdfViewerScreen(
            state, onBack = { navController.popBackStack() }, onRetry = viewModel::retry,
            onPrevious = viewModel::previous, onNext = viewModel::next
        )
    }
}
