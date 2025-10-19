package dev.panthu.imagesliderapplication.ui.screens.slider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.panthu.imagesliderapplication.data.model.ImageItem
import dev.panthu.imagesliderapplication.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Slider screen.
 * Manages fullscreen image display with swipe navigation and state preservation.
 *
 * @property savedStateHandle Handle for preserving state across process death
 * @property imageRepository Repository providing image data from resources
 */
class SliderViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
    private val imageRepository: ImageRepository = ImageRepository()
) : ViewModel() {

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
    }

    // Image list state
    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images.asStateFlow()

    // Current page index - survives configuration changes
    private val _currentPage = MutableStateFlow(
        savedStateHandle?.get<Int>(KEY_CURRENT_PAGE) ?: 0
    )
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

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
     * Updates the current page index.
     * Called when user swipes to a different image.
     *
     * @param page The new page index
     */
    fun updateCurrentPage(page: Int) {
        _currentPage.value = page
        savedStateHandle?.set(KEY_CURRENT_PAGE, page)
    }

    /**
     * Sets the initial page when entering the slider.
     *
     * @param initialPage The starting page index
     */
    fun setInitialPage(initialPage: Int) {
        val safeIndex = initialPage.coerceIn(0, _images.value.size - 1)
        updateCurrentPage(safeIndex)
    }

    /**
     * Returns the total number of images.
     */
    fun getImageCount(): Int = _images.value.size

    /**
     * Returns the image at the specified index, or null if invalid.
     */
    fun getImageAt(index: Int): ImageItem? = _images.value.getOrNull(index)

    /**
     * Defines edge behavior for swipe navigation.
     * BLOCK: Stop at first/last image (default)
     * WRAP_AROUND: Loop back to beginning/end
     */
    fun getEdgeBehavior(): EdgeBehavior = EdgeBehavior.BLOCK
}

/**
 * Defines behavior when user swipes at the edges (first/last image).
 */
enum class EdgeBehavior {
    /**
     * Block swiping beyond first/last image (default Android behavior).
     */
    BLOCK,

    /**
     * Wrap around: swiping left on last image goes to first, and vice versa.
     */
    WRAP_AROUND
}
