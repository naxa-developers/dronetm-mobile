package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskUnFlyableRequestState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.utils.round

@Composable
fun TaskDetailSectionView(
    modifier: Modifier = Modifier,
    task: ProjectTask,
    waypointsCount: Int? = null
) {

    val scope = rememberCoroutineScope()
    val tasksViewModel = LocalTasksViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    var isTaskFlyable by remember { mutableStateOf(true) }

    // Collect the state
    val unFlyableState by tasksViewModel.taskUnFlyableRequestState.collectAsState()

    // Perform tasks based on the state
    LaunchedEffect(unFlyableState) {
        when (unFlyableState) {
            TaskUnFlyableRequestState.Idle, TaskUnFlyableRequestState.Requesting -> {}
            is TaskUnFlyableRequestState.Error -> {
                val errorState = unFlyableState as TaskUnFlyableRequestState.Error
                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnSnackBarShow(message = errorState.message))
            }

            is TaskUnFlyableRequestState.Success -> {
                tasksViewModel.triggerEvent(TasksEvent.ResetState(unFlyableState = true))
                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnPopBackStack)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeadingText(text = "Task Description")
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            task.createdAt?.let {
                KeyValueText(
                    key = "Created Date",
                    value = it.split("T").firstOrNull() ?: it
                )
            }

            task.updatedAt?.let {
                KeyValueText(
                    key = "Task Locked Date",
                    value = it.split("T").firstOrNull() ?: it
                )
            }

            task.totalAreaSqkm?.let {
                KeyValueText(
                    key = "Total Task Area",
                    value = it.round(2).toString(),
                    suffix = "km²"
                )
            }

            waypointsCount?.let {
                KeyValueText(
                    key = "Total Waypoints Count",
                    value = it.toString(),
                )
            }

            task.flightTimeMinutes?.let {
                KeyValueText(
                    key = "Est. Flight Time",
                    value = it.round(2).toString(),
                    suffix = "minutes"
                )
            }

        }
        HeadingText(text = "Flight Parameters")
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            task.frontOverlap?.let {
                KeyValueText(
                    key = "Front Overlap",
                    value = it.toString(),
                    suffix = "%",
                    hasSpaceBetweenSuffix = false
                )
            }

            task.sideOverlap?.let {
                KeyValueText(
                    key = "Side Overlap",
                    value = it.toString(),
                    suffix = "%",
                    hasSpaceBetweenSuffix = false
                )
            }

            task.gsdCmPx?.let {
                KeyValueText(
                    key = "GSD",
                    value = it.toString(),
                    suffix = "cm"
                )
            }

        }

        if (listOf(
                ProjectTaskState.ImageUploaded,
                ProjectTaskState.ImageProcessingStarted,
                ProjectTaskState.ImageProcessingFailed,
                ProjectTaskState.ImageProcessingFinished,
            ).contains(task.state)
        ) {
            HeadingText(text = "Upload Information")
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                task.totalImageUploaded?.let {
                    KeyValueText(
                        key = "Image Count",
                        value = it.toString()
                    )
                }

                KeyValueText(
                    key = "Orthophoto Available",
                    value = if (task.assetsUrl != null) "Yes" else "No"
                )

                task.state?.let {
                    KeyValueText(
                        key = "Image Status",
                        value = it.label,
                    )
                }
            }
        } else {
            HeadingText(
                text = "Is this task flyable?",
                color = MaterialTheme.colorScheme.onSurface,
            )
            IsTaskFlyableRadioButtonsView(
                flyable = isTaskFlyable,
                onValueChange = { flyable ->
                    isTaskFlyable = flyable
                }
            )
            Crossfade(
                targetState = isTaskFlyable,
                label = "isTaskFlyable"
            ) { flyable ->
                if (!flyable) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeadingText(text = "Comment", color = MaterialTheme.colorScheme.onSurface)
                        CommentTextFieldView {
                            if (task.id == null || task.projectId == null) return@CommentTextFieldView

                            scope.launch {
                                tasksViewModel.triggerEvent(
                                    TasksEvent.FlagTaskAsUnFlyable(
                                        taskId = task.id,
                                        projectId = task.projectId,
                                        comment = it
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeadingText(text = "Upload Images, GCP, and align.laz")
                        UploadTaskImageView(
                            task = task
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


@Composable
private fun HeadingText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
    )
}

@Composable
private fun KeyValueText(
    key: String,
    value: String,
    suffix: String? = null,
    hasSpaceBetweenSuffix: Boolean = true
) = Row(
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        modifier = Modifier.weight(1f), text = key,
        style = MaterialTheme.typography.bodyMedium
    )
    Text(modifier = Modifier.width(20.dp), text = ":")
    Text(
        modifier = Modifier.weight(1.5f),
        text = buildAnnotatedString {
            append(value)
            suffix?.let {
                if (hasSpaceBetweenSuffix) append(" ")
                append(it)
            }
        },
        style = MaterialTheme.typography.bodyMedium
    )
}