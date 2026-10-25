package com.arsave.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.R
import com.arsave.app.ui.theme.*

@Composable
fun ArSaveBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_ar_save_logo),
        contentDescription = "AR SAVE Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun ArSaveTopBar(
    onLogoClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Logo + "AR SAVE"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onLogoClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            ArSaveBrandLogo(size = 30.dp)
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
                fontSize = 19.sp,
                letterSpacing = 0.5.sp
            )
        }

        // Right: Settings Gear
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
}
