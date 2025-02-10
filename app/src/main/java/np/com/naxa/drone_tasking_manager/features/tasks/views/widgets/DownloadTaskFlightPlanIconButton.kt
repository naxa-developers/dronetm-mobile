package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskFlightPlanDownloadState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import java.io.File

@Composable
fun DownloadTaskFlightPlanIconButton(
    modifier: Modifier = Modifier,
    task: ProjectTask,
) {

    val tasksViewModel = LocalTasksViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val downloadState by tasksViewModel.taskFlightPlanDownloadState.collectAsState()
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var file: File? by remember { mutableStateOf(null) }


    // Listen event in launch effect
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is TaskFlightPlanDownloadState.DownloadCompleted -> {
                downloading = false
                progress = 1f
                file = (downloadState as TaskFlightPlanDownloadState.DownloadCompleted).file
                navigationEventsViewModel.sendEvent(
                    DroneTMAppNavigationEvent.OnSnackBarShow(
                        "File downloaded successfully",
                        actionLabel = "Transfer",
                        onActionPerformed = {

                        }
                    )
                )
            }

            is TaskFlightPlanDownloadState.DownloadError -> {
                downloading = false

                navigationEventsViewModel.sendEvent(
                    DroneTMAppNavigationEvent.OnSnackBarShow(
                        (downloadState as TaskFlightPlanDownloadState.DownloadError).error
                    )
                )
            }

            is TaskFlightPlanDownloadState.Downloading -> {
                if (!downloading) downloading = true
                progress = (downloadState as TaskFlightPlanDownloadState.Downloading).progress
            }

            TaskFlightPlanDownloadState.Idle -> {
                downloading = false
            }
        }
    }


    IconButton(
        modifier = modifier,
        onClick = {
            if (task.id == null || task.projectId == null) return@IconButton

            if (downloading) return@IconButton

            file = null
            progress = 0f
            tasksViewModel.triggerEvent(
                TasksEvent.DownloadTaskFlightPlan(
                    taskId = task.id,
                    projectId = task.projectId,
                    null
                )
            )

        }
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (downloading || file != null) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Crossfade(
                    modifier = Modifier.align(Alignment.Center),
                    targetState = progress > 0,
                    label = "Progress Text and Icon Blink"
                ) { inProgress ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (inProgress) {
                            if (file != null) {
                                Icon(
                                    Icons.Outlined.DownloadDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
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