package dev.panthu.imagesliderapplication.ui.navigation

/**
 * Sealed class representing navigation destinations in the app.
 * Provides type-safe navigation with compile-time route validation.
 */
sealed class Screen(val route: String) {
    /**
     * Gallery screen showing grid of images.
     */
    data object Gallery : Screen("gallery")

    /**
     * Slider screen showing fullscreen image with swipe navigation.
     * @property imageIndex The index of the image to display initially
     */
    data class Slider(val imageIndex: Int = 0) : Screen("slider/{imageIndex}") {
        companion object {
            const val ROUTE = "slider/{imageIndex}"
            const val ARG_IMAGE_INDEX = "imageIndex"

            /**
             * Creates navigation route with specific image index.
             */
            fun createRoute(imageIndex: Int): String = "slider/$imageIndex"
        }
    }
}
