package com.arsave.app.ui.downloads

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.arsave.app.data.ActiveDownload
import com.arsave.app.data.CompletedDownload
import com.arsave.app.data.DownloadEngine
import com.arsave.app.ui.theme.*
import java.io.File

@Composable
fun DownloadsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeDownloads by DownloadEngine.activeDownloads.collectAsState()
    val completedDownloads by DownloadEngine.completedDownloads.collectAsState()

    var selectedCompletedItem by remember { mutableStateOf<CompletedDownload?>(null) }
    var showMenuForId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        DownloadEngine.initCompletedFiles()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Top Header matching Screen 5
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Section 1: Active Downloads
            if (activeDownloads.isNotEmpty()) {
                item {
                    Text(
                        text = "Active (${activeDownloads.size})",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(activeDownloads, key = { it.id }) { item ->
                    ActiveDownloadCard(
                        item = item,
                        onPauseToggle = {
                            if (item.isPaused) {
                                DownloadEngine.resumeDownload(item.id)
                            } else {
                                DownloadEngine.pauseDownload(item.id)
                            }
                        },
                        onCancel = {
                            DownloadEngine.cancel(item.id)
                        }
                    )
                }
            }

            // Section 2: Completed Downloads
            item {
                Text(
                    text = "Completed (${completedDownloads.size})",
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            if (completedDownloads.isEmpty() && activeDownloads.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No downloads yet",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Videos you download will show up here",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(completedDownloads, key = { it.id }) { item ->
                    CompletedDownloadCard(
                        item = item,
                        onPlay = { openFileWithPlayer(context, item.filePath) },
                        onShare = { shareMediaFile(context, item.filePath) },
                        onDelete = {
                            try {
                                val f = File(item.filePath)
                                if (f.exists()) f.delete()
                                DownloadEngine.initCompletedFiles()
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveDownloadCard(
    item: ActiveDownload,
    onPauseToggle: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or Placeholder
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.thumbnail.isNullOrBlank()) {
                        AsyncImage(
                            model = item.thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${item.progressPercent.toInt()}% • ${item.downloadedSize} / ${item.totalSize}",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Pause / Resume Button
                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                ) {
                    Icon(
                        imageVector = if (item.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        contentDescription = "Pause/Resume",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { item.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentBlue,
                trackColor = SurfaceVariantDark
            )
        }
    }
}

@Composable
private fun CompletedDownloadCard(
    item: CompletedDownload,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onPlay() },
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
            // Thumbnail with Duration pill
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                if (!item.thumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = item.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (item.ext in listOf("MP3", "M4A", "WAV")) Icons.Outlined.Audiotrack else Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Small badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.ext,
                        color = TextWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${item.resolution} • ${item.fileSizeFormatted} • ${item.completedDateFormatted}",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            Box {
                IconButton(
                    onClick = { expandedMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(CardBackground)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play", color = TextWhite) },
                        onClick = {
                            expandedMenu = false
                            onPlay()
                        },
                        leadingIcon = { Icon(Icons.Outlined.PlayArrow, null, tint = AccentCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Share", color = TextWhite) },
                        onClick = {
                            expandedMenu = false
                            onShare()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Share, null, tint = TextGray) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = StatusDanger) },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = StatusDanger) }
                    )
                }
            }
        }
    }
}

private fun openFileWithPlayer(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val ext = file.extension.lowercase()
        val mime = if (ext in listOf("mp3", "m4a", "wav", "aac", "opus")) "audio/*" else "video/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareMediaFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) return
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val ext = file.extension.lowercase()
        val mime = if (ext in listOf("mp3", "m4a", "wav", "aac")) "audio/*" else "video/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share media"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
