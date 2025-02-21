package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.core.widgets.CircularAngleSlider
import np.com.naxa.drone_tasking_manager.utils.DebouncedAction
import np.com.naxa.drone_tasking_manager.utils.round

@Composable
fun TaskWaypointAngleSlider(
    modifier: Modifier = Modifier,
    onAngleChanged: ((Float) -> Unit)? = null,
    onSaved: ((Float) -> Unit)? = null,
    onCanceled: (() -> Unit)? = null,
    delay: Long = 25L
) {

    var visible by remember { mutableStateOf(false) }
    var angle by remember { mutableFloatStateOf(0f) }

    DebouncedAction(
        input = { angle },
        debounceMillis = delay
    ) { n ->
        onAngleChanged?.invoke(n)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = visible
        ) {
            Box(
                modifier = Modifier
                    .size(175.dp),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(0.15f))
                )

                CircularAngleSlider(
                    modifier = Modifier.fillMaxSize(),
                    thumbColor = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.35f
                    ),
                    progressColor = MaterialTheme.colorScheme.primary,
                    thumbRadius = 10.dp,
                    trackWidth = 14.dp,
                    onAngleChanged = {
                        angle = it
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = angle.toInt() != 0,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${angle.toDouble().round(2)}°",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            onClick = {
                                onSaved?.invoke(angle)
                                visible = !visible
                            }
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        angle = 0f
                        visible = !visible
                        if (!visible) onCanceled?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = visible
                ) { visible ->
                    Icon(
                        modifier = Modifier.size((24 / 1.75).dp),
                        imageVector = if (visible) Icons.Default.Close else Icons.Default.Rotate90DegreesCw,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
            Text(
                "Rotate",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}