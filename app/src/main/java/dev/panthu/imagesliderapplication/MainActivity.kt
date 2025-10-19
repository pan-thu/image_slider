package dev.panthu.imagesliderapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.panthu.imagesliderapplication.ui.navigation.AppNavGraph
import dev.panthu.imagesliderapplication.ui.theme.ImageSliderApplicationTheme

/**
 * Main activity for the GallerySlider application.
 * Hosts the navigation graph with Gallery and Slider screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageSliderApplicationTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}