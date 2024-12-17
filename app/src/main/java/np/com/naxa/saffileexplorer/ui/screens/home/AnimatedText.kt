package np.com.naxa.saffileexplorer.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle

enum class TextAnimationType(var initial: Float, var target: Float, var duration: Int = 1000) {
    /**
     * A blurred text animation.
     *
     */
    Blurred(initial = 1f, target = 5f),

    /**
     * A scaled text animation.
     *
     */
    Scaled(initial = 0.25f, target = 1.15f);

    /**
     * Creates a copy of this [TextAnimationType] with optional new initial and target values.
     *
     * @param initial The new initial value, or null to keep the original.
     * @param target The new target value, or null to keep the original.
     *
     * @return A copy of this [TextAnimationType] with the specified initial and target values.
     */
    fun copy(
        initial: Float? = null,
        target: Float? = null,
        duration: Int? = null
    ): TextAnimationType {
        initial?.let { this.initial = it }
        target?.let { this.target = it }
        duration?.let { this.duration = it }
        return this
    }
}

@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    type: TextAnimationType = TextAnimationType.Blurred,
) {
    val stateList = text.mapIndexed { index, character ->
        if (character == ' ') {
            remember {
                mutableFloatStateOf(0f)
            }
        } else {
            val infiniteTransition =
                rememberInfiniteTransition(label = "infinite transition $index")
            infiniteTransition.animateFloat(
                initialValue = type.initial,
                targetValue = type.target,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = type.duration,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        offsetMillis = (type.duration / text.length) * index
                    )
                ),
                label = "blur animation"
            )
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.forEachIndexed { index, character ->
            Text(
                text = character.toString(),
                style = style,
                modifier = Modifier
                    .graphicsLayer {
                        if (character != ' ') {
                            when (type) {
                                TextAnimationType.Blurred -> {
                                    val blurAmount = stateList[index].value
                                    renderEffect = BlurEffect(
                                        radiusX = blurAmount,
                                        radiusY = blurAmount
                                    )
                                }

                                TextAnimationType.Scaled -> {
                                    val scaledAmount = stateList[index].value
                                    scaleX = scaledAmount
                                    scaleY = scaledAmount
                                }
                            }

                        }
                    }
            )
        }
    }
}