package com.arsave.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.ui.theme.*

enum class DrawerDestination {
    HOME,
    DOWNLOADS,
    LIBRARY,
    SETTINGS,
    ABOUT,
    HELP
}

@Composable
fun SideDrawerContent(
    currentDestination: DrawerDestination,
    onSelectDestination: (DrawerDestination) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(ObsidianBackground)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        // Drawer Header with Logo and Close '✕'
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ArSaveBrandLogo(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextWhite, fontWeight = FontWeight.ExtraBold)) {
                            append("AR ")
                        }
                        withStyle(style = SpanStyle(color = AccentCyan, fontWeight = FontWeight.ExtraBold)) {
                            append("SAVE")
                        }
                    },
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
            }

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close Drawer",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Navigation Items
        DrawerItem(
            label = "Home",
            icon = Icons.Outlined.Home,
            isSelected = currentDestination == DrawerDestination.HOME,
            onClick = { onSelectDestination(DrawerDestination.HOME) }
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            label = "Downloads",
            icon = Icons.Outlined.FileDownload,
            isSelected = currentDestination == DrawerDestination.DOWNLOADS,
            onClick = { onSelectDestination(DrawerDestination.DOWNLOADS) }
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            label = "Library",
            icon = Icons.Outlined.Folder,
            isSelected = currentDestination == DrawerDestination.LIBRARY,
            onClick = { onSelectDestination(DrawerDestination.LIBRARY) }
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            label = "Settings",
            icon = Icons.Outlined.Settings,
            isSelected = currentDestination == DrawerDestination.SETTINGS,
            onClick = { onSelectDestination(DrawerDestination.SETTINGS) }
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            label = "About",
            icon = Icons.Outlined.Info,
            isSelected = currentDestination == DrawerDestination.ABOUT,
            onClick = { onSelectDestination(DrawerDestination.ABOUT) }
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            label = "Help & Support",
            icon = Icons.Outlined.HelpOutline,
            isSelected = currentDestination == DrawerDestination.HELP,
            onClick = { onSelectDestination(DrawerDestination.HELP) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom note
        Text(
            text = "AR SAVE v1.0.0",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) SurfaceVariantDark else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (isSelected) AccentCyan else TextGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (isSelected) TextWhite else TextGray,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
