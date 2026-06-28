# Product Requirement Document (PRD)
# Keciput Asrifa — Toko Oleh-Oleh Digital

> **Versi Dokumen:** 2.1.0
> **Tanggal Revisi:** 18 Juni 2026
> **Status:** Revisi — Menggantikan PRD v1.0.0 secara penuh
> **Stack Teknologi:** Jetpack Compose + Material Design 3

### Ringkasan Perubahan Utama (v1.0 → v2.0)

| # | Perubahan |
|---|-----------|
| 1 | Halaman **Favorite** dihapus total. Digantikan halaman **Pesanan** — keranjang multi-produk yang tetap checkout via WhatsApp. |
| 2 | Detail Screen ditambahkan section **Ulasan Pembeli** (rating breakdown + daftar ulasan). |
| 3 | Model data **Snack** & **Kategori** diperbarui mengikuti katalog produk riil Asrifah Food (7 kategori, 78 SKU nyata). |
| 4 | Kebijakan **Shimmer Effect** & **Pull-to-Refresh** didefinisikan eksplisit per halaman — Info Toko hanya shimmer, tanpa refresh. |

---

## 1. Pendahuluan & Latar Belakang

Keciput Asrifa adalah aplikasi mobile berbasis Android yang dirancang khusus sebagai platform katalog dan etalase digital untuk produk oleh-oleh khas Mojokerto, khususnya kue Keciput dan varian snack tradisional lainnya (kerupuk, kacang, kue kering, olahan tempe & tahu, dsb).

Aplikasi ini bertujuan menjembatani pelanggan dengan toko fisik Keciput Asrifa di Mojokerto dengan menyajikan informasi produk yang lengkap, menarik, interaktif, serta memberikan kemudahan pemesanan — baik satu produk maupun banyak produk sekaligus — yang terintegrasi langsung dengan WhatsApp.

## 2. Tujuan & Audiens Utama

### 2.1 Tujuan Produk

- Menyediakan katalog digital yang informatif, bersih, dan modern bagi seluruh produk Keciput Asrifa / Asrifah Food.
- Meningkatkan *conversion rate* penjualan dengan mengarahkan pembeli langsung ke chat WhatsApp bisnis resmi secara instan.
- Memberikan pengalaman belanja yang efisien lewat fitur **Pesanan (keranjang multi-produk)**, sehingga pelanggan bisa mengumpulkan beberapa item sebelum mengirim satu pesan checkout terstruktur ke WhatsApp.
- Membantu calon pembeli mengambil keputusan lewat **ulasan pembeli** pada tiap produk dan promo terkurasi (Flash Sale/Produk Unggulan).

### 2.2 Audiens Target

- Wisatawan atau pembeli lokal yang mencari oleh-oleh khas Mojokerto.
- Pelanggan setia Keciput Asrifa yang ingin melihat varian produk baru, ulasan dari pembeli lain, atau harga terbaru sebelum melakukan pemesanan.

---

## 3. Arsitektur Informasi & Fitur Utama

Aplikasi menggunakan pola navigasi **Bottom Navigation Tabs** untuk menu utama dan halaman penuh (full-screen push) untuk fungsionalitas spesifik.

### 3.1 Peta Navigasi & Rute Halaman (Routes)

| # | Halaman | Route | Keterangan |
|---|---------|-------|------------|
| 1 | Splash Screen | `splash` | Halaman pembuka, memuat aset branding (Logo). |
| 2 | Home Screen | `home` | Pusat aktivitas pengguna, promo banner, pintasan produk. |
| 3 | Catalog Screen | `catalog?category={category}` | Pencarian berkategori untuk seluruh produk snack. |
| 4 | **Pesanan Screen** *(baru, pengganti Favorite)* | `pesanan` | Keranjang multi-produk sebelum checkout via WhatsApp. |
| 5 | Info Screen | `info` | Informasi detail profil toko dan operasional. |
| 6 | Detail Screen | `detail/{snackId}` | Informasi mendalam per produk + ulasan pembeli. |
| 7 | Search Screen | `search?query={query}` | Hasil pencarian teks bebas untuk produk snack. |

