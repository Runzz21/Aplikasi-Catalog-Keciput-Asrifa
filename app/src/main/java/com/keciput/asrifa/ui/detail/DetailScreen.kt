package com.keciput.asrifa.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.domain.model.PackagingType
import com.keciput.asrifa.domain.model.Review
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.ui.components.SectionHeader
import com.keciput.asrifa.ui.components.SnackCard
import com.keciput.asrifa.ui.components.formatRupiah
import com.keciput.asrifa.ui.components.KeciputSnackbarData
import com.keciput.asrifa.ui.components.KeciputSnackbarHost
import com.keciput.asrifa.ui.components.SnackbarType
import com.keciput.asrifa.ui.components.rememberKeciputSnackbarState
import com.keciput.asrifa.ui.components.ScallopedTopShape
import com.keciput.asrifa.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    snackId: Int,
    onBack: () -> Unit,
    onSnackClick: (Int) -> Unit,
    onLoginClick: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snack = uiState.snack
    val context = LocalContext.current
    val snackbarState = rememberKeciputSnackbarState()
    val scope = rememberCoroutineScope()

    var showAllReviews by remember { mutableStateOf(false) }
    val reviewSheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = Cream,
        snackbarHost = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            snack?.let {
                DetailBottomBar(
                    quantity = uiState.quantity,
                    onQtyChange = { viewModel.updateQuantity(it) },
                    onAddToCart = {
                        viewModel.addToCart()
                        snackbarState.show(
                            KeciputSnackbarData(
                                message = "${it.name} ditambahkan ke pesanan!",
                                type = SnackbarType.SUCCESS,
                                icon = Icons.Default.ShoppingBag
                            )
                        )
                    },
                    onWhatsAppClick = {
                        if (uiState.isLoggedIn) {
                            val message = "Halo, saya tertarik dengan ${it.name}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6285730166721?text=${Uri.encode(message)}"))
                            context.startActivity(intent)
                        } else {
                            onLoginClick()
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingFullScreen()
        } else if (snack == null) {
            ErrorFullScreen(message = "Snack tidak ditemukan", onBack = onBack)
        } else {
            // Box utama menggunakan fillMaxSize TAPI padding hanya untuk bagian bawah (agar tidak tertutup BottomBar)
            Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                // 1. Hero Image - MENTOK KE ATAS (y=0)
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(snack.imageUrl).size(320).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Ink.copy(0.6f)),
                                    startY = 400f
                                )
                            )
                    )
                }

                // Top Floating Action Buttons (Back & Share)
                // zIndex(1f) dan statusBarsPadding() memastikan tombol aman di bawah status bar
                Row(
                    modifier = Modifier
                        .zIndex(1f)
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.White.copy(0.8f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink)
                    }
                    IconButton(
                        onClick = {
                            val shareText = "Cek ${snack.name} di Keciput Asrifa! Rp${snack.price.toInt().formatRupiah()}"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.background(Color.White.copy(0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, null, tint = Ink)
                    }
                }

                // 2. Main Content Card
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 280.dp)
                ) {
                    item {
                        val scallopedTop = remember { ScallopedTopShape() }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = scallopedTop,
                            color = Cream
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryBadge(snack.category, snack.categoryId)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(18.dp))
                                    Text(" ${String.format(Locale.US, "%.1f", snack.rating)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(" (${snack.reviewCount} ulasan)", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(Modifier.height(16.dp))
                                Text(snack.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                                
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.headlineMedium.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold))
                                    if (snack.originalPrice != null) {
                                        Text("Rp${snack.originalPrice.toInt().formatRupiah()}", style = MaterialTheme.typography.titleSmall.copy(color = InkMuted, textDecoration = TextDecoration.LineThrough))
                                        val discountText = "Hemat Rp${(snack.originalPrice - snack.price).toInt().formatRupiah()}"
                                        Surface(color = CoralSoft.copy(0.2f), shape = RoundedCornerShape(4.dp)) {
                                            Text(discountText, color = CoralDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                Spacer(Modifier.height(24.dp))
                                Text("Deskripsi", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Text(snack.description, style = MaterialTheme.typography.bodyMedium, color = Ink.copy(0.8f), lineHeight = 24.sp)

                                if (snack.hasBulkPackaging) {
                                    Spacer(Modifier.height(24.dp))
                                    Text("Pilihan Kemasan", style = MaterialTheme.typography.titleSmall)
                                    Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DetailOptionChip("Eceran (250g)", uiState.selectedPackaging == PackagingType.ECERAN) {
                                            viewModel.onPackagingSelected(PackagingType.ECERAN)
                                        }
                                        DetailOptionChip("Los (Grosir 3-5kg)", uiState.selectedPackaging == PackagingType.LOS) {
                                            viewModel.onPackagingSelected(PackagingType.LOS)
                                        }
                                    }
                                }

                                if (snack.variants.isNotEmpty()) {
                                    Spacer(Modifier.height(20.dp))
                                    Text("Pilihan Varian", style = MaterialTheme.typography.titleSmall)
                                    LazyRow(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(snack.variants) { variant ->
                                            DetailOptionChip(variant, uiState.selectedVariant == variant) {
                                                viewModel.onVariantSelected(variant)
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(32.dp))
                                HorizontalDivider(thickness = 1.dp, color = Ink.copy(0.08f))
                                Spacer(Modifier.height(32.dp))

                                DetailedReviewSection(
                                    rating = snack.rating,
                                    reviewCount = snack.reviewCount,
                                    reviews = uiState.reviews,
                                    onSeeAllClick = { showAllReviews = true }
                                )
                                
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }

                    if (uiState.relatedSnacks.isNotEmpty()) {
                        item {
                            Column(Modifier.padding(bottom = 40.dp)) {
                                SectionHeader(title = "Mungkin Kamu Suka")
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(uiState.relatedSnacks) { related ->
                                        SnackCard(
                                            snack = related,
                                            modifier = Modifier.width(170.dp).shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.2f), spotColor = CoralMid.copy(0.2f)),
                                            onClick = { onSnackClick(related.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } // Close LazyColumn


                // Custom Snackbar overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    KeciputSnackbarHost(state = snackbarState)
                }
            }
        }
    }

    if (showAllReviews) {
        ModalBottomSheet(
            onDismissRequest = { showAllReviews = false },
            sheetState = reviewSheetState,
            containerColor = Cream
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 40.dp)) {
                Text("Semua Ulasan", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(uiState.reviews) { review ->
                        DetailReviewItem(review)
                        HorizontalDivider(thickness = 0.5.dp, color = Ink.copy(0.05f), modifier = Modifier.padding(top = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String, categoryId: String) {
    val icon = when {
        categoryId.contains("kerupuk") -> Icons.Default.BakeryDining
        categoryId.contains("kacang") -> Icons.Default.Grain
        categoryId.contains("kue") -> Icons.Default.Cookie
        categoryId.contains("singkong") -> Icons.Default.LunchDining
        else -> Icons.Default.LocalMall
    }
    Surface(color = CoralSoft.copy(0.12f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = CoralDark, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(category, color = CoralDark, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailOptionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CoralMid else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Ink.copy(0.1f)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = if (isSelected) Color.White else Ink, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DetailedReviewSection(rating: Double, reviewCount: Int, reviews: List<Review>, onSeeAllClick: () -> Unit) {
    val total = reviews.size.coerceAtLeast(1).toFloat()
    val star5 = reviews.count { it.rating == 5 } / total
    val star4 = reviews.count { it.rating == 4 } / total
    val star3 = reviews.count { it.rating == 3 } / total
    val star2 = reviews.count { it.rating == 2 } / total
    val star1 = reviews.count { it.rating == 1 } / total

    Column {
        Text("Ulasan Pembeli", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 32.dp)) {
                Text(String.format(Locale.US, "%.1f", rating), fontSize = 52.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Row {
                    repeat(5) { index ->
                        Icon(Icons.Default.Star, null, tint = if (index < rating.toInt()) Gold else Ink.copy(0.1f), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("$reviewCount ulasan", style = MaterialTheme.typography.bodySmall)
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RatingBar(5, star5)
                RatingBar(4, star4)
                RatingBar(3, star3)
                RatingBar(2, star2)
                RatingBar(1, star1)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        if (reviews.isEmpty()) {
            Text("Belum ada ulasan untuk produk ini.", style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        } else {
            reviews.take(3).forEach { review ->
                DetailReviewItem(review)
                Spacer(Modifier.height(20.dp))
            }
            if (reviews.size > 3) {
                OutlinedButton(
                    onClick = onSeeAllClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CoralMid)
                ) {
                    Text("Lihat Semua ${reviews.size} Ulasan", color = CoralMid, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RatingBar(star: Int, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$star", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(12.dp))
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
            color = Gold,
            trackColor = Ink.copy(0.05f)
        )
    }
}

@Composable
fun DetailReviewItem(review: Review) {
    val formattedDate = remember(review.reviewDate) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        sdf.format(java.util.Date(review.reviewDate))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(CoralSoft.copy(0.25f), CircleShape), contentAlignment = Alignment.Center) {
                Text(review.userName.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = CoralDark)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(review.userName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(formattedDate, style = MaterialTheme.typography.labelSmall, color = InkMuted)
                }
                Row {
                    repeat(5) { index ->
                        Icon(Icons.Default.Star, null, tint = if (index < review.rating) Gold else Ink.copy(0.1f), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = Ink.copy(0.85f), lineHeight = 20.sp)
    }
}

@Composable
fun DetailBottomBar(quantity: Int, onQtyChange: (Int) -> Unit, onAddToCart: () -> Unit, onWhatsAppClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "bounce")

    Surface(shadowElevation = 24.dp, color = Color.White) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Cream, RoundedCornerShape(12.dp)).height(50.dp).padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = { if (quantity > 1) onQtyChange(quantity - 1) }) {
                        Icon(Icons.Default.Remove, null, tint = CoralMid)
                    }
                    Text("$quantity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { onQtyChange(quantity + 1) }) {
                        Icon(Icons.Default.Add, null, tint = CoralMid)
                    }
                }
                
                Button(
                    onClick = { 
                        isPressed = true
                        onAddToCart()
                    },
                    modifier = Modifier.weight(1f).height(50.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralMid),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingBag, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ke Pesanan", fontWeight = FontWeight.Bold)
                }

                Surface(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.size(50.dp),
                    color = GreenWa,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
                    }
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
fun LoadingFullScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CoralMid)
    }
}

@Composable
fun ErrorFullScreen(message: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ErrorOutline, null, tint = InkMuted, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(20.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = CoralMid), shape = RoundedCornerShape(12.dp)) { 
            Text("Kembali", fontWeight = FontWeight.Bold) 
        }
    }
}
