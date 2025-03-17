package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularAngleSlider(
    modifier: Modifier = Modifier,
    initialAngle: Float = 0f,
    onAngleChanged: (Float) -> Unit,
    trackColor: Color = Color.Gray,
    progressColor: Color = Color.Blue,
    thumbColor: Color = Color.Red,
    trackWidth: Dp = 8.dp,
    thumbRadius: Dp = 10.dp
) {
    var angle by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    LaunchedEffect(initialAngle) {
        angle = initialAngle
    }


    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        val center = size.center
                        val radius =
                            (size.toSize().minDimension / 2) - with(density) { trackWidth.toPx() } / 2

                        // Get touch position relative to center
                        val touchOffset = change.position - center.toOffset()

                        // Calculate angle in radians from positive x-axis
                        val touchAngle = atan2(touchOffset.y, touchOffset.x)

                        // Convert to degrees (0-360) with 0° at top (12 o'clock)
                        var degrees = Math.toDegrees(touchAngle.toDouble()).toFloat() + 90f
                        if (degrees < 0) degrees += 360f

                        angle = degrees
                        onAngleChanged(degrees)
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val trackWidthPx = with(density) { trackWidth.toPx() }
        val radius = (size.minDimension / 2) - trackWidthPx / 2

        // Draw background track
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(trackWidthPx)
        )

        // Draw progress arc
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = angle,
            useCenter = false,
            style = Stroke(trackWidthPx, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // Calculate thumb position
        val theta = Math.toRadians(angle.toDouble() - 90)
        val thumbRadiusPx = with(density) { thumbRadius.toPx() }
        val thumbX = center.x + radius * cos(theta).toFloat()
        val thumbY = center.y + radius * sin(theta).toFloat()

        // Draw thumb
        drawCircle(
            color = thumbColor,
            center = Offset(thumbX, thumbY),
            radius = thumbRadiusPx
        )
    }
}