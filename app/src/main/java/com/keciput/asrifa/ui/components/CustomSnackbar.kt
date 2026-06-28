package com.keciput.asrifa.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keciput.asrifa.ui.theme.*

// ── Tipe snackbar ────────────────────────────────────────
enum class SnackbarType {
    SUCCESS, ERROR, WARNING, INFO, UNDO
}

// ── Data class untuk custom snackbar ─────────────────────
data class KeciputSnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val type: SnackbarType = SnackbarType.SUCCESS,
    val icon: ImageVector? = null
)

// ── State holder ─────────────────────────────────────────
class KeciputSnackbarState {
    var currentSnackbar by mutableStateOf<KeciputSnackbarData?>(null)
        private set

    fun show(data: KeciputSnackbarData) {
        currentSnackbar = data
    }

    fun dismiss() {
        currentSnackbar = null
    }
}

@Composable
fun rememberKeciputSnackbarState(): KeciputSnackbarState {
    return remember { KeciputSnackbarState() }
}

// ── Host Container ────────────────────────────────────────
@Composable
fun KeciputSnackbarHost(
    state: KeciputSnackbarState,
    onAction: (() -> Unit)? = null,
    durationMs: Long = 3000L
) {
    val snackbarData = state.currentSnackbar

    LaunchedEffect(snackbarData) {
        if (snackbarData != null) {
            kotlinx.coroutines.delay(durationMs)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = snackbarData != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        ) + fadeOut()
    ) {
        snackbarData?.let { data ->
            KeciputSnackbarItem(
                data = data,
                onAction = {
                    onAction?.invoke()
                    state.dismiss()
                },
                onDismiss = { state.dismiss() }
            )
        }
    }
}

// ── Item UI ───────────────────────────────────────────────
@Composable
fun KeciputSnackbarItem(
    data: KeciputSnackbarData,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val (bgBrush, accentColor, iconDefault) = when (data.type) {
        SnackbarType.SUCCESS -> Triple(
            Brush.horizontalGradient(listOf(CoralDark, CoralMid)),
            Color.White,
            Icons.Default.CheckCircle
        )
        SnackbarType.UNDO -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))),
            CoralSoft,
            Icons.Default.Undo
        )
        SnackbarType.ERROR -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFC0392B))),
            Color.White,
            Icons.Default.Error
        )
        SnackbarType.WARNING -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF8B6914), Color(0xFFBA7517))),
            Color.White,
            Icons.Default.Warning
        )
        SnackbarType.INFO -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1976D2))),
            Color.White,
            Icons.Default.Info
        )
    }

    val icon = data.icon ?: iconDefault
    val textColor = accentColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CoralMid.copy(0.3f),
                spotColor = CoralMid.copy(0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
    ) {
        // Decorative dots pattern
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White.copy(alpha = 0.03f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Message
            Text(
                text = data.message,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )

            // Action button
            if (data.actionLabel != null) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = onAction,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = data.actionLabel,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = textColor.copy(0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