> ⚠️ **Catatan migrasi:** Seluruh referensi route `favorite`, model `isFavorite`, ikon hati (♡) pada kartu produk, dan logika *bookmark* pada versi PRD sebelumnya **dihapus**. Fungsinya digantikan oleh tombol **"+ Tambah ke Pesanan"**.

### 3.2 Bottom Navigation

| Posisi | Label Tab | Icon | Route Target | Badge |
|--------|-----------|------|---------------|-------|
| 1 | Beranda | `Icons.Home` | `home` | – |
| 2 | Katalog | `Icons.Grid` | `catalog` | – |
| 3 | **Pesanan** | `Icons.ShoppingBag` | `pesanan` | Angka jumlah item di keranjang (real-time) |
| 4 | Info Toko | `Icons.Storefront` | `info` | – |

---

## 4. Spesifikasi Fungsional Detail

### 4.1 Home Screen (Beranda)

- **Banner Promo Otomatis:** Horizontal Pager berisi banner promo (`Banner`). Banner berganti otomatis setiap 4 detik secara sirkular.
- **Bilah Pencarian (Search Bar):** Input statis, jika diketuk mengarahkan ke `Search Screen`.
- **Flash Sale Section:** Menampilkan produk bertanda `isFlashSale = true`, dilengkapi **Countdown Timer** real-time (Jam : Menit : Detik).
- **Produk Unggulan & Terpopuler:** Kartu horizontal (`FeaturedSnackCard`) dan grid (`PopularGridCard`).
- **Badge Pesanan:** Ikon tab "Pesanan" pada Bottom Navigation menampilkan jumlah total item di keranjang secara real-time begitu pengguna menambah/menghapus item dari mana pun di aplikasi.
- **Store Info Banner (Integrasi Google Maps):** Banner di bagian bawah beranda; saat diklik membuka aplikasi Google Maps untuk rute "Keciput Asrifa Mojokerto", dengan fallback ke Google Maps browser bila aplikasi Maps tidak terpasang.
- **Loading & Refresh:** Shimmer Effect saat `UiState.Loading`; mendukung **Pull-to-Refresh**.

### 4.2 Catalog Screen

- **Filter Kategori (Chip Horizontal Scrollable):** 7 kategori sesuai katalog Asrifah Food — *Kerupuk & Rambak, Snack Ringan, Kacang-kacangan, Olahan Singkong & Pisang, Olahan Tempe, Kue Kering, Olahan Tahu* — plus chip "Semua" di posisi pertama.
- **Grid Produk 2 Kolom:** Infinite scroll dengan Paging, menampilkan thumbnail, nama, harga (+ harga coret bila diskon), rating ringkas.
- Tap kartu produk → `detail/{snackId}`.
- **Loading & Refresh:** Shimmer Effect saat `UiState.Loading`; mendukung **Pull-to-Refresh**.

### 4.3 Detail Screen (Detail Produk)

- **Hero Image Section:** Gambar produk kualitas tinggi, full-width, dengan efek gradien transparan di atasnya.
- **Metadata & Informasi Produk:** Nama snack, kategori, ulasan ringkas (rating bintang & jumlah ulasan), harga saat ini + harga coret (`originalPrice`) bila diskon.
- **Grid Spesifikasi Produk:** Berat (`weight`), Estimasi Ketahanan (`expired`), Status Stok (`isAvailable`), Jumlah Terjual (`soldCount`).
- **(BARU) Section Ulasan Pembeli:**
  - **Ringkasan Rating:** Rata-rata rating besar (mis. `4.7`) + total jumlah ulasan, didampingi *distribution bar* persentase untuk masing-masing bintang 5→1.
  - **Daftar Ulasan:** Maksimal 3 ulasan terbaru ditampilkan langsung di halaman; tiap kartu ulasan berisi inisial/avatar reviewer, nama, tanggal, rating bintang, dan komentar teks.
  - **Tombol "Lihat Semua Ulasan":** Membuka Bottom Sheet/halaman terpisah berisi daftar ulasan lengkap (paginated) jika jumlah ulasan > 3.
  - **Empty State:** Jika belum ada ulasan, tampilkan ilustrasi ringan + teks *"Belum ada ulasan untuk produk ini"*.
  - **Catatan implementasi:** Karena aplikasi ini belum memiliki sistem autentikasi pengguna, ulasan bersifat **read-only** (data di-*seed*/dikelola oleh admin toko melalui Firestore Console). Fitur "tulis ulasan oleh pengguna" menjadi kandidat *future enhancement* dan **tidak** termasuk scope versi ini.
