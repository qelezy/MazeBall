package com.example.mazeball.game

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun Ball(position: Offset, scrollOffset: Offset) {
    val density = LocalDensity.current
    val ballSizeDp = 25.dp
    val ballSizePx = with(density) { ballSizeDp.toPx() }

    val shader = remember(ballSizePx) {
        val textureBitmap = Bitmap.createBitmap(ballSizePx.toInt(), ballSizePx.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(textureBitmap)
        
        val paint1 = android.graphics.Paint().apply { color = 0xFF885A89.toInt() }
        val paint2 = android.graphics.Paint().apply { color = 0xFF048A81.toInt() }

        val halfSize = ballSizePx / 2f
        canvas.drawRect(0f, 0f, halfSize, halfSize, paint1)
        canvas.drawRect(halfSize, 0f, ballSizePx, halfSize, paint2)
        canvas.drawRect(0f, halfSize, halfSize, ballSizePx, paint2)
        canvas.drawRect(halfSize, halfSize, ballSizePx, ballSizePx, paint1)

        BitmapShader(textureBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    val paint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
        }
    }

    val matrix = remember { Matrix() }
    matrix.setTranslate(scrollOffset.x, scrollOffset.y)
    shader.setLocalMatrix(matrix)
    paint.shader = shader

    val x = with(density) { position.x.toDp() - ballSizeDp / 2 }
    val y = with(density) { position.y.toDp() - ballSizeDp / 2 }

    Canvas(
        modifier = Modifier
            .offset(x, y)
            .size(ballSizeDp)
    ) {
        drawContext.canvas.nativeCanvas.drawCircle(
            size.width / 2,
            size.height / 2,
            size.width / 2,
            paint
        )
    }
}
