package com.keciput.asrifa.ui.riwayat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.OrderHistory
import com.keciput.asrifa.ui.components.formatRupiah
import com.keciput.asrifa.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatPesananScreen(
    onBack: () -> Unit,
    viewModel: RiwayatPesananViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat Pesanan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CoralMid
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CoralMid)
                }
            } else if (uiState.orders.isEmpty()) {
                EmptyRiwayatState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderHistoryCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryCard(order: OrderHistory) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = GreenWa,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Pesanan Dikirim",
                        style = MaterialTheme.typography.labelLarge,
                        color = GreenWa,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    dateFormat.format(Date(order.orderDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(alpha = 0.08f))
            Spacer(Modifier.height(12.dp))

            order.items.take(3).forEach { item ->
                RiwayatItemRow(item = item)
                Spacer(Modifier.height(8.dp))
            }

            if (order.items.size > 3) {
                Text(
                    "+${order.items.size - 3} item lainnya",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralMid,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(alpha = 0.08f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${order.itemCount} item",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
                Text(
                    "Rp${order.totalPrice.toInt().formatRupiah()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CoralDark
                    )
                )
            }

            if (order.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Catatan: ${order.note}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = InkMuted,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RiwayatItemRow(item: CartItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CoralSoft.copy(0.1f))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.snackName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val suffix = if (item.selectedVariant != null) " • ${item.selectedVariant}" else ""
            Text(
                "${item.quantity}x ${item.packagingType.name}$suffix",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }
        Text(
            "Rp${(item.quantity * item.pricePerUnit).toInt().formatRupiah()}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Ink
        )
    }
}

@Composable
private fun EmptyRiwayatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CoralSoft.copy(0.15f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                Icons.Outlined.History,
                null,
                modifier = Modifier.padding(32.dp),
                tint = CoralMid
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Belum ada riwayat pesanan",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Pesanan yang sudah kamu kirim melalui WhatsApp akan muncul di sini.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center
        )
    }
}
