package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.utils.TaskActionPlanFileUtils
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskFlightPlanDownloadState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.routes.Routes
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.utils.PermissionUtils
import java.io.File

@Composable
fun DownloadTaskFlightPlanIconButton(
    modifier: Modifier = Modifier,
    task: ProjectTask,
    rotationAngle: Int
) {

    val context = LocalContext.current
    val tasksViewModel = LocalTasksViewModel.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current
    val downloadState by tasksViewModel.taskFlightPlanDownloadState.collectAsState()
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val angle = remember{mutableIntStateOf(rotationAngle)}
    var file: File? by remember {
        mutableStateOf(
            if (task.id != null) TaskActionPlanFileUtils.downloadedFileOf(
                context,
                task.id
            ) else null
        )
    }

    // Update angle when rotationAngle changes
    LaunchedEffect(rotationAngle) {
        angle.intValue = rotationAngle
    }

    var showAlreadyDownloadedAlertDialog by remember { mutableStateOf(false) }

    val initiateDownload = remember(context) {
        {
            if (task.id != null && task.projectId != null && !downloading) {
                file = null
                progress = 0f

                Log.d("TaskDetailsScreen", "Rotation angle Changed ===DownloadTaskFlightPlan1=== Angle: ${angle.intValue}")

                tasksViewModel.triggerEvent(
                    TasksEvent.DownloadTaskFlightPlan(
                        taskId = task.id,
                        projectId = task.projectId,
                        isWayPoints = null,
                        rotationAngle = angle.intValue
                    )
                )
            }
        }
    }

    val allFilesStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                initiateDownload()
            } else {
                Toast.makeText(
                    context,
                    "You have denied all file access permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    val mediaFilesStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasReadPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        val hasWritePermission = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

        when {
            hasReadPermission && hasWritePermission -> {
                initiateDownload()
            }

            else -> {
                Toast.makeText(
                    context,
                    "You have denied write permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val checkAndRequestStorageRelatedPermissions = remember(context) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!PermissionUtils.hasAllFileAccessPermission()) {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    allFilesStoragePermissionLauncher.launch(intent)
                } else {
                    initiateDownload()
                }
            } else {
                if (!PermissionUtils.hasMediaFileAccessPermissions(context)) {
                    mediaFilesStoragePermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    initiateDownload()
                }
            }
        }
    }

    // Listen event in launch effect
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is TaskFlightPlanDownloadState.DownloadCompleted -> {
                if (file == null) {
                    navigationEventsViewModel.sendEvent(
                        DroneTMAppNavigationEvent.OnSnackBarShow(
                            "File downloaded successfully",
                            actionLabel = "Transfer",
                            onActionPerformed = {
                                if (file == null) return@OnSnackBarShow
                                navigationEventsViewModel.sendEvent(
                                    DroneTMAppNavigationEvent.OnNavigateToDeviceConnection(
                                        route = Routes.FileTransfer
                                    )
                                )
                            }
                        )
                    )
                }

                file = (downloadState as TaskFlightPlanDownloadState.DownloadCompleted).file
                downloading = false
                progress = 1f
            }

            is TaskFlightPlanDownloadState.DownloadError -> {
                downloading = false
                val error = (downloadState as TaskFlightPlanDownloadState.DownloadError).error
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
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
            if (file == null) {
                checkAndRequestStorageRelatedPermissions()
                return@IconButton
            }

            // navigationEventsViewModel.sendEvent(
            //     DroneTMAppNavigationEvent.OnNavigateToDeviceConnection(
            //         route = Routes.FileTransfer
            //     )
            // )
            showAlreadyDownloadedAlertDialog = true
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
                    targetState = progress > 0 || file != null,
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

    if (showAlreadyDownloadedAlertDialog) {
        TaskFlightPlanAlreadyDownloadedAlertDialog(
            onDismissRequest = {
                showAlreadyDownloadedAlertDialog = false
            },
            onDownload = {
                checkAndRequestStorageRelatedPermissions()
            },
            onTransfer = {
                if (file == null) return@TaskFlightPlanAlreadyDownloadedAlertDialog
                navigationEventsViewModel.sendEvent(
                    DroneTMAppNavigationEvent.OnNavigateToDeviceConnection(
                        route = Routes.FileTransfer
                    )
                )
            }
        )
    }
}