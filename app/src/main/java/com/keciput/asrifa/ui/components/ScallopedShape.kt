package com.keciput.asrifa.ui.components

import androidx.compose.ui.geometry.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class ScallopedTopShape(
    private val scallopRadius: Float = 15f,
    private val scallopHeight: Float = 30f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
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

class ScallopedBottomShape(
    private val scallopRadius: Float = 10f,
    private val scallopHeight: Float = 15f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - scallopHeight)
            val count = (size.width / (scallopRadius * 2)).toInt().coerceAtLeast(1)
            val step = size.width / count
            for (i in 0 until count) {
                val x = size.width - (i * step)
                quadraticTo(x - step / 2, size.height + scallopHeight, x - step, size.height - scallopHeight)
            }
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
