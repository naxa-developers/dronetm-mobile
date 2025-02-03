package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

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

    val showUnlockAlertDialog = remember { mutableStateOf(false) }

    val storageService = remember { MMKVStorageService.getInstance() }

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

    val userName by remember(infoJsonObject) {
        mutableStateOf(
            try {
                infoJsonObject?.get("name")?.asString
            } catch (e: Exception) {
                null
            }
        )
    }

    val userId by remember(infoJsonObject) {
        mutableStateOf(
            try {
                infoJsonObject?.get("userId")?.asString
            } catch (e: Exception) {
                null
            }
        )
    }

    val isSelf by remember(userId, storageService) {
        derivedStateOf {
            storageService.get(StorageKeys.User.ID, "110412063342847231417") == userId
        }
    }

    val messageAsPerState by remember(taskState, userName, isSelf) {
        derivedStateOf {
            when (taskState) {
                ProjectTaskState.RequestForMapping -> "This task is requested for mapping by ${if (isSelf) "you" else userName}."
                ProjectTaskState.UnlockedToMap -> "This task is unlocked and available for mapping."
                ProjectTaskState.LockedForMapping -> if (isSelf) "You have locked this task for mapping." else "$userName has locked this task for mapping."
                ProjectTaskState.UnlockedToValidate -> "This task is unlocked and available for validation."
                ProjectTaskState.LockedForValidation -> if (isSelf) "You have locked this task for validation." else "$userName has locked this task for validation."
                ProjectTaskState.UnlockedDone -> "This task has been completed and is unlocked."
                ProjectTaskState.UnflyableTask -> "This task is marked as unflyable."
                ProjectTaskState.ImageUploaded -> if (isSelf) "You have uploaded the image." else "$userName has uploaded the image for this task."
                ProjectTaskState.ImageProcessingFailed -> if (isSelf) "Image processing initiated by you has failed." else "Image processing initiated by $userName has failed."
                ProjectTaskState.ImageProcessingStarted -> if (isSelf) "Image processing initiated by you has started." else "Image processing initiated by $userName has started."
                ProjectTaskState.ImageProcessingFinished -> if (isSelf) "Image processing initiated by you has finished." else "Image processing initiated by $userName has finished."
                null -> "This task is available for mapping. Lock this task and start mapping."
            }
        }
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
                        text = messageAsPerState,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AnimatedVisibility(
                            modifier = Modifier.weight(1f),
                            visible = (listOf(
                                ProjectTaskState.LockedForMapping,
                                ProjectTaskState.UnlockedToMap,
                                ProjectTaskState.UnlockedDone,
                            ).contains(taskState) || taskState == null) && (isSelf || userId == null)
                        ) {
                            if (taskState == ProjectTaskState.LockedForMapping) {
                                OutlinedButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    onClick = {
                                        showUnlockAlertDialog.value = true
                                    }
                                ) {
                                    Text(
                                        "Unlock Task",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
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
                                        "Lock Task",
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            modifier = Modifier.weight(1f),
                            visible = (isSelf && listOf(
                                ProjectTaskState.RequestForMapping,
                                ProjectTaskState.LockedForMapping,
                                ProjectTaskState.ImageUploaded,
                                ProjectTaskState.ImageProcessingStarted,
                                ProjectTaskState.ImageProcessingFinished,
                                ProjectTaskState.ImageProcessingFailed,
                                ProjectTaskState.LockedForValidation,
                            ).contains(taskState)) || (!isSelf && taskState == ProjectTaskState.ImageProcessingFinished)
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
                                    if (taskId != null) {
                                        navigationEventsViewModel.sendEvent(
                                            DroneTMAppNavigationEvent.OnNavigateToTaskDetail(
                                                taskId!!,
                                                projectId
                                            )
                                        )
                                    }
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

    if (showUnlockAlertDialog.value) {
        UnlockTaskAlertDialog(
            onDismissRequest = { showUnlockAlertDialog.value = false },
            onConfirm = {
                showUnlockAlertDialog.value = false
                scope.launch {
                    if (taskId == null || projectId == null) return@launch

                    tasksViewModel.triggerEvent(
                        TasksEvent.UnlockTask(
                            taskId!!,
                            projectId!!,
                        )
                    )
                    infoSheetState.hide()
                }
            }
        )
    }
}