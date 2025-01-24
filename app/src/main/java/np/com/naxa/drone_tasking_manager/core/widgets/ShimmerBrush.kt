package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.35f),
        Color.Gray.copy(alpha = 0.15f),
        Color.LightGray.copy(alpha = 0.35f)
    ),
    animationDurationMillis: Int = 1200,
    gradientStart: Offset = Offset.Zero,
    gradientEndOffset: Float = 1500F
): Brush {
    // Infinite transition for shimmer animation
    val transition = rememberInfiniteTransition(label = "ShimmerBrushInfiniteTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = gradientEndOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerBrushTranslation"
    )

    // Linear gradient brush with animated offset
    return Brush.linearGradient(
        colors = shimmerColors,
        start = gradientStart,
        end = Offset(translateAnim.value, translateAnim.value)
    )
}


@Composable
fun ShimmerEffectPreview(
    modifier: Modifier = Modifier
) {
    val shimmerBrush = rememberShimmerBrush(
        shimmerColors = listOf(
            Color(0xFFCCCCCC),
            Color(0xFFEEEEEE),
            Color(0xFFCCCCCC)
        ),
        animationDurationMillis = 1500,
        gradientEndOffset = 1200f
    )
    Box(
        modifier = modifier.background(shimmerBrush)
    )
}