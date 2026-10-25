package com.arsave.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.data.DownloadEngine
import com.arsave.app.ui.about.AboutScreen
import com.arsave.app.ui.components.DrawerDestination
import com.arsave.app.ui.components.SideDrawerContent
import com.arsave.app.ui.downloads.DownloadsScreen
import com.arsave.app.ui.library.LibraryScreen
import com.arsave.app.ui.main.MainScreen
import com.arsave.app.ui.settings.SettingsScreen
import com.arsave.app.ui.splash.SplashScreen
import com.arsave.app.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var sharedUrlState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedUrlState.value = extractSharedUrl(intent)

        setContent {
            ArSaveTheme(darkTheme = true) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    AppMainScaffold(
                        initialUrl = sharedUrlState.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUrlState.value = extractSharedUrl(intent)
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                val regex = "(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]+)".toRegex()
                val match = regex.find(text)
                return match?.value ?: text
            }
        }
        return null
    }
}

@Composable
fun AppMainScaffold(
    initialUrl: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Navigation State: 0 = Home, 1 = Downloads, 2 = Library, 3 = Settings, 4 = About
    var currentScreen by remember { mutableIntStateOf(0) }
    var previousScreen by remember { mutableIntStateOf(0) }

    val activeDownloads by DownloadEngine.activeDownloads.collectAsState()

    BackHandler(enabled = drawerState.isOpen || currentScreen in listOf(3, 4)) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen in listOf(3, 4)) {
            currentScreen = previousScreen
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideDrawerContent(
                currentDestination = when (currentScreen) {
                    0 -> DrawerDestination.HOME
                    1 -> DrawerDestination.DOWNLOADS
                    2 -> DrawerDestination.LIBRARY
                    3 -> DrawerDestination.SETTINGS
                    4 -> DrawerDestination.ABOUT
                    else -> DrawerDestination.HOME
                },
                onSelectDestination = { dest ->
                    coroutineScope.launch { drawerState.close() }
                    when (dest) {
                        DrawerDestination.HOME -> currentScreen = 0
                        DrawerDestination.DOWNLOADS -> currentScreen = 1
                        DrawerDestination.LIBRARY -> currentScreen = 2
                        DrawerDestination.SETTINGS -> {
                            previousScreen = currentScreen
                            currentScreen = 3
                        }
                        DrawerDestination.ABOUT -> {
                            previousScreen = currentScreen
                            currentScreen = 4
                        }
                        DrawerDestination.HELP -> {
                            previousScreen = currentScreen
                            currentScreen = 4
                        }
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBackground,
            bottomBar = {
                // Only show bottom navigation for main 3 tabs (Home, Downloads, Library)
                if (currentScreen in 0..2) {
                    NavigationBar(
                        containerColor = ObsidianBackground,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        // Tab 0: Home
                        NavigationBarItem(
                            selected = currentScreen == 0,
                            onClick = { currentScreen = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Home,
                                    contentDescription = "Home",
                                    tint = if (currentScreen == 0) AccentBlueLight else TextGray
                                )
                            },
                            label = {
                                Text(
                                    text = "Home",
                                    fontSize = 11.sp,
                                    color = if (currentScreen == 0) AccentBlueLight else TextGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )

                        // Tab 1: Downloads
                        NavigationBarItem(
                            selected = currentScreen == 1,
                            onClick = { currentScreen = 1 },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (activeDownloads.isNotEmpty()) {
                                            Badge(
                                                containerColor = StatusDanger,
                                                contentColor = TextWhite
                                            ) {
                                                Text("${activeDownloads.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FileDownload,
                                        contentDescription = "Downloads",
                                        tint = if (currentScreen == 1) AccentBlueLight else TextGray
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Downloads",
                                    fontSize = 11.sp,
                                    color = if (currentScreen == 1) AccentBlueLight else TextGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )

                        // Tab 2: Library
                        NavigationBarItem(
                            selected = currentScreen == 2,
                            onClick = { currentScreen = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Folder,
                                    contentDescription = "Library",
                                    tint = if (currentScreen == 2) AccentBlueLight else TextGray
                                )
                            },
                            label = {
                                Text(
                                    text = "Library",
                                    fontSize = 11.sp,
                                    color = if (currentScreen == 2) AccentBlueLight else TextGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    0 -> MainScreen(
                        initialUrl = initialUrl,
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onNavigateToSettings = {
                            previousScreen = currentScreen
                            currentScreen = 3
                        },
                        onNavigateToDownloads = {
                            currentScreen = 1
                        }
                    )
                    1 -> DownloadsScreen()
                    2 -> LibraryScreen()
                    3 -> SettingsScreen(
                        onBack = { currentScreen = previousScreen }
                    )
                    4 -> AboutScreen(
                        onBack = { currentScreen = previousScreen }
                    )
                }
            }
        }
    }
}
