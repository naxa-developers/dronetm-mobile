package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask

@Composable
fun DownloadTaskFlightPlanIconButton(
    modifier: Modifier = Modifier,
    task: ProjectTask
) {
    var downloading by remember { mutableStateOf(true) }

    IconButton(
        modifier = modifier,
        onClick = {
            downloading = !downloading
        }
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (downloading) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = { 0.5f },
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Crossfade(
                    modifier = Modifier.align(Alignment.Center),
                    targetState = alpha > 0.6,
                    label = "Progress Text and Icon Blink"
                ) { showProgress ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (showProgress) {
                            Text(
                                text = "90%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(4.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {

                            Icon(
                                Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}