- **Rekomendasi "Snack Serupa":** Baris horizontal (`LazyRow`) merekomendasikan produk sejenis berdasarkan `categoryId`.
- **Fitur Berbagi (Share):** Tombol aksi mengambang untuk membagikan tautan/teks promosi produk.
- **(BARU) Tombol "+ Tambah ke Pesanan":** Stepper kuantitas (− 1 +) lalu tombol konfirmasi memasukkan produk ke keranjang `Pesanan`, memunculkan Snackbar konfirmasi *"Ditambahkan ke Pesanan"* dengan aksi "Lihat Pesanan".
- **Tombol "Hubungi via WhatsApp":** Tetap tersedia untuk jalur tanya-cepat satu produk tanpa melalui keranjang (lihat 4.7 Skema A).
- **Loading & Refresh:** Shimmer Effect saat `UiState.Loading`; mendukung **Pull-to-Refresh** (menyegarkan harga, stok, dan ulasan terbaru).

### 4.4 Pesanan Screen — Keranjang & Checkout via WhatsApp *(pengganti Favorite Screen)*

> Halaman ini **menggantikan total** konsep *Favorite/Wishlist* pada versi sebelumnya. "Pesanan" bukan daftar simpan-untuk-nanti, melainkan **keranjang sementara** sebelum pelanggan checkout ke WhatsApp.

- **Daftar Item Pesanan:** Tiap item menampilkan thumbnail, nama produk, harga satuan, *stepper* kuantitas (− qty +), dan subtotal otomatis (`price × quantity`).
- **Hapus Item:** Swipe-to-delete atau tombol ikon tempat sampah → memunculkan **Bottom Sheet konfirmasi** terlebih dahulu (carry-over UX dari fitur Favorit lama) sebelum item benar-benar dihapus.
- **Undoable Snackbar:** Setiap kali item dihapus atau kuantitas diubah signifikan, Snackbar muncul di bawah dengan aksi **"Urungkan"** untuk mengembalikan status sebelumnya secara instan.
- **Ringkasan Total (Sticky Bottom Bar):** Menampilkan Subtotal, jumlah total item, dan **Total Pembayaran**, selalu terlihat di bagian bawah layar selagi scroll.
- **Tombol Utama "Pesan via WhatsApp":** Men-generate satu pesan WhatsApp terstruktur berisi seluruh isi keranjang (lihat 4.7 Skema B), lalu memicu Intent `VIEW` ke `wa.me`.
- **Empty State:** Jika keranjang kosong → ilustrasi ringan + teks *"Pesanan kamu masih kosong"* + tombol CTA **"Mulai Belanja"** yang mengarahkan ke `Catalog Screen`.
- **Penyimpanan Data:** Item pesanan disimpan secara lokal di **Room Database** (`CartDao`) — bersifat offline-capable dan tidak memerlukan akun/login. Data persisten meski aplikasi ditutup, sampai pengguna kirim checkout atau hapus manual.
- **Loading & Refresh:** Shimmer Effect saat `UiState.Loading`; mendukung **Pull-to-Refresh** — refresh di sini berfungsi menyinkronkan ulang harga & ketersediaan terbaru tiap item dari Firestore (mengantisipasi perubahan harga sejak item ditambahkan).

### 4.5 Info Screen (Info Toko)

- Profil toko (nama, deskripsi singkat, logo), jam operasional, alamat lengkap, dan peta lokasi (Google Maps embed/tautan).
- Kontak resmi (nomor WhatsApp, Instagram, dsb).
- **Loading:** Shimmer Effect ditampilkan saat data pertama kali dimuat.
- **Refresh:** ❌ **Tidak ada Pull-to-Refresh** pada halaman ini. Data toko bersifat statis dan jarang berubah, sehingga cukup di-*cache* secara lokal (Room) dan disegarkan secara berkala di background, bukan melalui aksi tarik-refresh pengguna.

