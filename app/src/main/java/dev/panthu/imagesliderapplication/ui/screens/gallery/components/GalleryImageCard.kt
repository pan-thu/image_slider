package dev.panthu.imagesliderapplication.ui.screens.gallery.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.panthu.imagesliderapplication.R
import dev.panthu.imagesliderapplication.data.model.ImageItem

/**
 * Displays a single image card in the gallery grid.
 * Uses Coil for efficient image loading with caching.
 *
 * @param imageItem The image data to display
 * @param onClick Callback invoked when the card is clicked
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun GalleryImageCard(
    imageItem: ImageItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityDescription = stringResource(R.string.cd_gallery_image)

    // Animation state for press feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "card_scale_animation"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = "${imageItem.contentDescription}. $accessibilityDescription"
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AsyncImage(
            model = imageItem.resourceId,
            contentDescription = null, // Handled by card semantics
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
