package dev.panthu.imagesliderapplication.ui.screens.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.panthu.imagesliderapplication.data.model.ImageItem
import dev.panthu.imagesliderapplication.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Gallery screen.
 * Manages the list of images and scroll position state with preservation across configuration changes.
 *
 * @property savedStateHandle Handle for preserving state across process death
 * @property imageRepository Repository providing image data from resources
 */
class GalleryViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
    private val imageRepository: ImageRepository = ImageRepository()
) : ViewModel() {

    companion object {
        private const val KEY_SCROLL_INDEX = "scroll_index"
        private const val KEY_SCROLL_OFFSET = "scroll_offset"
    }

    // Image list state
    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images.asStateFlow()

    // Scroll position state - survives configuration changes
    private val _scrollIndex = MutableStateFlow(
        savedStateHandle?.get<Int>(KEY_SCROLL_INDEX) ?: 0
    )
    val scrollIndex: StateFlow<Int> = _scrollIndex.asStateFlow()

    private val _scrollOffset = MutableStateFlow(
        savedStateHandle?.get<Int>(KEY_SCROLL_OFFSET) ?: 0
    )
    val scrollOffset: StateFlow<Int> = _scrollOffset.asStateFlow()

    init {
        loadImages()
    }

    /**
     * Loads images from the repository.
     */
    private fun loadImages() {
        _images.value = imageRepository.getImages()
    }

    /**
     * Updates the scroll position for state preservation.
     * Called when user scrolls or when returning from slider.
     *
     * @param firstVisibleItemIndex Index of the first visible item in the grid
     * @param firstVisibleItemScrollOffset Scroll offset of the first visible item
     */
    fun updateScrollPosition(
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int = 0
    ) {
        _scrollIndex.value = firstVisibleItemIndex
        _scrollOffset.value = firstVisibleItemScrollOffset

        // Save to SavedStateHandle for process death survival
        savedStateHandle?.let {
            it[KEY_SCROLL_INDEX] = firstVisibleItemIndex
            it[KEY_SCROLL_OFFSET] = firstVisibleItemScrollOffset
        }
    }

    /**
     * Returns the current image count.
     */
    fun getImageCount(): Int = _images.value.size
}