### 4.6 Search Screen

- Hasil pencarian teks bebas (`query`) terhadap nama & kategori produk, ditampilkan dalam layout grid yang sama dengan Catalog Screen.
- Riwayat pencarian terakhir (opsional, disimpan lokal).

### 4.7 Integrasi Pemesanan WhatsApp (2 Skema)

Pada aplikasi ini terdapat **dua jalur** pemicu checkout WhatsApp, keduanya memicu Intent `VIEW` menuju tautan resmi `wa.me` dengan nomor tujuan bisnis yang sudah dikonfigurasi:

**Skema A — Single Item (dari Detail Screen, tombol "Hubungi via WhatsApp"):**
```
Halo, saya tertarik dengan [Nama Produk]
```

**Skema B — Multi Item (dari Pesanan Screen, tombol "Pesan via WhatsApp"):**
```
Halo Kak, saya ingin memesan dari Keciput Asrifa:

1. Keciput Panjang Wijen (x2) — Rp27.000
2. Stik Keju (x1) — Rp15.000
3. Basreng Stik (x3) — Rp45.000

Total: Rp87.000

Mohon info ketersediaan & cara pembayarannya. Terima kasih 🙏
```

Pesan pada Skema B digenerate otomatis dari isi keranjang (`List<CartItem>`) menggunakan utility `WhatsAppUtil.buildOrderMessage()`, lalu di-*encode* sebagai parameter `text` pada URL `wa.me`.

### 4.8 Fitur Notifikasi (Local Notification — Info Toko) *(baru)*

Notifikasi pada aplikasi ini bersifat **Local Notification** (tanpa server/FCM), dipicu oleh kondisi yang dievaluasi langsung di dalam aplikasi berdasarkan data `StoreInfo` yang di-*cache* secara lokal.

**Jenis Notifikasi & Trigger:**

| Notifikasi | Trigger | Contoh Pesan |
|---|---|---|
| Toko Buka | Waktu saat ini mencapai `openTime` (cek berkala via WorkManager) | *"Keciput Asrifa sudah buka! Yuk mampir atau pesan via app 🛍️"* |
| Toko Akan Tutup | 1 jam sebelum `closeTime` | *"Toko tutup 1 jam lagi, masih bisa pesan via WhatsApp sebelum tutup ya!"* |
| Toko Libur | Tanggal hari ini termasuk dalam `holidayDates` | *"Hari ini Keciput Asrifa libur. Kami buka kembali besok pukul 08.00"* |

**Mekanisme:**

- **Sumber data:** Field `StoreInfo` (lihat Model 5.5) di-*cache* di Room dari data Info Toko, diperbarui sesekali saat halaman Info Screen dimuat — worker tidak perlu koneksi internet setiap kali mengevaluasi kondisi.
- **Penjadwalan:**
  - `PeriodicWorkRequest` (WorkManager) mengevaluasi status buka/tutup/libur setiap ±15 menit.
  - Notifikasi yang butuh presisi jam tertentu (mis. "Toko Buka") menggunakan `AlarmManager.setExactAndAllowWhileIdle()`, di-*reschedule* ulang setiap hari.
- **Notification Channel:** `store_info_channel` (`IMPORTANCE_DEFAULT`) — wajib didefinisikan karena target minSdk 26+.
- **Permission Android 13+ (API 33):** Meminta izin runtime `POST_NOTIFICATIONS` saat pertama kali membuka aplikasi; tanpa ini notifikasi tidak tampil di Android 13+ (relevan karena targetSdk 36).
- **Tap Aksi:** Mengetuk notifikasi membuka langsung `Info Screen` (`info` route) via deep link.
- **Kontrol Pengguna:** Tersedia toggle **"Aktifkan Notifikasi Info Toko"** di Info Screen, agar pengguna dapat menonaktifkan kapan saja (selaras dengan kebijakan Play Store terkait notifikasi opsional).

**Alur Singkat:**
```
App start / daily worker
   → baca StoreInfo dari Room
   → bandingkan waktu sekarang vs openTime / closeTime / holidayDates
   → jika kondisi terpenuhi → tampilkan NotificationCompat.Builder(...).build()
   → user tap notifikasi → buka Info Screen
```

