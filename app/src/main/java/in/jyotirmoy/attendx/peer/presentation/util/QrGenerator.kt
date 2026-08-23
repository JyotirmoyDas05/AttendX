package `in`.jyotirmoy.attendx.peer.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import `in`.jyotirmoy.attendx.R
import qrcode.QRCode

object QrGenerator {

    /**
     * Generates a styled QR bitmap:
     *  - Rounded square (squircle) modules via RoundSquaresShapeFactory
     *  - Theme-adaptive colors passed in from the Composable
     *  - Bear mascot (`bear_with_white`) composited in center
     */
    fun generate(
        content: String,
        context: Context,
        moduleColorArgb: Int,
        backgroundColorArgb: Int,
        cellSize: Int = 18
    ): Bitmap {
        // Build QR with rounded square modules
        val qrBytes = QRCode.ofRoundedSquares()
            .withColor(moduleColorArgb)
            .withBackgroundColor(backgroundColorArgb)
            .withSize(cellSize)
            .build(content)
            .renderToBytes()

        val qrBitmap = android.graphics.BitmapFactory
            .decodeByteArray(qrBytes, 0, qrBytes.size)
            .copy(Bitmap.Config.ARGB_8888, true)

        // Composite bear mascot in center
        composeBearLogo(context, qrBitmap, backgroundColorArgb)

        return qrBitmap
    }

    private fun composeBearLogo(
        context: Context,
        qrBitmap: Bitmap,
        backgroundColorArgb: Int
    ) {
        val canvas = Canvas(qrBitmap)
        val cx = qrBitmap.width / 2f
        val cy = qrBitmap.height / 2f

        // Logo takes ~22% of QR width
        val logoSize = (qrBitmap.width * 0.22f).toInt()
        val padding = logoSize * 0.18f
        val circleRadius = logoSize / 2f + padding

        // Draw circular background so logo doesn't break QR readability
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColorArgb
        }
        canvas.drawCircle(cx, cy, circleRadius, bgPaint)

        // Draw bear mascot
        val bearDrawable = ContextCompat.getDrawable(context, R.drawable.bear_with_white)
        if (bearDrawable != null) {
            val bearBitmap = bearDrawable.toBitmap(width = logoSize, height = logoSize)
            val left = cx - logoSize / 2f
            val top  = cy - logoSize / 2f
            canvas.drawBitmap(bearBitmap, left, top, null)
            bearBitmap.recycle()
        }
    }
}
