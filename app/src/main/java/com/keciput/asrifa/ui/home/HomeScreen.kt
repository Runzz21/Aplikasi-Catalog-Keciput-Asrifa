package com.keciput.asrifa.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.R
import com.keciput.asrifa.domain.model.Banner
import com.keciput.asrifa.domain.model.Category
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.ui.components.*
import com.keciput.asrifa.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSnackClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllClick: (SeeAllType) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val featuredSnacks = uiState.featuredSnacks
    val popularSnacks = uiState.popularSnacks
    val flashSaleSnacks = uiState.flashSaleSnacks
    val popularChunks = remember(popularSnacks) { popularSnacks.chunked(2) }
    
    val context = LocalContext.current
    val snackbarState = rememberKeciputSnackbarState()
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.toggleNotification()
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier.padding(padding)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // TOP SECTION & BANNER
                    item {
                        HomeTopSection(
                            banners = uiState.banners,
                            isNotificationEnabled = uiState.storeInfo.isNotificationEnabled,
                            onSearchClick = onSearchClick,
                            onNotificationClick = {
                                val willEnable = !uiState.storeInfo.isNotificationEnabled
                                if (willEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleNotification()
                                }
                                
                                // Show Validation UI
                                snackbarState.show(
                                    KeciputSnackbarData(
                                        message = if (willEnable) "Notifikasi Info Toko diaktifkan" else "Notifikasi Info Toko dimatikan",
                                        type = SnackbarType.INFO,
                                        icon = if (willEnable) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff
                                    )
                                )
                            },
                            isLoading = uiState.isLoading
                        )
                    }

                    // SCALLOPED DIVIDER
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(Brush.verticalGradient(listOf(CoralSoft.copy(0.2f), Color.Transparent)))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = 12.dp.toPx()
                                val count = (size.width / (radius * 2)).toInt()
                                val step = size.width / count
                                val path = Path()
                                path.moveTo(0f, 0f)
                                for (i in 0 until count) {
                                    val x = i * step
                                    path.quadraticTo(x + step / 2, radius * 1.5f, x + step, 0f)
                                }
                                drawPath(path, color = Cream)
                            }
                        }
                    }

                    when {
                        uiState.isLoading -> {
                            item { HomeLoadingSkeleton() }
                        }
                        uiState.error != null -> {
                            item {
                                HomeValidationState(
                                    icon = Icons.Default.ErrorOutline,
                                    title = "Ups, Terjadi Kesalahan",
                                    subtitle = uiState.error ?: "Gagal memuat data. Periksa koneksi internetmu.",
                                    actionText = "Coba Lagi",
                                    onAction = { viewModel.refreshData() }
                                )
                            }
                        }
                        featuredSnacks.isEmpty() && popularSnacks.isEmpty() && flashSaleSnacks.isEmpty() -> {
                            item {
                                HomeValidationState(
                                    icon = Icons.Default.Inventory2,
                                    title = "Produk Belum Tersedia",
                                    subtitle = "Saat ini kami sedang menyiapkan produk-produk terbaik untukmu. Kembali lagi nanti ya!",
                                    actionText = "Segarkan",
                                    onAction = { viewModel.refreshData() }
                                )
                            }
                        }
                        else -> {
                            // FLASH SALE
                            if (flashSaleSnacks.isNotEmpty()) {
                                item {
                                    FlashSaleSection(
                                        snacks = flashSaleSnacks,
                                        h = timerState.hour, m = timerState.minute, s = timerState.second,
                                        onSnackClick = onSnackClick
                                    )
                                }
                            }

                            // PRODUK UNGGULAN
                            if (featuredSnacks.isNotEmpty()) {
                                item { SectionHeader(title = "Produk Unggulan", onSeeAll = { onSeeAllClick(SeeAllType.FEATURED) }) }
                                items(featuredSnacks) { snack ->
                                    FeaturedSnackCard(
                                        snack = snack, 
                                        onClick = { onSnackClick(snack.id) },
                                        onAddToCart = { 
                                            viewModel.addToCart(snack)
                                            snackbarState.show(
                                                KeciputSnackbarData(
                                                    message = "${snack.name} ditambahkan ke pesanan!",
                                                    type = SnackbarType.SUCCESS,
                                                    icon = Icons.Default.ShoppingBag
                                                )
                                            )
                                        }
                                    )
                                }
                            }

                            // TERPOPULER
                            if (popularSnacks.isNotEmpty()) {
                                item { SectionHeader(title = "Produk Terpopuler", onSeeAll = { onSeeAllClick(SeeAllType.POPULAR) }) }
                                items(popularChunks) { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        row.forEach { snack ->
                                            PopularGridCard(
                                                snack = snack, 
                                                modifier = Modifier.weight(1f), 
                                                onClick = { onSnackClick(snack.id) },
                                                onAddToCart = {
                                                    viewModel.addToCart(snack)
                                                    snackbarState.show(
                                                        KeciputSnackbarData(
                                                            message = "${snack.name} ditambahkan ke pesanan!",
                                                            type = SnackbarType.SUCCESS,
                                                            icon = Icons.Default.ShoppingBag
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                        if (row.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        StoreInfoBanner(onMapsClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Keciput+Asrifa+Mojokerto"))
                            context.startActivity(intent)
                        })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

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

@Composable
fun HomeValidationState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CoralSoft.copy(0.15f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(32.dp),
                tint = CoralMid
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(52.dp).padding(horizontal = 32.dp)
        ) {
            Text(actionText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeaturedSnackCard(snack: Snack, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.2f), spotColor = CoralMid.copy(0.2f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.height(120.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(120.dp).fillMaxHeight()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(snack.imageUrl).size(120).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(CoralSoft.copy(0.1f))
                )
                if (snack.originalPrice != null) {
                    val discount = ((snack.originalPrice - snack.price) / snack.originalPrice * 100).toInt()
                    DiscountBadge(text = "Hemat $discount%")
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Text(snack.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(snack.category, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        if (snack.originalPrice != null) {
                            Text("Rp${snack.originalPrice.toInt().formatRupiah()}", style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough))
                        }
                        Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.titleMedium.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = onAddToCart,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralMid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PopularGridCard(snack: Snack, modifier: Modifier, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.2f), spotColor = CoralMid.copy(0.2f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(snack.imageUrl).size(140).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                if (snack.originalPrice != null) {
                    val discount = ((snack.originalPrice - snack.price) / snack.originalPrice * 100).toInt()
                    DiscountBadge(text = "-$discount%")
                }
                Surface(
                    onClick = onAddToCart,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(0.85f)
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = CoralMid, modifier = Modifier.size(14.dp))
                        Text("Tambah", style = MaterialTheme.typography.labelSmall.copy(color = CoralMid, fontWeight = FontWeight.Bold))
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(snack.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.titleSmall.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold))
            }
        }
    }
}

@Composable
fun DiscountBadge(text: String) {
    Surface(
        color = CoralMid,
        shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun FlashSaleSection(snacks: List<Snack>, h: String, m: String, s: String, onSnackClick: (Int) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = CoralDark.copy(0.3f), spotColor = CoralDark.copy(0.3f))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(CoralDark, CoralMid)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Bolt, null, tint = Color.Yellow, 
                    modifier = Modifier.size(24.dp).graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                )
                Spacer(Modifier.width(8.dp))
                Text("Flash Sale", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimerBox(h); Text(":", color = Color.White, modifier = Modifier.padding(horizontal = 2.dp)); 
                TimerBox(m); Text(":", color = Color.White, modifier = Modifier.padding(horizontal = 2.dp)); 
                TimerBox(s)
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(snacks) { snack ->
                FlashSaleCard(snack, onClick = { onSnackClick(snack.id) })
            }
        }
    }
}

@Composable
fun FlashSaleCard(snack: Snack, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(snack.imageUrl).size(110).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(110.dp))
        }
        Column(Modifier.padding(8.dp)) {
            Text(snack.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.labelLarge.copy(color = CoralMid, fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(6.dp))
            // Dummy Progress Bar
            val progress = remember { (30..90).random() / 100f }
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = CoralMid,
                    trackColor = CoralSoft.copy(0.3f)
                )
                Text("Tersisa ${(progress * 100).toInt()}%", fontSize = 8.sp, color = InkMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
fun TimerBox(t: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White.copy(0.2f)).padding(horizontal = 6.dp, vertical = 4.dp)) {
        Text(t, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun HomeTopSection(
    banners: List<Banner>, 
    isNotificationEnabled: Boolean,
    onSearchClick: () -> Unit, 
    onNotificationClick: () -> Unit,
    isLoading: Boolean
) {
    val pagerState = rememberPagerState(pageCount = { if (banners.isEmpty()) 1 else banners.size })

    LaunchedEffect(banners) {
        if (banners.size > 1) {
            while (true) {
                delay(4000)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.sweepGradient(listOf(CoralDark, CoralMid, CoralSoft, CoralDark))) // Diagonal effect with sweep
            .sesamePattern()
    ) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.logokeciputasrifa), contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Keciput Asrifa", style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
                Text("Oleh-oleh Khas Mojokerto", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = if (isNotificationEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff, 
                    null, 
                    tint = Color.White
                )
            }
        }
        
        // Search Bar
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { onSearchClick() }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = CoralMid)
                Spacer(Modifier.width(12.dp))
                Text("Cari snack favoritmu...", color = InkMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(16.dp))
        
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier.height(190.dp), 
            contentPadding = PaddingValues(horizontal = 24.dp), 
            pageSpacing = 12.dp
        ) { page ->
            if (banners.isNotEmpty()) BannerCard(banners[page]) else Box(Modifier.fillMaxSize().shimmerEffect())
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun BannerCard(banner: Banner) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CoralMid)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Pattern for Banner
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(0.1f), radius = 100.dp.toPx(), center = Offset(size.width, 0f))
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1.2f).padding(20.dp)) {
                    Surface(color = Color.White.copy(0.2f), shape = CircleShape) {
                        Text(banner.promoLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(banner.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                    Text(banner.subtitle, color = Color.White.copy(0.9f), style = MaterialTheme.typography.bodySmall)
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(banner.imageUrl).size(160).crossfade(true).build(), 
                    contentDescription = null, 
                    contentScale = ContentScale.Crop, 
                    modifier = Modifier.weight(0.8f).fillMaxHeight().clip(RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp))
                )
            }
        }
    }
}

@Composable
fun HomeLoadingSkeleton() {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
        repeat(3) { HorizontalSnackSkeleton() }
    }
}

@Composable
fun HorizontalSnackSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(Color.White)
            .height(110.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(110.dp).fillMaxHeight().shimmerEffect())
        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).shimmerEffect())
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 8.dp), 
        horizontalArrangement = Arrangement.SpaceBetween, 
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Box(Modifier.width(32.dp).height(4.dp).clip(CircleShape).background(CoralMid))
        }
        TextButton(onClick = onSeeAll) {
            Text("Lihat semua", style = MaterialTheme.typography.labelLarge.copy(color = CoralMid))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = CoralMid, modifier = Modifier.size(16.dp).padding(start = 4.dp))
        }
    }
}

@Composable
fun StoreInfoBanner(onMapsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = CoralMid.copy(0.1f), spotColor = CoralMid.copy(0.1f))
            .clickable { onMapsClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.lokasitoko),
                contentDescription = "Preview Lokasi Toko",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Lokasi Toko", style = MaterialTheme.typography.titleSmall)
                Text("Jl. Raya Jabon No.08, Mojokerto", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Directions, null, tint = CoralMid, modifier = Modifier.size(14.dp))
                    Text(" Lihat Rute", color = CoralMid, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = InkMuted.copy(0.4f), modifier = Modifier.size(20.dp))
        }
    }
}
