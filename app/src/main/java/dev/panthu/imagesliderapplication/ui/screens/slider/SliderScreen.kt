package dev.panthu.imagesliderapplication.ui.screens.slider

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.panthu.imagesliderapplication.R
import dev.panthu.imagesliderapplication.ui.screens.slider.components.EmptySliderState
import dev.panthu.imagesliderapplication.ui.screens.slider.components.FullscreenImage

/**
 * Slider screen displaying fullscreen images with swipe navigation.
 * Users can swipe left/right to browse through images.
 *
 * Features:
 * - HorizontalPager for swipe gestures
 * - State preservation across rotation
 * - Index indicator showing current position
 * - Back navigation to gallery
 * - First-time swipe hint
 * - Empty/error state handling
 *
 * @param initialIndex The index of the image to display initially
 * @param onBackClick Callback invoked when user navigates back
 * @param viewModel ViewModel managing slider state
 * @param modifier Modifier to be applied to the root composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderScreen(
    initialIndex: Int,
    onBackClick: () -> Unit,
    viewModel: SliderViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val images by viewModel.images.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    // Set initial page on first composition
    LaunchedEffect(Unit) {
        viewModel.setInitialPage(initialIndex)
    }

    // PagerState for swipe navigation
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, images.size.coerceAtLeast(1) - 1),
        pageCount = { images.size }
    )

    // Sync pager state with ViewModel for state preservation
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateCurrentPage(pagerState.currentPage)
    }

    // First-time swipe hint state
    var showHint by rememberSaveable { mutableStateOf(true) }

    // Auto-dismiss hint after user swipes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != initialIndex) {
            showHint = false
        }
    }

    // Handle system back gesture
    BackHandler {
        onBackClick()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (images.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.slider_index_indicator,
                                pagerState.currentPage + 1,
                                images.size
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    val backButtonDescription = stringResource(R.string.cd_back_button)
                    val backToGalleryText = stringResource(R.string.back_to_gallery)

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics {
                            contentDescription = backButtonDescription
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backToGalleryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (images.isEmpty()) {
            // Empty state
            EmptySliderState(modifier = Modifier.padding(padding))
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // HorizontalPager for swipe navigation
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    beyondViewportPageCount = 1 // Preload adjacent pages for smooth swiping
                ) { page ->
                    val imageItem = images.getOrNull(page)
                    if (imageItem != null) {
                        val accessibilityDescription = stringResource(
                            R.string.cd_slider_image,
                            page + 1,
                            images.size
                        )
                        FullscreenImage(
                            imageItem = imageItem,
                            contentDescription = "$accessibilityDescription ${imageItem.contentDescription}",
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics {
                                    contentDescription = accessibilityDescription
                                }
                        )
                    } else {
                        // Error state for invalid page
                        EmptySliderState()
                    }
                }

                // First-time swipe hint
                if (showHint) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(dimensionResource(R.dimen.slider_indicator_padding)),
                        action = {
                            TextButton(onClick = { showHint = false }) {
                                Text("Got it")
                            }
                        }
                    ) {
                        Text(stringResource(R.string.swipe_hint))
                    }
                }
            }
        }
    }
}
