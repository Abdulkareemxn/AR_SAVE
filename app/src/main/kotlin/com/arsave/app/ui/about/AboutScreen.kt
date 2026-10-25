package com.arsave.app.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.ui.components.ArSaveBrandLogo
import com.arsave.app.ui.theme.*

@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

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
                text = "About",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Brand Hero
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArSaveBrandLogo(size = 80.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AR SAVE",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0",
                color = TextGray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Credits Box matching Mockup Screen 10
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    // Graduation Cap Icon Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "Education",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Project by",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "A. Kareem",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Instagram ID: @akareemxn
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariantDark)
                                .clickable { openInstagram(context, "akareemxn") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Instagram",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@akareemxn",
                                color = AccentCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Organization",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "AR INSTITUTE OF COMPUTER AND TECHNOLOGY",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Instagram ID: @ar_institute_
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariantDark)
                                .clickable { openInstagram(context, "ar_institute_") }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Instagram",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@ar_institute_",
                                color = AccentCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A locally processed Android media downloader powered by yt-dlp.",
                    color = TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Secondary Links matching Mockup Screen 10
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            AboutActionItem(
                title = "Open Source Licenses",
                icon = Icons.Outlined.Code,
                onClick = { showLicensesDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))
            AboutActionItem(
                title = "Privacy Information",
                icon = Icons.Outlined.Security,
                onClick = { showPrivacyDialog = true }
            )
            Spacer(modifier = Modifier.height(10.dp))
            AboutActionItem(
                title = "Disclaimer",
                icon = Icons.Outlined.Gavel,
                onClick = { showDisclaimerDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", color = AccentBlue)
                }
            },
            title = { Text("Open Source Licenses", color = TextWhite) },
            text = {
                Text(
                    "• yt-dlp (Unlicense License)\n• Python 3.12 (PSF License)\n• FFmpeg (LGPL/GPL)\n• QuickJS (MIT License)\n• Jetpack Compose & AndroidX (Apache 2.0)",
                    color = TextGray,
                    fontSize = 13.sp
                )
            },
            containerColor = CardBackground
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = AccentBlue)
                }
            },
            title = { Text("Privacy Information", color = TextWhite) },
            text = {
                Text(
                    "AR SAVE operates 100% locally on your device. No analytics, tracking, telemetry, or remote servers are contacted. All video downloads are directly streamed from content hosts to your device storage.",
                    color = TextGray,
                    fontSize = 13.sp
                )
            },
            containerColor = CardBackground
        )
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("Close", color = AccentBlue)
                }
            },
            title = { Text("Disclaimer", color = TextWhite) },
            text = {
                Text(
                    "AR SAVE is designed for personal archival and educational purposes. Please respect copyright laws and the terms of service of each content provider before downloading media.",
                    color = TextGray,
                    fontSize = 13.sp
                )
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun AboutActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openInstagram(context: Context, handle: String) {
    val clean = handle.removePrefix("@")
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/$clean")).apply {
        setPackage("com.instagram.android")
    }
    try {
        context.startActivity(appIntent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$clean"))
        context.startActivity(webIntent)
    }
}
