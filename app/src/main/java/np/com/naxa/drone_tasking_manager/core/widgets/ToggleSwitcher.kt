package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.utils.toBitmap
import kotlin.math.roundToInt

@Composable
fun ToggleSwitcher(
    modifier: Modifier = Modifier,
    isToggled: Boolean,
    @DrawableRes firstDrawable: Int,
    @DrawableRes secondDrawable: Int,
    onToggle: (Boolean) -> Unit,
    thumbSize: Dp = 28.dp,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    vertical: Boolean = false,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val trackWidth = if (vertical) thumbSize else thumbSize * 2
    val trackHeight = if (vertical) thumbSize * 2 else thumbSize

    val backgroundOffset by animateFloatAsState(
        targetValue = if (isToggled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "backgroundOffset"
    )

    val firstBitmap = remember(thumbSize) {
        try {
            firstDrawable.toBitmap(
                context,
                with(density) { (thumbSize.toPx() / 1.6).roundToInt() },
                with(density) { (thumbSize.toPx() / 1.6).roundToInt() }
            )?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    val secondBitmap = remember(thumbSize) {
        try {
            secondDrawable.toBitmap(
                context,
                with(density) { (thumbSize.toPx() / 1.6).roundToInt() },
                with(density) { (thumbSize.toPx() / 1.6).roundToInt() }
            )?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable {
                onToggle(!isToggled)
            }
            .drawBehind {
                firstBitmap?.let {
                    drawImage(
                        it,
                        topLeft = Offset(
                            x = (thumbSize.roundToPx() - it.width) / 2f,
                            y = (thumbSize.roundToPx() - it.height) / 2f
                        ),
                        alpha = 0.25f
                    )
                }

                secondBitmap?.let {
                    drawImage(
                        it,
                        topLeft = Offset(
                            x = if (vertical) (thumbSize.roundToPx() - it.width) / 2f else thumbSize.roundToPx() + (thumbSize.roundToPx() - it.width) / 2f,
                            y = if (vertical) thumbSize.roundToPx() + (thumbSize.roundToPx() - it.height) / 2f else (thumbSize.roundToPx() - it.height) / 2f
                        ),
                        alpha = 0.25f
                    )
                }
            },
        contentAlignment = if (vertical) Alignment.TopCenter else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        if (vertical) 0 else (backgroundOffset * thumbSize.roundToPx()).toInt(),
                        if (vertical) (backgroundOffset * thumbSize.roundToPx()).toInt() else 0
                    )
                }
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = isToggled,
                label = "icon_transition"
            ) { toggled ->
                Icon(
                    modifier = Modifier.size(with(density) {
                        (thumbSize.toPx() / 1.75).roundToInt().toDp()
                    }),
                    painter = painterResource(id = if (toggled) secondDrawable else firstDrawable),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}
