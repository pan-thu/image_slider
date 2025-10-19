package dev.panthu.imagesliderapplication.data.repository

import dev.panthu.imagesliderapplication.R
import dev.panthu.imagesliderapplication.data.model.ImageItem

/**
 * Repository for providing image data from Android resources.
 * All images are stored in res/drawable and loaded from there.
 */
class ImageRepository {

    /**
     * Returns the complete list of gallery images.
     * Note: Currently using placeholder resource IDs.
     * Replace with actual drawable resources when images are added.
     */
    fun getImages(): List<ImageItem> = listOf(
        ImageItem(1, R.drawable.gallery_image_01, "Landscape photo 1"),
        ImageItem(2, R.drawable.gallery_image_02, "Portrait photo 2"),
        ImageItem(3, R.drawable.gallery_image_03, "Square photo 3"),
        ImageItem(4, R.drawable.gallery_image_04, "Landscape photo 4"),
        ImageItem(5, R.drawable.gallery_image_05, "Portrait photo 5"),
        ImageItem(6, R.drawable.gallery_image_06, "Square photo 6"),
        ImageItem(7, R.drawable.gallery_image_07, "Landscape photo 7"),
        ImageItem(8, R.drawable.gallery_image_08, "Portrait photo 8"),
        ImageItem(9, R.drawable.gallery_image_09, "Square photo 9"),
        ImageItem(10, R.drawable.gallery_image_10, "Landscape photo 10"),
        ImageItem(11, R.drawable.gallery_image_11, "Landscape photo 11"),
        ImageItem(12, R.drawable.gallery_image_12, "Portrait photo 12"),
        ImageItem(13, R.drawable.gallery_image_13, "Square photo 13"),
        ImageItem(14, R.drawable.gallery_image_14, "Landscape photo 14"),
        ImageItem(15, R.drawable.gallery_image_15, "Portrait photo 15"),
    )
}
