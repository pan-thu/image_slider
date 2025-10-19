package dev.panthu.imagesliderapplication.ui.screens.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.panthu.imagesliderapplication.R
import dev.panthu.imagesliderapplication.ui.screens.gallery.components.EmptyGalleryState
import dev.panthu.imagesliderapplication.ui.screens.gallery.components.GalleryImageCard
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Gallery screen displaying a grid of image thumbnails.
 * Users can tap any image to open it in the fullscreen slider.
 *
 * Features:
 * - Responsive grid layout (GridCells.Adaptive)
 * - Scroll position preservation across rotation
 * - Empty state handling
 * - Accessibility support
 *
 * @param onImageClick Callback invoked when an image is clicked, passes the image index
 * @param viewModel ViewModel managing gallery state
 * @param modifier Modifier to be applied to the root composable
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun GalleryScreen(
    onImageClick: (Int) -> Unit,
    viewModel: GalleryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val images by viewModel.images.collectAsState()
    val scrollIndex by viewModel.scrollIndex.collectAsState()
    val scrollOffset by viewModel.scrollOffset.collectAsState()

    // Remember grid state with restored scroll position
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = scrollIndex,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.gallery_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (images.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.gallery_items_count, images.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (images.isEmpty()) {
            EmptyGalleryState(modifier = Modifier.padding(padding))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(
                    minSize = dimensionResource(R.dimen.grid_min_item_size)
                ),
                state = gridState,
                contentPadding = PaddingValues(
                    dimensionResource(R.dimen.grid_padding)
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.grid_spacing)
                ),
                verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.grid_spacing)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                itemsIndexed(
                    items = images,
                    key = { _, item -> item.id }
                ) { index, imageItem ->
                    GalleryImageCard(
                        imageItem = imageItem,
                        onClick = {
                            // Save scroll position before navigating
                            viewModel.updateScrollPosition(
                                firstVisibleItemIndex = gridState.firstVisibleItemIndex,
                                firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
                            )
                            onImageClick(index)
                        }
                    )
                }
            }

            // Track scroll position changes for state preservation
            LaunchedEffect(gridState) {
                snapshotFlow {
                    Pair(
                        gridState.firstVisibleItemIndex,
                        gridState.firstVisibleItemScrollOffset
                    )
                }
                    .debounce(500) // Debounce to avoid excessive updates
                    .collect { (index, offset) ->
                        viewModel.updateScrollPosition(index, offset)
                    }
            }
        }
    }
}
