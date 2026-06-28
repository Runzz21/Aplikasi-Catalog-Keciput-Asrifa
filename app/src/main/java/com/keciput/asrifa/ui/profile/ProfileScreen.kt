package com.keciput.asrifa.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keciput.asrifa.R
import com.keciput.asrifa.domain.model.StoreInfo
import com.keciput.asrifa.ui.components.sesamePattern
import com.keciput.asrifa.ui.components.shimmerEffect
import com.keciput.asrifa.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class ScallopedTopShape(private val scallopRadius: Float = 12f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val scallopHeight = 20f
            moveTo(0f, scallopHeight)
            val count = (size.width / (scallopRadius * 2)).toInt().coerceAtLeast(1)
            val step = size.width / count
            for (i in 0 until count) {
                val x = i * step
                quadraticTo(x + step / 2, -scallopHeight, x + step, scallopHeight)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun ProfileScreen(
    onRiwayatClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onNotificationToggle(true)
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        if (uiState.isLoading) {
            item { ProfileSkeleton() }
        } else {
            if (uiState.isLoggedIn) {
                item { LoggedInHeroHeader(name = uiState.userName, email = uiState.userEmail) }
            } else {
                item { GuestHeroHeader(onLoginClick = onLoginClick, onRegisterClick = onRegisterClick) }
            }

            item {
                val scallopedTop = remember { ScallopedTopShape() }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    shape = scallopedTop,
                    color = Cream
                ) {
                    Column(Modifier.padding(top = 16.dp)) {
                        val isOpen = isStoreOpen(uiState.storeInfo)
                        StatusCard(isOpen = isOpen, storeInfo = uiState.storeInfo)

                        if (uiState.isLoggedIn) {
                            SectionLabel(text = "AKUN SAYA")
                            RiwayatMenuCard(onRiwayatClick = onRiwayatClick)
                            Spacer(Modifier.height(4.dp))
                        }

                        SectionLabel(text = "PENGATURAN")
                        NotificationSettingCard(
                            enabled = uiState.storeInfo.isNotificationEnabled,
                            onToggle = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.onNotificationToggle(enabled)
                                }
                            }
                        )

                        SectionLabel(text = "KONTAK & LOKASI TOKO")
                        ContactCard()

                        SectionLabel(text = "JAM OPERASIONAL")
                        OperationalHoursCard(storeInfo = uiState.storeInfo)

                        SectionLabel(text = "TENTANG APLIKASI")
                        AboutCard(appVersion = uiState.appVersion)

                        SectionLabel(text = "HUBUNGI & BAGIKAN")
                        CtaButtons(
                            onWaClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6285730166721?text=Halo%20Keciput%20Asrifa"))
                                context.startActivity(intent)
                            },
                            onMapsClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Keciput+Asrifa+Mojokerto"))
                                context.startActivity(intent)
                            }
                        )

                        if (uiState.isLoggedIn) {
                            Spacer(Modifier.height(16.dp))
                            LogoutButton(onLogout = { viewModel.logout() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggedInHeroHeader(name: String, email: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(CoralDark, CoralMid)))
            .sesamePattern(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Person,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (name.isNotBlank()) "Halo, $name!" else "Halo!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (email.isNotBlank()) {
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GuestHeroHeader(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(CoralDark, CoralMid)))
            .sesamePattern(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logokeciputasrifa),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Keciput Asrifa",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                "Oleh-oleh khas Mojokerto",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Masuk", color = CoralDark, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRegisterClick,
                    border = BorderStroke(1.5.dp, Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Daftar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Keluar Akun", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah kamu yakin ingin keluar?") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onLogout() }) {
                    Text("Ya, Keluar", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Button(
        onClick = { showConfirm = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEF9A9A))
    ) {
        Icon(Icons.Outlined.Logout, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Keluar Akun", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NotificationSettingCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CoralMid.copy(alpha = 0.1f),
                spotColor = CoralMid.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CoralSoft.copy(0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Notifications, null, tint = CoralDark, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Notifikasi Info Toko", style = MaterialTheme.typography.labelLarge)
                Text("Dapatkan info buka/tutup & libur", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CoralMid,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Ink.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
private fun RiwayatMenuCard(onRiwayatClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CoralMid.copy(alpha = 0.1f),
                spotColor = CoralMid.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRiwayatClick() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CoralSoft.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.History, null, tint = CoralDark, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Riwayat Pesanan", style = MaterialTheme.typography.labelLarge)
                Text("Lihat pesanan yang sudah dikirim", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Ink.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StatusCard(isOpen: Boolean, storeInfo: StoreInfo) {
    val indicatorColor = if (isOpen) Color(0xFF1D9E75) else Color(0xFFE8593C)
    val bgColor = if (isOpen) Color(0xFFE1F5EE) else Color(0xFFFFEDE7)
    val title = if (isOpen) "Toko sedang buka" else "Toko sedang tutup"
    val sub = if (isOpen) "Tutup pukul ${storeInfo.closeTime} WIB hari ini" else "Buka pukul ${storeInfo.openTime} besok"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = indicatorColor.copy(0.2f),
            spotColor = indicatorColor.copy(0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
                if (isOpen) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                            .clip(CircleShape)
                            .background(indicatorColor.copy(alpha = pulseAlpha))
                    )
                }
                Box(Modifier.size(10.dp).clip(CircleShape).background(indicatorColor))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(color = indicatorColor))
                Text(sub, style = MaterialTheme.typography.bodySmall.copy(color = indicatorColor.copy(0.7f)))
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = indicatorColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun OperationalHoursCard(storeInfo: StoreInfo) {
    val labels = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = CoralMid.copy(alpha = 0.1f),
            spotColor = CoralMid.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column {
            labels.forEachIndexed { index, hari ->
                val isToday = index == today
                val isOperating = storeInfo.operatingDays.contains(index)
                val jam = if (isOperating) "${storeInfo.openTime} – ${storeInfo.closeTime}" else "Tutup"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isToday) CoralSoft.copy(0.1f) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(hari, style = MaterialTheme.typography.labelLarge.copy(color = if (isToday) CoralDark else Ink))
                    Text(jam, style = if (isToday) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = CoralDark) else MaterialTheme.typography.bodySmall.copy(color = if (!isOperating) InkMuted.copy(alpha = 0.5f) else Ink))
                }
                if (index < labels.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(alpha = 0.05f))
                }
            }
        }
    }
}

@Composable
private fun ProfileSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(CoralDark))
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
            Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
            Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
        }
    }
}

