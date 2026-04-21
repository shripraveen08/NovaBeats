package com.novabeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.novabeats.player.PlayerViewModel
import com.novabeats.ui.home.HomeScreen
import com.novabeats.ui.explore.ExploreScreen
import com.novabeats.ui.library.LibraryScreen
import com.novabeats.ui.playlists.PlaylistsScreen
import com.novabeats.ui.player.MiniPlayer
import com.novabeats.ui.player.FullPlayerScreen
import com.novabeats.ui.theme.NovaBeatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaBeatTheme {
                NovaBeatApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaBeatApp() {
    val navController = rememberNavController()
    val playerVm: PlayerViewModel = hiltViewModel()
    val playerState by playerVm.state.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem("home",      "Home",      "🏠"),
        BottomNavItem("explore",   "Explore",   "🔍"),
        BottomNavItem("library",   "Library",   "🎵"),
        BottomNavItem("playlists", "Playlists", "📋")
    )

    Scaffold(
        bottomBar = {
            Column {
                // Mini player — shown when a song is loaded
                if (playerState.currentSong != null) {
                    MiniPlayer(
                        song       = playerState.currentSong!!,
                        isPlaying  = playerState.isPlaying,
                        onToggle   = playerVm::togglePlayPause,
                        onExpand   = { showFullPlayer = true }
                    )
                }

                // Bottom nav
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentDest = navBackStack?.destination

                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon     = { Text(item.icon) },
                            label    = { Text(item.label) },
                            selected = currentDest?.hierarchy?.any { it.route == item.route } == true,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(padding)
        ) {
            composable("home")      { HomeScreen(playerVm) }
            composable("explore")   { ExploreScreen(playerVm) }
            composable("library")   { LibraryScreen(playerVm) }
            composable("playlists") { PlaylistsScreen(playerVm) }
        }
    }

    // Full-screen player sheet
    if (showFullPlayer && playerState.currentSong != null) {
        FullPlayerScreen(
            playerVm   = playerVm,
            onDismiss  = { showFullPlayer = false }
        )
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: String)
