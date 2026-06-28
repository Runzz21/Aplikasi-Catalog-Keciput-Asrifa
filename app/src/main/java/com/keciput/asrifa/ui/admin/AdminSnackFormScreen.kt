package com.keciput.asrifa.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSnackFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isEditing = formState.id != 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Produk" else "Tambah Produk") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSnack()
                            if (formState.id == 0 && formState.error == null) onSaved()
                        },
                        enabled = !formState.isSaving
                    ) {
                        Icon(Icons.Default.Check, "Simpan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (formState.error != null) {
                Text(formState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            if (isEditing) {
                Text("ID: ${formState.id}", fontSize = 12.sp, color = Color.Gray)
            }

            OutlinedTextField(
                value = formState.name, onValueChange = { v -> viewModel.updateForm { it.copy(name = v) } },
                label = { Text("Nama Produk") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.price, onValueChange = { v -> viewModel.updateForm { it.copy(price = v) } },
                label = { Text("Harga") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.category, onValueChange = { v -> viewModel.updateForm { it.copy(category = v) } },
                label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.description, onValueChange = { v -> viewModel.updateForm { it.copy(description = v) } },
                label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = formState.imageUrl, onValueChange = { v -> viewModel.updateForm { it.copy(imageUrl = v) } },
                label = { Text("URL Gambar") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.rating, onValueChange = { v -> viewModel.updateForm { it.copy(rating = v) } },
                label = { Text("Rating") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.weight, onValueChange = { v -> viewModel.updateForm { it.copy(weight = v) } },
                label = { Text("Berat") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Label", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = formState.isFeatured,
                    onClick = { viewModel.updateForm { it.copy(isFeatured = !it.isFeatured) } },
                    label = { Text("Featured") }
                )
                FilterChip(
                    selected = formState.isBestseller,
                    onClick = { viewModel.updateForm { it.copy(isBestseller = !it.isBestseller) } },
                    label = { Text("Bestseller") }
                )
                FilterChip(
                    selected = formState.isFlashSale,
                    onClick = { viewModel.updateForm { it.copy(isFlashSale = !it.isFlashSale) } },
                    label = { Text("Flash Sale") }
                )
            }

            if (formState.isSaving) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun <T> MutableState<T>.update(transform: (T) -> T) {
    this.value = transform(this.value)
}
