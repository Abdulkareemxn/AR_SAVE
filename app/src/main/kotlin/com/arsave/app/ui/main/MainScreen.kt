package com.arsave.app.ui.main

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arsave.app.ArSaveApp
import com.arsave.app.data.DownloadEngine
import com.arsave.app.data.ExtractedMedia
import com.arsave.app.data.FormatChoice
import com.arsave.app.ui.components.ArSaveTopBar
import com.arsave.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class MainViewState {
    HOME,
    DETAILS,
    DOWNLOADING
}

@Composable
fun MainScreen(
    initialUrl: String? = null,
    onOpenDrawer: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var viewState by remember { mutableStateOf(MainViewState.HOME) }
    var urlInput by remember { mutableStateOf(initialUrl ?: "") }

    // Extraction State
    var isExtracting by remember { mutableStateOf(false) }
    var extractedMedia by remember { mutableStateOf<ExtractedMedia?>(null) }
    var extractError by remember { mutableStateOf<String?>(null) }

    // Format selection
    var formatCategoryTab by remember { mutableIntStateOf(0) } // 0: Video, 1: Audio, 2: Subtitles
    var selectedFormat by remember {
        mutableStateOf(FormatChoice("1080", "1080p", "MP4 • 523 MB", "mp4", false, tags = listOf("60fps")))
    }

    // Active downloading state
    var currentProcessId by remember { mutableStateOf<String?>(null) }
    var downloadPercent by remember { mutableFloatStateOf(0f) }
    var downloadSpeed by remember { mutableStateOf("0 KB/s") }
    var downloadEta by remember { mutableStateOf("--:--") }
    var downloadElapsed by remember { mutableStateOf("00:00:00") }
    var downloadedBytesStr by remember { mutableStateOf("0 MB") }
    var totalBytesStr by remember { mutableStateOf("523 MB") }
    var isDownloadPaused by remember { mutableStateOf(false) }

    // If initialUrl is provided from Android share sheet
    LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrBlank() && initialUrl != urlInput) {
            urlInput = initialUrl
            // Trigger extraction automatically
            isExtracting = true
            extractError = null
            coroutineScope.launch {
                val res = DownloadEngine.extractInfo(initialUrl, context)
                isExtracting = false
                res.onSuccess { info ->
                    extractedMedia = info
                    selectedFormat = info.formats.firstOrNull() ?: selectedFormat
                    viewState = MainViewState.DETAILS
                }.onFailure { err ->
                    extractError = err.message ?: "Failed to fetch media details"
                    Toast.makeText(context, extractError, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AnimatedContent(
        targetState = viewState,
        label = "mainViewAnim"
    ) { state ->
        when (state) {
            MainViewState.HOME -> {
                HomeScreenView(
                    urlInput = urlInput,
                    onUrlChange = { urlInput = it },
                    isExtracting = isExtracting,
                    onPasteFromClipboard = {
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            urlInput = clip.trim()
                            Toast.makeText(context, "Link pasted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onGetInfo = {
                        if (urlInput.isBlank()) {
                            Toast.makeText(context, "Please paste a video link first", Toast.LENGTH_SHORT).show()
                            return@HomeScreenView
                        }
                        isExtracting = true
                        extractError = null
                        coroutineScope.launch {
                            val res = DownloadEngine.extractInfo(urlInput, context)
                            isExtracting = false
                            res.onSuccess { info ->
                                extractedMedia = info
                                selectedFormat = info.formats.firstOrNull() ?: selectedFormat
                                viewState = MainViewState.DETAILS
                            }.onFailure { err ->
                                extractError = err.message ?: "Extraction failed"
                                Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onOpenDrawer = onOpenDrawer,
                    onSettingsClick = onNavigateToSettings,
                    modifier = modifier
                )
            }

            MainViewState.DETAILS -> {
                val media = extractedMedia
                if (media != null) {
                    VideoDetailsView(
                        media = media,
                        selectedCategory = formatCategoryTab,
                        onSelectCategory = { formatCategoryTab = it },
                        selectedFormat = selectedFormat,
                        onSelectFormat = { selectedFormat = it },
                        onBack = { viewState = MainViewState.HOME },
                        onStartDownload = {
                            val processId = UUID.randomUUID().toString()
                            currentProcessId = processId
                            downloadPercent = 0f
                            downloadSpeed = "Connecting..."
                            downloadEta = "--:--"
                            totalBytesStr = selectedFormat.fileSizeFormatted.ifBlank { "523 MB" }
                            downloadedBytesStr = "0 MB"
                            viewState = MainViewState.DOWNLOADING

                            coroutineScope.launch {
                                val res = DownloadEngine.downloadMedia(
                                    context = context,
                                    url = media.url,
                                    formatChoice = selectedFormat,
                                    processId = processId,
                                    mediaTitle = media.title,
                                    thumbnailUrl = media.thumbnail
                                ) { progress ->
                                    downloadPercent = progress.percent
                                    downloadSpeed = progress.speed
                                    downloadEta = progress.eta
                                    downloadElapsed = progress.elapsedFormatted
                                    downloadedBytesStr = progress.downloadedBytesFormatted
                                    if (progress.totalBytesFormatted.isNotBlank()) {
                                        totalBytesStr = progress.totalBytesFormatted
                                    }
                                }

                                res.onSuccess { file ->
                                    Toast.makeText(context, "Download complete: ${file.name}", Toast.LENGTH_LONG).show()
                                    onNavigateToDownloads()
                                }.onFailure { err ->
                                    Toast.makeText(context, "Download failed: ${err.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = modifier
                    )
                } else {
                    viewState = MainViewState.HOME
                }
            }

            MainViewState.DOWNLOADING -> {
                val media = extractedMedia
                DownloadingView(
                    mediaTitle = media?.title ?: "Video Download",
                    channelName = media?.uploader ?: "Creator",
                    thumbnailUrl = media?.thumbnail,
                    qualityLabel = "${selectedFormat.label} (${selectedFormat.ext.uppercase()})",
                    fileSize = totalBytesStr,
                    downloadSpeed = downloadSpeed,
                    elapsedTime = downloadElapsed,
                    remainingTime = downloadEta,
                    progressPercent = downloadPercent,
                    downloadedBytes = downloadedBytesStr,
                    totalBytes = totalBytesStr,
                    isPaused = isDownloadPaused,
                    onPauseToggle = {
                        val pid = currentProcessId
                        if (pid != null) {
                            if (isDownloadPaused) {
                                DownloadEngine.resumeDownload(pid)
                                isDownloadPaused = false
                            } else {
                                DownloadEngine.pauseDownload(pid)
                                isDownloadPaused = true
                            }
                        }
                    },
                    onCancel = {
                        val pid = currentProcessId
                        if (pid != null) {
                            DownloadEngine.cancel(pid)
                        }
                        viewState = MainViewState.HOME
                    },
                    onBack = { viewState = MainViewState.HOME },
                    onSettingsClick = onNavigateToSettings,
                    modifier = modifier
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2: HOME SCREEN ("Video Downloader")
// -------------------------------------------------------------
@Composable
private fun HomeScreenView(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isExtracting: Boolean,
    onPasteFromClipboard: () -> Unit,
    onGetInfo: () -> Unit,
    onOpenDrawer: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Navigation Bar
        ArSaveTopBar(
            onLogoClick = onOpenDrawer,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Headline & Subtitle matching Screen 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Video Downloader",
                color = TextWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Save videos from YouTube, Instagram, Facebook and many more",
                color = TextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Link Input Card matching mockup
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = InputBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                TextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    placeholder = {
                        Text(
                            text = "Paste link here...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = onPasteFromClipboard,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = "Paste",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Action Button: "Get Video Info"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Button(
                onClick = onGetInfo,
                enabled = !isExtracting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isExtracting) {
                    CircularProgressIndicator(
                        color = TextWhite,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing media...",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Video Info",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Social Platform Row matching Screen 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialPlatformIcon(
                name = "YouTube",
                iconColor = YouTubeRed,
                label = "YouTube",
                isGradient = false,
                onClick = { onUrlChange("https://youtu.be/4HRC6c5-2lQ") }
            )
            SocialPlatformIcon(
                name = "Instagram",
                iconColor = InstagramPink,
                label = "Instagram",
                isGradient = true,
                onClick = { onUrlChange("https://instagram.com") }
            )
            SocialPlatformIcon(
                name = "Facebook",
                iconColor = FacebookBlue,
                label = "Facebook",
                isGradient = false,
                onClick = { onUrlChange("https://facebook.com") }
            )
            SocialPlatformIcon(
                name = "TikTok",
                iconColor = TikTokDark,
                label = "TikTok",
                isGradient = false,
                onClick = { onUrlChange("https://tiktok.com") }
            )
            SocialPlatformIcon(
                name = "More",
                iconColor = SurfaceVariantDark,
                label = "More",
                isGradient = false,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2x2 Feature Cards Grid matching Screen 2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    icon = Icons.Outlined.ElectricBolt,
                    title = "Fast Downloads",
                    subtitle = "High speed & stable",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = Icons.Outlined.SmartDisplay,
                    title = "All Major Sites",
                    subtitle = "YouTube, Instagram, Facebook & more",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    icon = Icons.Outlined.Shield,
                    title = "Safe & Private",
                    subtitle = "Everything runs on your device",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = Icons.Outlined.Diamond,
                    title = "High Quality",
                    subtitle = "Up to 4K (when available)",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SocialPlatformIcon(
    name: String,
    iconColor: Color,
    label: String,
    isGradient: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .then(
                    if (isGradient) {
                        Modifier.background(
                            Brush.linearGradient(
                                listOf(InstagramGradientStart, InstagramPink, Color(0xFFFCAF45))
                            )
                        )
                    } else {
                        Modifier.background(iconColor)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (name) {
                "YouTube" -> Icon(Icons.Outlined.PlayArrow, null, tint = TextWhite, modifier = Modifier.size(26.dp))
                "Instagram" -> Icon(Icons.Outlined.CameraAlt, null, tint = TextWhite, modifier = Modifier.size(24.dp))
                "Facebook" -> Text("f", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                "TikTok" -> Icon(Icons.Outlined.MusicNote, null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                "More" -> Icon(Icons.Outlined.MoreHoriz, null, tint = TextWhite, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentBlueLight,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 3: VIDEO DETAILS SCREEN
// -------------------------------------------------------------
@Composable
private fun VideoDetailsView(
    media: ExtractedMedia,
    selectedCategory: Int,
    onSelectCategory: (Int) -> Unit,
    selectedFormat: FormatChoice,
    onSelectFormat: (FormatChoice) -> Unit,
    onBack: () -> Unit,
    onStartDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Top Bar: ← Video Details
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
                    text = "Video Details",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 16:9 Thumbnail with duration pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
            ) {
                if (!media.thumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = media.thumbnail,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // Duration badge bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = media.durationFormatted,
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Title
            Text(
                text = media.title,
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Row (Avatar + Name + Subs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = media.uploader.take(1).uppercase(),
                        color = AccentCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = media.uploader,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = media.subscriberCount ?: "Verified Creator",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row (Views, Date, Duration)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Visibility, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(media.viewsCount ?: "12M views", color = TextGray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(media.uploadDate ?: "2 weeks ago", color = TextGray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(media.durationFormatted, color = TextGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Available Formats Section
            Text(
                text = "Available Formats",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Format Category Pills: [Video] [Audio] [Subtitles]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormatTabPill("Video", selected = selectedCategory == 0) { onSelectCategory(0) }
                FormatTabPill("Audio", selected = selectedCategory == 1) { onSelectCategory(1) }
                FormatTabPill("Subtitles", selected = selectedCategory == 2) { onSelectCategory(2) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Formats List with Radio Buttons & Tags matching Screen 3
            val activeFormatsList = when (selectedCategory) {
                0 -> media.formats
                1 -> media.audioFormats
                else -> media.subtitleFormats
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeFormatsList.forEach { format ->
                    FormatItemRow(
                        format = format,
                        isSelected = selectedFormat.id == format.id,
                        onClick = { onSelectFormat(format) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sticky Bottom Download Button matching Screen 3
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, ObsidianBackground.copy(alpha = 0.95f), ObsidianBackground)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = onStartDownload,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Outlined.FileDownload, null, tint = TextWhite, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FormatTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AccentBlue else CardBackground,
        border = if (selected) null else BorderStroke(1.dp, CardBorder),
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) TextWhite else TextGray,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FormatItemRow(
    format: FormatChoice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, if (isSelected) AccentBlue else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = format.label,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = format.resolution,
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            // Tags like [HDR] [60fps]
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                format.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceVariantDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 4: DOWNLOADING SCREEN
// -------------------------------------------------------------
@Composable
private fun DownloadingView(
    mediaTitle: String,
    channelName: String,
    thumbnailUrl: String?,
    qualityLabel: String,
    fileSize: String,
    downloadSpeed: String,
    elapsedTime: String,
    remainingTime: String,
    progressPercent: Float,
    downloadedBytes: String,
    totalBytes: String,
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .padding(horizontal = 20.dp)
    ) {
        // Top Bar: ← Downloading + Settings icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Downloading",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini preview card: Thumbnail + Title + Channel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (!thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Outlined.FileDownload, null, tint = AccentCyan, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaTitle,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channelName,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Box matching Screen 4
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatsLineRow(label = "Quality", value = qualityLabel)
                StatsLineRow(label = "File Size", value = fileSize)
                StatsLineRow(label = "Download Speed", value = downloadSpeed)
                StatsLineRow(label = "Elapsed Time", value = elapsedTime)
                StatsLineRow(label = "Remaining Time", value = remainingTime)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress section matching Screen 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPaused) "Paused" else "Downloading...",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${progressPercent.toInt()}%",
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = AccentBlue,
            trackColor = SurfaceVariantDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$downloadedBytes / $totalBytes",
            color = TextGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Action Buttons: [⏸ Pause] and [✕ Cancel] matching Screen 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = onPauseToggle,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, StatusDanger.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = StatusDanger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel",
                    color = StatusDanger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatsLineRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 13.sp)
        Text(text = value, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
