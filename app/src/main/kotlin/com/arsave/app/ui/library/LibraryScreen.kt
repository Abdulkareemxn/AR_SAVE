package com.arsave.app.ui.library

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.arsave.app.data.CompletedDownload
import com.arsave.app.data.DownloadEngine
import com.arsave.app.ui.theme.*
import java.io.File

@Composable
fun LibraryScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedDownloads by DownloadEngine.completedDownloads.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Videos, 1: Audios, 2: All
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        DownloadEngine.initCompletedFiles()
    }

    val filteredList = remember(completedDownloads, selectedTab, searchQuery) {
        completedDownloads.filter { item ->
            val isAudio = item.ext in listOf("MP3", "M4A", "WAV", "AAC", "OPUS")
            val matchesTab = when (selectedTab) {
                0 -> !isAudio
                1 -> isAudio
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else item.title.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // Top Bar matching Screen 7
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
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
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (!isSearching) {
                Text(
                    text = "Library",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            } else {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search downloaded media...", color = TextMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            IconButton(
                onClick = {
                    isSearching = !isSearching
                    if (!isSearching) searchQuery = ""
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Outlined.Close else Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Filter Pills: [Videos] [Audios] [All]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterPill(
                label = "Videos",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            FilterPill(
                label = "Audios",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            FilterPill(
                label = "All",
                isSelected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Media List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No media matching '$searchQuery'" else "No files in this category",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    LibraryItemCard(
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
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) AccentBlue else CardBackground
    val textColor = if (isSelected) TextWhite else TextGray
    val border = if (isSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, CardBorder)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = border,
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
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
                    .width(76.dp)
                    .height(50.dp)
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

                // Small format pill
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
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val ext = file.extension.lowercase()
        val mime = if (ext in listOf("mp3", "m4a", "wav", "aac")) "audio/*" else "video/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
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
