package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs

/**
 * A Composable function that provides a gesture overlay for detecting rotation gestures.
 *
 * This function allows users to rotate an element by using a two-finger twist gesture.
 * It also supports enabling or disabling the gesture detection through the `draggable` parameter.
 *
 * @param angle The initial rotation angle in degrees. Defaults to 0f.
 * @param draggable A boolean indicating whether the gesture detection is enabled.
 *                  If true, gesture detection is disabled and the Box will not react to touch inputs.
 *                  If false, gesture detection is active. Defaults to false.
 * @param onRotationChanged A callback function that is invoked whenever the rotation angle changes.
 *                          It provides the updated rotation angle in degrees as a parameter.
 * @param onTransformStopped A callback function that is invoked when the rotation gesture has ended.
 *                           It provides the final rotation angle in degrees as a parameter.
 */
@Composable
fun GestureOverlay(
    angle: Float = 0f,
    draggable: Boolean = false,
    onRotationChanged: (Float) -> Unit,
    onTransformStopped: (Float) -> Unit
) {
    var rotationAngle by remember { mutableFloatStateOf(angle) }
    var isTransforming by remember { mutableStateOf(false) }
    var lastRotationAngle by remember { mutableFloatStateOf(0f) }
    var gestureEndTimer by remember { mutableLongStateOf(0L) }
    var dragging by remember { mutableStateOf(draggable) }

    var isMultiTouch by remember { mutableStateOf(false) }

    LaunchedEffect(draggable) {
        dragging = draggable
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                if (dragging) return@pointerInput

                forEachGesture {
                    awaitPointerEventScope {
                        var rotation = 0f
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop

                        awaitFirstDown(requireUnconsumed = false)

                        do {
                            val event = awaitPointerEvent()
                            val pointerCount = event.changes.size

                            // Only perform transform if multiple pointers are detected
                            if (pointerCount >= 2) {
                                val zoomChange = event.calculateZoom()
                                val rotationChange = event.calculateRotation()
                                val panChange = event.calculatePan()

                                if (!pastTouchSlop) {
                                    zoom *= zoomChange
                                    rotation += rotationChange
                                    pan += panChange

                                    val centroidSize =
                                        event.calculateCentroidSize(useCurrent = false)
                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                    val rotationMotion =
                                        abs(rotation * PI.toFloat() * centroidSize / 180f)
                                    val panMotion = pan.getDistance()

                                    if (zoomMotion > touchSlop ||
                                        rotationMotion > touchSlop ||
                                        panMotion > touchSlop
                                    ) {
                                        pastTouchSlop = true
                                    }
                                }

                                if (pastTouchSlop) {
                                    isTransforming = true
                                    rotationAngle += rotationChange
                                    onRotationChanged(rotationAngle)
                                    lastRotationAngle = rotationAngle
                                    gestureEndTimer = System.currentTimeMillis()

                                    // Consume the gesture events
                                    event.changes.forEach { it.consumeAllChanges() }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        //                        // Reset transformation state when all pointers are up
                        //                        isTransforming = false
                    }
                }

            }
    )

    LaunchedEffect(isTransforming, gestureEndTimer) {
        if (isTransforming) {
            var lastGestureTime = gestureEndTimer

            while (isTransforming) {
                delay(10L) // Check periodically
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