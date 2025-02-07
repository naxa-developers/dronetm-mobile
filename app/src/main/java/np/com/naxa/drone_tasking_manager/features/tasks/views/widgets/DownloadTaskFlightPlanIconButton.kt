package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask

@Composable
fun DownloadTaskFlightPlanIconButton(
    modifier: Modifier = Modifier,
    task: ProjectTask
) {

    var show by remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = {
            // show = !show
        }
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {

            Crossfade(
                targetState = show,
                label = "Download Task Flight Plan"
            ) { downloading ->
                if (downloading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize(),
                        progress = { 0.5f },
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }

            Icon(
                Icons.Outlined.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}