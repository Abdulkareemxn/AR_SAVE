package com.arsave.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsave.app.ui.components.ArSaveBrandLogo
import com.arsave.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground),
        contentAlignment = Alignment.Center
    ) {
        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            ArSaveBrandLogo(size = 96.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextWhite, fontWeight = FontWeight.Black)) {
                        append("AR ")
                    }
                    withStyle(style = SpanStyle(color = AccentCyan, fontWeight = FontWeight.Black)) {
                        append("SAVE")
                    }
                },
                fontSize = 32.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Download. Save. Enjoy.",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Glowing Progress Line
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceVariantDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.7f)
                        .alpha(glowAlpha)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentBlue, AccentCyan)
                            )
                        )
                )
            }
        }

        // Bottom Credits
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alphaAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Project by A. Kareem",
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AR INSTITUTE OF COMPUTER\nAND TECHNOLOGY",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
