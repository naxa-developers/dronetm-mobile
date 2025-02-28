package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

@Composable
fun GestureOverlay(
    angle: Float = 0f,
    onRotationChanged: (Float) -> Unit,
    onTransformStopped: (Float) -> Unit
) {
    var rotationAngle by remember { mutableFloatStateOf(angle) }
    var isTransforming by remember { mutableStateOf(false) }
    var lastRotationAngle by remember { mutableFloatStateOf(0f) }
    var gestureEndTimer by remember { mutableLongStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, _, rotation ->
                    isTransforming = true
                    rotationAngle += rotation
                    onRotationChanged(rotationAngle)
                    lastRotationAngle = rotationAngle
                    gestureEndTimer = System.currentTimeMillis() // Reset timer on each transform
                }
            }
    )

    LaunchedEffect(isTransforming, gestureEndTimer) {
        if (isTransforming) {
            var lastGestureTime = gestureEndTimer

            while (isTransforming) {
                delay(50) // Check periodically
                val currentTime = System.currentTimeMillis()

                // If no new gestures have occurred in the last 50ms
                if (lastGestureTime == gestureEndTimer && currentTime - gestureEndTimer > 50) {
                    isTransforming = false
                    onTransformStopped(lastRotationAngle)
                }

                lastGestureTime = gestureEndTimer
            }
        }
    }
}