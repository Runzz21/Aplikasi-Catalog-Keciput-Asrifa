package com.keciput.asrifa.ui.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")

    // — Bottom nav tabs —
    data object Home : Routes("home")
    data object Catalog : Routes("catalog?category={category}&filterType={filterType}") {
        fun createRoute(category: String? = null, filterType: String? = null) : String {
            val builder = StringBuilder("catalog")
            val params = mutableListOf<String>()
            if (category != null) params.add("category=${Uri.encode(category)}")
            if (filterType != null) params.add("filterType=${Uri.encode(filterType)}")
            
            if (params.isNotEmpty()) {
                builder.append("?")
                builder.append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    data object Pesanan : Routes("pesanan")
    data object Info : Routes("info")

    // — Full-screen routes (tanpa bottom nav) —
    data object Detail : Routes("detail/{snackId}") {
        fun createRoute(snackId: Int) = "detail/$snackId"
    }
    data object Search : Routes("search?query={query}") {
        fun createRoute(query: String? = null) = 
            if (query != null) "search?query=${Uri.encode(query)}" else "search"
    }

    data object RiwayatPesanan : Routes("riwayat_pesanan")
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object AdminList : Routes("admin_list")
    data object AdminForm : Routes("admin_form")
}
