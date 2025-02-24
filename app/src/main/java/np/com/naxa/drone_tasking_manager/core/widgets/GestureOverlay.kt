package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.platform.LocalContext

@Composable
fun GestureOverlay(
    angle: Float = 0f,
    onRotationChanged: (Float) -> Unit,
) {
    val context = LocalContext.current
    var rotationAngle =  remember { mutableFloatStateOf(angle) } // Cumulative rotation angle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    // Accumulate the rotation angle (in degrees)
                    rotationAngle.floatValue += rotation

                    if(rotationAngle.floatValue != 0f){
                        onRotationChanged(rotationAngle.floatValue)
                    }
                }
            }
    )
}