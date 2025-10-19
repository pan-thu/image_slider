package dev.panthu.imagesliderapplication.ui.screens.slider.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import dev.panthu.imagesliderapplication.data.model.ImageItem

/**
 * Displays a single image in fullscreen mode with proper aspect ratio preservation.
 * Uses Coil for efficient image loading with caching.
 *
 * @param imageItem The image data to display
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier to be applied to the container
 */
@Composable
fun FullscreenImage(
    imageItem: ImageItem,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageItem.resourceId,
            contentDescription = contentDescription ?: imageItem.contentDescription,
            contentScale = ContentScale.Fit, // Maintain aspect ratio, fit within bounds
            modifier = Modifier.fillMaxSize()
        )
    }
}