@Composable
private fun PillItem(icon: ImageVector, text: String, iconColor: Color) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ContactCard() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = CoralMid.copy(alpha = 0.1f),
            spotColor = CoralMid.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column {
            ContactRow(icon = Icons.Outlined.LocationOn, label = "Alamat", value = "Jl. Raya Jabon No. 08, Mojokerto", onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Keciput+Asrifa+Mojokerto")))
            })
            ContactRow(icon = Icons.Outlined.Phone, label = "WhatsApp", value = "0857-3016-6721", onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6285730166721")))
            })
            ContactRow(icon = Icons.Outlined.Email, label = "Email", value = "keciputasrifa@gmail.com", isLast = true, onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:keciputasrifa@gmail.com"))
                try { context.startActivity(intent) } catch (e: Exception) {}
            })
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String, isLast: Boolean = false, onClick: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CoralSoft.copy(0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = CoralDark, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.labelLarge)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Ink.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
        }
        if (!isLast) HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(alpha = 0.05f), modifier = Modifier.padding(start = 62.dp))
    }
}

@Composable
private fun AboutCard(appVersion: String) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = CoralMid.copy(alpha = 0.1f),
            spotColor = CoralMid.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CoralSoft.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Info, null, tint = CoralDark, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Versi Aplikasi", style = MaterialTheme.typography.labelSmall)
                    Text(appVersion, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(alpha = 0.05f))
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Gold.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Cookie, null, tint = Gold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Katalog digital snack dan oleh-oleh khas Mojokerto dari Asrifah Food Indonesia. Kami berkomitmen menyajikan produk berkualitas tinggi dan layanan terbaik.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = InkMuted
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Kebijakan Privasi",
                        style = MaterialTheme.typography.labelSmall.copy(color = CoralMid, textDecoration = TextDecoration.Underline),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://keciputasrifa.com/privacy-policy"))
                            context.startActivity(intent)
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    Text(
                        "Syarat & Ketentuan",
                        style = MaterialTheme.typography.labelSmall.copy(color = CoralMid, textDecoration = TextDecoration.Underline),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://keciputasrifa.com/terms"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CtaButtons(onWaClick: () -> Unit, onMapsClick: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CtaButton(icon = Icons.AutoMirrored.Filled.Chat, text = "Hubungi via WhatsApp", color = GreenWa, onClick = onWaClick)
        CtaButton(icon = Icons.Outlined.Map, text = "Buka Google Maps", color = Color(0xFF4285F4), onClick = onMapsClick)

        OutlinedButton(
            onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Yuk cek produk oleh-oleh khas Mojokerto di Keciput Asrifa! Download sekarang di Play Store.")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CoralMid)
        ) {
            Icon(Icons.Outlined.Share, null, tint = CoralMid, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Bagikan Aplikasi", color = CoralMid, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CtaButton(icon: ImageVector, text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp),
        color = InkMuted.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

private fun isStoreOpen(info: StoreInfo): Boolean {
    val now = Calendar.getInstance()
    val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1
    if (!info.operatingDays.contains(dayOfWeek)) return false

    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentTime = sdf.format(now.time)

    return currentTime >= info.openTime && currentTime < info.closeTime
}
