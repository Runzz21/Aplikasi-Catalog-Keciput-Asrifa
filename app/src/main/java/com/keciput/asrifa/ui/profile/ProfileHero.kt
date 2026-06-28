package com.keciput.asrifa.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileHero(
    name: String,
    email: String,
    viewedCount: Int,
    onEditPhoto: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        // Avatar + tombol edit foto
        Box {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (name.isNotEmpty()) {
                    Text(
                        // Ambil inisial: "Asrifa Keciput" → "AK"
                        name.split(" ").take(2).joinToString("") { it.first().uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            // Tombol kamera di pojok kanan bawah
            SmallFloatingActionButton(
                onClick = onEditPhoto,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Outlined.CameraAlt, "Edit foto", Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            name.ifEmpty { "Pengguna Baru" },
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Text(
            email.ifEmpty { "-" },
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(16.dp))
        // Stats row
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    viewedCount.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    "Produk Dilihat",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
