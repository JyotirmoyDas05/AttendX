package `in`.jyotirmoy.attendx.settings.presentation.components.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

class ScallopedShape(private val waves: Int = 12) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.width / 2f
        
        path.moveTo(centerX + radius, centerY)
        
        for (i in 1..360) {
            val angle = Math.toRadians(i.toDouble())
            // 12 petals (waves), amplitude is 5% of radius
            val r = radius * (1f + 0.05f * cos(waves * angle))
            
            // Limit r to actual radius to ensure it stays within bounds if needed, 
            // but for a flower shape we usually oscillate around a base radius.
            // Let's use a slightly smaller base radius so the peaks touch the edge.
            val outputR = radius * 0.90f + (radius * 0.10f * cos(waves * angle))
            
            val x = centerX + outputR * cos(angle)
            val y = centerY + outputR * sin(angle)
            
            path.lineTo(x.toFloat(), y.toFloat())
        }
        path.close()
        
        return Outline.Generic(path)
    }
}
