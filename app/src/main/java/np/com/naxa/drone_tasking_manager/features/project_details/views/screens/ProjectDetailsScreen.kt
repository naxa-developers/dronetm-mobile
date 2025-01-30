package np.com.naxa.drone_tasking_manager.features.project_details.views.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events.ProjectDetailEvent
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.states.ProjectDetailState
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectDetailMapView
import np.com.naxa.drone_tasking_manager.features.project_details.views.widgets.ProjectDetailTabView
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
    val listState = rememberLazyListState()

    var isScrollable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (projectId != null) {
            viewModel.triggerEvent(ProjectDetailEvent.FetchProjectById(projectId, true))
        }
    }

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

    // React to changes in lockTaskState
    LaunchedEffect(lockTaskState) {
        if (lockTaskState is TaskLockOrUnlockState.Success && projectId != null) {
            viewModel.triggerEvent(ProjectDetailEvent.FetchProjectById(projectId, true))
        }

        if (lockTaskState is TaskLockOrUnlockState.Error) {
            val error = (lockTaskState as TaskLockOrUnlockState.Error).message
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnSnackBarShow(
                message = error
            ))
        }

        tasksViewModel.triggerEvent(TasksEvent.ResetState(
            lockState = true,
        ))
    }

    // React to changes in unlockTaskState
    LaunchedEffect(unlockTaskState) {
        if (unlockTaskState is TaskLockOrUnlockState.Success && projectId != null) {
            viewModel.triggerEvent(ProjectDetailEvent.FetchProjectById(projectId, true))
        }

        if (unlockTaskState is TaskLockOrUnlockState.Error) {
            val error = (unlockTaskState as TaskLockOrUnlockState.Error).message
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnSnackBarShow(
                message = error
            ))
        }

        tasksViewModel.triggerEvent(TasksEvent.ResetState(
            unlockState = true,
        ))
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
                            project = project
                        )
                    }
                }

                ProjectDetailMapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { mapViewMaxHeightPx.toDp() })
                        .offset { IntOffset(x = 0, y = mapViewOffset.roundToInt()) },
                    project = project
                )

                TopAppBar(
                    title = {
                        Text(project.name ?: "")
                    },
                    navigationIcon = {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
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