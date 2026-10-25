package com.arsave.app.ui.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.ui.theme.*

enum class SettingsSubView {
    MAIN,
    THEME
}

enum class AppThemeMode {
    SYSTEM_DEFAULT,
    LIGHT_MODE,
    DARK_MODE
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubView by remember { mutableStateOf(SettingsSubView.MAIN) }

    // Settings state
    var defaultQuality by remember { mutableStateOf("1080p") }
    var selectedTheme by remember { mutableStateOf(AppThemeMode.SYSTEM_DEFAULT) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var audioOnlyDefault by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showConcurrentDialog by remember { mutableStateOf(false) }
    var concurrentDownloads by remember { mutableIntStateOf(2) }

    AnimatedContent(
        targetState = currentSubView,
        label = "settingsSubView"
    ) { subView ->
        when (subView) {
            SettingsSubView.MAIN -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(ObsidianBackground)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CardBackground)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Settings",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Settings List Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // 1. Default Video Quality
                        SettingRow(
                            icon = Icons.Outlined.Videocam,
                            title = "Default Video Quality",
                            value = defaultQuality,
                            onClick = { showQualityDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Default Download Folder
                        SettingRow(
                            icon = Icons.Outlined.Folder,
                            title = "Default Download Folder",
                            value = "Downloads/AR SAVE",
                            onClick = {
                                Toast.makeText(context, "Storage: Downloads/AR SAVE", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Concurrent Downloads
                        SettingRow(
                            icon = Icons.Outlined.SwapVert,
                            title = "Concurrent Downloads",
                            value = "$concurrentDownloads",
                            onClick = { showConcurrentDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4. Theme
                        SettingRow(
                            icon = Icons.Outlined.Palette,
                            title = "Theme",
                            value = when (selectedTheme) {
                                AppThemeMode.SYSTEM_DEFAULT -> "System Default"
                                AppThemeMode.LIGHT_MODE -> "Light Mode"
                                AppThemeMode.DARK_MODE -> "Dark Mode"
                            },
                            onClick = { currentSubView = SettingsSubView.THEME }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 5. Download Notifications (Switch)
                        SettingSwitchRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Download Notifications",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6. Playlist Behavior
                        SettingRow(
                            icon = Icons.Outlined.PlaylistPlay,
                            title = "Playlist Behavior",
                            value = "Ask Every Time",
                            onClick = {
                                Toast.makeText(context, "Playlist mode: Ask Every Time", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 7. Audio-only Default (Switch)
                        SettingSwitchRow(
                            icon = Icons.Outlined.Audiotrack,
                            title = "Audio-only Default",
                            checked = audioOnlyDefault,
                            onCheckedChange = { audioOnlyDefault = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 8. Clear Download History
                        SettingRow(
                            icon = Icons.Outlined.DeleteSweep,
                            title = "Clear Download History",
                            value = "",
                            onClick = {
                                Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 9. Language
                        SettingRow(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            value = "English",
                            onClick = {
                                Toast.makeText(context, "Language: English (US)", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            SettingsSubView.THEME -> {
                ThemeSelectionView(
                    currentTheme = selectedTheme,
                    onSelectTheme = {
                        selectedTheme = it
                        currentSubView = SettingsSubView.MAIN
                    },
                    onBack = { currentSubView = SettingsSubView.MAIN }
                )
            }
        }
    }

    if (showQualityDialog) {
        val qualities = listOf("2160p (4K)", "1440p (2K)", "1080p", "720p", "480p")
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            confirmButton = {},
            title = { Text("Default Video Quality", color = TextWhite) },
            text = {
                Column {
                    qualities.forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    defaultQuality = q.split(" ").first()
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = defaultQuality in q,
                                onClick = {
                                    defaultQuality = q.split(" ").first()
                                    showQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = q, color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            containerColor = CardBackground
        )
    }

    if (showConcurrentDialog) {
        val options = listOf(1, 2, 3, 4)
        AlertDialog(
            onDismissRequest = { showConcurrentDialog = false },
            confirmButton = {},
            title = { Text("Concurrent Downloads", color = TextWhite) },
            text = {
                Column {
                    options.forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    concurrentDownloads = count
                                    showConcurrentDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = concurrentDownloads == count,
                                onClick = {
                                    concurrentDownloads = count
                                    showConcurrentDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$count concurrent tasks", color = TextWhite, fontSize = 14.sp)
                        }
                    }
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = AccentBlue,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceVariantDark
                )
            )
        }
    }
}

// Mockup Screen 9: Theme Selection
@Composable
private fun ThemeSelectionView(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(bottom = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Theme",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Option 1: System Default
            ThemeCardItem(
                title = "System Default",
                isSelected = currentTheme == AppThemeMode.SYSTEM_DEFAULT,
                onClick = { onSelectTheme(AppThemeMode.SYSTEM_DEFAULT) },
                previewDark = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Light Mode
            ThemeCardItem(
                title = "Light Mode",
                isSelected = currentTheme == AppThemeMode.LIGHT_MODE,
                onClick = { onSelectTheme(AppThemeMode.LIGHT_MODE) },
                previewDark = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: Dark Mode
            ThemeCardItem(
                title = "Dark Mode",
                isSelected = currentTheme == AppThemeMode.DARK_MODE,
                onClick = { onSelectTheme(AppThemeMode.DARK_MODE) },
                previewDark = true
            )
        }
    }
}

@Composable
private fun ThemeCardItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    previewDark: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, if (isSelected) AccentBlue else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Graphical Card Preview Box matching mockup
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (previewDark) Color(0xFF1E2738) else Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (previewDark) AccentBlue else Color(0xFF334155))
                )
            }
        }
    }
}