> 💡 *Catatan pengembangan lanjutan:* Reminder "pesanan ngendon di keranjang > 24 jam" dapat ditambahkan dengan mekanisme local notification yang sama (trigger dari `CartItem.addedAt`), namun **tidak termasuk scope** versi ini karena fokus awal hanya pada notifikasi Info Toko.

---

## 5. Model Data Utama

### 5.1 Model: Kategori *(baru)*

Mengacu pada katalog riil Asrifah Food (7 kategori):

| categoryId | Nama Kategori | Contoh Produk |
|---|---|---|
| `kerupuk_rambak` | Kerupuk & Rambak | Tengiri, Rambak, Opak Gulung, Bagelan, Kripik Ceker |
| `snack_ringan` | Snack Ringan | Astor, Makaroni, Basreng, Seblak, Getas |
| `kacang` | Kacang-kacangan | Kacang Shanghai, Koro, Kacang Telur, Kedelai |
| `singkong_pisang` | Olahan Singkong & Pisang | Sale Pisang, Singkong Bawang, Getuk, Stik Tales |
| `tempe` | Olahan Tempe | Tempe Putih, Tempe Kedelai, Tempe Kuning |
| `kue_kering` | Kue Kering | Keciput Wijen, Stik Keju, Stik Bawang, Akar Kelapa |
| `tahu` | Olahan Tahu | Tahu Walik, Tahu Bulat Mini |

```kotlin
data class Category(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = ""
)
```

### 5.2 Model: Snack *(diperbarui)*

| Atribut | Tipe | Keterangan |
|---|---|---|
| `id` | String | ID unik produk. |
| `name` | String | Nama snack. |
| `imageUrl` | String | URL gambar produk. |
| `price` | Double | Harga jual saat ini. |
| `originalPrice` | Double? | Harga sebelum diskon (coret), `null` jika tidak diskon. |
| `rating` | Double | Rata-rata rating (skala 1–5), dihitung dari koleksi `Review`. |
| `reviewCount` | Int | Jumlah total ulasan. |
| `categoryId` | String | Referensi ke `Category.id` (lihat 5.1). |
| `categoryName` | String | Nama kategori (denormalisasi untuk efisiensi tampilan). |
| `weight` | String | Berat bersih (Netto), contoh: `"250g"` (default katalog). |
| `expired` | String | Estimasi ketahanan, contoh: `"3 Bulan"`. |
| `soldCount` | Int | Jumlah produk terjual. |
| `isAvailable` | Boolean | Status stok tersedia/tidak. |
| `isBestseller` | Boolean | Flag bestseller. |
| `isNew` | Boolean | Flag produk baru. |
| `isFeatured` | Boolean | Flag produk unggulan. |
| `isFlashSale` | Boolean | Flag sedang flash sale. |

> ❌ **Field `isFavorite` dihapus** dari model ini — status "disimpan" tidak lagi relevan, digantikan keberadaan produk di koleksi `CartItem` (lihat 5.4).

```kotlin
data class Snack(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val categoryId: String = "",
    val categoryName: String = "",
    val weight: String = "250g",
    val expired: String = "",
    val soldCount: Int = 0,
    val isAvailable: Boolean = true,
    val isBestseller: Boolean = false,
    val isNew: Boolean = false,
    val isFeatured: Boolean = false,
    val isFlashSale: Boolean = false
)
```

### 5.3 Model: Review / Ulasan *(baru)*

```kotlin
data class Review(
    val id: String = "",
    val snackId: String = "",
    val reviewerName: String = "",
    val avatarUrl: String? = null,
    val rating: Int = 5,
    val comment: String = "",
    val reviewDate: String = ""   // format: "dd MMM yyyy"
)
```

### 5.4 Model: CartItem / ItemPesanan *(baru, pengganti konsep Favorite)*

```kotlin
data class CartItem(
    val id: String = "",
    val snackId: String = "",
    val snackName: String = "",
    val snackImageUrl: String = "",
    val price: Double = 0.0,
    var quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = price * quantity
}
```

### 5.5 Model: StoreInfo *(baru — mendukung fitur Notifikasi & Info Screen)*

