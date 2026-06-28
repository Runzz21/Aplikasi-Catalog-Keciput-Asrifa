package com.keciput.asrifa.ui.pesanan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.keciput.asrifa.ui.components.sesamePattern
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.PackagingType
import com.keciput.asrifa.ui.components.formatRupiah
import com.keciput.asrifa.ui.components.KeciputSnackbarData
import com.keciput.asrifa.ui.components.KeciputSnackbarHost
import com.keciput.asrifa.ui.components.SnackbarType
import com.keciput.asrifa.ui.components.rememberKeciputSnackbarState
import com.keciput.asrifa.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

// Signature Scalloped Shape for the Header Transition
class ScallopedBottomShape(private val scallopRadius: Float = 10f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            val scallopHeight = 20f
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - scallopHeight)
            val count = (size.width / (scallopRadius * 2)).toInt().coerceAtLeast(1)
            val step = size.width / count
            for (i in 0 until count) {
                val x = size.width - (i * step)
                quadraticTo(x - step / 2, size.height, x - step, size.height - scallopHeight)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

// Signature Scalloped Shape for the Checkout Bar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananScreen(
    onExploreClick: () -> Unit,
    onSnackClick: (Int) -> Unit,
    onLoginClick: () -> Unit = {},
    viewModel: PesananViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarState = rememberKeciputSnackbarState()
    
    var itemToDelete by remember { mutableStateOf<CartItem?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    
    val totalItems = uiState.cartItems.sumOf { it.quantity }

    Scaffold(
        containerColor = Cream,
        topBar = {
            PesananHeader(
                itemCount = totalItems,
                onClearCart = { showClearConfirm = true }
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                if (uiState.isLoggedIn) {
                    CheckoutBar(
                        totalPrice = uiState.totalPrice,
                        itemCount = totalItems,
                        note = note,
                        onNoteChange = { note = it },
                        onCheckout = {
                            viewModel.saveOrderHistory(note)
                            sendWhatsAppMessage(context, uiState.cartItems, uiState.totalPrice, note)
                        }
                    )
                } else {
                    LoginPromptBar(onLoginClick = onLoginClick)
                }
            }
        },
        snackbarHost = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                KeciputSnackbarHost(
                    state = snackbarState,
                    onAction = { viewModel.undoRemove() }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
            } else if (uiState.cartItems.isEmpty()) {
                EmptyCartState(onExploreClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.cartItems, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            onClick = { onSnackClick(item.snackId) },
                            onUpdateQuantity = { viewModel.updateQuantity(item.id, it) },
                            onRemove = { itemToDelete = item }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (itemToDelete != null) {
        ModalBottomSheet(
            onDismissRequest = { itemToDelete = null },
            sheetState = sheetState,
            containerColor = Cream
        ) {
            DeleteConfirmSheet(
                snackName = itemToDelete?.snackName ?: "",
                onDismiss = { itemToDelete = null },
                onConfirm = {
                    val item = itemToDelete!!
                    viewModel.removeItem(item)
                    itemToDelete = null
                    snackbarState.show(
                        KeciputSnackbarData(
                            message = "${item.snackName} dihapus dari pesanan",
                            actionLabel = "Urungkan",
                            type = SnackbarType.UNDO,
                            icon = Icons.Default.Delete
                        )
                    )
                }
            )
        }
    }

    if (showClearConfirm) {
        ModalBottomSheet(
            onDismissRequest = { showClearConfirm = false },
            sheetState = sheetState,
            containerColor = Cream
        ) {
            ClearCartConfirmSheet(
                onDismiss = { showClearConfirm = false },
                onConfirm = {
                    viewModel.clearCartWithUndo()
                    showClearConfirm = false
                    snackbarState.show(
                        KeciputSnackbarData(
                            message = "Semua item dihapus dari pesanan",
                            actionLabel = "Urungkan",
                            type = SnackbarType.UNDO,
                            icon = Icons.Default.DeleteSweep
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun DeleteConfirmSheet(
    snackName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BaseConfirmSheet(
        title = "Hapus dari Pesanan?",
        description = "\"$snackName\" akan dihapus dari keranjang pesananmu. Kamu bisa urungkan setelah menghapus.",
        icon = Icons.Default.Delete,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun ClearCartConfirmSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BaseConfirmSheet(
        title = "Kosongkan Keranjang?",
        description = "Semua produk dalam keranjang pesananmu akan dihapus. Kamu bisa mengembalikan semuanya dengan tombol urungkan.",
        icon = Icons.Default.DeleteSweep,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun BaseConfirmSheet(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Ink.copy(0.12f), RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.height(24.dp))

        // Icon besar dengan background gradient
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Brush.radialGradient(listOf(CoralSoft.copy(0.25f), CoralSoft.copy(0.05f))),
                    CircleShape
                )
                .border(1.5.dp, CoralMid.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                tint = CoralDark,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            description,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Ink.copy(0.12f))
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = InkMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Batal", color = Ink, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Ya, Hapus", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun PesananHeader(
    itemCount: Int,
    onClearCart: () -> Unit
) {
    val scallopedBottom = remember { ScallopedBottomShape() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(CoralDark, CoralMid)),
                shape = scallopedBottom
            )
            .sesamePattern(alpha = 0.06f)
    ) {
        // Area transparan untuk status bar
        Spacer(Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CoralMid)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ShoppingBag, 
                        null, 
                        tint = Color.White, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (itemCount > 0) {
                    Surface(
                        color = Gold,
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, CoralMid),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                    ) {
                        Text(
                            text = itemCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Keranjang Pesanan", 
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                val subtitle = if (itemCount > 0) "$itemCount item menunggu checkout" else "Belum ada item"
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f))
                )
            }
            
            if (itemCount > 0) {
                Surface(
                    onClick = onClearCart,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DeleteSweep, 
                            "Bersihkan", 
                            tint = Color.White, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onClick: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = CoralMid.copy(alpha = 0.25f),
                spotColor = CoralMid.copy(alpha = 0.25f)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        color = CoralMid,
                        size = Size(width = 3.dp.toPx(), height = size.height)
                    )
                }
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).size(85).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CoralSoft.copy(0.1f))
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.snackName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.selectedVariant != null) "${item.packagingType} • ${item.selectedVariant}" else item.packagingType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Rp${item.pricePerUnit.toInt().formatRupiah()}",
                    style = MaterialTheme.typography.titleMedium.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, null, tint = InkMuted.copy(0.4f), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(CoralSoft.copy(0.15f), CircleShape)
                        .padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onUpdateQuantity(item.quantity - 1) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = CoralMid, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "${item.quantity}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQuantity(item.quantity + 1) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = CoralMid, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutBar(
    totalPrice: Double, 
    itemCount: Int,
    note: String,
    onNoteChange: (String) -> Unit,
    onCheckout: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "bounce")
    val scallopedTop = remember { ScallopedTopShape() }
    
    Surface(
        color = Color.White,
        shadowElevation = 24.dp,
        shape = scallopedTop
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = { Text("Tambahkan catatan untuk penjual...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralMid,
                    unfocusedBorderColor = Ink.copy(0.1f),
                    focusedContainerColor = Cream.copy(0.4f),
                    unfocusedContainerColor = Cream.copy(0.4f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("$itemCount item", style = MaterialTheme.typography.labelMedium, color = InkMuted)
                    Text("Total Pembayaran", style = MaterialTheme.typography.labelLarge, color = Ink)
                    Text(
                        "Rp${totalPrice.toInt().formatRupiah()}",
                        style = MaterialTheme.typography.headlineSmall.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold)
                    )
                }
                Button(
                    onClick = { 
                        isPressed = true
                        onCheckout()
                    },
                    modifier = Modifier
                        .height(54.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenWa),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pesan via WA", style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
    
    if (isPressed) {
        LaunchedEffect(Unit) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun LoginPromptBar(onLoginClick: () -> Unit) {
    val scallopedTop = remember { ScallopedTopShape() }
    Surface(
        color = Color.White,
        shadowElevation = 24.dp,
        shape = scallopedTop
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Login untuk Memesan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Silakan masuk atau daftar akun terlebih dahulu untuk memesan",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Masuk / Daftar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyCartState(onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CoralSoft.copy(0.15f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                null,
                modifier = Modifier.padding(32.dp),
                tint = CoralMid
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Pesanan kamu masih kosong",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Yuk, pilih oleh-oleh favoritmu sekarang dan kumpulkan di sini sebelum dipesan!",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onExploreClick,
            colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(52.dp).padding(horizontal = 32.dp)
        ) {
            Text("Mulai Belanja", fontWeight = FontWeight.Bold)
        }
    }
}

private fun sendWhatsAppMessage(context: Context, items: List<CartItem>, total: Double, note: String) {
    val phoneNumber = "6285730166721"
    val sb = StringBuilder("Halo Kak, saya ingin memesan dari Keciput Asrifa:\n\n")
    
    items.forEachIndexed { index, item ->
        val subtotal = item.quantity * item.pricePerUnit
        val variantSuffix = if (item.selectedVariant != null) " ${item.selectedVariant}" else ""
        sb.append("${index + 1}. ${item.snackName}$variantSuffix (x${item.quantity}) — Rp${subtotal.toInt().formatRupiah()}\n")
    }
    
    sb.append("\nTotal: Rp${total.toInt().formatRupiah()}\n\n")
    
    if (note.isNotBlank()) {
        sb.append("Catatan: $note\n\n")
    }
    
    sb.append("Mohon info ketersediaan & cara pembayarannya. Terima kasih 🙏")
    
    val message = URLEncoder.encode(sb.toString(), "UTF-8")
    val uri = Uri.parse("https://wa.me/$phoneNumber?text=$message")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
    }
}
