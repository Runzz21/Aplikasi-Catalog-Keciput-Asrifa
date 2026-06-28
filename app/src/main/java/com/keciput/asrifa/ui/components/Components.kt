package com.keciput.asrifa.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.domain.model.Snack
import java.text.NumberFormat
import java.util.*

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.4f),
        Color.LightGray.copy(alpha = 0.9f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}

fun Modifier.sesamePattern(alpha: Float = 0.08f): Modifier = composed {
    val patternColor = remember { Color.White.copy(alpha = alpha) }
    val dotRadius = 2.dp
    val step = 32.dp

    this.drawBehind {
        val stepPx = step.toPx()
        val radiusPx = dotRadius.toPx()
        val stepInt = stepPx.toInt().coerceAtLeast(1)
        for (x in 0..size.width.toInt() step stepInt) {
            for (y in 0..size.height.toInt() step stepInt) {
                val offset = if ((x / stepInt + y / stepInt) % 2 == 0)
                    Offset(x.toFloat(), y.toFloat())
                else
                    Offset(x + stepPx / 2, y + stepPx / 2)
                drawCircle(patternColor, radiusPx, offset)
            }
        }
    }
}

@Composable
fun SnackCard(
    snack: Snack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAddToCart: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(0.5f))
    ) {
        Column {
            Box {
                val imgContext = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(imgContext).data(snack.imageUrl).size(200).crossfade(true).build(),
                    contentDescription = snack.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart),
                    color = Color.Black.copy(0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        snack.category,
                        color = Color.White,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Column(Modifier.padding(10.dp)) {
                Text(
                    snack.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Rp${snack.price.toInt().formatRupiah()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF993C1D),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "⭐ ${String.format(Locale.US, "%.1f", snack.rating)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    
                    if (onAddToCart != null) {
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFD85A30), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tambah ke Keranjang",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        onSeeAllClick?.let {
            TextButton(onClick = it) {
                Text("Lihat semua", color = Color(0xFFD85A30), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun Int.formatRupiah(): String = NumberFormat.getNumberInstance(Locale("id", "ID")).format(this)
