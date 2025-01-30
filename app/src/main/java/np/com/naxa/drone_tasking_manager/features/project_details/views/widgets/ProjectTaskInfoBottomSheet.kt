package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events.ProjectDetailEvent
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskLockOrUnlockState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTaskInfoBottomSheet(
    modifier: Modifier = Modifier,
    infoJsonObject: JsonObject?,
    infoSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    show: Boolean,
    onDismiss: () -> Unit = {},
) {

    val scope = rememberCoroutineScope()
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val tasksViewModel = LocalTasksViewModel.current

    val taskId by remember(infoJsonObject) {
        mutableStateOf(
            try {
                infoJsonObject?.get("id")?.asString
            } catch (e: Exception) {
                null
            }
        )
    }

    val projectId by remember(infoJsonObject) {
        mutableStateOf(
            try {
                infoJsonObject?.get("projectId")?.asString
            } catch (e: Exception) {
                null
            }
        )
    }

    val taskState by remember(infoJsonObject) {
        mutableStateOf(
            ProjectTaskState.fromString(
                try {
                    infoJsonObject?.get("state")?.asString
                } catch (e: Exception) {
                    null
                }
            )
        )
    }

    if (show) {
        ModalBottomSheet(
            modifier = modifier
                .wrapContentHeight(),
            sheetState = infoSheetState,
            onDismissRequest = {
                onDismiss.invoke()
            }
        ) {
            infoJsonObject?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = "Task #$taskId",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = "Project #$projectId",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = "State ${taskState?.key ?: "No"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = try {
                            it.get("name").asString
                        } catch (e: Exception) {
                            ""
                        },
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AnimatedVisibility(
                            modifier = Modifier.weight(1f),
                            visible = taskState == null || listOf(
                                ProjectTaskState.LockedForMapping,
                                ProjectTaskState.UnlockedToMap,
                                ProjectTaskState.UnlockedDone,
                            ).contains(taskState)
                        ) {
                            ElevatedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                onClick = {
                                    scope.launch {
                                        if (taskId == null || projectId == null) return@launch

                                        if (taskState == ProjectTaskState.LockedForMapping) {
                                            tasksViewModel.triggerEvent(
                                                TasksEvent.UnlockTask(
                                                    taskId!!,
                                                    projectId!!,
                                                )
                                            )
                                            infoSheetState.hide()
                                            return@launch
                                        }

                                        tasksViewModel.triggerEvent(
                                            TasksEvent.LockTask(
                                                taskId!!,
                                                projectId!!,
                                            )
                                        )
                                        infoSheetState.hide()
                                    }
                                }
                            ) {
                                Text(
                                    if (taskState == ProjectTaskState.LockedForMapping) "Unlock" else "Lock",
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        AnimatedVisibility(
                            modifier = Modifier.weight(1f),
                            visible = listOf(
                                ProjectTaskState.LockedForMapping,
                                ProjectTaskState.ImageUploaded,
                                ProjectTaskState.ImageProcessingStarted,
                                ProjectTaskState.ImageProcessingFinished,
                                ProjectTaskState.ImageProcessingFailed,
                                ProjectTaskState.LockedForValidation,
                            ).contains(taskState)
                        ) {
                            ElevatedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                onClick = {

                                }
                            ) {
                                Text("Go to Task", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}