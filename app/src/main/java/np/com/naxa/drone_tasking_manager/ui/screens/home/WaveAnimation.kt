package np.com.naxa.drone_tasking_manager.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaveAnimation(
    noOfWaves: Int = 4,
) {

    // Primary color
    val primary = MaterialTheme.colorScheme.primary

    // Creating list of infinite waves
    val waves = List(noOfWaves) {
        produceState(initialValue = 0f) {
            while (true) {
                val startTime = withFrameNanos { it }
                while (true) {
                    val currentTime = withFrameNanos { it }
                    val elapsedTime = (currentTime - startTime) / 1_000_000_000.0f
                    value = ((sin(elapsedTime * PI * 0.5 + it) + 1) / 2).toFloat()
                }
            }
        }
    }

    // List of wave colors
    val colors = List(noOfWaves) {
        primary.copy(
            alpha = ((it + 1) * 0.05).toFloat()
        )
    }

    // Finally drawing waves
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        waves.forEachIndexed { index, wave ->
            // Create a wave path
            val path = Path().apply {
                moveTo(0f, centerY)

                // Create wave using sine function
                for (x in 0..width.toInt() step 10) {
                    val wavelength = width / 2
                    val amplitude = height / 2.25f
                    val phase = x.toFloat() / wavelength + wave.value * 2 * PI.toFloat()

                    // Adjust wave height and transparency based on animation progress
                    val y = centerY - (amplitude *
                            sin(phase) *
                            (1 - wave.value * 0.7f))

                    lineTo(x.toFloat(), y)
                }

                lineTo(width, centerY)
                close()
            }

            // Draw wave with varying color and opacity
            drawPath(
                path = path,
                color = colors[index],
                style = Fill
            )
        }
    }
}