package com.keciput.asrifa.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keciput.asrifa.ui.components.formatRupiah

data class FilterState(
    val priceRange: ClosedFloatingPointRange<Float>,
    val minRating: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    priceRange: ClosedFloatingPointRange<Float>,
    minRating: Float,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var tempPrice by remember { mutableStateOf(priceRange) }
    var tempRating by remember { mutableFloatStateOf(minRating) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Filter Snack", 
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(20.dp))

            // Range harga
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rentang Harga", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Rp ${tempPrice.start.toInt().formatRupiah()} – ${tempPrice.endInclusive.toInt().formatRupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            RangeSlider(
                value = tempPrice,
                onValueChange = { tempPrice = it },
                valueRange = 0f..200_000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            Spacer(Modifier.height(20.dp))

            // Rating minimum
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rating Minimum", style = MaterialTheme.typography.titleMedium)
                Text(
                    "★ ${tempRating.toInt()}+",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFFFB400)
                )
            }
            Slider(
                value = tempRating, 
                onValueChange = { tempRating = it }, 
                valueRange = 0f..5f, 
                steps = 4,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(24.dp))

            // Tombol aksi
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss, 
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = { onApply(FilterState(tempPrice, tempRating)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Terapkan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
