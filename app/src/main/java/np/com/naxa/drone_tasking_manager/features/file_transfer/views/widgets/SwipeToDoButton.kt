package np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


/**
 * A composable function that displays a swipe-to-do button.
 */
@Composable
fun SwipeToDoButton(
    labelText: String = "Swipe",
    labelTextOnFullySwiped: String = "Transfer",
    labelTextOnBackground: String = "To Transfer",
    height: Int = 42,
    radius: Dp = 42.dp,
    onTransferComplete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    var parentWidth by remember { mutableFloatStateOf(0f) }

    // Animate reset position
    val animatedOffsetX by animateDpAsState(
        targetValue = if (offsetX < parentWidth * 0.75f) Dp(offsetX) else 0.dp,
        finishedListener = {
            // if (offsetX >= parentWidth * 0.8f) {
            //     onTransferComplete()
            // }
        },
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(radius))
            .onGloballyPositioned { layoutCoordinates ->
                parentWidth = layoutCoordinates.size.width.toFloat()
            }
    ) {
        Text(
            text = labelTextOnBackground,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(
                    Dp(
                        offsetX.coerceIn(
                            if (parentWidth == 0f) 0f else height * 1.75f,
                            parentWidth
                        )
                    )
                )
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(radius))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(
                                if (parentWidth == 0f) 0f else height * 1.75f,
                                parentWidth
                            )
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX >= parentWidth * 0.75f) {
                                    onTransferComplete()
                                }
                                offsetX = 0f
                            }
                        }
                    )
                }
        ) {
            Text(
                text = if (offsetX >= parentWidth * 0.75f) labelTextOnFullySwiped else labelText,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
