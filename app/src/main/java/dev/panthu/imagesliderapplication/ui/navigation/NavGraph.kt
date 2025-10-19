package dev.panthu.imagesliderapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.panthu.imagesliderapplication.ui.screens.gallery.GalleryScreen
import dev.panthu.imagesliderapplication.ui.screens.gallery.GalleryViewModel
import dev.panthu.imagesliderapplication.ui.screens.slider.SliderScreen
import dev.panthu.imagesliderapplication.ui.screens.slider.SliderViewModel

/**
 * Main navigation graph for the GallerySlider app.
 * Defines navigation structure between Gallery and Slider screens.
 *
 * @param navController Navigation controller for handling navigation actions
 * @param modifier Modifier to be applied to the NavHost
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Gallery.route,
        modifier = modifier
    ) {
        // Gallery Screen - Grid of images
        composable(
            route = Screen.Gallery.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            val viewModel: GalleryViewModel = viewModel()
            GalleryScreen(
                onImageClick = { imageIndex ->
                    navController.navigate(Screen.Slider.createRoute(imageIndex))
                },
                viewModel = viewModel
            )
        }

        // Slider Screen - Fullscreen image with swipe navigation
        composable(
            route = Screen.Slider.ROUTE,
            arguments = listOf(
                navArgument(Screen.Slider.ARG_IMAGE_INDEX) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        ) { backStackEntry ->
            val imageIndex = backStackEntry.arguments?.getInt(Screen.Slider.ARG_IMAGE_INDEX) ?: 0
            val viewModel: SliderViewModel = viewModel()

            SliderScreen(
                initialIndex = imageIndex,
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
