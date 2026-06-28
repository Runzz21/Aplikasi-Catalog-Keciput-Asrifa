package com.keciput.asrifa.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keciput.asrifa.R
import com.keciput.asrifa.ui.theme.CoralDark
import com.keciput.asrifa.ui.theme.CoralMid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    // Animation states
    val scale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(50f) }
    val taglineAlpha = remember { Animatable(0f) }
    val loadingWidth = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // 1. Logo scales up with a spring effect
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // 2. Main title slides up and fades in
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            textOffsetY.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        }
        
        delay(300)

        // 3. Tagline fades in
        launch {
            taglineAlpha.animateTo(1f, animationSpec = tween(500))
        }

        delay(300)

        // 4. Loading bar expands smoothly
        loadingWidth.animateTo(
            targetValue = 180f, // target width in dp
            animationSpec = tween(1500, easing = LinearOutSlowInEasing)
        )

        // Give user a moment to see the completed animation
        delay(300)

        // Navigate to main screen
        onNavigateToMain()
    }

    // Modern Elegant Orange Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            CoralMid, // Orange Utama
            CoralDark  // Orange Gelap
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - Adjusted to be wider (Horizontal) for "Keciput Panjang"
            Image(
                painter = painterResource(id = R.drawable.logokeciputasrifa),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(260.dp)  // Wider to make text readable
                    .height(120.dp) // Proportional height
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Title - Changed to White for better contrast on Orange background
            Text(
                text = "Keciput Asrifa",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textOffsetY.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline - Changed to White (with transparency) for better contrast
            Text(
                text = "Oleh-oleh Khas Mojokerto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Modern Elegant Loading Bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.3f)) // Track color
                    .alpha(taglineAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .width(loadingWidth.value.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White) // Solid White progress
                )
            }
        }
    }
}