```kotlin
data class StoreInfo(
    val openTime: String = "08:00",          // format "HH:mm"
    val closeTime: String = "17:00",
    val operatingDays: List<Int> = listOf(1,2,3,4,5,6), // 0=Minggu .. 6=Sabtu
    val holidayDates: List<String> = emptyList(),        // format "yyyy-MM-dd"
    val holidayMessage: String = "",
    val isNotificationEnabled: Boolean = true            // toggle pengguna
)
```

---

## 6. Kebutuhan Non-Fungsional

### 6.1 Desain UI/UX & Tema

- **Palet Warna:** Dominan warna bumi dan hangat bernuansa terakota/coral (`CoralDark #993C1D`, `CoralMid #D85A30`, `CoralLight #FAECE7`) untuk merepresentasikan identitas industri makanan tradisional premium.
- **Feedback Visual:** Implementasi **Shimmer Effect** (Skeleton Screen) sebagai penanda loading di seluruh halaman yang mengambil data dari repositori lokal/jaringan, untuk mencegah layar kosong kaku.

### 6.2 Teknologi & Performa

- **UI Framework:** Jetpack Compose (Modern Android Toolkit) dengan Material Design 3.
- **Arsitektur:** MVVM (Model-View-ViewModel) menggunakan `StateFlow` dan `collectAsStateWithLifecycle`.
- **Penyimpanan Lokal:** Room Database (`KeciputDatabase`) untuk manajemen data luring (offline capability), penyimpanan **isi Pesanan/keranjang**, dan cache katalog produk.
- **Pemuatan Gambar:** Coil `AsyncImage` dengan penanganan state loader mandiri.
- **Dependency Injection:** Hilt untuk manajemen dependensi yang bersih dan terisolasi.

### 6.3 Kebijakan Shimmer Effect & Pull-to-Refresh *(baru — eksplisit per halaman)*

| Halaman | Shimmer saat Loading | Pull-to-Refresh | Alasan |
|---|---|---|---|
| Beranda (Home) | ✅ | ✅ | Data promo, flash sale, dan produk unggulan sering berubah. |
| Catalog | ✅ | ✅ | Stok & harga produk perlu sinkron real-time. |
| Detail Screen | ✅ | ✅ | Harga, stok, dan ulasan terbaru perlu bisa disegarkan. |
| **Pesanan** | ✅ | ✅ | Menyinkronkan ulang harga/stok item di keranjang sebelum checkout. |
| **Info Toko** | ✅ | ❌ | Data statis (alamat, jam buka) jarang berubah — cukup di-*cache* & shimmer di load awal, tanpa perlu aksi tarik-refresh. |

---

## 7. Riwayat Revisi

| Versi | Tanggal | Perubahan |
|---|---|---|
| 1.0.0 | 16 Juni 2026 | Versi awal: Home, Catalog, **Favorite**, Info, Detail, Search. |
| **2.0.0** | **18 Juni 2026** | Favorite → **Pesanan** (keranjang + checkout WhatsApp multi-item); tambah section **Ulasan** di Detail Screen; model `Snack` & `Category` diperbarui mengikuti katalog riil 7 kategori/78 SKU Asrifah Food; kebijakan Shimmer & Pull-to-Refresh didefinisikan per halaman (Info Toko dikecualikan dari refresh). |
| **2.1.0** | **18 Juni 2026** | Tambah fitur **Notifikasi (Local Notification — Info Toko)**: notifikasi buka/tutup/libur toko via WorkManager + AlarmManager, channel `store_info_channel`, permission `POST_NOTIFICATIONS` (API 33+), toggle aktif/nonaktif di Info Screen; tambah model `StoreInfo`. |

---

*Dokumen ini menggantikan seluruh isi PRD v1.0.0 terkait fitur Favorite, model data Snack, dan kategori produk. Bagian Gradle/Build Configuration, struktur folder Clean Architecture, dan aturan coding pada PRD teknis sebelumnya (berbasis XML View System) **tidak berlaku** untuk versi ini karena stack yang dipilih adalah Jetpack Compose.*

**Versi:** 2.1.0 | **Terakhir Diperbarui:** 18 Juni 2026
