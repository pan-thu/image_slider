package dev.panthu.imagesliderapplication.data.model

/**
 * Represents an image item in the gallery.
 *
 * @property id Unique identifier for the image
 * @property resourceId Android drawable resource ID
 * @property contentDescription Accessibility description for screen readers
 */
data class ImageItem(
    val id: Int,
    val resourceId: Int,
    val contentDescription: String
)
