package np.com.naxa.drone_tasking_manager.features.project_details.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events.ProjectDetailEvent
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.states.ProjectDetailState
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectDetailMapView
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectDetailTabView
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectTaskInfoBottomSheet
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskLockOrUnlockState
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    modifier: Modifier = Modifier,
    projectId: String?,
) {

    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val listState = rememberLazyListState()

    val viewModel = LocalProjectDetailViewModel.current
    val state by viewModel.projectState.collectAsState()

    val tasksViewModel = LocalTasksViewModel.current
    val lockTaskState by tasksViewModel.taskLockState.collectAsState()
    val unlockTaskState by tasksViewModel.taskUnlockState.collectAsState()

    val mapViewMaxHeightPx =
        with(LocalDensity.current) { (configuration.screenHeightDp * 0.5).dp.toPx() }
    val mapViewMinHeightPx =
        with(LocalDensity.current) { (configuration.screenHeightDp * 0.25).dp.toPx() }
    var mapViewOffset by remember { mutableFloatStateOf(0f) }

    var selectedTab by remember { mutableIntStateOf(0) }

    var infoJsonObject: JsonObject? by remember { mutableStateOf(null) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // internal function to close info sheet
    fun closeInfoSheet() {
        infoJsonObject = null
        showInfoBottomSheet = false
    }


    var isScrollable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalHeightPx = layoutInfo.visibleItemsInfo.sumOf { it.size }
                val contentHeight = with(density) { totalHeightPx.toDp() }

                isScrollable =
                    contentHeight > (configuration.screenHeightDp.dp - with(density) { mapViewMaxHeightPx.toDp() })
                if (!isScrollable) mapViewOffset = 0f
            }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset =
                    (mapViewOffset + delta).coerceIn(-(mapViewMaxHeightPx - mapViewMinHeightPx), 0f)
                mapViewOffset = newOffset
                return Offset.Zero
            }
        }
    }

    // Fetch project details on screen start
    LaunchedEffect(Unit) {
        if (projectId != null) {
            viewModel.triggerEvent(ProjectDetailEvent.FetchProjectById(projectId, true))
        }
    }

    // React to changes in lockTaskState
    LaunchedEffect(lockTaskState) {
        if (lockTaskState is TaskLockOrUnlockState.Success && projectId != null) {
            closeInfoSheet()
            viewModel.triggerEvent(
                ProjectDetailEvent.FetchProjectById(
                    projectId,
                    forceRefresh = true,
                )
            )
        }

        if (lockTaskState is TaskLockOrUnlockState.Error) {
            val error = (lockTaskState as TaskLockOrUnlockState.Error).message
            navigationEventsViewModel.sendEvent(
                DroneTMAppNavigationEvent.OnSnackBarShow(
                    message = error
                )
            )
        }

        tasksViewModel.triggerEvent(
            TasksEvent.ResetState(
                lockState = true,
            )
        )
    }

    // React to changes in unlockTaskState
    LaunchedEffect(unlockTaskState) {
        if (unlockTaskState is TaskLockOrUnlockState.Success && projectId != null) {
            closeInfoSheet()
            viewModel.triggerEvent(
                ProjectDetailEvent.FetchProjectById(
                    projectId,
                    forceRefresh = true,
                )
            )
        }

        if (unlockTaskState is TaskLockOrUnlockState.Error) {
            val error = (unlockTaskState as TaskLockOrUnlockState.Error).message
            navigationEventsViewModel.sendEvent(
                DroneTMAppNavigationEvent.OnSnackBarShow(
                    message = error
                )
            )
        }

        tasksViewModel.triggerEvent(
            TasksEvent.ResetState(
                unlockState = true,
            )
        )
    }

    when (state) {
        ProjectDetailState.Idle, ProjectDetailState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ProjectDetailState.Success -> {
            val project = (state as ProjectDetailState.Success).project
            Box(modifier = modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = with(density) { mapViewMaxHeightPx.toDp() }),
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    userScrollEnabled = isScrollable
                ) {
                    item {
                        ProjectDetailTabView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            project = project,
                            selected = selectedTab,
                            onSelected = {
                                selectedTab = it
                            },
                            onTaskClick = { task ->
                                scope.launch {
                                    try {
                                        val properties = mapOf(
                                            "id" to task.id,
                                            "projectId" to task.projectId,
                                            "projectTaskIndex" to task.projectTaskIndex,
                                            "state" to task.state?.key?.uppercase(),
                                            "userId" to task.userId,
                                            "name" to task.userName,
                                            "imageCount" to task.imageCount,
                                            "assetsUrl" to task.assetsUrl,
                                            "totalAreaSqkm" to task.totalAreaSqkm,
                                            "flightTimeMinutes" to task.flightTimeMinutes,
                                            "flightDistanceKm" to task.flightDistanceKm,
                                            "totalImageUploaded" to task.totalImageUploaded,
                                        )

                                        val jsonObject = Gson().toJsonTree(properties).asJsonObject

                                        showInfoBottomSheet = true
                                        infoJsonObject = jsonObject

                                    } catch (e: Exception) {
                                        // Do nothing
                                    }
                                }
                            }
                        )
                    }
                }

                ProjectDetailMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { mapViewMaxHeightPx.toDp() })
                        .offset { IntOffset(x = 0, y = mapViewOffset.roundToInt()) },
                    project = project,
                    onFeatureClick = {
                        infoJsonObject = it
                        showInfoBottomSheet = true
                    }
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .safeGesturesPadding()
                        .padding(top = 8.dp, start = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnPopBackStack)
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }

                    Text(project.name ?: "", style = MaterialTheme.typography.titleMedium)
                }

                ProjectTaskInfoBottomSheet(
                    infoJsonObject = infoJsonObject,
                    infoSheetState = infoSheetState,
                    show = showInfoBottomSheet,
                    onDismiss = {
                        closeInfoSheet()
                    }
                )
            }
        }

        is ProjectDetailState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val error = (state as ProjectDetailState.Error).message
                Text(error)
            }
        }
    }


}